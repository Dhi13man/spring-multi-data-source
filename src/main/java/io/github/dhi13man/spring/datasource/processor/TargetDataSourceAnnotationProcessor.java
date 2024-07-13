package io.github.dhi13man.spring.datasource.processor;

import static io.github.dhi13man.spring.datasource.constants.MultiDataSourceErrorConstants.NO_REPOSITORY_METHOD_ANNOTATED_WITH_TARGET_SECONDARY_DATA_SOURCE;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import io.github.dhi13man.spring.datasource.annotations.TargetSecondaryDataSource;
import io.github.dhi13man.spring.datasource.annotations.TargetSecondaryDataSources;
import io.github.dhi13man.spring.datasource.generators.MultiDataSourceConfigGenerator;
import io.github.dhi13man.spring.datasource.generators.MultiDataSourceRepositoryGenerator;
import io.github.dhi13man.spring.datasource.utils.MultiDataSourceCommonStringUtils;
import io.github.dhi13man.spring.datasource.utils.MultiDataSourceGeneratorUtils;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

/**
 * Annotation processor to  create copies of the repositories in relevant packages for all the
 * repositories annotated with  {@link TargetSecondaryDataSource}
 */
@AutoService(Processor.class)
public class TargetDataSourceAnnotationProcessor extends AbstractProcessor {

