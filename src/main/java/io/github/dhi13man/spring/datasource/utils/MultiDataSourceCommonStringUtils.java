package io.github.dhi13man.spring.datasource.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class for common string operations.
 */
public class MultiDataSourceCommonStringUtils {

  public static final String NON_ALPHA_NUMERIC_REGEX = "[^a-zA-Z0-9]";

  private static MultiDataSourceCommonStringUtils instance;

  private MultiDataSourceCommonStringUtils() {
  }

  public static MultiDataSourceCommonStringUtils getInstance() {
    if (instance == null) {
      instance = new MultiDataSourceCommonStringUtils();
    }
    return instance;
  }

  /**
   * Convert any special character separated string to PascalCase
   *
   * @param input input string
   * @return PascalCase string
   */
  public String toPascalCase(String input) {
    final List<String> split = splitByNonAlphaNumericRemoved(input);
    if (split.size() == 1) {
      return split.get(0).substring(0, 1).toUpperCase() + split.get(0).substring(1);
    }

    return split.stream()
        .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
        .collect(Collectors.joining());
  }

  /**
   * Convert any special character separated string to snake_case
   *
   * @param input input string
   * @return snake_case string
   */
  public String toSnakeCase(String input) {
    final List<String> split = splitByNonAlphaNumericRemoved(input);
    if (split.size() == 1) {
      final List<String> elements = splitByCamelCaseNotRemoved(split.get(0)).stream()
          .map(String::toLowerCase)
          .collect(Collectors.toList());
      return String.join("_", elements);
    }

    return split.stream().map(String::toLowerCase).collect(Collectors.joining("_"));
  }

  /**
   * Convert any special character separated string to kebab-case
   *
   * @param input input string
   * @return kebab-case string
   */
  public String toKebabCase(String input) {
    final List<String> split = splitByNonAlphaNumericRemoved(input);
    if (split.size() == 1) {
      final List<String> elements = splitByCamelCaseNotRemoved(split.get(0)).stream()
          .map(String::toLowerCase)
          .collect(Collectors.toList());
      return String.join("-", elements);
    }

    return split.stream().map(String::toLowerCase).collect(Collectors.joining("-"));
  }

  /**
   * Split by all non-alphanumeric characters while preserving only the alphabets and numbers.
   *
   * @param input input string
   * @return list of strings
   */
  private List<String> splitByNonAlphaNumericRemoved(String input) {
    return Stream.of(input.split(NON_ALPHA_NUMERIC_REGEX))
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toList());
  }

  /**
   * Split by camel case while preserving all the characters.
   *
   * @param input input string
   * @return list of strings
   */
  private List<String> splitByCamelCaseNotRemoved(String input) {
    StringBuilder currentWord = new StringBuilder();
    final List<String> result = new ArrayList<>();
    for (final char c : input.toCharArray()) {
      if (Character.isUpperCase(c)) {
        if (currentWord.length() > 0) {
          result.add(currentWord.toString());
          currentWord = new StringBuilder();
        }
      }
      currentWord.append(c);
    }
    if (currentWord.length() > 0) {
      result.add(currentWord.toString());
    }

    return result;
  }

}
