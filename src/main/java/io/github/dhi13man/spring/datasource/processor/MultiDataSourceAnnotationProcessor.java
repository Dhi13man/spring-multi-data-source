package io.github.dhi13man.spring.datasource.processor;

import static io.github.dhi13man.spring.datasource.constants.MultiDataSourceErrorConstants.MULTIPLE_ENABLE_CONFIG_ANNOTATIONS_FOR_ONE_DATASOURCE;
import static io.github.dhi13man.spring.datasource.constants.MultiDataSourceErrorConstants.NO_ENTITY_PACKAGES_PROVIDED_IN_CONFIG;
import static io.github.dhi13man.spring.datasource.constants.MultiDataSourceErrorConstants.NO_REPOSITORY_METHOD_ANNOTATED_WITH_MULTI_DATA_SOURCE_REPOSITORY;
import static io.github.dhi13man.spring.datasource.constants.MultiDataSourceErrorConstants.NO_REPOSITORY_PACKAGES_PROVIDED_IN_CONFIG;
import static io.github.dhi13man.spring.datasource.utils.MultiDataSourceCommonStringUtils.toKebabCase;
import static io.github.dhi13man.spring.datasource.utils.MultiDataSourceCommonStringUtils.toPascalCase;
import static io.github.dhi13man.spring.datasource.utils.MultiDataSourceCommonStringUtils.toSnakeCase;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import io.github.dhi13man.spring.datasource.annotations.EnableMultiDataSourceConfig;
import io.github.dhi13man.spring.datasource.annotations.EnableMultiDataSourceConfigs;
import io.github.dhi13man.spring.datasource.annotations.MultiDataSourceRepositories;
import io.github.dhi13man.spring.datasource.annotations.MultiDataSourceRepository;
import io.github.dhi13man.spring.datasource.dto.ElementAndConfigAnnotationHolder;
import io.github.dhi13man.spring.datasource.generators.MultiDataSourceConfigGenerator;
import io.github.dhi13man.spring.datasource.generators.MultiDataSourceRepositoryGenerator;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic.Kind;
import org.springframework.util.StringUtils;

/**
 * Annotation processor to generate config classes for all the repositories annotated with
 * {@link MultiDataSourceRepository} and create copies of the repositories in the relevant
 * packages.
 */
@AutoService(Processor.class)
public class MultiDataSourceAnnotationProcessor extends AbstractProcessor {

  private static final String MULTI_DATA_SOURCE_CONFIG_SUFFIX = "DataSourceConfig";

  private static final String JPA_REPOSITORY_INTERFACE_NAME = "JpaRepository";

  private static final String CONFIG_PACKAGE_SUFFIX = ".config";

  private static final String REPOSITORIES_PACKAGE_SUFFIX = ".repositories";

  private Filer filer;

  private Messager messager;

  private Elements elementUtils;

  private MultiDataSourceConfigGenerator configGenerator;

  private MultiDataSourceRepositoryGenerator repositoryGenerator;

  /**
   * Constructor for the annotation processor to be run during compile time.
   */
  public MultiDataSourceAnnotationProcessor() {
  }

  /**
   * Constructor for the annotation processor with dependency injection.
   * <p>
   * This constructor is used for testing purposes.
   *
   * @param filer               the filer to use for writing files
   * @param messager            the messager to use for printing messages
   * @param elementUtils        the element utils to use for getting packages
   * @param configGenerator     the Multi Data Source config generator
   * @param repositoryGenerator the Multi Data Source repository generator
   */
  public MultiDataSourceAnnotationProcessor(
      Filer filer,
      Messager messager,
      Elements elementUtils,
      MultiDataSourceConfigGenerator configGenerator,
      MultiDataSourceRepositoryGenerator repositoryGenerator
  ) {
    this.filer = filer;
    this.messager = messager;
    this.elementUtils = elementUtils;
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
    this.configGenerator = Objects.nonNull(this.configGenerator) ? this.configGenerator
        : new MultiDataSourceConfigGenerator(messager);
    this.repositoryGenerator = Objects.nonNull(this.repositoryGenerator) ? this.repositoryGenerator
        : new MultiDataSourceRepositoryGenerator(messager, processingEnv.getTypeUtils());
  }

