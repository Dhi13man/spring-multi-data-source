package io.github.dhi13man.spring.datasource.config;

import java.util.Properties;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * This interface is implemented by all the generated Multi Data Source Config classes.
 */
public interface IMultiDataSourceConfig {

  /**
   * Get the {@link DataSourceProperties} properties for the data source to be used.
   *
   * @return The data source properties.
   */
  DataSourceProperties dataSourceProperties();

  /**
   * Get the JPA properties to override for the data source to be used.
   * <p>
   * This allows overriding of the JPA properties for each data source. These properties will be
   * merged with the usual properties under the spring.jpa.properties key.
   *
   * @return The JPA properties.
   */
  Properties overridingJpaProperties();

  /**
   * Get the {@link DataSource} instance for the data source to be used.
   * <p>
   * This is instantiated using the data source properties.
   *
   * @param dataSourceProperties The data source properties.
   * @return The data source.
   */
  DataSource dataSource(DataSourceProperties dataSourceProperties);

  /**
   * Get the entity manager factory to be used to interface with the data source.
   *
   * @param builder               The entity manager factory builder, used to build the entity
   *                              manager
   * @param beanFactory           The bean factory, used to create the entity manager factory bean
   * @param dataSource            The data source, used to create the entity manager factory bean
   * @param overrideJpaProperties The JPA properties, used to create the entity manager factory
   *                              bean
   * @return The entity manager factory bean.
   */
  LocalContainerEntityManagerFactoryBean entityManagerFactory(
      Properties overrideJpaProperties,
      DataSource dataSource,
      EntityManagerFactoryBuilder builder,
      ConfigurableListableBeanFactory beanFactory
  );

  /**
   * Get the transaction manager to be used for the data source.
   *
   * @param entityManagerFactory The entity manager factory used to create the transaction manager
   * @return The transaction manager.
   */
  PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory);
}
