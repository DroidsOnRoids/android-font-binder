package pl.droidsonroids.fontbinder

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
annotation class BindFont(val value: String, val bold: Boolean = false)