  /**
   * {@inheritDoc}
   * <p>
   * Performs the following tasks for each {@link MultiDataSourceRepository} annotated repository
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
    // Get all the elements annotated with @EnableMultiDataSourceConfig and make a map of the
    // data source name to the annotation and the element on which it is declared
    final Map<String, ElementAndConfigAnnotationHolder> dataSourceToConfigMap =
        createDataSourceToEnableConfigAnnotationAndElementMap(roundEnv);
    // Create all Datasource configs
    for (final ElementAndConfigAnnotationHolder holder : dataSourceToConfigMap.values()) {
      createDataSourceConfig(holder);
    }

    // Get relevant alternate data source to target type elements map
    final Map<String, Set<ExecutableElement>> dataSourceToTargetExecutableElementsMap =
        createDataSourceToTargetElementsMap(roundEnv, dataSourceToConfigMap);
    if (dataSourceToTargetExecutableElementsMap.isEmpty()) {
      messager.printMessage(
          Kind.NOTE,
          NO_REPOSITORY_METHOD_ANNOTATED_WITH_MULTI_DATA_SOURCE_REPOSITORY
      );
      return false;
    }

    // Process the target executable elements to produce the alternate data source config classes
    for (final Entry<String, Set<ExecutableElement>> executableElementsEntry
        : dataSourceToTargetExecutableElementsMap.entrySet()) {
      // Get the relevant details for this data source
      final String dataSourceName = executableElementsEntry.getKey();
      final Set<ExecutableElement> executableElements = executableElementsEntry.getValue();
      final Element annotatedElement = dataSourceToConfigMap.get(dataSourceName).getElement();
      final EnableMultiDataSourceConfig annotation = dataSourceToConfigMap.get(dataSourceName)
          .getAnnotation();

      // Create a map of type elements to executable elements for this data source
      final Map<TypeElement, Set<ExecutableElement>> repositoryToMethodMap =
          createTypeElementToExecutableElementsMap(executableElements);
      // Get the entity packages related to this data source from the type elements
      // (entities will be provided in JPA Type parameters)
      final List<String> dataSourceEntityPackages = repositoryToMethodMap.keySet().stream()
          .map(this::getJpaRepositoryEntityPackage)
          .collect(Collectors.toList());
      dataSourceEntityPackages.addAll(List.of(annotation.exactEntityPackages()));

      // Get the data source name and other relevant details for this data source
      final String dataSourcePropertiesPath = annotation.datasourcePropertiesPrefix() + "."
          + toKebabCase(dataSourceName);
      final PackageElement elementPackage = elementUtils.getPackageOf(annotatedElement);
      final String repositoryPackage = annotation.generatedRepositoryPackagePrefix();
      final String generatedRepositoryPackagePrefix = StringUtils.hasText(repositoryPackage)
          ? repositoryPackage : elementPackage + REPOSITORIES_PACKAGE_SUFFIX;
      final String dataSourceRepositorySubPackage =
          generatedRepositoryPackagePrefix + "." + toSnakeCase(dataSourceName);

      // Copy all the repositories to the relevant sub-package with only the annotated methods
      for (final Entry<TypeElement, Set<ExecutableElement>> typeToExecutableEntry
          : repositoryToMethodMap.entrySet()) {
        final TypeElement typeElement = typeToExecutableEntry.getKey();

        // Get the annotated methods for this type element
        final Set<ExecutableElement> annotatedMethods = typeToExecutableEntry.getValue();
        final TypeSpec copiedTypeSpec = repositoryGenerator.generateRepositoryTypeElementWithAnnotatedMethods(
            typeElement,
            annotatedMethods,
            dataSourceName
        );
        writeTypeSpecToPackage(dataSourceRepositorySubPackage, copiedTypeSpec);
      }

      final String generatedInfoString = "Generated config class " + " for data source "
          + dataSourceName + " and extended " + executableElements.size() + " repositories to"
          + " package " + dataSourceRepositorySubPackage + ".\nPlease add the config values to the"
          + " relevant properties file at " + dataSourcePropertiesPath + "\n";
      messager.printMessage(Kind.NOTE, generatedInfoString);
    }
    // As per sonatype, return false to indicate that the annotation processor is not claiming
    // the annotations: https://errorprone.info/bugpattern/DoNotClaimAnnotations
    return false;
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Set.of(
        EnableMultiDataSourceConfig.class.getCanonicalName(),
        MultiDataSourceRepository.class.getCanonicalName()
    );
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  /**
   * Creates a map of the data source name to the {@link ElementAndConfigAnnotationHolder} for the
   * {@link EnableMultiDataSourceConfig} annotation and the element on which it is declared.
   *
   * @param roundEnv environment for information about the current and prior round
   * @return map of the data source name to the {@link ElementAndConfigAnnotationHolder}.
   */
  private Map<String, ElementAndConfigAnnotationHolder> createDataSourceToEnableConfigAnnotationAndElementMap(
      RoundEnvironment roundEnv
  ) {
    // Deal with @EnableMultiDataSourceConfigs container annotations
    final List<ElementAndConfigAnnotationHolder> elementAndConfigAnnotationHolders = roundEnv
        .getElementsAnnotatedWith(EnableMultiDataSourceConfig.class)
        .stream()
        .filter(element -> element instanceof TypeElement)
        .map(TypeElement.class::cast)
        .map(
            element -> new ElementAndConfigAnnotationHolder(
                element,
                element.getAnnotation(EnableMultiDataSourceConfig.class)
            )
        )
        .collect(Collectors.toList());

    // Deal with @EnableMultiDataSourceConfigs container annotations
    final Set<TypeElement> annotatedElements = roundEnv
        .getElementsAnnotatedWith(EnableMultiDataSourceConfigs.class)
        .stream()
        .filter(element -> element instanceof TypeElement)
        .map(TypeElement.class::cast)
        .collect(Collectors.toSet());
    for (final TypeElement element : annotatedElements) {
      final EnableMultiDataSourceConfigs repositoriesAnnotation = element
          .getAnnotation(EnableMultiDataSourceConfigs.class);
      for (final EnableMultiDataSourceConfig configAnnotation : repositoriesAnnotation.value()) {
        elementAndConfigAnnotationHolders
            .add(new ElementAndConfigAnnotationHolder(element, configAnnotation));
      }
    }

    // Validate that there is only one config per data source and create a map of the data source
    // name to the config annotation and the element on which it is declared
    final Map<String, ElementAndConfigAnnotationHolder> dataSourceToConfigMapWithUniqueKeys =
        new HashMap<>();
    for (final ElementAndConfigAnnotationHolder holder : elementAndConfigAnnotationHolders) {
      final String dataSourceName = holder.getAnnotation().dataSourceName();
      if (dataSourceToConfigMapWithUniqueKeys.containsKey(dataSourceName)) {
        messager.printMessage(Kind.ERROR, MULTIPLE_ENABLE_CONFIG_ANNOTATIONS_FOR_ONE_DATASOURCE);
        throw new IllegalArgumentException(MULTIPLE_ENABLE_CONFIG_ANNOTATIONS_FOR_ONE_DATASOURCE);
      }
      dataSourceToConfigMapWithUniqueKeys.put(dataSourceName, holder);
    }
    return dataSourceToConfigMapWithUniqueKeys;
  }

