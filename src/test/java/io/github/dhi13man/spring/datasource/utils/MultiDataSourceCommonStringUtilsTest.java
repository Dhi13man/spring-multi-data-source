package io.github.dhi13man.spring.datasource.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class MultiDataSourceCommonStringUtilsTest {

  private final MultiDataSourceCommonStringUtils multiDataSourceCommonStringUtils = MultiDataSourceCommonStringUtils
      .getInstance();

  @Test
  public void toPascalCase_withSingleWord_returnsPascalCase() {
    // Arrange
    final String input = "hello";

    // Act
    final String result = multiDataSourceCommonStringUtils.toPascalCase(input);

    // Assert
    assertEquals("Hello", result);
  }

  @Test
  public void toPascalCase_withMultipleWords_returnsPascalCase() {
    // Arrange
    final String input = "hello_world";

    // Act
    final String result = multiDataSourceCommonStringUtils.toPascalCase(input);

    // Assert
    assertEquals("HelloWorld", result);
  }

  @Test
  public void toPascalCase_withEmptyString_returnsEmptyString() {
    // Arrange
    final String input = "";

    // Act
    final String result = multiDataSourceCommonStringUtils.toPascalCase(input);

    // Assert
    assertEquals("", result);
  }

  @Test
  public void toSnakeCase_withSingleWord_returnsSnakeCase() {
    // Arrange
    final String input = "Hello";

    // Act
    final String result = multiDataSourceCommonStringUtils.toSnakeCase(input);

    // Assert
    assertEquals("hello", result);
  }

  @Test
  public void toSnakeCase_withMultipleWords_returnsSnakeCase() {
    // Arrange
    final String input = "HelloWorld";

    // Act
    final String result = multiDataSourceCommonStringUtils.toSnakeCase(input);

    // Assert
    assertEquals("hello_world", result);
  }

  @Test
  public void toSnakeCase_withEmptyString_returnsEmptyString() {
    // Arrange
    final String input = "";

    // Act
    final String result = multiDataSourceCommonStringUtils.toSnakeCase(input);

    // Assert
    assertEquals("", result);
  }

  @Test
  public void toKebabCase_withSingleWord_returnsKebabCase() {
    // Arrange
    final String input = "Hello";

    // Act
    final String result = multiDataSourceCommonStringUtils.toKebabCase(input);

    // Assert
    assertEquals("hello", result);
  }

  @Test
  public void toKebabCase_withMultipleWords_returnsKebabCase() {
    // Arrange
    final String input = "HelloWorld";

    // Act
    final String result = multiDataSourceCommonStringUtils.toKebabCase(input);

    // Assert
    assertEquals("hello-world", result);
  }

  @Test
  public void toKebabCase_withEmptyString_returnsEmptyString() {
    // Arrange
    final String input = "";

    // Act
    final String result = multiDataSourceCommonStringUtils.toKebabCase(input);

    // Assert
    assertEquals("", result);
  }
}