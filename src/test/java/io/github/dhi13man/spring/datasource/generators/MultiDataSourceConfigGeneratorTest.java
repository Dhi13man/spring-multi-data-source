package io.github.dhi13man.spring.datasource.generators;

import io.github.dhi13man.spring.datasource.annotations.EnableMultiDataSourceConfig;
import io.github.dhi13man.spring.datasource.annotations.MultiDataSourceRepository;
import io.github.dhi13man.spring.datasource.generators.config.MasterDataSourceConfig;
import io.github.dhi13man.spring.datasource.generators.config.ReadReplicaDataSourceConfig;
import java.util.HashMap;
import java.util.List;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.lang.NonNull;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

class MultiDataSourceConfigGeneratorTest {

  @Test
  void generateMultiDataSourceMasterConfigGetDataSourceProperties() {
    // Arrange
    final MasterDataSourceConfig masterDataSourceConfig = new MasterDataSourceConfig(); // Generated config class

    // Act
    final DataSourceProperties dataSourceProperties = masterDataSourceConfig.dataSourceProperties();

    // Assert
    Assertions.assertNotNull(dataSourceProperties);
  }

  @Test
  void generateMultiDataSourceMasterConfigGetDataSourceUsingProperties() {
    // Arrange
    final MasterDataSourceConfig masterDataSourceConfig = new MasterDataSourceConfig(); // Generated config class

    // Act
    final DataSourceProperties dataSourceProperties = masterDataSourceConfig.dataSourceProperties();
    dataSourceProperties.setEmbeddedDatabaseConnection(EmbeddedDatabaseConnection.H2);
    dataSourceProperties.setType(SingleConnectionDataSource.class);
    final DataSource dataSource = masterDataSourceConfig.dataSource(dataSourceProperties);

    // Assert
    Assertions.assertNotNull(dataSource);
  }

  @Test
  void generateMultiDataSourceMasterConfigGetEntityManagerFactory() {
    // Arrange
    final MasterDataSourceConfig masterDataSourceConfig = new MasterDataSourceConfig(); // Generated config class
    final DefaultPersistenceUnitManager mockPersistentUnitManager =
        new DefaultPersistenceUnitManager();
    final EntityManagerFactoryBuilder mockEntityManagerFactoryBuilder = new EntityManagerFactoryBuilder(
        new HibernateJpaVendorAdapter(),
        new HashMap<>(),
        mockPersistentUnitManager
    );
    final ConfigurableListableBeanFactory mockBeanFactory = Mockito
        .mock(ConfigurableListableBeanFactory.class);

    // Act
    final DataSourceProperties dataSourceProperties = masterDataSourceConfig.dataSourceProperties();
    dataSourceProperties.setEmbeddedDatabaseConnection(EmbeddedDatabaseConnection.H2);
    dataSourceProperties.setType(SingleConnectionDataSource.class);

    final DataSource dataSource = masterDataSourceConfig.dataSource(dataSourceProperties);
    final LocalContainerEntityManagerFactoryBean entityManagerFactory = masterDataSourceConfig
        .entityManagerFactory(mockEntityManagerFactoryBuilder, mockBeanFactory, dataSource);

    // Assert
    Assertions.assertNotNull(entityManagerFactory);
  }

  @Test
  void generateMultiDataSourceMasterConfigGetTransactionManager() {
    // Arrange
    final MasterDataSourceConfig masterDataSourceConfig = new MasterDataSourceConfig(); // Generated config class
    final EntityManagerFactory mockEntityManagerFactory = Mockito
        .mock(EntityManagerFactory.class);

    // Act
    final PlatformTransactionManager transactionManager = masterDataSourceConfig
        .transactionManager(mockEntityManagerFactory);

    // Assert
    Assertions.assertNotNull(transactionManager);
  }

  @Test
  void generateMultiDataSourceReplicaConfigGetDataSourceProperties() {
    // Arrange
    final ReadReplicaDataSourceConfig masterDataSourceConfig = new ReadReplicaDataSourceConfig(); // Generated config class

    // Act
    final DataSourceProperties dataSourceProperties = masterDataSourceConfig.dataSourceProperties();

    // Assert
    Assertions.assertNotNull(dataSourceProperties);
  }

  @Test
  void generateMultiDataSourceReplicaConfigGetDataSourceUsingProperties() {
    // Arrange
    final ReadReplicaDataSourceConfig masterDataSourceConfig = new ReadReplicaDataSourceConfig(); // Generated config class

    // Act
    final DataSourceProperties dataSourceProperties = masterDataSourceConfig.dataSourceProperties();
    dataSourceProperties.setEmbeddedDatabaseConnection(EmbeddedDatabaseConnection.H2);
    dataSourceProperties.setType(SingleConnectionDataSource.class);
    final DataSource dataSource = masterDataSourceConfig.dataSource(dataSourceProperties);

    // Assert
    Assertions.assertNotNull(dataSource);
  }

  @Test
  void generateMultiDataSourceReplicaConfigGetEntityManagerFactory() {
    // Arrange
    final ReadReplicaDataSourceConfig masterDataSourceConfig = new ReadReplicaDataSourceConfig(); // Generated config class
    final DefaultPersistenceUnitManager mockPersistentUnitManager =
        new DefaultPersistenceUnitManager();
    final EntityManagerFactoryBuilder mockEntityManagerFactoryBuilder = new EntityManagerFactoryBuilder(
        new HibernateJpaVendorAdapter(),
        new HashMap<>(),
        mockPersistentUnitManager
    );
    final ConfigurableListableBeanFactory mockBeanFactory = Mockito
        .mock(ConfigurableListableBeanFactory.class);

    // Act
    final DataSourceProperties dataSourceProperties = masterDataSourceConfig.dataSourceProperties();
    dataSourceProperties.setEmbeddedDatabaseConnection(EmbeddedDatabaseConnection.H2);
    dataSourceProperties.setType(SingleConnectionDataSource.class);

    final DataSource dataSource = masterDataSourceConfig.dataSource(dataSourceProperties);
    final LocalContainerEntityManagerFactoryBean entityManagerFactory = masterDataSourceConfig
        .entityManagerFactory(mockEntityManagerFactoryBuilder, mockBeanFactory, dataSource);

    // Assert
    Assertions.assertNotNull(entityManagerFactory);
  }

  @Test
  void generateMultiDataSourceReplicaConfigGetTransactionManager() {
    // Arrange
    final ReadReplicaDataSourceConfig masterDataSourceConfig = new ReadReplicaDataSourceConfig(); // Generated config class
    final EntityManagerFactory mockEntityManagerFactory = Mockito
        .mock(EntityManagerFactory.class);

    // Act
    final PlatformTransactionManager transactionManager = masterDataSourceConfig
        .transactionManager(mockEntityManagerFactory);

    // Assert
    Assertions.assertNotNull(transactionManager);
  }

  @EnableMultiDataSourceConfig(
      exactEntityPackages = "java.lang", // Object is entity
      repositoryPackages = "io.github.dhi13man.spring.datasource.generators" // MockRepository is repository
  )
  private interface MockRepository extends JpaRepository<Object, Long> {

    @Override
    @MultiDataSourceRepository("read-replica")
    @NonNull
    List<Object> findAll();
  }
}