package pl.droidsonroids.fontbinder;

import android.app.Activity;
import android.widget.TextView;

public class NonTextViewFieldActivity extends Activity {
	@BindFont("test.ttf")
	int foo;
}
