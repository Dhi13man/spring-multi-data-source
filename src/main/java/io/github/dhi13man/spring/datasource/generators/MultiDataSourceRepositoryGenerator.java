package io.github.dhi13man.spring.datasource.generators;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import io.github.dhi13man.spring.datasource.annotations.TargetSecondaryDataSource;
import io.github.dhi13man.spring.datasource.annotations.TargetSecondaryDataSources;
import io.github.dhi13man.spring.datasource.config.IGeneratedDataSourceRepository;
import io.github.dhi13man.spring.datasource.utils.MultiDataSourceCommonStringUtils;
import io.github.dhi13man.spring.datasource.utils.MultiDataSourceGeneratorUtils;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import javax.annotation.processing.Messager;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import org.springframework.stereotype.Repository;

/**
 * Annotation processor to generate config classes for all the repositories annotated with
 * {@link TargetSecondaryDataSource} and create copies of the repositories in the relevant
 * packages.
 */
public class MultiDataSourceRepositoryGenerator {

  private static final String REPOSITORY_BEAN_NAME = "REPOSITORY_BEAN_NAME";

  private final @Nonnull Messager messager;

  private final @Nonnull Types typeUtils;

  private final @Nonnull MultiDataSourceCommonStringUtils multiDataSourceCommonStringUtils;

  private final @Nonnull MultiDataSourceGeneratorUtils multiDataSourceGeneratorUtils;

  public MultiDataSourceRepositoryGenerator(
      @Nonnull Messager messager,
      @Nonnull Types typeUtils,
      @Nonnull MultiDataSourceCommonStringUtils multiDataSourceCommonStringUtils,
      @Nonnull MultiDataSourceGeneratorUtils multiDataSourceGeneratorUtils
  ) {
    this.messager = messager;
    this.typeUtils = typeUtils;
    this.multiDataSourceCommonStringUtils = multiDataSourceCommonStringUtils;
    this.multiDataSourceGeneratorUtils = multiDataSourceGeneratorUtils;
  }

  /**
   * Create the {@link TypeSpec} for a generated Spring Repository interface with annotated methods
   * for the given {@link TypeElement} and set of {@link ExecutableElement} methods.
   * <p>
   * The generated interface will have the same name as the given {@link TypeElement} with the
   * prefix as the PascalCase version of the given {@code dataSourceName}.
   *
   * @param typeElement    the {@link TypeElement} to generate the interface for (must be an
   *                       interface or class)
   * @param methods        the {@link ExecutableElement} methods to generate annotated methods for
   *                       (must be methods of the given {@link TypeElement})
   * @param dataSourceName the name of the data source the generated interface is for
   * @return the {@link TypeSpec} for a generated Spring Repository interface with annotated methods
   */
  public @Nonnull TypeSpec generateRepositoryTypeElementWithAnnotatedMethods(
      @Nonnull TypeElement typeElement,
      @Nonnull Set<ExecutableElement> methods,
      @Nonnull String dataSourceName
  ) {
    // Generate the class/interface definition
    final String generatedTypename = multiDataSourceCommonStringUtils.toPascalCase(dataSourceName)
        + typeElement.getSimpleName().toString();
    final TypeSpec.Builder builder;
    switch (typeElement.getKind()) {
      case INTERFACE: {
        builder = TypeSpec.interfaceBuilder(generatedTypename);
        break;
      }

      case CLASS: {
        builder = TypeSpec.classBuilder(generatedTypename);
        break;
      }

      default: {
        final String errorMessage = "Type " + typeElement.getQualifiedName()
            + " is not a class or interface!";
        messager.printMessage(Kind.ERROR, errorMessage);
        throw new IllegalArgumentException(errorMessage);
      }
    }

    // Create all type parameters
    final List<TypeVariableName> typeVariableNames = typeElement.getTypeParameters().stream()
        .map(TypeVariableName::get)
        .collect(Collectors.toList());

    // Add all superclasses
    final Set<MethodSpec> superMethods = new HashSet<>();
    for (final TypeMirror typeMirror : typeElement.getInterfaces()) {
      builder.addSuperinterface(typeMirror);
      // Override and disable all method signatures from the superclass
      final Set<MethodSpec> methodsToAdd = this
          .generateOverridenAndDisabledSuperMethods(methods, (DeclaredType) typeMirror);
      superMethods.addAll(methodsToAdd);
    }
    builder.addSuperinterface(IGeneratedDataSourceRepository.class);

    // Create all necessary methods to be copied to the generated class
    final List<MethodSpec> methodSpecs = methods.stream()
        .map(this::convertExecutableMethodElementToMethodSpec)
        .collect(Collectors.toList());

    // Create the bean name constant
    final FieldSpec repositoryBeanNameFieldSpec = multiDataSourceGeneratorUtils
        .createConstantStringFieldSpec(REPOSITORY_BEAN_NAME, generatedTypename);

    // Add the Repository annotation
    final AnnotationSpec repositoryAnnotation = AnnotationSpec.builder(Repository.class)
        .addMember("value", "$L.$N", generatedTypename, repositoryBeanNameFieldSpec)
        .build();

    // Return the generated class
    return builder.addModifiers(Modifier.PUBLIC)
        .addTypeVariables(typeVariableNames)
        .addAnnotation(repositoryAnnotation)
        .addField(repositoryBeanNameFieldSpec)
        .addMethods(methodSpecs)
        .addMethods(superMethods)
        .build();
  }

