package io.github.dhi13man.spring.datasource.generators;


import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import io.github.dhi13man.spring.datasource.annotations.EnableMultiDataSourceConfig.DataSourceConfig;
import io.github.dhi13man.spring.datasource.annotations.TargetSecondaryDataSource;
import io.github.dhi13man.spring.datasource.config.IMultiDataSourceConfig;
import io.github.dhi13man.spring.datasource.utils.MultiDataSourceGeneratorUtils;
import java.util.Properties;
import javax.lang.model.element.Modifier;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.hibernate5.SpringBeanContainer;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Annotation processor to generate config classes for all the repositories annotated with
 * {@link TargetSecondaryDataSource} and create copies of the repositories in the relevant
 * packages.
 */
public class MultiDataSourceConfigGenerator {

  public static final String OVERRIDING_JPA_PROPERTIES_BEAN_SUFFIX = "-overriding-jpa-properties";
  private static final String DATA_SOURCE_PROPERTIES_BEAN_SUFFIX = "-data-source-properties";
  private static final String DATA_SOURCE_BEAN_SUFFIX = "-data-source";

  private static final String ENTITY_MANAGER_FACTORY_BEAN_SUFFIX = "-entity-manager-factory";

  private static final String TRANSACTION_MANAGER_BEAN_SUFFIX = "-transaction-manager";

  private static final String DATA_SOURCE_PROPERTIES_BEAN_NAME_CONSTANT_NAME = "DATA_SOURCE_PROPERTIES_BEAN_NAME";

  private static final String OVERRIDING_JPA_PROPERTIES_BEAN_NAME_CONSTANT_NAME = "OVERRIDING_JPA_PROPERTIES";

  private static final String DATA_SOURCE_BEAN_NAME_CONSTANT_NAME = "DATA_SOURCE_BEAN_NAME";

  private static final String ENTITY_MANAGER_FACTORY_BEAN_NAME_CONSTANT_NAME = "ENTITY_MANAGER_FACTORY_BEAN_NAME";

  private static final String TRANSACTION_MANAGER_BEAN_NAME_CONSTANT_NAME = "TRANSACTION_MANAGER_BEAN_NAME";

  private static final String DATA_SOURCE_ENTITY_PACKAGES_CONSTANT_NAME = "DATA_SOURCE_ENTITY_PACKAGES";

  private static final String ADD_THE_SPRING_BEAN_CONTAINER_TO_THE_HIBERNATE_PROPERTIES = "Adds the SpringBeanContainer to the hibernate properties to allow the use of Spring beans in JPQL queries";

  private static final String VALUE_FIELD_NAME_STRING = "value";

  private static final String HIBERNATE_BEAN_CONTAINER_PROPERTY_CONSTANT_NAME = "HIBERNATE_BEAN_CONTAINER_PROPERTY";

  private static final String HIBERNATE_BEAN_CONTAINER_PROPERTY_PATH = "hibernate.resource.beans.container";

  private final MultiDataSourceGeneratorUtils multiDataSourceGeneratorUtils;

  public MultiDataSourceConfigGenerator(
      MultiDataSourceGeneratorUtils multiDataSourceGeneratorUtils
  ) {
    this.multiDataSourceGeneratorUtils = multiDataSourceGeneratorUtils;
  }

