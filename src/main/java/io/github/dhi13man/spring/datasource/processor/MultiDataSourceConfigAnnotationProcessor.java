package io.github.dhi13man.spring.datasource.processor;

import static io.github.dhi13man.spring.datasource.constants.MultiDataSourceErrorConstants.MULTIPLE_CLASSES_ANNOTATED_WITH_ENABLE_CONFIG_ANNOTATION;
import static io.github.dhi13man.spring.datasource.constants.MultiDataSourceErrorConstants.MULTIPLE_CONFIG_ANNOTATIONS_FOR_ONE_DATASOURCE;
import static io.github.dhi13man.spring.datasource.constants.MultiDataSourceErrorConstants.NO_REPOSITORY_PACKAGES_PROVIDED_IN_CONFIG;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import io.github.dhi13man.spring.datasource.annotations.EnableMultiDataSourceConfig;
import io.github.dhi13man.spring.datasource.annotations.EnableMultiDataSourceConfig.DataSourceConfig;
import io.github.dhi13man.spring.datasource.annotations.TargetSecondaryDataSource;
import io.github.dhi13man.spring.datasource.dto.EnableConfigAnnotationAndElementHolder;
import io.github.dhi13man.spring.datasource.generators.MultiDataSourceConfigGenerator;
import io.github.dhi13man.spring.datasource.generators.MultiDataSourceRepositoryGenerator;
import io.github.dhi13man.spring.datasource.utils.MultiDataSourceCommonStringUtils;
import io.github.dhi13man.spring.datasource.utils.MultiDataSourceGeneratorUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import org.springframework.data.repository.Repository;
import org.springframework.util.StringUtils;

/**
 * Annotation processor to generate config classes for all the repositories annotated with
 * {@link TargetSecondaryDataSource} and create copies of the repositories in the relevant
 * packages.
 */
@AutoService(Processor.class)
public class MultiDataSourceConfigAnnotationProcessor extends AbstractProcessor {

  private static final String MULTI_DATA_SOURCE_CONFIG_SUFFIX = "DataSourceConfig";

  private static final String CONFIG_PACKAGE_SUFFIX = ".generated.config";

  private static final String ERROR_WHILE_WRITING_THE_CLASS = "Error while writing the class: ";

  private Filer filer;

  private Messager messager;

  private Elements elementUtils;

  private Types typeUtils;

  private MultiDataSourceCommonStringUtils commonStringUtils;

  private MultiDataSourceGeneratorUtils generatorUtils;

  private MultiDataSourceConfigGenerator configGenerator;

  private MultiDataSourceRepositoryGenerator repositoryGenerator;

  /**
   * Constructor for the annotation processor to be run during compile time.
   */
  public MultiDataSourceConfigAnnotationProcessor() {
  }

  /**
   * Constructor for the annotation processor with dependency injection.
   * <p>
   * This constructor is used for testing purposes.
   *
   * @param filer               the filer to use for writing files
   * @param messager            the messager to use for printing messages
   * @param elementUtils        the element utils to use for getting packages
   * @param typeUtils           the type utils to use for getting types
   * @param commonStringUtils   Utility class for common string operations
   * @param generatorUtils      Utility class for generating code for the Multi Data Source library
   * @param configGenerator     the Multi Data Source config generator
   * @param repositoryGenerator the Multi Data Source repository generator
   */
  public MultiDataSourceConfigAnnotationProcessor(
      Filer filer,
      Messager messager,
      Elements elementUtils,
      Types typeUtils,
      MultiDataSourceCommonStringUtils commonStringUtils,
      MultiDataSourceGeneratorUtils generatorUtils,
      MultiDataSourceConfigGenerator configGenerator,
      MultiDataSourceRepositoryGenerator repositoryGenerator
  ) {
    this.filer = filer;
    this.messager = messager;
    this.elementUtils = elementUtils;
    this.typeUtils = typeUtils;
    this.commonStringUtils = commonStringUtils;
    this.generatorUtils = generatorUtils;
    this.configGenerator = configGenerator;
    this.repositoryGenerator = repositoryGenerator;
  }

