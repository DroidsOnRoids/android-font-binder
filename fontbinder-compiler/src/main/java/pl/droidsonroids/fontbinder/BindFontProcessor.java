package pl.droidsonroids.fontbinder;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class BindFontProcessor extends AbstractProcessor {

	private static final String TARGET_PARAMETER = "target";
	private Elements elementUtils;
	private Filer filer;

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		final HashSet<String> annotations = new HashSet<>(1);
		annotations.add(BindFont.class.getCanonicalName());
		return annotations;
	}

	@Override
	public synchronized void init(final ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		elementUtils = processingEnv.getElementUtils();
		filer = processingEnv.getFiler();
	}

	@Override
	public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
		final HashMap<Element, Set<Element>> targetMap = new HashMap<>();

		for (Element element : roundEnv.getElementsAnnotatedWith(BindFont.class)) {
			targetMap.computeIfAbsent(element.getEnclosingElement(), key -> new HashSet<>())
					.add(element);
		}

		targetMap.entrySet().forEach(entry -> {
			final Element enclosingElement = entry.getKey();

			final MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("bind")
					.addModifiers(Modifier.STATIC)
					.addParameter(TypeName.get(enclosingElement.asType()), TARGET_PARAMETER);

			entry.getValue().forEach(field -> addBinding(methodBuilder, field));

			final TypeSpec classSpec = TypeSpec
					.classBuilder(enclosingElement.getSimpleName().toString() + "Binder")
					.addMethod(methodBuilder.build())
					.build();

			createJavaFile(enclosingElement, classSpec);
		});

		return true;
	}

	private void createJavaFile(Element enclosingElement, TypeSpec classSpec) {
		final String packageName = elementUtils.getPackageOf(enclosingElement)
				.getQualifiedName()
				.toString();

		final JavaFile javaFile = JavaFile
				.builder(packageName, classSpec)
				.build();
		try {
			javaFile.writeTo(filer);
		} catch (IOException e) {
			processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
		}
	}

	private void addBinding(MethodSpec.Builder methodBuilder, Element field) {
		final Name fieldName = field.getSimpleName();
		final ClassName typeface = ClassName.get("android.graphics", "Typeface");
		final String fontPath = field.getAnnotation(BindFont.class).value();
		methodBuilder.addStatement("$N.$N.setTypeface($T.createFromAsset($N.getAssets(), $S))", TARGET_PARAMETER, fieldName, typeface, TARGET_PARAMETER, fontPath);
	}
}
