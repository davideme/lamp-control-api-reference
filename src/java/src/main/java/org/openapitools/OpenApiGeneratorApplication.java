package org.openapitools;

import com.fasterxml.jackson.databind.Module;
import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;

@SpringBootApplication(
    nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class,
    exclude = {
      DataSourceAutoConfiguration.class,
      HibernateJpaAutoConfiguration.class,
      JpaRepositoriesAutoConfiguration.class
    })
@ComponentScan(
    basePackages = {"org.openapitools", "org.openapitools.api", "org.openapitools.configuration"},
    nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class)
public class OpenApiGeneratorApplication {

  public static void main(final String[] args) {
    // Determine operation mode from command line arguments
    ApplicationMode.Mode mode = ApplicationMode.parseMode(args);

    // Handle migrate-only mode
    if (mode == ApplicationMode.Mode.MIGRATE) {
      ApplicationMode.runMigrationsOnly(args);
      return;
    }

    // Configure Spring properties based on mode
    ApplicationMode.configureMode(mode);

    // Start Spring Boot application
    SpringApplication.run(OpenApiGeneratorApplication.class, args);
  }

  @Bean(name = "org.openapitools.OpenApiGeneratorApplication.jsonNullableModule")
  public Module jsonNullableModule() {
    return new JsonNullableModule();
  }
}
