package io.github.dhi13man.spring.datasource.config;


import io.github.dhi13man.spring.datasource.annotations.EnableMultiDataSourceConfig;
import io.github.dhi13man.spring.datasource.annotations.EnableMultiDataSourceConfig.DataSourceConfig;

/**
 * Created to enable generation of the Multi Data Source classes for testing.
 */
@EnableMultiDataSourceConfig(
    exactEntityPackages = "java.lang", // Object is entity
    repositoryPackages = "io.github.dhi13man.spring.datasource.generators", // MockRepository is repository
    generatedConfigPackage = "io.github.dhi13man.spring.datasource.generated.config",
    generatedRepositoryPackagePrefix = "io.github.dhi13man.spring.datasource.generated.repositories",
    dataSourceConfigs = {
        @DataSourceConfig(dataSourceName = "master", isPrimary = true),
        @DataSourceConfig(dataSourceName = "replica-2"),
        @DataSourceConfig(dataSourceName = "read-replica")
    }
)
public class MultiDataSourceTestConfig {

}