  /**
   * {@inheritDoc}
   * <p>
   * Prepares the {@link Filer}, {@link Messager} and {@link Elements} for use in the processor.
   *
   * @param processingEnv environment to access facilities the tool framework provides to the
   *                      processor
   */
  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    this.filer = Objects.nonNull(this.filer) ? this.filer : processingEnv.getFiler();
    this.messager = Objects.nonNull(this.messager) ? this.messager : processingEnv.getMessager();
    this.elementUtils = Objects.nonNull(this.elementUtils) ? this.elementUtils
        : processingEnv.getElementUtils();
    this.typeUtils = Objects.nonNull(this.typeUtils) ? this.typeUtils
        : processingEnv.getTypeUtils();
    this.commonStringUtils = Objects.nonNull(this.commonStringUtils) ? this.commonStringUtils
        : MultiDataSourceCommonStringUtils.getInstance();
    this.generatorUtils = Objects.nonNull(this.generatorUtils) ? this.generatorUtils
        : MultiDataSourceGeneratorUtils.getInstance();
    this.configGenerator = Objects.nonNull(this.configGenerator) ? this.configGenerator
        : new MultiDataSourceConfigGenerator(this.generatorUtils, this.commonStringUtils);
    this.repositoryGenerator = Objects.nonNull(this.repositoryGenerator) ? this.repositoryGenerator
        : new MultiDataSourceRepositoryGenerator(
            this.messager,
            processingEnv.getTypeUtils(),
            this.commonStringUtils,
            this.generatorUtils
        );
  }

  /**
   * {@inheritDoc}
   * <p>
   * Performs the following tasks for each {@link TargetSecondaryDataSource} annotated repository
   * method:
   * <p>
   * 1. Aggregates the list of data sources to be used throughout the system
   * <p>
   * 2. Generates configs to create relevant beans for each of the data sources.
   * <p>
   * 3. Creates copies of the repositories with only the annotated methods in the relevant packages,
   * for package segregated data source injection which is required for multiple data source support
   * in Spring.
   *
   * @param annotations the annotation types requested to be processed
   * @param roundEnv    environment for information about the current and prior round
   * @return whether the set of annotations are claimed by this processor
   */
  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    // Get the element annotated with @EnableMultiDataSourceConfig and validate
    final EnableConfigAnnotationAndElementHolder holder = this
        .validateAndGetEnableConfigAnnotationAndElementHolder(roundEnv);
    // No holder means no annotation found
    if (holder == null) {
      return false;
    }
    final EnableMultiDataSourceConfig annotation = holder.getAnnotation();
    final Element annotatedElement = holder.getAnnotatedElement();

    // Create the primary data source config class
    final DataSourceConfig primaryConfig = this
        .validateAndGetPrimaryDataSourceConfig(annotation);
    final PackageElement annotatedElementPackage = elementUtils.getPackageOf(annotatedElement);
    final String nonEmptyGeneratedConfigPackage = this
        .getGeneratedConfigPackage(annotation, annotatedElementPackage);
    this.createDataSourceConfigurationClass(
        primaryConfig,
        annotation,
        nonEmptyGeneratedConfigPackage,
        annotation.repositoryPackages()
    );

    // Get the data source config maps
    final List<DataSourceConfig> secondaryDataSourceConfigs = List
        .of(annotation.secondaryDataSourceConfigs());

    final Map<String, DataSourceConfig> secondaryDataSourceConfigMap = this
        .createDataSourceToConfigMap(secondaryDataSourceConfigs);
    // Process the target executable elements to produce the alternate data source config classes
    for (final var executableElementsEntry : secondaryDataSourceConfigMap.entrySet()) {
      // Get the relevant details for this data source
      final String dataSourceName = executableElementsEntry.getKey();
      final DataSourceConfig dataSourceConfig = executableElementsEntry.getValue();
      this.createDataSourceConfigurationClass(
          dataSourceConfig,
          annotation,
          nonEmptyGeneratedConfigPackage,
          annotation.repositoryPackages()
      );

      final String generatedInfoString = "Generated config class for data source " + dataSourceName
          + ".\nPlease add the config values to the relevant properties file.";
      messager.printMessage(Kind.NOTE, generatedInfoString);
    }
    // As per sonatype, return false to indicate that the annotation processor is not claiming
    // the annotations: https://errorprone.info/bugpattern/DoNotClaimAnnotations
    return false;
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Set.of(EnableMultiDataSourceConfig.class.getCanonicalName());
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  /**
   * Get the {@link EnableMultiDataSourceConfig} annotation and the element on which it is
   * declared.
   *
   * @param roundEnv environment for information about the current and prior round
   * @return the {@link EnableMultiDataSourceConfig} annotation and the element on which it is
   * declared
   */
  private @Nullable EnableConfigAnnotationAndElementHolder validateAndGetEnableConfigAnnotationAndElementHolder(
      @Nonnull RoundEnvironment roundEnv
  ) {
    // Get all the DTOs annotated with @EnableMultiDataSourceConfig and validate count
    final Set<? extends Element> annotatedElements = roundEnv
        .getElementsAnnotatedWith(EnableMultiDataSourceConfig.class);
    if (annotatedElements.isEmpty()) {
      return null;
    }
    if (annotatedElements.size() > 1) {
      messager.printMessage(Kind.ERROR, MULTIPLE_CLASSES_ANNOTATED_WITH_ENABLE_CONFIG_ANNOTATION);
      throw new IllegalArgumentException(MULTIPLE_CLASSES_ANNOTATED_WITH_ENABLE_CONFIG_ANNOTATION);
    }

    // Get all the element annotated with @EnableMultiDataSourceConfig and the annotation
    final Element annotatedElement = annotatedElements.iterator().next();
    final EnableMultiDataSourceConfig annotation = annotatedElement
        .getAnnotation(EnableMultiDataSourceConfig.class);
    return new EnableConfigAnnotationAndElementHolder(annotatedElement, annotation);
  }


  /**
   * Validates that there is exactly one primary data source config and returns it.
   *
   * @param annotation the {@link EnableMultiDataSourceConfig} annotation
   * @return the primary {@link DataSourceConfig}
   */
  private @Nonnull DataSourceConfig validateAndGetPrimaryDataSourceConfig(
      @Nonnull EnableMultiDataSourceConfig annotation
  ) {
    final DataSourceConfig primaryConfig = annotation.primaryDataSourceConfig();
    final DataSourceConfig[] secondaryDataSourceConfigs = annotation.secondaryDataSourceConfigs();

    // Validate that there is exactly one primary data source config
    final boolean isPrimaryDataSourceNeverSecondaryDataSource = Stream
        .of(secondaryDataSourceConfigs)
        .noneMatch(config -> primaryConfig.dataSourceName().equals(config.dataSourceName()));
    if (isPrimaryDataSourceNeverSecondaryDataSource) {
      return primaryConfig;
    }

    final String errorMessage = "Primary data source " + primaryConfig.dataSourceName()
        + " is also marked as a secondary data source. Please do not mark the primary data source"
        + " as a secondary data source in the @EnableMultiDataSourceConfig annotation.";
    messager.printMessage(Kind.ERROR, errorMessage);
    throw new IllegalArgumentException(errorMessage);
  }

  /**
   * Creates a map of the data source name to the {@link DataSourceConfig} associated with it.
   *
   * @param dataSourceConfigs list of {@link DataSourceConfig}s
   * @return map of the data source name to the {@link DataSourceConfig} associated with it
   */
  private @Nonnull Map<String, DataSourceConfig> createDataSourceToConfigMap(
      @Nonnull List<DataSourceConfig> dataSourceConfigs
  ) {
    final Map<String, DataSourceConfig> secondaryDataSourceConfigMap = new HashMap<>();
    for (final DataSourceConfig dataSourceConfig : dataSourceConfigs) {
      if (secondaryDataSourceConfigMap.containsKey(dataSourceConfig.dataSourceName())) {
        messager.printMessage(Kind.ERROR, MULTIPLE_CONFIG_ANNOTATIONS_FOR_ONE_DATASOURCE);
        throw new IllegalArgumentException(MULTIPLE_CONFIG_ANNOTATIONS_FOR_ONE_DATASOURCE);
      }
      secondaryDataSourceConfigMap.put(dataSourceConfig.dataSourceName(), dataSourceConfig);
    }
    return secondaryDataSourceConfigMap;
  }

  /**
   * Get the generated config package from the annotation or the element package.
   * <p>
   * If the annotation has a value for the generated config package, use that. Otherwise, use the
   * package of the element on which the annotation is declared.
   *
   * @param annotation     the {@link EnableMultiDataSourceConfig} annotation
   * @param elementPackage the package of the element on which the annotation is declared
   * @return the generated config package
   */
  private @Nonnull String getGeneratedConfigPackage(
      @Nonnull EnableMultiDataSourceConfig annotation,
      @Nonnull PackageElement elementPackage
  ) {
    final String generatedConfigPackage = annotation.generatedConfigPackage();
    return StringUtils.hasText(generatedConfigPackage) ? generatedConfigPackage
        : elementPackage + CONFIG_PACKAGE_SUFFIX;
  }

  /**
   * Creates a data source config class for the {@link DataSourceConfig} provided.
   *
   * @param dataSourceConfig                  the {@link DataSourceConfig} for which the config
   *                                          class is to be generated
   * @param annotation                        the {@link EnableMultiDataSourceConfig} annotation
   *                                          from which the global level config is to be read
   * @param generatedConfigPackage            the package where the generated data source
   *                                          configuration will be placed
   * @param repositoryPackagesToIncludeInScan the repository packages to be scanned for
   *                                          repositories, specifically for this data source
   * @throws IllegalArgumentException if no entity packages or repository packages are provided in
   *                                  the annotation
   */
  private void createDataSourceConfigurationClass(
      @Nonnull DataSourceConfig dataSourceConfig,
      @Nonnull EnableMultiDataSourceConfig annotation,
      @Nonnull String generatedConfigPackage,
      @Nonnull String[] repositoryPackagesToIncludeInScan
  ) {
    final String dataSourceName = dataSourceConfig.dataSourceName();
    final String dataSourceConfigClassName = this.getDataSourceConfigClassName(dataSourceName);
    final String dataSourceConfigPropertiesPath = annotation.datasourcePropertiesPrefix()
        + "." + commonStringUtils.toKebabCase(dataSourceName);
    final Set<String> entityPackages = Set.of(dataSourceConfig.exactEntityPackages());

    // Validate the provided data source values
    if (entityPackages.isEmpty()) {
      final String errorMessage = "No entity packages are provided in @DataSourceConfig for data"
          + " source " + dataSourceName + ". Please provide all the packages (or a parent package)"
          + " that hold your entities.";
      messager.printMessage(Kind.ERROR, errorMessage);
      throw new IllegalArgumentException(errorMessage);
    }
    if (repositoryPackagesToIncludeInScan.length == 0) {
      messager.printMessage(Kind.ERROR, NO_REPOSITORY_PACKAGES_PROVIDED_IN_CONFIG);
      throw new IllegalArgumentException(NO_REPOSITORY_PACKAGES_PROVIDED_IN_CONFIG);
    }

    // Create the data source config class
    final boolean isPrimaryConfig = dataSourceName
        .equals(annotation.primaryDataSourceConfig().dataSourceName());
    final TypeSpec configurationTypeSpec = configGenerator.generateMultiDataSourceConfigTypeElement(
        dataSourceConfig,
        isPrimaryConfig,
        dataSourceConfigClassName,
        dataSourceConfigPropertiesPath,
        repositoryPackagesToIncludeInScan,
        entityPackages.toArray(String[]::new)
    );

    // Write the data source config class to the relevant package
    this.writeTypeSpecToPackage(generatedConfigPackage, configurationTypeSpec);
  }

  /**
   * Use a data source name to generate a PascalCase data source config class name.
   *
   * @param dataSourceName the data source name
   * @return the PascalCase data source config class name
   */
  private @Nonnull String getDataSourceConfigClassName(@Nonnull String dataSourceName) {
    return commonStringUtils.toPascalCase(dataSourceName) + MULTI_DATA_SOURCE_CONFIG_SUFFIX;
  }


  /**
   * Write a {@link TypeSpec} to a package using the {@link Filer}.
   *
   * @param targetPackage the package to write the {@link TypeSpec} to
   * @param typeSpec      the {@link TypeSpec} to write
   */
  private void writeTypeSpecToPackage(@Nonnull String targetPackage, @Nonnull TypeSpec typeSpec) {
    try {
      JavaFile.builder(targetPackage, typeSpec).build().writeTo(filer);
    } catch (IOException e) {
      messager.printMessage(Kind.ERROR, ERROR_WHILE_WRITING_THE_CLASS + e);
      throw new IllegalStateException(ERROR_WHILE_WRITING_THE_CLASS + e);
    }
  }

  /**
   * Get associated entity package from the {@link TypeElement} after validating that it is a valid
   * {@link org.springframework.data.jpa.repository.JpaRepository}
   * <p>
   * Jpa repositories are expected to extend
   * {@link org.springframework.data.jpa.repository.JpaRepository} and have generic type parameters
   * that extend entity classes.
   * <p>
   * Not used since 0.2.2.
   *
   * @param typeElement {@link TypeElement} to get the entity name from
   * @return entity name
   */
  @Deprecated
  private @Nonnull String getJpaRepositoryEntityPackage(@Nonnull TypeElement typeElement) {
    // Validate that the repository is a valid Repository
    final List<? extends TypeMirror> interfaceList = typeElement.getInterfaces();
    final DeclaredType validRepositoryType = typeUtils
        .getDeclaredType(elementUtils.getTypeElement(Repository.class.getCanonicalName()));
    final boolean isNotEligibleTypeElement = interfaceList.stream()
        .filter(element -> Objects.nonNull(element) && element instanceof DeclaredType)
        .noneMatch(
            element -> typeUtils
                .isAssignable(((DeclaredType) element).asElement().asType(), validRepositoryType)
        );
    if (isNotEligibleTypeElement) {
      final String errorMessage = "Repository " + typeElement.getSimpleName() +
          " is not a valid repository.";
      messager.printMessage(Kind.ERROR, errorMessage);
      throw new IllegalArgumentException(errorMessage);
    }

    // Validate that the repository has proper generic type parameters
    final DeclaredType inheritedJpaInterfaceType = (DeclaredType) interfaceList.get(0);
    final List<? extends TypeMirror> jpaInterfaceTypeArguments = inheritedJpaInterfaceType
        .getTypeArguments();
    if (jpaInterfaceTypeArguments.isEmpty()) {
      final String errorMessage = "Repository " + typeElement.getSimpleName()
          + " needs to have Entity and key type arguments.";
      messager.printMessage(Kind.ERROR, errorMessage);
      throw new IllegalArgumentException(errorMessage);
    }

    // Get the package of the entity from the generic type parameters of the JPA repository
    final Element entityElement = ((DeclaredType) jpaInterfaceTypeArguments.get(0)).asElement();
    return elementUtils.getPackageOf(entityElement).toString();
  }
}
