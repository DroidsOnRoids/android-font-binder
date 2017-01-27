package pl.droidsonroids.fontbinder

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class BindFont (val value: String, val bold:Boolean = true)
