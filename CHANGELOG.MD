# Releases

## [0.3.1] - 13th July 2024

- BREAKING CHANGE: Exact entity packages are now mandatory for each data source. This is to ensure
  that only the required entities are scanned for each data source and no unnecessary entities are
  scanned. This is a breaking change as the previous versions took the entity packages from the
  `@EnableMultiDataSourceConfig` annotation, not from the `@DataSourceConfig` annotation.

  Before:

  ```java
  @Configuration
  @EnableMultiDataSourceConfig(
    repositoryPackages = {
      "com.sample"
    },
    exactEntityPackages = {
      "com.sample.project.sample_service.entities.mysql"
    },
    primaryDataSourceConfig = @DataSourceConfig(dataSourceName = "master"),
    secondaryDataSourceConfigs = {
        @DataSourceConfig(dataSourceName = "replica-2"),
        @DataSourceConfig(dataSourceName = "read-replica")
    }
  )
  public class ServiceConfig {
  }
  ```

  After:

  ```java
   @Configuration
   @EnableMultiDataSourceConfig(
      repositoryPackages = {
        "com.sample"
      },
      primaryDataSourceConfig = @DataSourceConfig(
          dataSourceName = "master",
          exactEntityPackages = {
              "com.sample.project.sample_service.entities.mysql",
              // Assuming master wants access to read entities as well. If not, above package is fine
              "com.sample.project.sample_service.read_entities.mysql",
              "com.sample.project.sample_service.read_entities_v2.mysql"
          }
      ),
      secondaryDataSourceConfigs = {
          @DataSourceConfig(
              dataSourceName = "read-replica",
              exactEntityPackages = "com.sample.project.sample_service.read_entities.mysql"
          ),
          @DataSourceConfig(
              dataSourceName = "replica-2",
              exactEntityPackages = {
                  "com.sample.project.sample_service.read_entities.mysql",
                  // Assuming replica-2 wants access to read entities as well as read entities v2
                  "com.sample.project.sample_service.read_entities_v2.mysql"
              }
          ),
      }
   )
   public class ServiceConfig {
   }
  ```

- MULTI-MODULE SUPPORT | BREAKING
  CHANGE: `@EnableMultiDataSourceConfig.generatedRepositoryPackagePrefix` has been removed.
  Instead, the generated repository package will be the package of the source repository followed by
  `.generated.repositories` and then the snake-cased datasource name. This allows this project
  to **finally support Multi-Module projects** as the generated repositories will be in the same
  module as the source repository, instead of being dictated by the
  `generatedRepositoryPackagePrefix`.

- MULTI MODULE SUPPORT: The inclusion and exclusion filters of the `@EnableJpaRepositories`
  annotations generated now internally use `Filter.ASSIGNABLE_TYPE` and `Filter.REGEX` so that
  repository scanning can be smarter across multiple modules.

- Provided dependency of `com.google.auto.service:auto-service` has been upgraded to `1.1.1` to
  avoid CVE-2023-2976 and CVE-2020-8908 vulnerabilities.

- Some minor null-safety improvements and code cleanup.

## [0.2.1] - 10th December 2023

- BREAKING CHANGE: Better organised DataSourceConfig to segregate Primary and Secondary data sources
  as per Spring Convention. Also renamed `@TargetDataSource` to `@TargetSecondaryDataSource` to
  avoid confusion.

  Before:

  ```java
  @Configuration
  @EnableMultiDataSourceConfig(
    repositoryPackages = {
      "com.sample"
    },
    exactEntityPackages = {
      "com.sample.project.sample_service.entities.mysql"
    },
    dataSourceConfigs = {
        @DataSourceConfig(dataSourceName = "master", isPrimary = true),
        @DataSourceConfig(dataSourceName = "replica-2"),
        @DataSourceConfig(dataSourceName = "read-replica")
    }
  )
  public class ServiceConfig {
  }
  ```

  After:

  ```java
   @Configuration
   @EnableMultiDataSourceConfig(
      repositoryPackages = {
        "com.sample"
      },
      exactEntityPackages = {
        "com.sample.project.sample_service.entities.mysql"
      },
      primaryDataSourceConfig = @DataSourceConfig(dataSourceName = "master"),
      secondaryDataSourceConfigs = {
          @DataSourceConfig(dataSourceName = "replica-2"),
          @DataSourceConfig(dataSourceName = "read-replica")
      }
   )
   public class ServiceConfig {
   }
  ```

