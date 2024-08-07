package io.github.dhi13man.spring.datasource.config;


import io.github.dhi13man.spring.datasource.annotations.EnableMultiDataSourceConfig;
import io.github.dhi13man.spring.datasource.annotations.EnableMultiDataSourceConfig.DataSourceConfig;

/**
 * Created to enable generation of the Multi Data Source classes for testing.
 */
@EnableMultiDataSourceConfig(
    repositoryPackages = "io.github.dhi13man.spring.datasource.generators", // MockRepository is repository
    generatedConfigPackage = "io.github.dhi13man.spring.datasource.generated.config",
    primaryDataSourceConfig = @DataSourceConfig(
        dataSourceName = "master",
        exactEntityPackages = "java.lang"
    ),
    secondaryDataSourceConfigs = {
        @DataSourceConfig(dataSourceName = "replica-2", exactEntityPackages = "java.lang"),
        @DataSourceConfig(dataSourceName = "read-replica", exactEntityPackages = "java.lang"),
        @DataSourceConfig(
            dataSourceName = "replica-no-target-data-source",
            exactEntityPackages = "java.lang"
        ),
    }
)
public class MultiDataSourceTestConfig {

}
