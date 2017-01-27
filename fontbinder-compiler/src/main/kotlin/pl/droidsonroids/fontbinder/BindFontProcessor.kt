package pl.droidsonroids.fontbinder

import com.squareup.javapoet.*
import java.io.IOException
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

class BindFontProcessor : AbstractProcessor() {

	private val TARGET_PARAMETER = "target"
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
		for ((enclosingElement, value) in targetMap) {
			val enclosingElementTypeName = TypeName.get(enclosingElement.asType())
			val methodBuilder = MethodSpec.methodBuilder("bind")
					.addModifiers(Modifier.STATIC)
					.addParameter(enclosingElementTypeName, TARGET_PARAMETER)
			value.forEach { field -> addBinding(methodBuilder, field) }

			val classSpec = TypeSpec.classBuilder("${enclosingElement.simpleName}_FontBinder")
					.addMethod(methodBuilder.build())
					.build()
			createJavaFile(enclosingElement, classSpec)
		}

		return true
	}

	private fun addBinding(methodBuilder: MethodSpec.Builder, field: Element) {
		val typeface = ClassName.get("android.graphics", "Typeface")
		val fontPath = field.getAnnotation(BindFont::class.java).value
		methodBuilder.addStatement("\$N.\$N.setTypeface(\$T.createFromAsset(\$N.getAssets(), \$S))", TARGET_PARAMETER, field.simpleName, typeface, TARGET_PARAMETER, fontPath)

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