  public static final String GENERATED_REPOSITORIES_PACKAGE_SUFFIX = ".generated.repositories";

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
  public TargetDataSourceAnnotationProcessor() {
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
  public TargetDataSourceAnnotationProcessor(
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
    final Map<String, Set<ExecutableElement>> dataSourceToTargetRepositoryMethodMap = this
        .createDataSourceToTargetRepositoryMethodMap(roundEnv);
    if (dataSourceToTargetRepositoryMethodMap.isEmpty()) {
      messager.printMessage(
          Kind.NOTE,
          NO_REPOSITORY_METHOD_ANNOTATED_WITH_TARGET_SECONDARY_DATA_SOURCE
      );
      return false;
    }

    // Process the target executable elements to produce the alternate data source config classes
    for (final var executableElementsEntry : dataSourceToTargetRepositoryMethodMap.entrySet()) {
      // Get the relevant details for this data source
      final String dataSourceName = executableElementsEntry.getKey();
      final Set<ExecutableElement> executableElements = executableElementsEntry.getValue();

      // Create map of type elements (repositories) to executable elements (methods) for this source
      final Map<TypeElement, Set<ExecutableElement>> repositoryToMethodMap = this
          .createTypeElementToExecutableElementsMap(executableElements);
      repositoryToMethodMap.forEach((k, v) -> this.generateRepositories(k, v, dataSourceName));

      final String generatedInfoString = executableElements.size()
          + " Repositories for data source " + dataSourceName + " generated.";
      messager.printMessage(Kind.NOTE, generatedInfoString);
    }
    // As per sonatype, return false to indicate that the annotation processor is not claiming
    // the annotations: https://errorprone.info/bugpattern/DoNotClaimAnnotations
    return false;
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Set.of(
        TargetSecondaryDataSource.class.getCanonicalName(),
        TargetSecondaryDataSources.class.getCanonicalName()
    );
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  /**
   * Generate the repositories for the given type element and annotated methods.
   * <p>
   * Creates a copy of the repository in the relevant package with only the annotated methods.
   *
   * @param typeElement      the type element for the source repository
   * @param annotatedMethods the set of annotated methods for the source repository that need to be
   *                         copied for the target data source
   * @param dataSourceName   the name of the target data source to generate the repository for
   */
  private void generateRepositories(
      @Nonnull TypeElement typeElement,
      @Nonnull Set<ExecutableElement> annotatedMethods,
      @Nonnull String dataSourceName
  ) {
    // Generate the repository type element with only the annotated methods as allowed
    final TypeSpec copiedTypeSpec = repositoryGenerator.generateRepositoryTypeElementWithAnnotatedMethods(
        typeElement,
        annotatedMethods,
        dataSourceName
    );
    final PackageElement elementPackage = elementUtils.getPackageOf(typeElement);
    final String repositoryDataSourceSubPackage = this
        .generateNonPrimaryDataSourceRepositoryPackage(elementPackage, dataSourceName);
    writeTypeSpecToPackage(repositoryDataSourceSubPackage, copiedTypeSpec);
  }

  /**
   * Creates a map of the data source name to the set of ExecutableElements that are annotated with
   * {@link TargetSecondaryDataSource} for that data source.
   * <p>
   * Targets all Classes annotated with {@link TargetSecondaryDataSource} or its container
   * annotation. Return a map grouped by the data source name to the set of ExecutableElements that
   * are annotated with {@link TargetSecondaryDataSource} for that data source.
   *
   * @param roundEnv environment for information about the current and prior round
   * @return map of the data source name to the set of ExecutableElements that are annotated with
   */
  private @Nonnull Map<String, Set<ExecutableElement>> createDataSourceToTargetRepositoryMethodMap(
      @Nonnull RoundEnvironment roundEnv
  ) {
    // Deal with individual @TargetSecondaryDataSource annotations
    final Map<String, Set<ExecutableElement>> targetDataSourceAnnotatedMethodMap = roundEnv
        .getElementsAnnotatedWith(TargetSecondaryDataSource.class)
        .stream()
        .filter(element -> element instanceof ExecutableElement)
        .map(ExecutableElement.class::cast)
        .collect(
            Collectors.groupingBy(
                x -> x.getAnnotation(TargetSecondaryDataSource.class).value(),
                Collectors.toSet()
            )
        );

    // Deal with @TargetSecondaryDataSources container annotations
    final Set<ExecutableElement> annotatedElements = roundEnv
        .getElementsAnnotatedWith(TargetSecondaryDataSources.class)
        .stream()
        .filter(element -> element instanceof ExecutableElement)
        .map(ExecutableElement.class::cast)
        .collect(Collectors.toSet());
    for (final ExecutableElement element : annotatedElements) {
      final TargetSecondaryDataSources repositoriesAnnotation = element
          .getAnnotation(TargetSecondaryDataSources.class);
      final List<String> dataSourcesInvolved = Arrays.stream(repositoriesAnnotation.value())
          .map(TargetSecondaryDataSource::value)
          .collect(Collectors.toList());
      for (final String dataSourceName : dataSourcesInvolved) {
        final Set<ExecutableElement> executableElements = targetDataSourceAnnotatedMethodMap
            .getOrDefault(dataSourceName, new HashSet<>());
        executableElements.add(element);
        targetDataSourceAnnotatedMethodMap.put(dataSourceName, executableElements);
      }
    }
    return targetDataSourceAnnotatedMethodMap;
  }

  /**
   * Creates a map of the {@link TypeElement} to the set of {@link ExecutableElement}s that are
   * annotated with {@link TargetSecondaryDataSource}.
   *
   * @param executableElements set of {@link ExecutableElement}s that are annotated with
   *                           {@link TargetSecondaryDataSource}
   * @return map of the {@link TypeElement} to the set of {@link ExecutableElement}s that are
   * annotated with {@link TargetSecondaryDataSource}
   */
  private @Nonnull Map<TypeElement, Set<ExecutableElement>> createTypeElementToExecutableElementsMap(
      @Nonnull Set<ExecutableElement> executableElements
  ) {
    return executableElements.stream().collect(
        Collectors.groupingBy(x -> (TypeElement) x.getEnclosingElement(), Collectors.toSet())
    );
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
   * Generate the package name for the non-primary data source repositories.
   *
   * @param elementPackage the package element of the source repository
   * @param dataSourceName the name of the data source to generate the repository package for
   * @return the package name for the non-primary data source repositories
   */
  private @Nonnull String generateNonPrimaryDataSourceRepositoryPackage(
      @Nonnull PackageElement elementPackage,
      @Nonnull String dataSourceName
  ) {
    return elementPackage + GENERATED_REPOSITORIES_PACKAGE_SUFFIX + "."
        + commonStringUtils.toSnakeCase(dataSourceName);
  }
}