- Allow `@TargetSecondaryDataSource` to be placed on any Repository, not just JpaRepository.

- Adaptive `EntityManagerFactory` for `TransactionManager` will be generated based on
  `LocalContainerEntityManagerFactoryBean`. Decoupled from Javax/Jakarta implementations.

- Allow possibility for overriding different JPA properties for each data source by adding
  `@DataSourceConfig.overridingJpaPropertiesPath` field. By default, it will take the default
  `spring.jpa.properties` path.

## [0.1.2] - 6th October 2023

- HOTFIX: Fix `@EnableJpaRepositories.basePackages` being empty for secondary data sources
  when `@EnableMultiDataSourceConfig.generatedRepositoryPackagePrefix` is not set. Instead, it will
  be set to the package of the `@EnableMultiDataSourceConfig` annotated class followed by
  `.generated.repositories` and then the snake-cased datasource name.
- Default `@EnableMultiDataSourceConfig.generatedConfigPackage` changed to the package of the
  `@EnableMultiDataSourceConfig` annotated class followed by `.generated.config` instead of
  `@EnableMultiDataSourceConfig.generatedRepositoryPackagePrefix` followed by `.config`.
- No repositories will be scanned for data sources which do not have a `@TargetDataSource`.
- Refactor internal logic for more readability.

## [0.1.1] - 26th September 2023

### [PR#13](https://github.com/Dhi13man/spring-multi-data-source/pull/13)

- BREAKING CHANGE: Configs must be defined for each data source now
  under `@EnableMultiDataSourceConfig.@DataSourceConfig`.
- BREAKING CHANGE: `@MultiDataSourceRepository` has been renamed to `@TargetDataSource` for
  easier understanding.
- Incremented version of Junit and Mockito.
- Restructured DataSource Level configuration for more readability and utility.

### [PR#11](https://github.com/Dhi13man/spring-multi-data-source/pull/11), [PR#12](https://github.com/Dhi13man/spring-multi-data-source/pull/12)

- Cosmetic improvements to generated code to include comment explaining hibernate bean container
  injection reasoning
- Turned utils into singleton classes instead of static classes, and enforced them with Unit Tests.
- Improved documentation

## [0.0.5] - 23rd July 2023

- Unit Tests added for most major elements.
    - Had to use reflection for unit tests of generated Repository classes. It could be better.
- Made generated config classes abstract-able by introducing `IMultiDataSourceConfig` interface.
- Some internal method name changes for consistency.
- Excluded SLF4J, even as a provided dependency, as it is not required.

## [0.0.4] - 22nd July 2023

- Fixed Sonatype Issues:
    - [DoNotClaimAnnotations](https://errorprone.info/bugpattern/DoNotClaimAnnotations)
    - [BadImport](https://errorprone.info/bugpattern/BadImport)
    - [TypeEquals](https://errorprone.info/bugpattern/TypeEquals)
- Made Error Messages final static constants in a separate
  class: `io.github.dhi13man.spring.datasource.constants.MultiDataSourceErrorConstants`.
- Changed name of certain classes by adding `MultiDataSource` prefix to avoid name clashes in client
  code.

## [0.0.3] - 19th June 2023

- Remove hardcoded DataSource type, and relevant legacy code for increased
  application-properties-based flexibility
  with DataSource type.

## [0.0.2] - 18th June 2023

- Improvements to default code generation package config.
- Proper `isMethodSignatureMatching` check while overriding and throwing unsupported exception in
  base Repository
  methods to ensure only overriden, annotated methods are supported.
- Fix Dependabot Issue #1

## [0.0.1] - 18th June 2022

- Initial Release with full working implementation of @EnableMultiDataSourceConfig and
  @MultiDataSourceRepository
- with relevant config.
- Setup for Central Maven Repository Deployment with changed group ID and packages.
- Documentation and Guidelines.