  /**
   * Convert a {@link VariableElement} to a {@link ParameterSpec}.
   *
   * @param parameter the {@link VariableElement} to convert
   * @return the {@link ParameterSpec} for the given {@link VariableElement}
   */
  private @Nonnull ParameterSpec convertParameterVariableElementToParameterSpec(
      @Nonnull VariableElement parameter
  ) {
    // Copy all annotations
    final List<AnnotationSpec> annotationSpecs = parameter.getAnnotationMirrors().stream()
        .map(AnnotationSpec::get)
        .collect(Collectors.toList());

    // Create the parameter spec
    return ParameterSpec
        .builder(TypeName.get(parameter.asType()), parameter.getSimpleName().toString())
        .addAnnotations(annotationSpecs)
        .build();
  }

  /**
   * Override all non-annotated methods from the superclass and disable them by throwing an
   * {@link UnsupportedOperationException}.
   *
   * @param annotatedMethods the annotated methods to exclude from overriding and disabling
   * @param declaredType     the {@link DeclaredType} of the superclass (must be an interface or
   *                         class) to override and disable methods from
   * @return the {@link MethodSpec}s for all overridden and disabled methods
   */
  private @Nonnull Set<MethodSpec> generateOverridenAndDisabledSuperMethods(
      @Nonnull Set<ExecutableElement> annotatedMethods,
      @Nonnull DeclaredType declaredType
  ) {
    final TypeElement superTypeElement = (TypeElement) declaredType.asElement();
    final Map<TypeName, TypeName> baseTypeNameToDerived = this
        .getBaseTypeNameToDerived(declaredType, superTypeElement);
    final List<ExecutableElement> superMethods = ElementFilter
        .methodsIn(superTypeElement.getEnclosedElements());

    final Set<MethodSpec> overridenMethods = new HashSet<>();
    for (final ExecutableElement superMethod : superMethods) {
      final boolean shouldExcludeMethod = superMethod.getModifiers().contains(Modifier.PRIVATE)
          || superMethod.getModifiers().contains(Modifier.FINAL)
          || annotatedMethods.stream().anyMatch(
          method -> this.isMethodSignatureMatching(superMethod, method, baseTypeNameToDerived)
      );
      if (shouldExcludeMethod) {
        continue;
      }

      final MethodSpec methodSpec = this.convertExecutableMethodElementToMethodSpec(superMethod);
      final MethodSpec.Builder typeReplacedSpecBuilder = this
          .replaceAllBaseMethodTypesWithDerivedTypes(methodSpec, baseTypeNameToDerived)
          .toBuilder();
      typeReplacedSpecBuilder.modifiers.remove(Modifier.ABSTRACT);
      final MethodSpec disabledSpec = typeReplacedSpecBuilder
          .addModifiers(Modifier.DEFAULT)
          .addStatement(
              "throw new $T($S)",
              UnsupportedOperationException.class,
              "This method is disabled for this data source!"
          )
          .build();
      overridenMethods.add(disabledSpec);
    }
    return overridenMethods;
  }

