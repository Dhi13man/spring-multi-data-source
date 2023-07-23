//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package io.github.dhi13man.spring.datasource.config;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * This interface is followed by all the generated Multi Data Source Config classes.
 */
public interface MultiDataSourceConfigInterface {

  /**
   * Get the data source properties for the data source.
   *
   * @return The data source properties.
   */
  DataSourceProperties dataSourceProperties();

  /**
   * Get the data source for the data source, using the data source properties.
   *
   * @param dataSourceProperties The data source properties.
   * @return The data source.
   */
  DataSource dataSource(DataSourceProperties dataSourceProperties);

  /**
   * Get the entity manager factory to be used with the data source.
   *
   * @param builder     The entity manager factory builder, used to build the entity manager
   * @param beanFactory The bean factory, used to create the entity manager factory bean
   * @param dataSource  The data source, used to create the entity manager factory bean
   * @return The entity manager factory bean.
   */
  LocalContainerEntityManagerFactoryBean entityManagerFactory(
      EntityManagerFactoryBuilder builder,
      ConfigurableListableBeanFactory beanFactory,
      DataSource dataSource
  );

  /**
   * Get the transaction manager to be used with the data source.
   *
   * @param entityManagerFactory The entity manager factory, used to create the transaction manager
   * @return The transaction manager.
   */
  PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory);
}
