# Releases

## [0.0.3] - 19th June, 2023

- Remove hardcoded DataSource type, and relevant legacy code for increased application-properties-based flexibility
with DataSource type.

## [0.0.2] - 18th June, 2023

- Improvements to default code generation package config.
- Proper `isMethodSignatureMatching` check while overriding and throwing unsupported exception in base Repository
methods to ensure only overriden, annotated methods are supported.
- Fix Dependabot Issue #1

## [0.0.1] - 18th June, 2022

- Initial Release with full working implementation of @EnableMultiDataSourceConfig and @MultiDataSourceRepository 
- with relevant config.
- Setup for Central Maven Repository Deployment with changed group ID and packages.
- Documentation and Guidelines.
