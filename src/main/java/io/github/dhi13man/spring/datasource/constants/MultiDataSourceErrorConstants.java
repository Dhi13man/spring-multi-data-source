package io.github.dhi13man.spring.datasource.constants;

public final class MultiDataSourceErrorConstants {

  public static final String MULTIPLE_CLASSES_ANNOTATED_WITH_ENABLE_CONFIG_ANNOTATION = "Multiple"
      + " classes are annotated with @EnableMultiDataSourceConfig. Please annotate only one class"
      + " with this annotation and provide the data source configs.";

  public static final String MULTIPLE_CONFIG_ANNOTATIONS_FOR_ONE_DATASOURCE = "Multiple"
      + " @DataSourceConfigs annotations are provided for the same data source. Please"
      + " provide only one @DataSourceConfigs annotation for each data source.";

  public static final String NO_REPOSITORY_PACKAGES_PROVIDED_IN_CONFIG = "No repository packages"
      + " are provided in @EnableMultiDataSourceConfig.repositoryPackages. Please provide all"
      + " the packages (or a parent package) that hold your repositories.";

  public static final String NO_REPOSITORY_METHOD_ANNOTATED_WITH_TARGET_SECONDARY_DATA_SOURCE =
      "No repository method is annotated with @TargetSecondaryDataSource. Please annotate at least"
          + " one repository method with this annotation if you are using"
          + " @EnableMultiDataSourceConfig and want to segregate your repositories by data source.";

  private MultiDataSourceErrorConstants() {
  }

}
