# spring-multi-data-source

[![License](https://img.shields.io/github/license/dhi13man/spring-multi-data-source)](https://github.com/Dhi13man/spring-multi-data-source/blob/main/LICENSE)
[![Contributors](https://img.shields.io/github/contributors-anon/dhi13man/spring-multi-data-source?style=flat)](https://github.com/Dhi13man/spring-multi-data-source/graphs/contributors)
[![GitHub forks](https://img.shields.io/github/forks/dhi13man/spring-multi-data-source?style=social)](https://github.com/Dhi13man/spring-multi-data-source/network/members)
[![GitHub Repo stars](https://img.shields.io/github/stars/dhi13man/spring-multi-data-source?style=social)](https://github.com/Dhi13man/spring-multi-data-source/stargazers)
[![Last Commit](https://img.shields.io/github/last-commit/dhi13man/spring-multi-data-source)](https://github.com/Dhi13man/spring-multi-data-source/commits/main)
[![GitHub issues](https://img.shields.io/github/issues/dhi13man/spring-multi-data-source)](https://github.com/Dhi13man/spring-multi-data-source/issues)
[![Build, Format, Test](https://github.com/dhi13man/spring-multi-data-source/actions/workflows/maven.yml/badge.svg)](https://github.com/Dhi13man/spring-multi-data-source/actions)

[![Apache Maven](https://img.shields.io/badge/apache_maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.dhi13man/spring-multi-data-source?style=for-the-badge&link=https%3A%2F%2Fmvnrepository.com%2Fartifact%2Fio.github.dhi13man%2Fspring-multi-data-source)](https://mvnrepository.com/artifact/io.github.dhi13man/spring-multi-data-source)

[!["Buy Me A Coffee"](https://img.buymeacoffee.com/button-api/?text=Buy%20me%20an%20Ego%20boost&emoji=%F0%9F%98%B3&slug=dhi13man&button_colour=FF5F5F&font_colour=ffffff&font_family=Lato&outline_colour=000000&coffee_colour=FFDD00****)](https://www.buymeacoffee.com/dhi13man)

[![Medium Article](https://img.shields.io/badge/Medium-12100E?style=for-the-badge&logo=medium&logoColor=white)](https://medium.com/@dhi13man/simplify-multiple-data-source-integration-for-spring-boot-services-c465ce1dcdb6)

Spring Boot has multiple limitations when using multiple data sources in a single service. This
project aims to solve those limitations by providing custom annotations that can be used to generate
the required Bean-providing configuration classes and repositories during the build process itself,
which the service can then use.

The best part is that the entirety of the generated code is clean, human-readable, and can be
directly carried over to the relevant packages of the main code if you no longer wish to be tied
down to this library in the future.

## Table of Contents

<!-- TOC -->
* [spring-multi-data-source](#spring-multi-data-source)
  * [Table of Contents](#table-of-contents)
  * [Introduction](#introduction)
  * [Annotations Provided](#annotations-provided)
    * [@EnableMultiDataSourceConfig](#enablemultidatasourceconfig)
      * [@EnableMultiDataSourceConfig.DataSourceConfig](#enablemultidatasourceconfigdatasourceconfig)
    * [@TargetSecondaryDataSource](#targetsecondarydatasource)
  * [Usage](#usage)
  * [Building from Source (Maven)](#building-from-source-maven)
  * [Removing Dependency on spring-multi-data-source without Losing Functionality](#removing-dependency-on-spring-multi-data-source-without-losing-functionality)
  * [Contributing](#contributing)
  * [License](#license)
  * [Resources](#resources)
<!-- TOC -->

## Introduction

The limitations of using multiple data sources in a single service in Spring are:

1. We need to split the packages of repositories to allow one `@EnableJpaRepositories` mapped to one
   package for each data source.

2. There is a lot of boilerplate config generation involved to create beans of data sources, entity
   managers, transaction managers etc. for each data source.

3. To get `EntityManagerFactoryBuilder` injected, we need to declare one of the data sources and all
   its beans as `@Primary`. Otherwise, the service won't even start up.

To mitigate the above limitations, I have created two custom annotations in Java that can be used
for configuring multi-data source configurations for a service. Let's break down each annotation:

## Annotations Provided

### @EnableMultiDataSourceConfig

- This annotation is used to enable multi-data source configuration for the service. This will
  replace the `@EnableJpaRepositories` and `@EntityScan` annotations used by Spring.

- It can be applied to a class (target: `ElementType.TYPE`).

- It has the following attributes:
    - `exactEntityPackages`: An array of exact packages to scan for entities. These packages are
      scanned to find the entities related to the data sources.
    - `repositoryPackages`: An array of packages to scan for repositories. These packages are
      scanned to find the repositories related to the data sources.
    - `datasourcePropertiesPrefix`: The prefix of the data source properties in the
      application properties file. The properties for each data source will be placed under this
      prefix followed by the kebab case of the data source name. Eg. When set as `spring.datasource`
      for master and readReplica data sources, the properties will be placed under
      `spring.datasource.master` and `spring.datasource.read-replica` respectively.
    - `generatedConfigPackage`: The package where the generated data source configs will
      be placed. The generated config class with relevant beans will follow a specific naming
      format. If this is not specified, the generated config will be placed in the same package as
      the class where this annotation is applied, followed by `.generated.config`.
    - `generatedRepositoryPackagePrefix`: The prefix of the package where the generated copies
      of the repositories will be placed. The generated repositories will follow a specific
      naming format. If this is not specified, the generated repositories will be placed in the
      same package as the class where this annotation is applied, followed by
      `.generated.repositories` and then `.<data_source_name>`.
    - `primaryDataSourceConfig`: A `@DataSourceConfig` annotation. This annotation represents
      the primary data source and its configuration. The primary data source will be able
      to access every repository other than the repositories generated for the secondary data
      sources.
    - `secondaryDataSourceConfigs`: An array of `@DataSourceConfig` annotations. Each annotation
      represents a data source and its configuration. The secondary data sources will only be able
      to access the repositories generated for them.

#### @EnableMultiDataSourceConfig.DataSourceConfig

- This sub-annotation is used to configure a data source and its properties for
  `@EnableMultiDataSourceConfig`. It can not be applied directly anywhere other than in
  the `dataSourceConfigs` attribute of `@EnableMultiDataSourceConfig`.

- It has the following attributes:
    - `dataSourceName`: The name of the data source. It is used to generate the data source
      beans and to name the generated classes, packages, and property paths for the data
      source properties.
    - `dataSourceClassPropertiesPath`:The application properties key/path of the data source class'
      properties. Eg. `spring.datasource.hikari` for Hikari data sources.
    - `overridingPropertiesPath`:  The application properties key/path under which the JPA
      properties to override for this data source are located. This allows overriding of the JPA
      properties for each data source. By default, it will take the default `spring.jpa.properties`
      path.

### @TargetSecondaryDataSource

- This annotation is used to create copies of repositories in relevant packages and
  autoconfigure them to use the relevant data sources.

- It can be applied to a method (target: `ElementType.METHOD`).

- It has the following attributes:
    - `dataSourceName` (or `value`): The name of the data source to use for the repository.

Both annotations are available at the source level and are not retained at runtime. They are
intended to be used for generating code for configuring data sources during the build process.

## Usage

1. Add `spring-multi-data-source` as a dependency in your service with a scope of `provided`. Eg.
   for Maven:

   ```xml
   <dependency>
     <groupId>com.dhi13man.spring</groupId>
     <artifactId>spring-multi-data-source</artifactId>
     <version>${desired.version}</version>
     <scope>provided</scope>
   </dependency>
   ```

2. Add the `@EnableMultiDataSourceConfig` annotation to a configuration class in your service, and
   specify the relevant attributes. At a bare minimum the `exactEntityPackages`
   and `repositoryPackages` attributes need to be specified. Ensure that you are no longer using
   `@EnableJpaRepositories` and `@EntityScan` annotations.

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
          },
          // In example application properties below (Usage Step 7), extra JPA Properties specific to this data source are provided under this key
          overridingPropertiesPath = "spring.datasource.master.extra-properties"
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

3. Add the `@TargetSecondaryDataSource` annotation to the repository methods that need to be
   configured for a specific data source, and specify the data source name.

    ```java
    @Repository
    public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {
    
       @TargetSecondaryDataSource("read-replica")
       ServiceEntity findByCustomIdAndDate(String id, Date date);
    
       // To override the default JpaRepository methods in the generated repository
       // All base methods that have not been overridden along with this annotation will throw an 
       // UnsupportedOperationException.
       @TargetSecondaryDataSource("read-replica")
       @Override
       ServiceEntity getById(Long id);
    }
   ```

4. Build the service and the generated classes will become available in
   the `target/generated-sources/annotations` directory of the service. Add that folder as a
   generated sources root in your IDE.

5. The configuration classes generated by the annotation processor will be named
   `<DataSourceName>DataSourceConfig` and will be placed in the package specified by the
   `generatedConfigPackage` attribute. These classes will provide the beans for the data
   source, transaction manager, entity manager factory, etc. for each data source which can be
   easily autowired with the given name constants.

   For example, if the data source name is `read-replica`, the generated configuration class will be
   named `ReadReplicaDataSourceConfig` and will be placed in the package given by the
   `generatedConfigPackage` attribute.

6. The repositories generated by the annotation processor will be named
   `<DataSourceName><RepositoryName>` and will be placed in the package specified by the
   `generatedRepositoryPackagePrefix` attribute followed by the snake case of the data source name.
   These repositories will be configured to use the relevant data source and can be autowired with
   the given name constants.

   For example, if the data source name is `read-replica` and the repository name
   is `ServiceRepository`, the generated repository will be named `ReadReplicaServiceRepository` and
   will be placed in the package given by the `generatedRepositoryPackagePrefix` attribute followed
   by `read_replica`.

7. The application data source properties will need to be provided under the key `spring.datasource`
   followed by the kebab case of the data source name.

   ```yaml
   spring:
    datasource:
      master: # This will become the master data source property as opposed to the usual direct spring.datasource property
        driver-class-name: com.mysql.cj.jdbc.Driver
        url: jdbc:mysql://${DB_IP}:${DB_PORT}/${MASTER_DB_NAME}
        username: ${DB_USERNAME}
        password: ${DB_PASSWORD}
        type: com.zaxxer.hikari.HikariDataSource
        extra-properties:  # Made possible by overridingPropertiesPath in Step 2 for master data source.
          hibernate.generate_statistics: true  # Generate hibernate statistics only for master data source.
      read-replica:  # This will become the property for the kebab case of the secondary data source name
        driver-class-name: com.mysql.cj.jdbc.Driver
        url: jdbc:mysql://${READ_REPLICA_DB_IP}:${DB_PORT}/${READ_REPLICA_DB_NAME}
        username: ${DB_USERNAME}
        password: ${DB_PASSWORD}
        type: com.zaxxer.hikari.HikariDataSource
    jpa:
      # Global JPA Properties
      properties: 
        # Hibernate properties can only be picked from here when using multiple data sources.
        hibernate.generate_statistics: false
        hibernate.physical_naming_strategy: org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy
        hibernate.implicit_naming_strategy: org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy
   ```

8. Please always go through the generated code to learn more about what configs to give and what
   beans to use for each data source.

## Building from Source (Maven)

1. Clone the repository.
2. Run `mvn clean install` to build the project and install it in your local maven repository.
3. Add the dependency in your project as mentioned above.
4. Run `mvn clean package` to build the project and generate the jar file.
5. The jar file will be available in the `target` directory.
6. Add the jar file as a dependency in your project.
7. Run `mvn clean compile` or `mvn clean install` in your project to generate the code.
8. The generated code will be available in the `target/generated-sources/annotations` directory.
9. Add that directory as a generated sources root in your IDE.
10. Use the generated code as mentioned above.

## Removing Dependency on spring-multi-data-source without Losing Functionality

A big selling point of this library is that it is not a black box. The generated code is clean,
human-readable, and can be directly carried over to the relevant packages of the main code if you no
longer wish to be tied down to this library.

1. Move the generated configuration classes and repositories to the relevant packages in your
   project from the `target/generated-sources/annotations` directory.
2. Remove `implements IMultiDataSourceConfig` from the generated `@Configuration` classes.
3. Remove the `@EnableMultiDataSourceConfig` annotation from your configuration class.
4. Remove the `@TargetSecondaryDataSource` annotation from your repository methods.
5. Remove the `spring-multi-data-source` dependency from your project pom.

And that's all you have to do! You are no longer tied down to this library and have the freedom to
use and modify the generated code to your liking.

## Contributing

Please feel free to raise issues and submit pull requests. Please check
out [CONTRIBUTING.md](CONTRIBUTING.md) for more details.

## License

This project is licensed under the GNU Lesser General Public License v3.0. Please check
out [LICENSE](LICENSE) for more details.

## Resources

1. [Spring Boot Official Documentation on configuring multiple data sources](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#howto.data-access.configure-two-datasources)

2. [Configuring and Using Multiple DataSources in Spring Boot](https://www.baeldung.com/spring-boot-configure-multiple-datasources)

3. [javapoet (for generating code in Java)](https://github.com/square/javapoet)

4. [Annotation Processing in Java](https://www.baeldung.com/java-annotation-processing-builder)
