package pl.droidsonroids.fontbinder

import com.google.common.truth.Truth.assert_
import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourceSubjectFactory.javaSource
import org.junit.Ignore
import org.junit.Test

class BindFontProcessorTest {

	@Test
	fun `binder for valid file compiles without warnings`() {
		assert_().about(javaSource())
				.that(JavaFileObjects.forResource("ValidActivity.java"))
				.processedWith(BindFontProcessor())
				.compilesWithoutWarnings()
				.and()
				.generatesSources(JavaFileObjects.forResource("ValidActivity_FontBinder.java"))
	}

	@Test
	fun `private field binding raises processing error`() {
		val sourceFile = JavaFileObjects.forResource("PrivateFieldActivity.java")
		assert_().about(javaSource())
				.that(sourceFile)
				.processedWith(BindFontProcessor())
				.failsToCompile()
				.withErrorContaining("private")
				.`in`(sourceFile)
				.onLine(8)
	}

	@Test
	@Ignore("TODO")
	fun `non TextView field binding raises processing error`() {
		val sourceFile = JavaFileObjects.forResource("NonTextViewFieldActivity.java")
		assert_().about(javaSource())
				.that(sourceFile)
				.processedWith(BindFontProcessor())
				.failsToCompile()
				.withErrorContaining("TextView")
				.`in`(sourceFile)
				.onLine(8)
	}
}