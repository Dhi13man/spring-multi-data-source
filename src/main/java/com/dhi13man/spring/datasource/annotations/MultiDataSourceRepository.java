package com.dhi13man.spring.datasource.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;

/**
 * Annotation to create copies of the repositories in the relevant packages, and autoconfigure them
 * to use the relevant data sources.
 * <p>
 * Will generate all relevant boilerplate code and beans.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
@Repeatable(MultiDataSourceRepositories.class)
public @interface MultiDataSourceRepository {

  /**
   * Alias for dataSourceName, the name of the data source to use for the repository.
   * <p>
   * 1. This will be used to generate the master beans
   * <p>
   * 2. The PascalCase version of this will be used to name the generated Classes
   * <p>
   * 3. The camelCase version of this will be used to name the generated packages
   * <p>
   * 4. The kebab-case version of this will be used to name the property paths from which the data
   * source properties will be read
   *
   * @return the data source to use for the repository.
   */
  @AliasFor("dataSourceName")
  String value() default "replica";
}