  /**
   * Generate the {@link TypeSpec} for a data source Spring Configuration class.
   * <p>
   * This configuration class will contain beans for the data source properties, data source, entity
   * manager factory and transaction manager and provide the proper constants for the bean names, to
   * conveniently auto-wire them where needed.
   *
   * @param dataSourceConfig            the {@link DataSourceConfig} for which the configuration
   *                                    class is being generated
   * @param isPrimaryConfig             whether the data source config is for the primary data
   *                                    source
   * @param dataSourceConfigClassName   the name of the data source configuration class being
   *                                    generated
   * @param dataSourcePropertiesPath    the path of where the properties of the data source are
   *                                    located in application.properties
   * @param repositoryPackagesToInclude the packages where the repositories associated with the data
   *                                    source are located (to be included in the
   *                                    {@link EnableJpaRepositories} annotation)
   * @param repositoryPackagesToExclude the packages where the repositories associated with other
   *                                    data sources are located (to be excluded in the
   *                                    {@link EnableJpaRepositories} annotation)
   * @param dataSourceEntityPackages    the exact packages where the entities associated with the
   *                                    data source are located
   * @return the {@link TypeSpec} for a data source Spring Configuration class
   */
  public TypeSpec generateMultiDataSourceConfigTypeElement(
      DataSourceConfig dataSourceConfig,
      boolean isPrimaryConfig,
      String dataSourceConfigClassName,
      String dataSourcePropertiesPath,
      String[] repositoryPackagesToInclude,
      String[] repositoryPackagesToExclude,
      String[] dataSourceEntityPackages
  ) {
    // Constants exposing important bean names
    final FieldSpec dataSourcePropertiesBeanNameField = multiDataSourceGeneratorUtils.createConstantStringFieldSpec(
        DATA_SOURCE_PROPERTIES_BEAN_NAME_CONSTANT_NAME,
        dataSourceConfig.dataSourceName() + DATA_SOURCE_PROPERTIES_BEAN_SUFFIX
    );
    final FieldSpec overrideJpaPropertiesBeanNameField = multiDataSourceGeneratorUtils.createConstantStringFieldSpec(
        OVERRIDING_JPA_PROPERTIES_BEAN_NAME_CONSTANT_NAME,
        dataSourceConfig.dataSourceName() + OVERRIDING_JPA_PROPERTIES_BEAN_SUFFIX
    );
    final FieldSpec dataSourceBeanNameField = multiDataSourceGeneratorUtils.createConstantStringFieldSpec(
        DATA_SOURCE_BEAN_NAME_CONSTANT_NAME,
        dataSourceConfig.dataSourceName() + DATA_SOURCE_BEAN_SUFFIX
    );
    final FieldSpec entityManagerFactoryBeanNameField = multiDataSourceGeneratorUtils.createConstantStringFieldSpec(
        ENTITY_MANAGER_FACTORY_BEAN_NAME_CONSTANT_NAME,
        dataSourceConfig.dataSourceName() + ENTITY_MANAGER_FACTORY_BEAN_SUFFIX
    );
    final FieldSpec transactionManagerBeanNameField = multiDataSourceGeneratorUtils.createConstantStringFieldSpec(
        TRANSACTION_MANAGER_BEAN_NAME_CONSTANT_NAME,
        dataSourceConfig.dataSourceName() + TRANSACTION_MANAGER_BEAN_SUFFIX
    );
    final FieldSpec dataSourceEntityPackageField = multiDataSourceGeneratorUtils.createConstantStringFieldSpec(
        DATA_SOURCE_ENTITY_PACKAGES_CONSTANT_NAME,
        dataSourceEntityPackages
    );
    final FieldSpec hibernateBeanContainerPropertyField = multiDataSourceGeneratorUtils.createConstantStringFieldSpec(
        HIBERNATE_BEAN_CONTAINER_PROPERTY_CONSTANT_NAME,
        HIBERNATE_BEAN_CONTAINER_PROPERTY_PATH
    );

    // Create the config class level annotations
    final AnnotationSpec.Builder enableJpaRepositoriesAnnotationBuilder = AnnotationSpec
        .builder(EnableJpaRepositories.class)
        .addMember(
            "basePackages",
            "$L",
            stringArrayToGeneratedStringArray(repositoryPackagesToInclude)
        )
        .addMember(
            "entityManagerFactoryRef",
            "$L.$N",
            dataSourceConfigClassName,
            entityManagerFactoryBeanNameField
        )
        .addMember(
            "transactionManagerRef",
            "$L.$N",
            dataSourceConfigClassName,
            transactionManagerBeanNameField
        );
    // Exclude the other data source repositories from the scan
    if (repositoryPackagesToExclude.length > 0) {
      enableJpaRepositoriesAnnotationBuilder.addMember(
          "excludeFilters",
          "$L",
          AnnotationSpec.builder(ComponentScan.Filter.class)
              .addMember("type", "$T.REGEX", FilterType.class)
              .addMember(
                  "pattern",
                  "$L",
                  stringArrayToGeneratedStringArray(repositoryPackagesToExclude)
              )
              .build()
      );
    }

    // Create the config class bean creation methods while adding the primary annotation to the
    // DataSourceProperties bean
    final MethodSpec dataSourcePropertiesMethod = addPrimaryAnnotationIfPrimaryConfigAndBuild(
        createDataSourcePropertiesBeanMethod(
            dataSourcePropertiesBeanNameField,
            dataSourcePropertiesPath
        ),
        isPrimaryConfig
    );

    // Overriding JPA Properties bean
    final MethodSpec overridingJpaPropertiesMethod = addPrimaryAnnotationIfPrimaryConfigAndBuild(
        createOverridingJpaPropertiesBeanMethod(
            overrideJpaPropertiesBeanNameField,
            dataSourceConfig.overridingJpaPropertiesPath()
        ),
        isPrimaryConfig
    );

    // DataSource bean
    final MethodSpec dataSourceMethod = addPrimaryAnnotationIfPrimaryConfigAndBuild(
        createDataSourceBeanMethod(
            dataSourceBeanNameField,
            dataSourceConfig.dataSourceClassPropertiesPath(),
            dataSourcePropertiesBeanNameField
        ),
        isPrimaryConfig
    );

    // EntityManagerFactory bean
    final MethodSpec entityManagerFactoryMethod = addPrimaryAnnotationIfPrimaryConfigAndBuild(
        createEntityManagerFactoryBeanMethod(
            entityManagerFactoryBeanNameField,
            dataSourceEntityPackageField,
            overrideJpaPropertiesBeanNameField,
            dataSourceEntityPackageField,
            hibernateBeanContainerPropertyField
        ),
        isPrimaryConfig
    );

    // TransactionManager bean
    final MethodSpec transactionManagerMethod = addPrimaryAnnotationIfPrimaryConfigAndBuild(
        createTransactionManagerBeanMethod(
            transactionManagerBeanNameField,
            entityManagerFactoryBeanNameField
        ),
        isPrimaryConfig
    );

    // Create the config class
    return TypeSpec.classBuilder(dataSourceConfigClassName)
        .addSuperinterface(IMultiDataSourceConfig.class)
        .addAnnotation(Configuration.class)
        .addAnnotation(enableJpaRepositoriesAnnotationBuilder.build())
        .addModifiers(Modifier.PUBLIC)
        .addField(dataSourcePropertiesBeanNameField)
        .addField(overrideJpaPropertiesBeanNameField)
        .addField(dataSourceBeanNameField)
        .addField(entityManagerFactoryBeanNameField)
        .addField(transactionManagerBeanNameField)
        .addField(dataSourceEntityPackageField)
        .addField(hibernateBeanContainerPropertyField)
        .addMethod(dataSourcePropertiesMethod)
        .addMethod(overridingJpaPropertiesMethod)
        .addMethod(dataSourceMethod)
        .addMethod(entityManagerFactoryMethod)
        .addMethod(transactionManagerMethod)
        .build();
  }

