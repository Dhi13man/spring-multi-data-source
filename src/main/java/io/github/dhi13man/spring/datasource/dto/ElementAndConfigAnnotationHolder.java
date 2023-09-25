package io.github.dhi13man.spring.datasource.dto;

import io.github.dhi13man.spring.datasource.annotations.EnableMultiDataSourceConfig;
import javax.lang.model.element.Element;

/**
 * Holder for a {@link EnableMultiDataSourceConfig} annotation and the element on which it is
 * declared.
 */
public class ElementAndConfigAnnotationHolder {

  private final Element element;

  private final EnableMultiDataSourceConfig annotation;

  public ElementAndConfigAnnotationHolder(Element element, EnableMultiDataSourceConfig annotation) {
    this.element = element;
    this.annotation = annotation;
  }

  public Element getElement() {
    return element;
  }

  public EnableMultiDataSourceConfig getAnnotation() {
    return annotation;
  }
}
