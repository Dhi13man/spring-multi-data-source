package io.github.dhi13man.spring.datasource.dto;

import io.github.dhi13man.spring.datasource.annotations.EnableMultiDataSourceConfig;
import javax.lang.model.element.Element;

public class EnableConfigAnnotationAndElementHolder {

  private final Element annotatedElement;

  private final EnableMultiDataSourceConfig annotation;

  public EnableConfigAnnotationAndElementHolder(Element annotatedElement,
      EnableMultiDataSourceConfig annotation) {
    this.annotatedElement = annotatedElement;
    this.annotation = annotation;
  }

  public Element getAnnotatedElement() {
    return annotatedElement;
  }

  public EnableMultiDataSourceConfig getAnnotation() {
    return annotation;
  }
}
