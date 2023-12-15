package io.github.dhi13man.spring.datasource.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;

/**
 * Annotation to create copies of the repositories in the relevant packages, and autoconfigure them
 * to use the relevant secondary data sources.
 * <p>
 * Will generate all relevant boilerplate code and beans.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
@Repeatable(TargetSecondaryDataSources.class)
public @interface TargetSecondaryDataSource {

  /**
   * Alias for dataSourceName, the name of the data source to use for the repository.
   * <p>
   * To use a data source other than the primary, it must have been configured in the
   * {@link EnableMultiDataSourceConfig#secondaryDataSourceConfigs()} annotations.
   *
   * @return the data source to use for the repository.
   * @see EnableMultiDataSourceConfig.DataSourceConfig#dataSourceName()
   */
  @AliasFor("dataSourceName")
  String value() default "replica";
}