  /**
   * Creates a data source config class for the {@link EnableMultiDataSourceConfig} annotation and
   * the element on which it is declared.
   *
   * @param holder the {@link ElementAndConfigAnnotationHolder} for the
   *               {@link EnableMultiDataSourceConfig} annotation and the element on which it is
   *               declared
   * @throws IllegalArgumentException if no entity packages or repository packages are provided in
   *                                  the annotation
   */
  private void createDataSourceConfig(ElementAndConfigAnnotationHolder holder) {
    // Get the relevant details from the annotation
    final EnableMultiDataSourceConfig annotation = holder.getAnnotation();
    final String dataSourceName = annotation.dataSourceName();
    final String dataSourceConfigClassName = getDataSourceConfigClassName(dataSourceName);
    final String dataSourceConfigPropertiesPath = annotation.datasourcePropertiesPrefix()
        + "." + toKebabCase(dataSourceName);
    final String[] repositoryPackages = annotation.repositoryPackages();
    final String[] entityPackages = annotation.exactEntityPackages();
    final PackageElement elementPackage = elementUtils.getPackageOf(holder.getElement());
    final String configPackage = annotation.generatedConfigPackage();
    final String generatedConfigPackage = StringUtils.hasText(configPackage) ? configPackage
        : elementPackage + CONFIG_PACKAGE_SUFFIX;
    final String repositoryPackage = annotation.generatedRepositoryPackagePrefix();
    final String generatedRepositoryPackagePrefix = StringUtils.hasText(repositoryPackage)
        ? repositoryPackage : elementPackage + REPOSITORIES_PACKAGE_SUFFIX;

    // Validate the provided master data source values
    if (entityPackages.length == 0) {
      messager.printMessage(Kind.ERROR, NO_ENTITY_PACKAGES_PROVIDED_IN_CONFIG);
      throw new IllegalArgumentException(NO_ENTITY_PACKAGES_PROVIDED_IN_CONFIG);
    }
    if (repositoryPackages.length == 0) {
      messager.printMessage(Kind.ERROR, NO_REPOSITORY_PACKAGES_PROVIDED_IN_CONFIG);
      throw new IllegalArgumentException(NO_REPOSITORY_PACKAGES_PROVIDED_IN_CONFIG);
    }

    // Create the Master data source config class
    final TypeSpec masterConfigTypeSpec = configGenerator.generateMultiDataSourceConfigTypeElement(
        annotation,
        dataSourceName,
        dataSourceConfigClassName,
        dataSourceConfigPropertiesPath,
        repositoryPackages,
        entityPackages,
        generatedRepositoryPackagePrefix
    );
    writeTypeSpecToPackage(generatedConfigPackage, masterConfigTypeSpec);
  }

