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
@Retention(RetentionPolicy.CLASS)
@Repeatable(TargetSecondaryDataSources.class)
public @interface TargetSecondaryDataSource {

  /**
   * Alias for dataSourceName, the name of the data source to use for the repository.
   * <p>
   * To use a data source other than the primary, it must have been configured in the
   * {@link EnableMultiDataSourceConfig#secondaryDataSourceConfigs()} annotations.
   * <p>
   * The generated repositories will be placed in the same package as the class annotated with
   * {@link EnableMultiDataSourceConfig} followed by .generated.repositories and then
   * .{snake_case_data_source_name}
   * <p>
   * The generated repositories will be placed in packages with the following format:
   * <p>
   * {generatedRepositoryPackagePrefix}.{PascalCaseDataSourceName}{AnnotatedMethodRepositoryName}
   *
   * @return the data source to use for the repository.
   * @see EnableMultiDataSourceConfig.DataSourceConfig#dataSourceName()
   */
  @AliasFor("dataSourceName")
  String value() default "replica";
}
