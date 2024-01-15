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
   * <p>
   * If {@link TargetSecondaryDataSource} is used on any repository, the package of the entity that
   * the repository is associated with will also be scanned for entities.
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
   * The prefix of the properties for each of the data sources in the application properties file.
   *
   * @return the prefix of the data source properties in the application properties file.
   */
  String datasourcePropertiesPrefix() default "spring.datasource";

  /**
   * The package where the generated data source configs will be placed.
   * <p>
   * If this is not provided, the generated config will be placed in the same package as the class
   * annotated with @EnableMultiDataSourceConfig followed by .generated.config
   * <p>
   * The generated Config class with all the relevant beans will be placed in this package with the
   * following format:
   * <p>
   * {generatedConfigPackage}.{PascalCaseDataSourceName}DataSourceConfig
   *
   * @return the package where the generated data source configs will be placed.
   */
  String generatedConfigPackage() default "";

  /**
   * The prefix of the package where the generated copies of the repositories will be placed.
   * <p>
   * If this is not provided, the generated repositories will be placed in the same package as the
   * class annotated with @EnableMultiDataSourceConfig followed by .generated.repositories and then
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

  /**
   * The config for the primary data source.
   * <p>
   * The primary data source is the data source which will be used by default for all repositories
   * which do not have the {@link TargetSecondaryDataSource} annotation.
   *
   * @return the {@link DataSourceConfig} annotation for the primary data source.
   */
  DataSourceConfig primaryDataSourceConfig() default @DataSourceConfig(dataSourceName = "master");

  /**
   * The array of {@link DataSourceConfig} annotations which contain the configuration for each
   * secondary data source.
   *
   * @return the array of {@link DataSourceConfig} annotations.
   * @see DataSourceConfig
   */
  DataSourceConfig[] secondaryDataSourceConfigs() default {};

  @Retention(RetentionPolicy.RUNTIME)
  @Target({})
  @interface DataSourceConfig {

    /**
     * The name of the data source which this config is for.
     * <p>
     * 1. This will be used to generate the data source beans
     * <p>
     * 2. The PascalCase version of this will be used to name the generated Classes
     * <p>
     * 3. The camelCase version of this will be used to name the generated packages
     * <p>
     * 4. The kebab-case version of this will be used to name the property paths from which the data
     * source properties will be read
     *
     * @return the name of the data source.
     */
    String dataSourceName() default "";

    /**
     * The application properties key/path of the data source class properties.
     *
     * @return the prefix of the data source class properties in the application properties file.
     */
    String dataSourceClassPropertiesPath() default "spring.datasource.hikari";

    /**
     * The application properties key/path under which the JPA properties to override for this data
     * source are located.
     * <p>
     * This allows overriding of the JPA properties for each data source. The properties under this
     * key will be merged with the usual properties under the spring.jpa.properties key.
     *
     * @return the key under which the JPA properties to override are located.
     */
    String overridingJpaPropertiesPath() default "spring.jpa.properties";
  }
}
