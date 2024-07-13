package io.github.dhi13man.spring.datasource.processor;

import io.github.dhi13man.spring.datasource.annotations.TargetSecondaryDataSource;
import io.github.dhi13man.spring.datasource.annotations.TargetSecondaryDataSources;
import io.github.dhi13man.spring.datasource.generators.MultiDataSourceConfigGenerator;
import io.github.dhi13man.spring.datasource.generators.MultiDataSourceRepositoryGenerator;
import io.github.dhi13man.spring.datasource.utils.MultiDataSourceCommonStringUtils;
import io.github.dhi13man.spring.datasource.utils.MultiDataSourceGeneratorUtils;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TargetDataSourceAnnotationProcessorTest {

  private final ProcessingEnvironment mockProcessingEnvironment = Mockito
      .mock(ProcessingEnvironment.class);

  private final Filer mockFiler = Mockito.mock(Filer.class);

  private final Messager mockMessager = Mockito.mock(Messager.class);

  private final Elements mockElementUtils = Mockito.mock(Elements.class);

  private final Types mockTypeUtils = Mockito.mock(Types.class);

  private final MultiDataSourceCommonStringUtils mockStringUtils = Mockito
      .mock(MultiDataSourceCommonStringUtils.class);

  private final MultiDataSourceGeneratorUtils mockGeneratorUtils = Mockito
      .mock(MultiDataSourceGeneratorUtils.class);

  private final MultiDataSourceConfigGenerator mockConfigGenerator = Mockito
      .mock(MultiDataSourceConfigGenerator.class);

  private final MultiDataSourceRepositoryGenerator mockRepositoryGenerator = Mockito
      .mock(MultiDataSourceRepositoryGenerator.class);

  private final TargetDataSourceAnnotationProcessor processor = new TargetDataSourceAnnotationProcessor(
      mockFiler,
      mockMessager,
      mockElementUtils,
      mockTypeUtils,
      mockStringUtils,
      mockGeneratorUtils,
      mockConfigGenerator,
      mockRepositoryGenerator
  );

  @Test
  void init() {
    // Arrange
    final TargetDataSourceAnnotationProcessor emptyConstructorProcessor =
        new TargetDataSourceAnnotationProcessor();

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
        .getElementsAnnotatedWith(TargetSecondaryDataSource.class);
    Assertions.assertFalse(isClaimed);
  }

  @Test
  void getSupportedAnnotationTypes() {
    // Arrange
    processor.init(mockProcessingEnvironment);
    final Set<String> expectedAnnotationTypes = Set.of(
        TargetSecondaryDataSource.class.getCanonicalName(),
        TargetSecondaryDataSources.class.getCanonicalName()
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