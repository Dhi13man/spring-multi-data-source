package com.dhi13man.spring.datasource.utils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CommonStringUtils {

  public static final String CAPITAL_LETTER_REGEX = "[A-Z]";

  public static final String NON_ALPHA_NUMERIC_REGEX = "[^a-zA-Z0-9]";

  /**
   * Convert any special character separated string to PascalCase
   *
   * @param input input string
   * @return PascalCase string
   */
  public static String toPascalCase(String input) {
    final List<String> split = splitByNonAlphaNumeric(input);
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
  public static String toSnakeCase(String input) {
    final List<String> split = splitByNonAlphaNumeric(input);
    if (split.size() == 1) {
      final List<String> elements = splitByCamelCase(split.get(0)).stream()
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
  public static String toKebabCase(String input) {
    final List<String> split = splitByNonAlphaNumeric(input);
    if (split.size() == 1) {
      final List<String> elements = splitByCamelCase(split.get(0)).stream()
          .map(String::toLowerCase)
          .collect(Collectors.toList());
      return String.join("-", elements);
    }

    return split.stream().map(String::toLowerCase).collect(Collectors.joining("-"));
  }

  /**
   * Split by all special characters and underscore
   *
   * @param input input string
   * @return list of strings
   */
  public static List<String> splitByNonAlphaNumeric(String input) {
    return Stream.of(input.split(NON_ALPHA_NUMERIC_REGEX))
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toList());
  }

  /**
   * Split by camel case
   *
   * @param input input string
   * @return list of strings
   */
  public static List<String> splitByCamelCase(String input) {
    return Stream.of(input.split(CAPITAL_LETTER_REGEX))
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toList());
  }

}
