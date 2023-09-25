package io.github.dhi13man.spring.datasource.config;


import io.github.dhi13man.spring.datasource.annotations.EnableMultiDataSourceConfig;

/**
 * Created to enable generation of the Multi Data Source classes for testing.
 */
@EnableMultiDataSourceConfig(
    exactEntityPackages = "java.lang", // Object is entity
    repositoryPackages = "io.github.dhi13man.spring.datasource.generators", // MockRepository is repository
    generatedConfigPackage = "io.github.dhi13man.spring.datasource.generated.config",
    generatedRepositoryPackagePrefix = "io.github.dhi13man.spring.datasource.generated.repositories"
)
@EnableMultiDataSourceConfig(
    dataSourceName = "replica-2",
    exactEntityPackages = "java.lang", // Object is entity
    repositoryPackages = "io.github.dhi13man.spring.datasource.generators", // MockRepository is repository
    generatedConfigPackage = "io.github.dhi13man.spring.datasource.generated.config2"
)
@EnableMultiDataSourceConfig(
    dataSourceName = "read-replica",
    exactEntityPackages = "java.lang", // Object is entity
    repositoryPackages = "io.github.dhi13man.spring.datasource.generators", // MockRepository is repository
    generatedConfigPackage = "io.github.dhi13man.spring.datasource.generated.config2"
)
public class MultiDataSourceTestConfig {

}
