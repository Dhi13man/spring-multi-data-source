# Releases

## [0.0.5] - 23rd July

- Unit Tests added for most major elements.
  - Had to use reflection for unit tests of generated Repository classes. Could it be better?
- Made generated config classes abstract-able by introducing IMultiDataSourceConfig interface.
- Some internal method name changes for consistency.

## [0.0.4] - 22nd July

- Fixed Sonatype Issues:
    - [DoNotClaimAnnotations](https://errorprone.info/bugpattern/DoNotClaimAnnotations)
    - [BadImport](https://errorprone.info/bugpattern/BadImport)
    - [TypeEquals](https://errorprone.info/bugpattern/TypeEquals)
- Made Error Messages final static constants in a separate
  class: `io.github.dhi13man.spring.datasource.constants.MultiDataSourceErrorConstants`.
- Changed name of certain classes by adding `MultiDataSource` prefix to avoid name clashes in client
  code.

## [0.0.3] - 19th June, 2023

- Remove hardcoded DataSource type, and relevant legacy code for increased
  application-properties-based flexibility
  with DataSource type.

## [0.0.2] - 18th June, 2023

- Improvements to default code generation package config.
- Proper `isMethodSignatureMatching` check while overriding and throwing unsupported exception in
  base Repository
  methods to ensure only overriden, annotated methods are supported.
- Fix Dependabot Issue #1

## [0.0.1] - 18th June, 2022

- Initial Release with full working implementation of @EnableMultiDataSourceConfig and
  @MultiDataSourceRepository
- with relevant config.
- Setup for Central Maven Repository Deployment with changed group ID and packages.
- Documentation and Guidelines.