  /**
   * Check if the method signatures of the given {@link ExecutableElement}s are matching.
   *
   * @param executableElement1 The first method
   * @param executableElement2 The second method
   * @return true if the method signatures are matching, false otherwise
   */
  private boolean isMethodSignatureMatching(
      @Nonnull ExecutableElement executableElement1,
      @Nonnull ExecutableElement executableElement2,
      @Nonnull Map<TypeName, TypeName> baseTypeNameToDerived
  ) {
    // Name check
    if (!executableElement2.getSimpleName().equals(executableElement1.getSimpleName())) {
      return false;
    }

    // Parameter type check
    final List<? extends VariableElement> parameters1 = executableElement1.getParameters();
    final List<? extends VariableElement> parameters2 = executableElement2.getParameters();
    if (parameters1.size() != parameters2.size()) {
      return false;
    }

    for (int i = 0; i < parameters1.size(); i++) {
      final TypeName parameter1 = recursivelyConvertType(
          TypeName.get(parameters1.get(i).asType()),
          baseTypeNameToDerived
      );
      final TypeName parameter2 = recursivelyConvertType(
          TypeName.get(parameters2.get(i).asType()),
          baseTypeNameToDerived
      );
      if (!parameter1.toString().equals(parameter2.toString())) {
        return false;
      }
    }

    // Return type check
    return !typeUtils
        .isSameType(executableElement1.getReturnType(), executableElement2.getReturnType());
  }

  /**
   * Convert a {@link ExecutableElement} to a {@link MethodSpec}.
   *
   * @param method the {@link ExecutableElement} to convert
   * @return the {@link MethodSpec} for the given {@link ExecutableElement}
   */
  private @Nonnull MethodSpec convertExecutableMethodElementToMethodSpec(
      @Nonnull ExecutableElement method
  ) {
    // Copy all annotations other than the ones used to mark the method as a repository method
    // (i.e. @TargetSecondaryDataSources and @TargetSecondaryDataSource)
    final List<AnnotationSpec> annotationsToSpec = method.getAnnotationMirrors().stream()
        .map(AnnotationSpec::get)
        .filter(
            annotationSpec -> !Set.of(
                TypeName.get(TargetSecondaryDataSources.class),
                TypeName.get(TargetSecondaryDataSource.class)
            ).contains(annotationSpec.type)
        )
        .collect(Collectors.toList());

    // Copy all parameters
    final List<ParameterSpec> parametersToSpec = method.getParameters().stream()
        .map(this::convertParameterVariableElementToParameterSpec)
        .collect(Collectors.toList());

    // Copy all type parameters
    final List<TypeVariableName> typeParametersToSpec = method.getTypeParameters().stream()
        .map(TypeVariableName::get)
        .collect(Collectors.toList());

    // Create the method spec
    return MethodSpec.methodBuilder(method.getSimpleName().toString())
        .addModifiers(method.getModifiers())
        .addAnnotations(annotationsToSpec)
        .addParameters(parametersToSpec)
        .addTypeVariables(typeParametersToSpec)
        .returns(TypeName.get(method.getReturnType()))
        .build();
  }

  /**
   * Get a map of the base type names to the derived type names.
   * <p>
   * This is necessary because the {@link TypeElement} being used to generate things does not
   * contain the type arguments of the {@link DeclaredType} of the superclass.
   *
   * @param declaredType the {@link DeclaredType} of the superclass (must be an interface or class)
   * @param typeElement  the {@link TypeElement} of the superclass (must be an interface or class)
   * @return a map of the base type names to the derived type names
   */
  private @Nonnull Map<TypeName, TypeName> getBaseTypeNameToDerived(
      @Nonnull DeclaredType declaredType,
      @Nonnull TypeElement typeElement
  ) {
    final List<TypeName> baseTypeNames = typeElement.getTypeParameters()
        .stream()
        .map(element -> TypeName.get(element.asType()))
        .collect(Collectors.toList());
    final List<TypeName> derivedTypeNames = declaredType.getTypeArguments()
        .stream()
        .map(TypeName::get)
        .collect(Collectors.toList());
    return IntStream.range(0, baseTypeNames.size())
        .boxed()
        .collect(Collectors.toMap(baseTypeNames::get, derivedTypeNames::get));
  }

