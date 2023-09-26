# Releases

## [0.1.0] - 26th September 2023

### [PR#11](https://github.com/Dhi13man/spring-multi-data-source/pull/11), [PR#12](https://github.com/Dhi13man/spring-multi-data-source/pull/12)

- Cosmetic improvements to generated code to include comment explaining hibernate bean container
  injection reasoning
- Improved documentation

### [PR#13](https://github.com/Dhi13man/spring-multi-data-source/pull/13)

- BREAKING Change: Configs must be defined for each data source now
  under `@EnableMultiDataSourceConfig.@DataSourceConfig`.
- Incremented version of Junit and Mockito.
- Restructured DataSource Level configuration for more readability and utility.

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
