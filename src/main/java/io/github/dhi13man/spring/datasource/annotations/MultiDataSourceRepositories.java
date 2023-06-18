package io.github.dhi13man.spring.datasource.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to create copies of the repositories in the relevant packages, and autoconfigure them
 * to use the relevant data sources.
 * <p>
 * Will generate all relevant boilerplate code and beans.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface MultiDataSourceRepositories {

  /**
   * The array of {@link MultiDataSourceRepository} annotations q
   *
   * @return the array of {@link MultiDataSourceRepository} annotations
   */
  MultiDataSourceRepository[] value();
}
