package io.github.dhi13man.spring.datasource.processor;

import com.squareup.javapoet.TypeSpec;
import io.github.dhi13man.spring.datasource.annotations.EnableMultiDataSourceConfig;
import io.github.dhi13man.spring.datasource.annotations.EnableMultiDataSourceConfig.DataSourceConfig;
import io.github.dhi13man.spring.datasource.annotations.MultiDataSourceRepository;
import io.github.dhi13man.spring.datasource.generators.MultiDataSourceConfigGenerator;
import io.github.dhi13man.spring.datasource.generators.MultiDataSourceRepositoryGenerator;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class MultiDataSourceAnnotationProcessorTest {

  private static final String MOCK_MASTER_DATA_SOURCE_NAME = "master";

  private static final String MOCK_SLAVE_DATA_SOURCE_NAME = "slave";

  private static final String MOCK_TEST_PACKAGE = "com.test";

  private static final String MOCK_MASTER_DATA_SOURCE_CONFIG_CLASS_NAME = "MasterDataSourceConfig";

  private static final String MOCK_DATASOURCE_PROPERTIES_PREFIX = "spring.datasource";

  private final ProcessingEnvironment mockProcessingEnvironment = Mockito
      .mock(ProcessingEnvironment.class);

  private final Filer mockFiler = Mockito.mock(Filer.class);

  private final Messager mockMessager = Mockito.mock(Messager.class);

  private final Elements mockElementUtils = Mockito.mock(Elements.class);

  private final MultiDataSourceConfigGenerator mockConfigGenerator = Mockito
      .mock(MultiDataSourceConfigGenerator.class);

  private final MultiDataSourceRepositoryGenerator mockRepositoryGenerator = Mockito
      .mock(MultiDataSourceRepositoryGenerator.class);

  private final MultiDataSourceAnnotationProcessor processor = new MultiDataSourceAnnotationProcessor(
      mockFiler,
      mockMessager,
      mockElementUtils,
      mockConfigGenerator,
      mockRepositoryGenerator
  );

  @Test
  void init() {
    // Arrange
    final MultiDataSourceAnnotationProcessor emptyConstructorProcessor =
        new MultiDataSourceAnnotationProcessor();

    // Act and Assert no exception thrown
    Assertions.assertDoesNotThrow(() -> processor.init(mockProcessingEnvironment));
    Assertions.assertDoesNotThrow(() -> emptyConstructorProcessor.init(mockProcessingEnvironment));
  }

  @Test
  void processNoAnnotatedElements() {
    // Arrange
    processor.init(mockProcessingEnvironment);
    final Set<? extends TypeElement> annotations = new HashSet<>();
    final RoundEnvironment mockRoundEnvironment = Mockito.mock(RoundEnvironment.class);

    // Act
    final boolean isClaimed = processor.process(annotations, mockRoundEnvironment);

    // Assert
    Mockito.verify(mockRoundEnvironment, Mockito.times(1))
        .getElementsAnnotatedWith(EnableMultiDataSourceConfig.class);
    Assertions.assertFalse(isClaimed);
  }

  @Test
  void processOneAnnotatedElementNoExactEntityPackages() {
    // Arrange
    processor.init(mockProcessingEnvironment);
    final Set<? extends TypeElement> annotations = Set.of(Mockito.mock(TypeElement.class));
    final RoundEnvironment mockRoundEnvironment = Mockito.mock(RoundEnvironment.class);
    final TypeElement mockAnnotatedElement = Mockito.mock(TypeElement.class);
    Mockito.when(mockRoundEnvironment.getElementsAnnotatedWith(EnableMultiDataSourceConfig.class))
        .then(invocation -> Set.of(mockAnnotatedElement));
    final EnableMultiDataSourceConfig mockAnnotation = Mockito
        .mock(EnableMultiDataSourceConfig.class);
    Mockito.when(mockAnnotation.generatedRepositoryPackagePrefix()).thenReturn(MOCK_TEST_PACKAGE);
    Mockito.when(mockAnnotation.generatedConfigPackage()).thenReturn(MOCK_TEST_PACKAGE);
    Mockito.when(mockAnnotation.exactEntityPackages()).thenReturn(new String[]{});
    Mockito.when(mockAnnotatedElement.getAnnotation(EnableMultiDataSourceConfig.class))
        .thenReturn(mockAnnotation);

    // Act and Assert IllegalArgumentException thrown
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> processor.process(annotations, mockRoundEnvironment)
    );
  }

  @Test
  void processOneAnnotatedElementNoRepositoryPackages() {
    // Arrange
    processor.init(mockProcessingEnvironment);
    final Set<? extends TypeElement> annotations = Set.of(Mockito.mock(TypeElement.class));
    final RoundEnvironment mockRoundEnvironment = Mockito.mock(RoundEnvironment.class);
    final TypeElement mockAnnotatedElement = Mockito.mock(TypeElement.class);
    Mockito.when(mockRoundEnvironment.getElementsAnnotatedWith(EnableMultiDataSourceConfig.class))
        .then(invocation -> Set.of(mockAnnotatedElement));
    final EnableMultiDataSourceConfig mockAnnotation = Mockito
        .mock(EnableMultiDataSourceConfig.class);
    Mockito.when(mockAnnotation.generatedRepositoryPackagePrefix()).thenReturn(MOCK_TEST_PACKAGE);
    Mockito.when(mockAnnotation.generatedConfigPackage()).thenReturn(MOCK_TEST_PACKAGE);
    Mockito.when(mockAnnotation.exactEntityPackages()).thenReturn(new String[]{MOCK_TEST_PACKAGE});
    Mockito.when(mockAnnotation.repositoryPackages()).thenReturn(new String[]{});
    Mockito.when(mockAnnotatedElement.getAnnotation(EnableMultiDataSourceConfig.class))
        .thenReturn(mockAnnotation);

    // Act and Assert IllegalArgumentException thrown
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> processor.process(annotations, mockRoundEnvironment)
    );
  }

  @Test
  void processOneAnnotatedElementProperPackages() {
    // Arrange
    processor.init(mockProcessingEnvironment);
    final Set<? extends TypeElement> annotations = Set.of(Mockito.mock(TypeElement.class));
    final RoundEnvironment mockRoundEnvironment = Mockito.mock(RoundEnvironment.class);
    final TypeElement mockAnnotatedElement = Mockito.mock(TypeElement.class);
    Mockito.when(mockRoundEnvironment.getElementsAnnotatedWith(EnableMultiDataSourceConfig.class))
        .then(invocation -> Set.of(mockAnnotatedElement));
    final EnableMultiDataSourceConfig mockAnnotation = Mockito
        .mock(EnableMultiDataSourceConfig.class);
    Mockito.when(mockAnnotation.generatedRepositoryPackagePrefix()).thenReturn(MOCK_TEST_PACKAGE);
    Mockito.when(mockAnnotation.generatedConfigPackage()).thenReturn(MOCK_TEST_PACKAGE);
    final String[] mockPackages = {MOCK_TEST_PACKAGE};
    Mockito.when(mockAnnotation.exactEntityPackages()).thenReturn(mockPackages);
    Mockito.when(mockAnnotation.repositoryPackages()).thenReturn(mockPackages);
    Mockito.when(mockAnnotation.datasourcePropertiesPrefix())
        .thenReturn(MOCK_DATASOURCE_PROPERTIES_PREFIX);
    Mockito.when(mockAnnotatedElement.getAnnotation(EnableMultiDataSourceConfig.class))
        .thenReturn(mockAnnotation);
    final DataSourceConfig mockDataSourceConfig = Mockito.mock(DataSourceConfig.class);
    Mockito.when(mockDataSourceConfig.dataSourceName()).thenReturn(MOCK_MASTER_DATA_SOURCE_NAME);
    Mockito.when(mockDataSourceConfig.isPrimary()).thenReturn(true);
    Mockito.when(mockAnnotation.dataSourceConfigs()).thenReturn(new DataSourceConfig[]{
        mockDataSourceConfig
    });
    final TypeSpec mockConfigTypeSpec = TypeSpec.classBuilder("MockConfig").build();
    Mockito.when(
        mockConfigGenerator.generateMultiDataSourceConfigTypeElement(
            mockDataSourceConfig,
            MOCK_MASTER_DATA_SOURCE_NAME,
            MOCK_MASTER_DATA_SOURCE_CONFIG_CLASS_NAME,
            MOCK_DATASOURCE_PROPERTIES_PREFIX + "." + MOCK_MASTER_DATA_SOURCE_NAME,
            mockPackages,
            mockPackages,
            MOCK_TEST_PACKAGE
        )
    ).thenReturn(mockConfigTypeSpec);

    // Act and Assert NullPointerException thrown for now as JavaFile is not mock-able.
    Assertions.assertThrows(
        NullPointerException.class,
        () -> processor.process(annotations, mockRoundEnvironment)
    );
  }

  @Test
  void processOneAnnotatedElementMoreThanOneDataSourceSameDatasource() {
    // Arrange
    processor.init(mockProcessingEnvironment);
    final Set<? extends TypeElement> annotations = Set
        .of(Mockito.mock(TypeElement.class), Mockito.mock(TypeElement.class));
    final RoundEnvironment mockRoundEnvironment = Mockito.mock(RoundEnvironment.class);
    final TypeElement mockAnnotatedElement = Mockito.mock(TypeElement.class);
    final Set<? extends Element> annotatedElements = Set.of(mockAnnotatedElement);
    Mockito.when(mockRoundEnvironment.getElementsAnnotatedWith(EnableMultiDataSourceConfig.class))
        .then(invocation -> annotatedElements);
    final EnableMultiDataSourceConfig mockAnnotation = Mockito
        .mock(EnableMultiDataSourceConfig.class);
    Mockito.when(mockAnnotatedElement.getAnnotation(EnableMultiDataSourceConfig.class))
        .thenReturn(mockAnnotation);
    final String[] mockPackages = {MOCK_TEST_PACKAGE};
    Mockito.when(mockAnnotation.exactEntityPackages()).thenReturn(mockPackages);
    Mockito.when(mockAnnotation.repositoryPackages()).thenReturn(mockPackages);
    final DataSourceConfig mockDataSourceConfig1 = Mockito.mock(DataSourceConfig.class);
    Mockito.when(mockDataSourceConfig1.dataSourceName()).thenReturn(MOCK_MASTER_DATA_SOURCE_NAME);
    Mockito.when(mockDataSourceConfig1.isPrimary()).thenReturn(true);
    final DataSourceConfig mockDataSourceConfig2 = Mockito.mock(DataSourceConfig.class);
    Mockito.when(mockDataSourceConfig2.dataSourceName()).thenReturn(MOCK_MASTER_DATA_SOURCE_NAME);
    Mockito.when(mockDataSourceConfig2.isPrimary()).thenReturn(false);
    Mockito.when(mockAnnotation.dataSourceConfigs()).thenReturn(new DataSourceConfig[]{
        mockDataSourceConfig1,
        mockDataSourceConfig2
    });

    // Act and Assert
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> processor.process(annotations, mockRoundEnvironment)
    );
  }

  @Test
  void processOneAnnotatedElementMoreThanOneDataSourceDifferentDatasource() {
    // Arrange
    processor.init(mockProcessingEnvironment);
    final Set<? extends TypeElement> annotations = Set
        .of(Mockito.mock(TypeElement.class), Mockito.mock(TypeElement.class));
    final RoundEnvironment mockRoundEnvironment = Mockito.mock(RoundEnvironment.class);
    final TypeElement mockAnnotatedElement = Mockito.mock(TypeElement.class);
    final Set<? extends Element> annotatedElements = Set.of(mockAnnotatedElement);
    Mockito.when(mockRoundEnvironment.getElementsAnnotatedWith(EnableMultiDataSourceConfig.class))
        .then(invocation -> annotatedElements);
    final EnableMultiDataSourceConfig mockAnnotation = Mockito
        .mock(EnableMultiDataSourceConfig.class);
    Mockito.when(mockAnnotatedElement.getAnnotation(EnableMultiDataSourceConfig.class))
        .thenReturn(mockAnnotation);
    final DataSourceConfig mockDataSourceConfig1 = Mockito.mock(DataSourceConfig.class);
    Mockito.when(mockDataSourceConfig1.dataSourceName()).thenReturn(MOCK_MASTER_DATA_SOURCE_NAME);
    Mockito.when(mockDataSourceConfig1.isPrimary()).thenReturn(true);
    final DataSourceConfig mockDataSourceConfig2 = Mockito.mock(DataSourceConfig.class);
    Mockito.when(mockDataSourceConfig2.dataSourceName()).thenReturn(MOCK_SLAVE_DATA_SOURCE_NAME);
    Mockito.when(mockDataSourceConfig2.isPrimary()).thenReturn(false);
    Mockito.when(mockAnnotation.dataSourceConfigs()).thenReturn(new DataSourceConfig[]{
        mockDataSourceConfig1,
        mockDataSourceConfig2
    });

    // Act and Assert
    Assertions.assertThrows(
        NullPointerException.class,
        () -> processor.process(annotations, mockRoundEnvironment)
    );
  }

  @Test
  void getSupportedAnnotationTypes() {
    // Arrange
    processor.init(mockProcessingEnvironment);
    final Set<String> expectedAnnotationTypes = Set.of(
        EnableMultiDataSourceConfig.class.getCanonicalName(),
        MultiDataSourceRepository.class.getCanonicalName()
    );

    // Act
    final Set<String> actualAnnotationTypes = processor.getSupportedAnnotationTypes();

    // Assert
    Assertions.assertEquals(expectedAnnotationTypes, actualAnnotationTypes);
  }

  @Test
  void getSupportedSourceVersion() {
    // Arrange
    processor.init(mockProcessingEnvironment);
    final SourceVersion expectedSourceVersion = SourceVersion.latestSupported();

    // Act
    final SourceVersion actualSourceVersion = processor.getSupportedSourceVersion();

    // Assert
    Assertions.assertEquals(expectedSourceVersion, actualSourceVersion);
  }
}