  /**
   * Takes a {@link java.util.function.Supplier} supplying a {@link MethodSpec.Builder} and adds the
   * {@link Primary} annotation to the {@link MethodSpec.Builder} if the data source config is for
   * the primary data source.
   *
   * @param methodSpecBuilder the {@link MethodSpec.Builder} to add the {@link Primary} annotation
   *                          to if eligible
   * @param isMasterConfig    whether the data source config is for the primary data source
   * @return the {@link MethodSpec} built with the {@link Primary} annotation added if eligible
   */
  private MethodSpec addPrimaryAnnotationIfPrimaryConfigAndBuild(
      MethodSpec.Builder methodSpecBuilder,
      boolean isMasterConfig
  ) {
    final AnnotationSpec primaryAnnotation = AnnotationSpec.builder(Primary.class).build();
    if (isMasterConfig) {
      methodSpecBuilder.addAnnotation(primaryAnnotation);
    }

    return methodSpecBuilder.build();
  }

  /**
   * Create the {@link MethodSpec} builder for the {@link DataSourceProperties} bean.
   *
   * @param beanNameFieldSpec        the {@link FieldSpec} for this bean name constant
   * @param datasourcePropertiesPath the path of where the properties of the data source are located
   *                                 in application.properties
   * @return the {@link MethodSpec} builder for the {@link DataSourceProperties} bean
   */
  private MethodSpec.Builder createDataSourcePropertiesBeanMethod(
      FieldSpec beanNameFieldSpec,
      String datasourcePropertiesPath
  ) {
    // Create the method annotations
    final AnnotationSpec beanAnnotation = createBeanAnnotationFromFieldSpec(beanNameFieldSpec);
    final AnnotationSpec configurationPropertiesAnnotation = AnnotationSpec
        .builder(ConfigurationProperties.class)
        .addMember("prefix", "$S", datasourcePropertiesPath)
        .build();

    // Create the method body
    return MethodSpec.methodBuilder("dataSourceProperties")
        .addAnnotation(beanAnnotation)
        .addAnnotation(configurationPropertiesAnnotation)
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(DataSourceProperties.class)
        .addStatement("return new $T()", DataSourceProperties.class);
  }

