package io.github.dhi13man.spring.datasource.constants;

public final class MultiDataSourceErrorConstants {

  public static final String MULTIPLE_CLASSES_ANNOTATED_WITH_ENABLE_CONFIG_ANNOTATION = "Multiple"
      + " classes are annotated with @EnableMultiDataSourceConfig. Please annotate only one class"
      + " with this annotation and provide the master data source name and entity packages in it.";

  public static final String NO_ENTITY_PACKAGES_PROVIDED_IN_CONFIG = "No entity packages are"
      + " provided in @EnableMultiDataSourceConfig.exactEntityPackages. Please provide all the"
      + " exact packages that hold your entities.";

  public static final String NO_REPOSITORY_PACKAGES_PROVIDED_IN_CONFIG = "No repository packages"
      + " are provided in @EnableMultiDataSourceConfig.repositoryPackages. Please provide all"
      + " the packages (or a parent package) that hold your repositories.";

  public static final String NO_REPOSITORY_METHOD_ANNOTATED_WITH_MULTI_DATA_SOURCE_REPOSITORY =
      "No repository method is annotated with @MultiDataSourceRepository. Please annotate at least"
          + " one repository method with this annotation if you are using"
          + " @EnableMultiDataSourceConfig";

  private MultiDataSourceErrorConstants() {
  }

}
