package pl.droidsonroids.fontbinder

import com.google.auto.service.AutoService
import com.squareup.javapoet.*
import java.io.IOException
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@AutoService(Processor::class)
class BindFontProcessor : AbstractProcessor() {

	private val TARGET_PARAMETER_NAME = "target"
	private val typefaceClassName: ClassName = ClassName.get("android.graphics", "Typeface")

	private val elementUtils by lazy { processingEnv.elementUtils }
	private val filer by lazy { processingEnv.filer }
	private val messager by lazy { processingEnv.messager }

	override fun getSupportedAnnotationTypes() = mutableSetOf(BindFont::class.java.canonicalName)

	override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

	override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
		val targetMap: MutableMap<Element, MutableSet<Element>> = mutableMapOf()

		roundEnv.getElementsAnnotatedWith(BindFont::class.java).forEach { element ->
			targetMap.getOrPut(element.enclosingElement, { mutableSetOf() }).add(element)
		}

		for ((enclosingElement, annotatedFields) in targetMap) {
			val enclosingElementTypeName = TypeName.get(enclosingElement.asType())
			val methodBuilder = MethodSpec.methodBuilder("bind")
					.addModifiers(Modifier.STATIC)
					.addParameter(enclosingElementTypeName, TARGET_PARAMETER_NAME)

			annotatedFields.forEach {
				if (it.modifiers.contains(Modifier.PRIVATE)) {
					messager.printMessage(Diagnostic.Kind.ERROR, "Could not bind typeface to private field", it)
				}
				addTypeFaceBindingStatement(methodBuilder, it)
			}

			val classSpec = TypeSpec.classBuilder("${enclosingElement.simpleName}_FontBinder")
					.addMethod(methodBuilder.build())
					.build()
			createJavaFile(enclosingElement, classSpec)
		}

		return true
	}

	private fun addTypeFaceBindingStatement(methodBuilder: MethodSpec.Builder, field: Element) {
		val fontPath = field.getAnnotation(BindFont::class.java).value
		methodBuilder.addStatement("\$N.\$N.setTypeface(\$T.createFromAsset(\$N.getAssets(), \$S))", TARGET_PARAMETER_NAME, field.simpleName, typefaceClassName, TARGET_PARAMETER_NAME, fontPath)
	}

	private fun createJavaFile(enclosingElement: Element, classSpec: TypeSpec) {
		val packageName = elementUtils.getPackageOf(enclosingElement)
				.qualifiedName
				.toString()

		val javaFile = JavaFile.builder(packageName, classSpec).build()

		try {
			javaFile.writeTo(filer)
		} catch (e: IOException) {
			messager.printMessage(Diagnostic.Kind.ERROR, e.message)
		}
	}
}