  /**
   * Creates a map of the data source name to the set of ExecutableElements that are annotated with
   * {@link MultiDataSourceRepository} for that data source.
   * <p>
   * Targets all Classes annotated with {@link MultiDataSourceRepository} or its container
   * annotation. Return a map grouped by the data source name to the set of ExecutableElements that
   * are annotated with {@link MultiDataSourceRepository} for that data source.
   *
   * @param roundEnv              environment for information about the current and prior round
   * @param dataSourceToConfigMap map of the data source name to the
   *                              {@link ElementAndConfigAnnotationHolder}
   * @return map of the data source name to the set of ExecutableElements that are annotated with
   */
  private Map<String, Set<ExecutableElement>> createDataSourceToTargetElementsMap(
      RoundEnvironment roundEnv,
      Map<String, ElementAndConfigAnnotationHolder> dataSourceToConfigMap
  ) {
    // Deal with individual @MultiDataSourceRepository annotations
    final Map<String, Set<ExecutableElement>> multiDataSourceRepositoryElementMap = roundEnv
        .getElementsAnnotatedWith(MultiDataSourceRepository.class)
        .stream()
        .filter(element -> element instanceof ExecutableElement)
        .map(ExecutableElement.class::cast)
        .collect(
            Collectors.groupingBy(
                x -> x.getAnnotation(MultiDataSourceRepository.class).value(),
                Collectors.toSet()
            )
        );

    // Deal with @MultiDataSourceRepositories container annotations
    final Set<ExecutableElement> annotatedElements = roundEnv
        .getElementsAnnotatedWith(MultiDataSourceRepositories.class)
        .stream()
        .filter(element -> element instanceof ExecutableElement)
        .map(ExecutableElement.class::cast)
        .collect(Collectors.toSet());
    for (final ExecutableElement element : annotatedElements) {
      final MultiDataSourceRepositories repositoriesAnnotation = element
          .getAnnotation(MultiDataSourceRepositories.class);
      final List<String> dataSourcesInvolved = Arrays.stream(repositoriesAnnotation.value())
          .map(MultiDataSourceRepository::value)
          .collect(Collectors.toList());
      for (final String dataSourceName : dataSourcesInvolved) {
        final Set<ExecutableElement> executableElements = multiDataSourceRepositoryElementMap
            .getOrDefault(dataSourceName, new HashSet<>());
        executableElements.add(element);
        multiDataSourceRepositoryElementMap.put(dataSourceName, executableElements);
      }
    }

    // Validate that all the data sources involved have a config
    final Set<String> dataSourcesWithoutConfig = multiDataSourceRepositoryElementMap.keySet()
        .stream()
        .filter(dataSourceName -> !dataSourceToConfigMap.containsKey(dataSourceName))
        .collect(Collectors.toSet());
    if (!dataSourcesWithoutConfig.isEmpty()) {
      final String errorMessage = "No config found for data sources: " + dataSourcesWithoutConfig
          + ". Please provide a @EnableMultiDataSourceConfig annotation for each data source.";
      messager.printMessage(Kind.ERROR, errorMessage);
      throw new IllegalArgumentException(errorMessage);
    }
    return multiDataSourceRepositoryElementMap;
  }

