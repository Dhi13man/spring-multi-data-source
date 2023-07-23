package io.github.dhi13man.spring.datasource.config;


import io.github.dhi13man.spring.datasource.annotations.EnableMultiDataSourceConfig;

/**
 * Created to enable generation of the Multi Data Source classes for testing.
 */
@EnableMultiDataSourceConfig(
    exactEntityPackages = "java.lang", // Object is entity
    repositoryPackages = "io.github.dhi13man.spring.datasource.generators" // MockRepository is repository
)
public class MultiDataSourceTestConfig {

}