  /**
   * Create the {@link MethodSpec} builder for the {@link java.util.Properties} bean containing JPA
   * properties to override.
   *
   * @param beanNameFieldSpec           the {@link FieldSpec} for this bean name constant
   * @param overridingJpaPropertiesPath The key under which the JPA properties to override are
   *                                    located in the application properties file.
   * @return the {@link MethodSpec} builder for the {@link DataSourceProperties} bean
   */
  private MethodSpec.Builder createOverridingJpaPropertiesBeanMethod(
      FieldSpec beanNameFieldSpec,
      String overridingJpaPropertiesPath
  ) {
    // Create the method annotations
    final AnnotationSpec beanAnnotation = createBeanAnnotationFromFieldSpec(beanNameFieldSpec);
    final AnnotationSpec configurationPropertiesAnnotation = AnnotationSpec
        .builder(ConfigurationProperties.class)
        .addMember("prefix", "$S", overridingJpaPropertiesPath)
        .build();

    // Create the method body
    return MethodSpec.methodBuilder("overridingJpaProperties")
        .addAnnotation(beanAnnotation)
        .addAnnotation(configurationPropertiesAnnotation)
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(Properties.class)
        .addStatement("return new $T()", Properties.class);
  }

  /**
   * Create the {@link MethodSpec} builder for the {@link DataSource} bean.
   *
   * @param beanNameFieldSpec                     the {@link FieldSpec} for this bean name constant
   * @param dataSourceClassPropertiesPrefix       the prefix of the properties of the data source
   *                                              class in application.properties
   * @param dataSourcePropertiesBeanNameFieldSpec the {@link FieldSpec} for the
   *                                              {@link DataSourceProperties} dependency bean name
   *                                              constant
   * @return the {@link MethodSpec} builder for the {@link DataSource} bean
   */
  private MethodSpec.Builder createDataSourceBeanMethod(
      FieldSpec beanNameFieldSpec,
      String dataSourceClassPropertiesPrefix,
      FieldSpec dataSourcePropertiesBeanNameFieldSpec
  ) {
    // Create the method annotations
    final AnnotationSpec beanAnnotation = createBeanAnnotationFromFieldSpec(beanNameFieldSpec);
    final AnnotationSpec configurationPropertiesAnnotation = AnnotationSpec
        .builder(ConfigurationProperties.class)
        .addMember("prefix", "$S", dataSourceClassPropertiesPrefix)
        .build();
    final AnnotationSpec qualifierAnnotation = AnnotationSpec.builder(Qualifier.class)
        .addMember(
            VALUE_FIELD_NAME_STRING,
            "$N",
            dataSourcePropertiesBeanNameFieldSpec
        ).build();

    // Create the method parameters (DataSourceProperties dependency)
    final ParameterSpec dataSourcePropertiesParameter = ParameterSpec
        .builder(DataSourceProperties.class, "dataSourceProperties")
        .addAnnotation(qualifierAnnotation)
        .build();

    // Create the method body
    return MethodSpec.methodBuilder("dataSource")
        .addAnnotation(beanAnnotation)
        .addAnnotation(configurationPropertiesAnnotation)
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(DataSource.class)
        .addParameter(dataSourcePropertiesParameter)
        .addStatement("return dataSourceProperties.initializeDataSourceBuilder().build()");
  }

