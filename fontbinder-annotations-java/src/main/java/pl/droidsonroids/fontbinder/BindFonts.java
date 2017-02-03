package pl.droidsonroids.fontbinder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
public @interface BindFonts {
	BindFont[] value();
}
