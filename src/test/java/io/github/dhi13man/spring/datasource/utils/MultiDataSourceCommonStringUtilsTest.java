package io.github.dhi13man.spring.datasource.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class MultiDataSourceCommonStringUtilsTest {

  private final MultiDataSourceCommonStringUtils multiDataSourceCommonStringUtils = MultiDataSourceCommonStringUtils
      .getInstance();

  @Test
  public void toPascalCase_returnsPascalCase() {
    // Arrange
    final String inputSingleWord = "hello";
    final String inputMultipleWords = "hello_world";
    final String inputEmpty = "";
    final String inputMultipleWordsWithSymbols = "hello-world";
    final String inputMultipleWordsWithMoreSymbols = "hello-#wo$rld";
    final String inputMultipleWordsWithNumbers = "hello123world";
    final String inputMultipleWordsCapitalized = "HelloWorld";

    // Act
    final String resultSingleWord = multiDataSourceCommonStringUtils.toPascalCase(inputSingleWord);
    final String resultMultipleWords = multiDataSourceCommonStringUtils
        .toPascalCase(inputMultipleWords);
    final String resultEmpty = multiDataSourceCommonStringUtils.toPascalCase(inputEmpty);
    final String resultMultipleWordsWithSymbols = multiDataSourceCommonStringUtils
        .toPascalCase(inputMultipleWordsWithSymbols);
    final String resultMultipleWordsWithMoreSymbols = multiDataSourceCommonStringUtils
        .toPascalCase(inputMultipleWordsWithMoreSymbols);
    final String resultMultipleWordsWithNumbers = multiDataSourceCommonStringUtils
        .toPascalCase(inputMultipleWordsWithNumbers);
    final String resultMultipleWordsCapitalized = multiDataSourceCommonStringUtils
        .toPascalCase(inputMultipleWordsCapitalized);

    // Assert
    assertEquals("Hello", resultSingleWord);
    assertEquals("HelloWorld", resultMultipleWords);
    assertEquals("", resultEmpty);
    assertEquals("HelloWorld", resultMultipleWordsWithSymbols);
    assertEquals("HelloWoRld", resultMultipleWordsWithMoreSymbols);
    assertEquals("Hello123world", resultMultipleWordsWithNumbers);
    assertEquals("HelloWorld", resultMultipleWordsCapitalized);
  }

  @Test
  public void toSnakeCase_returnsSnakeCase() {
    // Arrange
    final String inputSingleWord = "Hello";
    final String inputMultipleWords = "HelloWorld";
    final String inputEmpty = "";
    final String inputMultipleWordsWithSymbols = "hello-world";
    final String inputMultipleWordsWithMoreSymbols = "hello-#wo$rld";
    final String inputMultipleWordsWithNumbers = "hello123world";
    final String inputMultipleWordsCapitalized = "HelloWorld";

    // Act
    final String resultSingleWord = multiDataSourceCommonStringUtils.toSnakeCase(inputSingleWord);
    final String resultMultipleWords = multiDataSourceCommonStringUtils
        .toSnakeCase(inputMultipleWords);
    final String resultEmpty = multiDataSourceCommonStringUtils.toSnakeCase(inputEmpty);
    final String resultMultipleWordsWithSymbols = multiDataSourceCommonStringUtils
        .toSnakeCase(inputMultipleWordsWithSymbols);
    final String resultMultipleWordsWithMoreSymbols = multiDataSourceCommonStringUtils
        .toSnakeCase(inputMultipleWordsWithMoreSymbols);
    final String resultMultipleWordsWithNumbers = multiDataSourceCommonStringUtils
        .toSnakeCase(inputMultipleWordsWithNumbers);
    final String resultMultipleWordsCapitalized = multiDataSourceCommonStringUtils
        .toSnakeCase(inputMultipleWordsCapitalized);

    // Assert
    assertEquals("hello", resultSingleWord);
    assertEquals("hello_world", resultMultipleWords);
    assertEquals("", resultEmpty);
    assertEquals("hello_world", resultMultipleWordsWithSymbols);
    assertEquals("hello_wo_rld", resultMultipleWordsWithMoreSymbols);
    assertEquals("hello123world", resultMultipleWordsWithNumbers);
    assertEquals("hello_world", resultMultipleWordsCapitalized);
  }

  @Test
  public void toKebabCase_returnsKebabCase() {
    // Arrange
    final String inputSingleWord = "Hello";
    final String inputMultipleWords = "HelloWorld";
    final String inputEmpty = "";
    final String inputMultipleWordsWithSymbols = "hello-world";
    final String inputMultipleWordsWithMoreSymbols = "hello-#wo$rld";
    final String inputMultipleWordsWithNumbers = "hello123world";
    final String inputMultipleWordsCapitalized = "HelloWorld";

    // Act
    final String resultSingleWord = multiDataSourceCommonStringUtils.toKebabCase(inputSingleWord);
    final String resultMultipleWords = multiDataSourceCommonStringUtils
        .toKebabCase(inputMultipleWords);
    final String resultEmpty = multiDataSourceCommonStringUtils.toKebabCase(inputEmpty);
    final String resultMultipleWordsWithSymbols = multiDataSourceCommonStringUtils
        .toKebabCase(inputMultipleWordsWithSymbols);
    final String resultMultipleWordsWithMoreSymbols = multiDataSourceCommonStringUtils
        .toKebabCase(inputMultipleWordsWithMoreSymbols);
    final String resultMultipleWordsWithNumbers = multiDataSourceCommonStringUtils
        .toKebabCase(inputMultipleWordsWithNumbers);
    final String resultMultipleWordsCapitalized = multiDataSourceCommonStringUtils
        .toKebabCase(inputMultipleWordsCapitalized);

    // Assert
    assertEquals("hello", resultSingleWord);
    assertEquals("hello-world", resultMultipleWords);
    assertEquals("", resultEmpty);
    assertEquals("hello-world", resultMultipleWordsWithSymbols);
    assertEquals("hello-wo-rld", resultMultipleWordsWithMoreSymbols);
    assertEquals("hello123world", resultMultipleWordsWithNumbers);
    assertEquals("hello-world", resultMultipleWordsCapitalized);
  }
}