  /**
   * Create the {@link MethodSpec} builder for the {@link EntityManagerFactory} bean.
   * <p>
   * {@link EntityManagerFactory} will determine the {@link javax.persistence.EntityManager}
   * implementation to use based on the {@link DataSource} implementation for complex queries.
   *
   * @param beanNameFieldSpece                      the {@link FieldSpec} for this bean name
   *                                                constant
   * @param dataSourceEntityPackagesFieldSpec       the packages to scan for entities for this
   *                                                entity manager
   * @param overrideJpaPropertiesFieldSpec          the {@link FieldSpec} for the JPA properties to
   *                                                override
   * @param dataSourceBeanNameFieldSpec             the {@link FieldSpec} for the {@link DataSource}
   *                                                dependency bean name constant
   * @param hibernateBeanContainerPropertyFieldSpec the {@link FieldSpec} for the hibernate bean
   *                                                container property constant
   * @return the {@link MethodSpec} builder for the {@link EntityManagerFactory} bean
   */
  private MethodSpec.Builder createEntityManagerFactoryBeanMethod(
      FieldSpec beanNameFieldSpece,
      FieldSpec dataSourceBeanNameFieldSpec,
      FieldSpec overrideJpaPropertiesFieldSpec,
      FieldSpec dataSourceEntityPackagesFieldSpec,
      FieldSpec hibernateBeanContainerPropertyFieldSpec
  ) {
    // Create the method annotations
    final AnnotationSpec beanAnnotation =
        createBeanAnnotationFromFieldSpec(beanNameFieldSpece);
    final AnnotationSpec datasourceQualifierAnnotation = AnnotationSpec.builder(Qualifier.class)
        .addMember(VALUE_FIELD_NAME_STRING, "$N", dataSourceBeanNameFieldSpec)
        .build();
    final AnnotationSpec jpaPropertiesFieldAnnotation = AnnotationSpec.builder(Qualifier.class)
        .addMember(VALUE_FIELD_NAME_STRING, "$N", overrideJpaPropertiesFieldSpec)
        .build();

    // Create the method parameters
    final ParameterSpec jpaPropertiesParameter = ParameterSpec // JPA properties dependency
        .builder(Properties.class, "overrideJpaProperties")
        .addAnnotation(jpaPropertiesFieldAnnotation)
        .build();
    final ParameterSpec dataSourceParameter = ParameterSpec // DataSource dependency
        .builder(DataSource.class, "dataSource")
        .addAnnotation(datasourceQualifierAnnotation)
        .build();
    final ParameterSpec builderParameter = ParameterSpec // EntityManagerFactoryBuilder dependency
        .builder(EntityManagerFactoryBuilder.class, "builder")
        .build();
    final ParameterSpec beanFactoryParameter = ParameterSpec // BeanFactory dependency
        .builder(ConfigurableListableBeanFactory.class, "beanFactory")
        .build();

    // Create the method body
    return MethodSpec.methodBuilder("entityManagerFactory")
        .addAnnotation(beanAnnotation)
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(LocalContainerEntityManagerFactoryBean.class)
        .addParameter(jpaPropertiesParameter)
        .addParameter(dataSourceParameter)
        .addParameter(builderParameter)
        .addParameter(beanFactoryParameter)
        .addStatement(
            "final $T emfb = builder.dataSource($N).packages($N).persistenceUnit($N).build()",
            LocalContainerEntityManagerFactoryBean.class,
            dataSourceParameter,
            dataSourceEntityPackagesFieldSpec,
            dataSourceBeanNameFieldSpec
        )
        .addComment(ADD_THE_SPRING_BEAN_CONTAINER_TO_THE_HIBERNATE_PROPERTIES)
        .addStatement(
            "emfb.getJpaPropertyMap().put($N, new $T($N))",
            hibernateBeanContainerPropertyFieldSpec,
            SpringBeanContainer.class,
            beanFactoryParameter
        )
        .addStatement("emfb.setJpaProperties($N)", jpaPropertiesParameter)
        .addStatement("return emfb");
  }

