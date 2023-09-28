package io.github.dhi13man.spring.datasource.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.squareup.javapoet.FieldSpec;
import javax.lang.model.element.Modifier;
import org.junit.jupiter.api.Test;

public class MultiDataSourceGeneratorUtilsTest {

  private final MultiDataSourceGeneratorUtils multiDataSourceCommonStringUtils = MultiDataSourceGeneratorUtils
      .getInstance();

  @Test
  public void createConstantStringFieldSpec_withSingleValue_createsSimpleConstant() {
    // Arrange
    final String fieldName = "FIELD_NAME";
    final String fieldValue = "FIELD_VALUE";

    // Act
    final FieldSpec result = multiDataSourceCommonStringUtils
        .createConstantStringFieldSpec(fieldName, fieldValue);

    // Assert
    assertEquals(fieldName, result.name);
    assertTrue(result.modifiers.contains(Modifier.PUBLIC));
    assertTrue(result.modifiers.contains(Modifier.STATIC));
    assertTrue(result.modifiers.contains(Modifier.FINAL));
    assertEquals('"' + fieldValue + '"', result.initializer.toString());
  }

  @Test
  public void createConstantStringFieldSpec_withMultipleValues_createsConstantArray() {
    // Arrange
    final String fieldName = "FIELD_NAME";
    final String[] fieldValues = {"FIELD_VALUE_1", "FIELD_VALUE_2"};

    // Act
    final FieldSpec result = multiDataSourceCommonStringUtils
        .createConstantStringFieldSpec(fieldName, fieldValues);

    // Assert
    assertEquals(fieldName, result.name);
    assertTrue(result.modifiers.contains(Modifier.PUBLIC));
    assertTrue(result.modifiers.contains(Modifier.STATIC));
    assertTrue(result.modifiers.contains(Modifier.FINAL));
    assertEquals(
        "new String[]{\"FIELD_VALUE_1\", \"FIELD_VALUE_2\"};",
        result.initializer.toString()
    );
  }

  @Test
  public void createConstantStringFieldSpec_withEmptyValues_throwsIllegalArgumentException() {
    // Arrange
    String fieldName = "FIELD_NAME";
    String[] fieldValues = {};

    // Act
    final FieldSpec result = multiDataSourceCommonStringUtils
        .createConstantStringFieldSpec(fieldName, fieldValues);

    // Assert
    assertEquals(fieldName, result.name);
    assertTrue(result.modifiers.contains(Modifier.PUBLIC));
    assertTrue(result.modifiers.contains(Modifier.STATIC));
    assertTrue(result.modifiers.contains(Modifier.FINAL));
    assertEquals("new String[]{};", result.initializer.toString());
  }
}