  /**
   * Creates a map of the {@link TypeElement} to the set of {@link ExecutableElement}s that are
   * annotated with {@link MultiDataSourceRepository}.
   *
   * @param executableElements set of {@link ExecutableElement}s that are annotated with
   *                           {@link MultiDataSourceRepository}
   * @return map of the {@link TypeElement} to the set of {@link ExecutableElement}s that are
   * annotated with {@link MultiDataSourceRepository}
   */
  private Map<TypeElement, Set<ExecutableElement>> createTypeElementToExecutableElementsMap(
      Set<ExecutableElement> executableElements
  ) {
    return executableElements.stream().collect(
        Collectors.groupingBy(
            x -> (TypeElement) x.getEnclosingElement(),
            Collectors.toSet()
        )
    );
  }

  /**
   * Get associated entity package from the {@link TypeElement} after validating that it is a valid
   * {@link org.springframework.data.jpa.repository.JpaRepository}
   * <p>
   * Jpa repositories are expected to extend
   * {@link org.springframework.data.jpa.repository.JpaRepository} and have generic type parameters
   * that extend entity classes.
   *
   * @param typeElement {@link TypeElement} to get the entity name from
   * @return entity name
   */
  private String getJpaRepositoryEntityPackage(TypeElement typeElement) {
    {
      // Validate that the repository is a valid JPA repository
      final List<? extends TypeMirror> interfaceList = typeElement.getInterfaces();
      final boolean isEligibleTypeElement = interfaceList.isEmpty()
          || !(interfaceList.get(0) instanceof DeclaredType)
          || !((DeclaredType) interfaceList.get(0)).asElement().getSimpleName()
          .toString().equals(JPA_REPOSITORY_INTERFACE_NAME);
      if (isEligibleTypeElement) {
        final String errorMessage = "Repository " + typeElement.getSimpleName() +
            " is not a valid JPA repository.";
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


  /**
   * Write a {@link TypeSpec} to a package using the {@link Filer}.
   *
   * @param targetPackage the package to write the {@link TypeSpec} to
   * @param typeSpec      the {@link TypeSpec} to write
   */
  private void writeTypeSpecToPackage(String targetPackage, TypeSpec typeSpec) {
    try {
      JavaFile.builder(targetPackage, typeSpec).build().writeTo(filer);
    } catch (IOException e) {
      messager.printMessage(Kind.ERROR, "Error while writing the class: " + e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Use a data source name to generate a PascalCase data source config class name.
   *
   * @param dataSourceName the data source name
   * @return the PascalCase data source config class name
   */
  private String getDataSourceConfigClassName(String dataSourceName) {
    return toPascalCase(dataSourceName) + MULTI_DATA_SOURCE_CONFIG_SUFFIX;
  }
}
