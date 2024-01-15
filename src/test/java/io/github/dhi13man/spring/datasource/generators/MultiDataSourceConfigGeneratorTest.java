package io.github.dhi13man.spring.datasource.generators;

import io.github.dhi13man.spring.datasource.annotations.TargetSecondaryDataSource;
import io.github.dhi13man.spring.datasource.config.IMultiDataSourceConfig;
import io.github.dhi13man.spring.datasource.generated.config.MasterDataSourceConfig;
import io.github.dhi13man.spring.datasource.generated.config.ReadReplicaDataSourceConfig;
import io.github.dhi13man.spring.datasource.generated.config.Replica2DataSourceConfig;
import io.github.dhi13man.spring.datasource.generated.config.ReplicaNoTargetDataSourceDataSourceConfig;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;
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

  /**
   * The set of generated config classes to test the generation logic with.
   * <p>
   * Running mvn clean install will generate the classes in the
   * target/generated-test-sources/annotations directory.
   */
  private final Set<IMultiDataSourceConfig> generatedConfigs = Set.of(
      new MasterDataSourceConfig(),
      new ReadReplicaDataSourceConfig(),
      new Replica2DataSourceConfig(),
      new ReplicaNoTargetDataSourceDataSourceConfig()
  );

  @Test
  void generateMultiDataSourceConfigTypeElementGetDataSourceProperties() {
    for (final IMultiDataSourceConfig generatedConfig : generatedConfigs) {
      // Act
      final DataSourceProperties dataSourceProperties = generatedConfig.dataSourceProperties();

      // Assert
      Assertions.assertNotNull(dataSourceProperties);
    }
  }

  @Test
  void generateMultiDataSourceConfigTypeElementGetDataSourceUsingProperties() {
    for (final IMultiDataSourceConfig generatedConfig : generatedConfigs) {
      // Act
      final DataSourceProperties dataSourceProperties = generatedConfig.dataSourceProperties();
      dataSourceProperties.setEmbeddedDatabaseConnection(EmbeddedDatabaseConnection.H2);
      dataSourceProperties.setType(SingleConnectionDataSource.class);
      final DataSource dataSource = generatedConfig.dataSource(dataSourceProperties);

      // Assert
      Assertions.assertNotNull(dataSource);
    }
  }

  @Test
  void generateMultiDataSourceConfigTypeElementGetEntityManagerFactory() {
    for (final IMultiDataSourceConfig generatedConfig : generatedConfigs) {
      // Arrange
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
      final DataSourceProperties dataSourceProperties = generatedConfig.dataSourceProperties();
      dataSourceProperties.setEmbeddedDatabaseConnection(EmbeddedDatabaseConnection.H2);
      dataSourceProperties.setType(SingleConnectionDataSource.class);
      final Properties overrideJpaProperties = generatedConfig.overridingJpaProperties();
      final DataSource dataSource = generatedConfig.dataSource(dataSourceProperties);
      final LocalContainerEntityManagerFactoryBean entityManagerFactory = generatedConfig.entityManagerFactory(
          overrideJpaProperties,
          dataSource,
          mockEntityManagerFactoryBuilder,
          mockBeanFactory
      );

      // Assert
      Assertions.assertNotNull(entityManagerFactory);
    }
  }

  @Test
  void generateMultiDataSourceConfigTypeElementGetTransactionManager() {

    for (final IMultiDataSourceConfig generatedConfig : generatedConfigs) {
      // Arrange
      final LocalContainerEntityManagerFactoryBean mockEntityManagerFactory = Mockito
          .mock(LocalContainerEntityManagerFactoryBean.class);
      Mockito.when(mockEntityManagerFactory.getNativeEntityManagerFactory())
          .thenReturn(Mockito.mock());

      // Act
      final PlatformTransactionManager transactionManager = generatedConfig
          .transactionManager(mockEntityManagerFactory);

      // Assert
      Assertions.assertNotNull(transactionManager);
    }
  }

  public interface MockConfigTestRepository extends JpaRepository<Object, Long> {

    @Override
    @TargetSecondaryDataSource("read-replica")
    @NonNull
    List<Object> findAll();
  }
}