  /**
   * Create the {@link MethodSpec} builder for the {@link PlatformTransactionManager} bean.
   *
   * @param beanNamefieldSpec                     the {@link FieldSpec} for this bean name constant
   * @param entityManagerFactoryBeanNameFieldSpec the {@link FieldSpec} for the
   *                                              {@link EntityManagerFactory} dependency bean
   * @return the {@link MethodSpec} builder for the {@link PlatformTransactionManager} bean
   */
  private MethodSpec.Builder createTransactionManagerBeanMethod(
      FieldSpec beanNamefieldSpec,
      FieldSpec entityManagerFactoryBeanNameFieldSpec
  ) {
    // Create the method annotations
    final AnnotationSpec beanAnnotation = createBeanAnnotationFromFieldSpec(beanNamefieldSpec);
    final AnnotationSpec qualifierAnnotation = AnnotationSpec.builder(Qualifier.class)
        .addMember(
            VALUE_FIELD_NAME_STRING,
            "$N",
            entityManagerFactoryBeanNameFieldSpec
        )
        .build();

    // Create the method parameters (EntityManagerFactory dependency)
    final ParameterSpec entityManagerFactoryParameter = ParameterSpec
        .builder(LocalContainerEntityManagerFactoryBean.class, "entityManagerFactoryBean")
        .addAnnotation(qualifierAnnotation)
        .build();

    // Create the method body
    return MethodSpec.methodBuilder("transactionManager")
        .addAnnotation(beanAnnotation)
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(PlatformTransactionManager.class)
        .addParameter(entityManagerFactoryParameter)
        .addStatement(
            "return new $T($N.getNativeEntityManagerFactory())",
            JpaTransactionManager.class,
            entityManagerFactoryParameter
        );
  }

  /**
   * Create the {@link AnnotationSpec} for a {@link Bean} annotation with a name attribute
   * referencing a {@link FieldSpec} constant
   *
   * @param beanNameFieldSpec the {@link FieldSpec} for the bean name constant
   * @return the {@link AnnotationSpec} for a {@link Bean} annotation with a name attribute
   */
  private AnnotationSpec createBeanAnnotationFromFieldSpec(FieldSpec beanNameFieldSpec) {
    return AnnotationSpec.builder(Bean.class)
        .addMember("name", "$N", beanNameFieldSpec)
        .build();
  }

  /**
   * Convert an actual run time {@link String} array to a generated {@link String} which represents
   * an array in code.
   *
   * @param stringArray the actual run time {@link String} array
   * @return the generated {@link String} which represents an array in code
   */
  private String stringArrayToGeneratedStringArray(String[] stringArray) {
    return stringArray.length == 0
        ? "{}"
        : "{\"" + String.join("\",\"", stringArray) + "\"}";
  }
}
