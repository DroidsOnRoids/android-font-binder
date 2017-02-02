package pl.droidsonroids.fontbinder

import android.graphics.Typeface
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
	private val typefaceClassName = ClassName.get(Typeface::class.java)

	private val elementUtils by lazy { processingEnv.elementUtils }
	private val filer by lazy { processingEnv.filer }
	private val messager by lazy { processingEnv.messager }

	override fun getSupportedAnnotationTypes() = setOf(BindFont::class.java.canonicalName)

	override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

	override fun process(annotatedElements: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
		val targetMap: MutableMap<Element, MutableSet<Element>> = mutableMapOf()

		roundEnv.getElementsAnnotatedWith(BindFont::class.java).forEach { element ->
			targetMap.getOrPut(element.enclosingElement, { mutableSetOf() })
					.add(element)
		}

		for ((enclosingElement, annotatedFields) in targetMap) {
			val enclosingElementTypeName = TypeName.get(enclosingElement.asType())
			val methodBuilder = MethodSpec.methodBuilder("bind")
					.addModifiers(Modifier.STATIC)
					.addParameter(enclosingElementTypeName, TARGET_PARAMETER_NAME)

			annotatedFields.forEach {
				if (Modifier.PRIVATE in it.modifiers) {
					it.printErrorMessage("Could not bind typeface to private field")
				}
				methodBuilder.addTypeFaceBindingStatement(it)
			}

			TypeSpec.classBuilder("${enclosingElement.simpleName}_FontBinder")
					.addMethod(methodBuilder.build())
					.build()
					.createJavaFile(enclosingElement)
		}
		return true
	}

	private fun MethodSpec.Builder.addTypeFaceBindingStatement(field: Element) {
		val fontPath = field.getAnnotation(BindFont::class.java).value
		addStatement("\$N.\$N.setTypeface(\$T.createFromAsset(\$N.getAssets(), \$S))", TARGET_PARAMETER_NAME, field.simpleName, typefaceClassName, TARGET_PARAMETER_NAME, fontPath)
	}

	private fun TypeSpec.createJavaFile(enclosingElement: Element) {
		val packageName = elementUtils.getPackageOf(enclosingElement)
				.qualifiedName
				.toString()

		try {
			JavaFile.builder(packageName, this)
					.build()
					.writeTo(filer)
		} catch (e: IOException) {
			messager.printMessage(Diagnostic.Kind.ERROR, e.message)
		}
	}

	private fun Element.printErrorMessage(message: String) {
		messager.printMessage(Diagnostic.Kind.ERROR, message, this)
	}
}