  /**
   * Replace all base type names with derived type names in the given {@link MethodSpec}.
   * <p>
   * This will recursively replace all return types, parameter types, and type variables.
   *
   * @param methodSpec            the {@link MethodSpec} to replace the types in
   * @param baseTypeNameToDerived a map of the base type names to the derived type names (see
   *                              {@link #getBaseTypeNameToDerived(DeclaredType, TypeElement)})
   * @return the {@link MethodSpec} with all base type names replaced with derived type names
   */
  private @Nonnull MethodSpec replaceAllBaseMethodTypesWithDerivedTypes(
      @Nonnull MethodSpec methodSpec,
      @Nonnull Map<TypeName, TypeName> baseTypeNameToDerived
  ) {
    final MethodSpec.Builder builder = MethodSpec.methodBuilder(methodSpec.name)
        .addModifiers(methodSpec.modifiers)
        .addCode(methodSpec.code)
        .addAnnotations(methodSpec.annotations)
        .varargs(methodSpec.varargs)
        .addAnnotation(Override.class);

    // First replace all return types
    final TypeName returnType = methodSpec.returnType;
    builder.returns(recursivelyConvertType(returnType, baseTypeNameToDerived));

    // Then replace all parameter types
    final List<ParameterSpec> parameterSpecs = methodSpec.parameters;
    final List<ParameterSpec> convertedParameterSpecs = parameterSpecs.stream()
        .map(
            parameterSpec -> {
              final TypeName parameterType = parameterSpec.type;
              final TypeName convertedParameterType =
                  recursivelyConvertType(parameterType, baseTypeNameToDerived);
              return ParameterSpec.builder(convertedParameterType, parameterSpec.name)
                  .addAnnotations(parameterSpec.annotations)
                  .build();
            }
        )
        .collect(Collectors.toList());
    builder.addParameters(convertedParameterSpecs);

    // Replace all type variables
    final List<TypeVariableName> typeVariables = methodSpec.typeVariables;
    final List<TypeVariableName> convertedTypeVariables = typeVariables.stream()
        .map(
            typeVariableName -> {
              final TypeName typeName = typeVariableName.bounds.get(0);
              final TypeName convertedTypeName =
                  recursivelyConvertType(typeName, baseTypeNameToDerived);
              return TypeVariableName.get(typeVariableName.name, convertedTypeName);
            }
        )
        .collect(Collectors.toList());
    builder.addTypeVariables(convertedTypeVariables);

    // Finally replace all thrown types
    final List<TypeName> thrownTypes = methodSpec.exceptions;
    final List<TypeName> convertedThrownTypes = thrownTypes.stream()
        .map(typeName -> recursivelyConvertType(typeName, baseTypeNameToDerived))
        .collect(Collectors.toList());
    builder.addExceptions(convertedThrownTypes);

    return builder.build();
  }

  /**
   * Recursively convert the given base {@link TypeName} to the derived {@link TypeName} using the
   * given {@code typeNameMap}.
   * <p>
   * If it is a parameterised type, all type arguments will be recursively replaced as well.
   *
   * @param input       the base {@link TypeName}
   * @param typeNameMap the map of base {@link TypeName} to derived {@link TypeName}
   * @return the derived {@link TypeName}
   */
  private @Nonnull TypeName recursivelyConvertType(
      @Nonnull TypeName input,
      @Nonnull Map<TypeName, TypeName> typeNameMap
  ) {
    if (!(input instanceof ParameterizedTypeName)) {
      return typeNameMap.getOrDefault(input, input);
    }

    // If the return type is a parameterised type, replace all type arguments
    final ParameterizedTypeName parameterizedTypeName = (ParameterizedTypeName) input;
    final List<TypeName> typeArguments = parameterizedTypeName.typeArguments;
    final TypeName[] convertedTypeNames = typeArguments.stream()
        .map(typeName -> recursivelyConvertType(typeName, typeNameMap))
        .toArray(TypeName[]::new);
    return ParameterizedTypeName.get(
        parameterizedTypeName.rawType,
        convertedTypeNames
    );
  }
}
