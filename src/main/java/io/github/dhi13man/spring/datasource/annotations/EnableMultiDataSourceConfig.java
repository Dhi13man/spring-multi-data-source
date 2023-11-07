package io.github.dhi13man.spring.datasource.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.config.BootstrapMode;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;

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
   * If {@link TargetDataSource} is used on any repository, the package of the entity that the
   * repository is associated with will also be scanned for entities.
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
   * The array of {@link DataSourceConfig} annotations which contain the configuration for each data
   * source.
   *
   * @return the array of {@link DataSourceConfig} annotations.
   * @see DataSourceConfig
   */
  DataSourceConfig[] dataSourceConfigs() default {
      @DataSourceConfig(dataSourceName = "master", isPrimary = true)
  };

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
     * Whether this data source is the primary data source.
     * <p>
     * There must be exactly one primary data source.
     *
     * @return whether this data source is the primary data source.
     */
    boolean isPrimary() default false;

    /**
     * The key of the data source class properties in the application properties file.
     *
     * @return the prefix of the data source class properties in the application properties file.
     */
    String dataSourceClassPropertiesPath() default "spring.datasource.hikari";

    /**
     * The path of the hibernate bean container property in the application properties.
     * <p>
     * This is needed to manually set the hibernate bean container to the spring bean container to
     * ensure that the hibernate beans like attribute converters are managed by spring.
     *
     * @return the prefix of the hibernate bean container properties in the application properties
     */
    String hibernateBeanContainerPropertyPath() default "hibernate.resource.beans.container";

    /**
     * Returns the postfix to be used when looking up custom repository implementations. Defaults to
     * {@literal Impl}. So for a repository named {@code PersonRepository} the corresponding
     * implementation class will be looked up scanning for {@code PersonRepositoryImpl}.
     *
     * @return the postfix to be used when looking up custom repository implementations.
     */
    String repositoryImplementationPostfix() default "Impl";

    /**
     * Configures the location of where to find the Spring Data named queries properties file. Will
     * default to {@code META-INF/jpa-named-queries.properties}.
     *
     * @return the location of where to find the Spring Data named queries properties file.
     */
    String namedQueriesLocation() default "";

    /**
     * Returns the key of the {@link QueryLookupStrategy} to be used for lookup queries for query
     * methods. Defaults to {@link Key#CREATE_IF_NOT_FOUND}.
     *
     * @return the key of the {@link QueryLookupStrategy} to be used for lookup queries for query
     * methods.
     */
    Key queryLookupStrategy() default Key.CREATE_IF_NOT_FOUND;

    // JPA specific configuration

    /**
     * Configures whether nested repository-interfaces (e.g. defined as inner classes) should be
     * discovered by the repositories' infrastructure.
     */
    boolean considerNestedRepositories() default false;

    /**
     * Configures whether to enable default transactions for Spring Data JPA repositories. Defaults
     * to {@literal true}. If disabled, repositories must be used behind a facade that's configuring
     * transactions (e.g. using Spring's annotation driven transaction facilities) or repository
     * methods have to be used to demarcate transactions.
     *
     * @return whether to enable default transactions, defaults to {@literal true}.
     */
    boolean enableDefaultTransactions() default true;

    /**
     * Configures when the repositories are initialized in the bootstrap lifecycle.
     * <p>
     * - {@link BootstrapMode#DEFAULT} (default) means eager initialization except all repository
     * interfaces annotated with {@link Lazy}
     * <p>
     * - {@link BootstrapMode#LAZY} means lazy by default including injection of lazy-initialization
     * proxies into client beans so that those can be instantiated but will only trigger the
     * initialization upon first repository usage (i.e a method invocation on it). This means
     * repositories can still be uninitialized when the application context has completed its
     * bootstrap.
     * <p>
     * - {@link BootstrapMode#DEFERRED} is fundamentally the same as {@link BootstrapMode#LAZY}, but
     * triggers repository initialization when the application context finishes its bootstrap.
     *
     * @return the {@link BootstrapMode} to use for repository initialization.
     * @since 2.1
     */
    BootstrapMode repositoryBootstrapMode() default BootstrapMode.DEFAULT;
  }
}
