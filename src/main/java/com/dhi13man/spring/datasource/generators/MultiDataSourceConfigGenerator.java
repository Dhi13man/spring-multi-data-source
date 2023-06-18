package com.dhi13man.spring.datasource.generators;


import static com.dhi13man.spring.datasource.utils.MultiDataSourceGeneratorUtils.createConstantStringFieldSpec;

import com.dhi13man.spring.datasource.annotations.EnableMultiDataSourceConfig;
import com.dhi13man.spring.datasource.annotations.MultiDataSourceRepository;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Modifier;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import javax.tools.Diagnostic.Kind;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
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
 * {@link MultiDataSourceRepository} and create copies of the repositories in the relevant
 * packages.
 */
public class MultiDataSourceConfigGenerator {

  private static final String DATA_SOURCE_PROPERTIES_BEAN_SUFFIX = "-data-source-properties";

  private static final String DATA_SOURCE_BEAN_SUFFIX = "-data-source";

  private static final String ENTITY_MANAGER_FACTORY_BEAN_SUFFIX = "-entity-manager-factory";

  private static final String TRANSACTION_MANAGER_BEAN_SUFFIX = "-transaction-manager";

  private static final String DATA_SOURCE_PROPERTIES_BEAN_NAME_CONSTANT_NAME = "DATA_SOURCE_PROPERTIES_BEAN_NAME";

  private static final String DATA_SOURCE_BEAN_NAME_CONSTANT_NAME = "DATA_SOURCE_BEAN_NAME";

  private static final String ENTITY_MANAGER_FACTORY_BEAN_NAME_CONSTANT_NAME = "ENTITY_MANAGER_FACTORY_BEAN_NAME";

  private static final String TRANSACTION_MANAGER_BEAN_NAME_CONSTANT_NAME = "TRANSACTION_MANAGER_BEAN_NAME";

  private static final String DATA_SOURCE_ENTITY_PACKAGES_CONSTANT_NAME = "DATA_SOURCE_ENTITY_PACKAGES";

  private final Messager messager;

  public MultiDataSourceConfigGenerator(Messager messager) {
    this.messager = messager;
  }

