package br.com.redemaisfarma.support.containers;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;

/** Starts a MySQL Testcontainer and wires spring.datasource.* dynamically */
@TestConfiguration
public class MySqlContainerConfig {
  @Bean(destroyMethod = "stop")
  public MySQLContainer<?> mySqlContainer() {
    MySQLContainer<?> mysql = new MySQLContainer<>(\"mysql:8.4\")
        .withDatabaseName(\"redemaisfarma\")
        .withUsername(\"root\")
        .withPassword(\"root\");
    mysql.start();
    System.setProperty(\"spring.datasource.url\", mysql.getJdbcUrl());
    System.setProperty(\"spring.datasource.username\", mysql.getUsername());
    System.setProperty(\"spring.datasource.password\", mysql.getPassword());
    return mysql;
  }
}
