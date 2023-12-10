# Releases

## [0.2.1] - 10th December 2023

- BREAKING: Better Organise DataSourceConfig to segregate Primary and Secondary data sources as per Spring
  Convention.
- Allow `@TargetSecondaryDataSource` to be placed on any Repository, not just JpaRepository.
- Adaptive `EntityManagerFactory` for `TransactionManager` will be generated based on
  `LocalContainerEntityManagerFactoryBean`. Decoupled from Javax/Jakarta implementations.

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