  /**
   * Generate the {@link TypeSpec} for a data source Spring Configuration class.
   * <p>
   * This configuration class will contain beans for the data source properties, data source, entity
   * manager factory and transaction manager and provide the proper constants for the bean names, to
   * conveniently auto-wire them where needed.
   *
   * @param masterAnnotation             the {@link EnableMultiDataSourceConfig} annotation
   *                                     containing the master configurations
   * @param dataSourceName               the name of the data source for which the configuration
   *                                     class is being generated
   * @param dataSourceConfigClassName    the name of the data source configuration class being
   *                                     generated
   * @param dataSourcePropertiesPath     the path of where the properties of the data source are
   *                                     located in application.properties
   * @param dataSourceRepositoryPackages the packages where the repositories associated with the
   *                                     data source are located
   * @param dataSourceEntityPackages     the exact packages where the entities associated with the
   *                                     data source are located
   * @return the {@link TypeSpec} for a data source Spring Configuration class
   */
  public TypeSpec generateMultiDataSourceConfig(
      EnableMultiDataSourceConfig masterAnnotation,
      String dataSourceName,
      String dataSourceConfigClassName,
      String dataSourcePropertiesPath,
      String[] dataSourceRepositoryPackages,
      String[] dataSourceEntityPackages
  ) {
    if (dataSourcePropertiesPath.equals(masterAnnotation.dataSourceClassPropertiesPrefix())) {
      final String errorMessage = "The data source properties path cannot be the same as the master"
          + " data source class properties path: " + dataSourcePropertiesPath;
      messager.printMessage(Kind.ERROR, errorMessage);
      throw new IllegalArgumentException(errorMessage);
    }

    // Constants exposing important bean names
    final FieldSpec dataSourcePropertiesBeanNameField = createConstantStringFieldSpec(
        DATA_SOURCE_PROPERTIES_BEAN_NAME_CONSTANT_NAME,
        dataSourceName + DATA_SOURCE_PROPERTIES_BEAN_SUFFIX
    );
    final FieldSpec dataSourceBeanNameField = createConstantStringFieldSpec(
        DATA_SOURCE_BEAN_NAME_CONSTANT_NAME,
        dataSourceName + DATA_SOURCE_BEAN_SUFFIX
    );
    final FieldSpec entityManagerFactoryBeanNameField = createConstantStringFieldSpec(
        ENTITY_MANAGER_FACTORY_BEAN_NAME_CONSTANT_NAME,
        dataSourceName + ENTITY_MANAGER_FACTORY_BEAN_SUFFIX
    );
    final FieldSpec transactionManagerBeanNameField = createConstantStringFieldSpec(
        TRANSACTION_MANAGER_BEAN_NAME_CONSTANT_NAME,
        dataSourceName + TRANSACTION_MANAGER_BEAN_SUFFIX
    );
    final FieldSpec dataSourceEntityPackageField = createConstantStringFieldSpec(
        DATA_SOURCE_ENTITY_PACKAGES_CONSTANT_NAME,
        dataSourceEntityPackages
    );

    // Create the config class level annotations
    final AnnotationSpec.Builder enableJpaRepositoriesAnnotationBuilder = AnnotationSpec
        .builder(EnableJpaRepositories.class)
        .addMember(
            "basePackages",
            "$L",
            stringArrayToGeneratedStringArray(dataSourceRepositoryPackages)
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
    final boolean isMasterConfig = dataSourceName.equals(masterAnnotation.masterDataSourceName());
    // Exclude the other data source repositories from the master data source config
    if (isMasterConfig) {
      enableJpaRepositoriesAnnotationBuilder.addMember(
          "excludeFilters",
          "$L",
          AnnotationSpec.builder(ComponentScan.Filter.class)
              .addMember("type", "$T.REGEX", FilterType.class)
              .addMember(
                  "pattern",
                  "{\"$L\"}",
                  masterAnnotation.generatedRepositoryPackagePrefix()
              )
              .build()
      );
    }

    // Create the config class bean creation methods while adding the primary annotation to the
    // master data source beans
    final AnnotationSpec primaryAnnotation = AnnotationSpec.builder(Primary.class).build();
    // DataSourceProperties bean
    final MethodSpec.Builder dataSourcePropertiesMethod = createDataSourcePropertiesBeanMethod(
        dataSourcePropertiesPath,
        dataSourcePropertiesBeanNameField
    );
    if (isMasterConfig) {
      dataSourcePropertiesMethod.addAnnotation(primaryAnnotation);
    }

    // DataSource bean
    final Class<? extends DataSource> dataSourceClass = parseClassWithStrictBase(
        masterAnnotation.dataSourceClassString(),
        DataSource.class
    );
    final MethodSpec.Builder dataSourceMethod = createDataSourceBeanMethod(
        masterAnnotation.dataSourceClassPropertiesPrefix(),
        dataSourceClass,
        dataSourceBeanNameField,
        dataSourcePropertiesBeanNameField
    );
    if (isMasterConfig) {
      dataSourceMethod.addAnnotation(primaryAnnotation);
    }

    // EntityManagerFactory bean
    final MethodSpec.Builder entityManagerFactoryMethod = createEntityManagerFactoryBeanMethod(
        masterAnnotation.hibernateBeanContainerPropertyPath(),
        dataSourceEntityPackageField,
        entityManagerFactoryBeanNameField,
        dataSourceBeanNameField
    );
    if (isMasterConfig) {
      entityManagerFactoryMethod.addAnnotation(primaryAnnotation);
    }

    // TransactionManager bean
    final MethodSpec.Builder transactionManagerMethod = createTransactionManagerBeanMethod(
        transactionManagerBeanNameField,
        entityManagerFactoryBeanNameField
    );
    if (isMasterConfig) {
      transactionManagerMethod.addAnnotation(primaryAnnotation);
    }

    // Create the config class
    return TypeSpec.classBuilder(dataSourceConfigClassName)
        .addAnnotation(Configuration.class)
        .addAnnotation(enableJpaRepositoriesAnnotationBuilder.build())
        .addModifiers(Modifier.PUBLIC)
        .addField(dataSourcePropertiesBeanNameField)
        .addField(dataSourceBeanNameField)
        .addField(entityManagerFactoryBeanNameField)
        .addField(transactionManagerBeanNameField)
        .addField(dataSourceEntityPackageField)
        .addMethod(dataSourcePropertiesMethod.build())
        .addMethod(dataSourceMethod.build())
        .addMethod(entityManagerFactoryMethod.build())
        .addMethod(transactionManagerMethod.build())
        .build();
  }

  /**
   * Create the {@link MethodSpec} builder for the {@link DataSourceProperties} bean.
   *
   * @param datasourcePropertiesPath the path of where the properties of the data source are located
   *                                 in application.properties
   * @param beanNameFieldSpec        the {@link FieldSpec} for the bean name constant
   * @return the {@link MethodSpec} builder for the {@link DataSourceProperties} bean
   */
  private MethodSpec.Builder createDataSourcePropertiesBeanMethod(
      String datasourcePropertiesPath,
      FieldSpec beanNameFieldSpec
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
        .addModifiers(Modifier.PUBLIC)
        .returns(DataSourceProperties.class)
        .addStatement("return new $T()", DataSourceProperties.class);
  }

  /**
   * Create the {@link MethodSpec} builder for the {@link DataSource} bean.
   *
   * @param dataSourceClassPropertiesPrefix       the prefix of the properties of the data source
   *                                              class in application.properties
   * @param dataSourceClass                       the {@link DataSource} class
   * @param beanNameFieldSpec                     the {@link FieldSpec} for the bean name constant
   * @param dataSourcePropertiesBeanNameFieldSpec the {@link FieldSpec} for the
   *                                              {@link DataSourceProperties} dependency bean name
   *                                              constant
   * @return the {@link MethodSpec} builder for the {@link DataSource} bean
   */
  private MethodSpec.Builder createDataSourceBeanMethod(
      String dataSourceClassPropertiesPrefix,
      Class<? extends DataSource> dataSourceClass,
      FieldSpec beanNameFieldSpec,
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
            "value",
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
        .addModifiers(Modifier.PUBLIC)
        .returns(dataSourceClass)
        .addParameter(dataSourcePropertiesParameter)
        .addStatement(
            "return dataSourceProperties.initializeDataSourceBuilder().type($T.class).build()",
            dataSourceClass
        );
  }

  /**
   * Create the {@link MethodSpec} builder for the {@link EntityManagerFactory} bean.
   * <p>
   * {@link EntityManagerFactory} will determine the {@link javax.persistence.EntityManager}
   * implementation to use based on the {@link DataSource} implementation for complex queries.
   *
   * @param hibernateBeanContainerPropertyPath the path of where the property for the
   *                                           {@link HibernatePropertiesCustomizer} is located in
   *                                           application.properties
   * @param dataSourceEntityPackagesFieldSpec  the packages to scan for entities for this entity
   *                                           manager
   * @param beanNamefieldSpec                  the {@link FieldSpec} for the bean name constant
   * @param dataSourceBeanNameFieldSpec        the {@link FieldSpec} for the {@link DataSource}
   *                                           dependency bean name constant
   * @return the {@link MethodSpec} builder for the {@link EntityManagerFactory} bean
   */
  private MethodSpec.Builder createEntityManagerFactoryBeanMethod(
      String hibernateBeanContainerPropertyPath,
      FieldSpec dataSourceEntityPackagesFieldSpec,
      FieldSpec beanNamefieldSpec,
      FieldSpec dataSourceBeanNameFieldSpec
  ) {
    // Create the method annotations
    final AnnotationSpec beanAnnotation = createBeanAnnotationFromFieldSpec(beanNamefieldSpec);
    final AnnotationSpec qualifierAnnotation = AnnotationSpec.builder(Qualifier.class)
        .addMember("value", "$N", dataSourceBeanNameFieldSpec)
        .build();

    // Create the method parameters
    final ParameterSpec dataSourceParameter = ParameterSpec // DataSource dependency
        .builder(DataSource.class, "dataSource")
        .addAnnotation(qualifierAnnotation)
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
        .addModifiers(Modifier.PUBLIC)
        .returns(LocalContainerEntityManagerFactoryBean.class)
        .addParameter(builderParameter)
        .addParameter(beanFactoryParameter)
        .addParameter(dataSourceParameter)
        .addStatement(
            "final $T emfb = builder.dataSource($N).packages($N).persistenceUnit($N).build()",
            LocalContainerEntityManagerFactoryBean.class,
            dataSourceParameter,
            dataSourceEntityPackagesFieldSpec,
            dataSourceBeanNameFieldSpec
        )
        .addStatement(
            "emfb.getJpaPropertyMap().put($S, new $T($N))",
            hibernateBeanContainerPropertyPath,
            SpringBeanContainer.class,
            beanFactoryParameter
        )
        .addStatement("return emfb");
  }

  /**
   * Create the {@link MethodSpec} builder for the {@link PlatformTransactionManager} bean.
   *
   * @param beanNamefieldSpec                     the {@link FieldSpec} for the bean name constant
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
            "value",
            "$N",
            entityManagerFactoryBeanNameFieldSpec
        )
        .build();

    // Create the method parameters (EntityManagerFactory dependency)
    final ParameterSpec entityManagerFactoryParameter = ParameterSpec
        .builder(EntityManagerFactory.class, "entityManagerFactory")
        .addAnnotation(qualifierAnnotation)
        .build();

    // Create the method body
    return MethodSpec.methodBuilder("transactionManager")
        .addAnnotation(beanAnnotation)
        .addModifiers(Modifier.PUBLIC)
        .returns(PlatformTransactionManager.class)
        .addParameter(entityManagerFactoryParameter)
        .addStatement(
            "return new $T($N)",
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
   * Parse a {@link String} into a {@link Class} with a strict base class.
   *
   * @param classString the {@link String} to parse
   * @param baseClass   the base class the parsed {@link Class} must extend
   * @param <T>         the type of the base class
   * @return the parsed {@link Class}
   */
  private <T> Class<? extends T> parseClassWithStrictBase(String classString, Class<T> baseClass) {
    try {
      // Check that the class is a valid implementation of the base class
      final Class<?> aClass = Class.forName(classString);
      if (!baseClass.isAssignableFrom(aClass)) {
        final String errorMessage = "Class " + classString
            + " is not a valid " + baseClass.getSimpleName();
        messager.printMessage(Kind.ERROR, errorMessage);
        throw new IllegalArgumentException(errorMessage);
      }

      // Return the cast parsed class
      return aClass.asSubclass(baseClass);
    } catch (ClassNotFoundException e) {
      final String errorMessage = "Unable to find class " + classString;
      messager.printMessage(Kind.ERROR, errorMessage);
      throw new IllegalArgumentException(errorMessage, e);
    }
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
