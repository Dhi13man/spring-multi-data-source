package io.github.dhi13man.spring.datasource.utils;

import com.squareup.javapoet.FieldSpec;
import javax.lang.model.element.Modifier;

/**
 * Utility class for generating code for the Multi Data Source library.
 */
public class MultiDataSourceGeneratorUtils {

  private static MultiDataSourceGeneratorUtils instance;

  private MultiDataSourceGeneratorUtils() {
  }

  public static MultiDataSourceGeneratorUtils getInstance() {
    if (instance == null) {
      instance = new MultiDataSourceGeneratorUtils();
    }
    return instance;
  }

  /**
   * Create the {@link FieldSpec} for a constant (public static final) {@link String}.
   *
   * @param fieldName  the name of the field
   * @param fieldValue the value of the field
   * @return the {@link FieldSpec} for a constant String
   */
  public FieldSpec createConstantStringFieldSpec(String fieldName, String... fieldValue) {
    // If there is only one value, create a simple constant
    if (fieldValue.length == 1) {
      return FieldSpec.builder(String.class, fieldName)
          .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
          .initializer("$S", fieldValue[0])
          .build();
    }

    // If there are multiple values, create a constant array
    final StringBuilder formatBuilder = new StringBuilder("new String[]{");
    for (int i = 0; i < fieldValue.length; i++) {
      formatBuilder.append("$S");
      if (i < fieldValue.length - 1) {
        formatBuilder.append(", ");
      }
    }
    formatBuilder.append("};");
    return FieldSpec.builder(String[].class, fieldName)
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
        .initializer(formatBuilder.toString(), (Object[]) fieldValue)
        .build();
  }

}
