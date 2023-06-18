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
public @interface EnableMultiDataSourceConfig {

  /**
   * The array of exact packages to scan for entities.
   * <p>
   * This must be an exact package name, and not a prefix as custom entity managers can not
   * recursively scan entities inside nested packages.
   *
   * @return the array of exact packages to scan for entities.
   */
  String[] exactEntityPackages() default {};

  /**
   * The array of packages to scan for repositories.
   * <p>
   * This can be a prefix, and will recursively scan all nested packages as @EnableJpaRepositories
   * annotation generated can recursively scan repositories inside nested packages.
   *
   * @return the array of packages to scan for repositories.
   */
  String[] repositoryPackages() default {};

  /**
   * The name of the master data source.
   * <p>
   * 1. This will be used to generate the master beans
   * <p>
   * 2. The PascalCase version of this will be used to name the generated Classes
   * <p>
   * 3. The camelCase version of this will be used to name the generated packages
   * <p>
   * 4. The kebab-case version of this will be used to name the property paths from which the data
   * source properties will be read
   */
  String masterDataSourceName() default "master";

  /**
   * The prefix of the master data source properties in the application properties file.
   *
   * @return the prefix of the master data source properties in the application properties file.
   */
  String datasourcePropertiesPrefix() default "spring.datasource";

  /**
   * The key of the data source class properties in the application properties file.
   *
   * @return the prefix of the data source class properties in the application properties file.
   */
  String dataSourceClassPropertiesPrefix() default "spring.datasource.hikari";

  /**
   * The path of the hibernate bean container property in the application properties.
   *
   * @return the prefix of the hibernate bean container properties in the application properties
   */
  String hibernateBeanContainerPropertyPath() default "hibernate.resource.beans.container";

  /**
   * The package where the generated master data source config will be placed.
   * <p>
   * If this is not provided, the generated config will be placed in the same package as the class
   * annotated with @EnableMultiDataSourceConfig followed by .config
   * <p>
   * The generated Config class with all the relevant beans will be placed in this package with the
   * following format:
   * <p>
   * {generatedConfigPackage}.{PascalCaseDataSourceName}DataSourceConfig
   *
   * @return the package where the generated master data source config will be placed.
   */
  String generatedConfigPackage() default "";

  /**
   * The prefix of the package where the generated copies of the repositories will be placed.
   * <p>
   * If this is not provided, the generated repositories will be placed in the same package as the
   * class annotated with @EnableMultiDataSourceConfig followed by .repositories and then
   * .{snake_case_data_source_name}
   * <p>
   * The generated repositories will be placed in packages with the following format:
   * <p>
   * {generatedRepositoryPackagePrefix}.{PascalCaseDataSourceName}{AnnotatedMethodRepositoryName}
   *
   * @return the prefix of the package where the generated copies of the repositories will be
   * placed.
   */
  String generatedRepositoryPackagePrefix() default "";
}
