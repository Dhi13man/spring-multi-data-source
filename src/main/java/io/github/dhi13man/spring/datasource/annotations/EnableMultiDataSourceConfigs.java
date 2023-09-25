package io.github.dhi13man.spring.datasource.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to enable multi data source configuration for the service.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface EnableMultiDataSourceConfigs {

  /**
   * The array of {@link EnableMultiDataSourceConfig} annotations.
   *
   * @return the array of {@link EnableMultiDataSourceConfig} annotations
   */
  EnableMultiDataSourceConfig[] value();
}
