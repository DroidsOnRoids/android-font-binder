package pl.droidsonroids.fontbinder;

import android.app.Activity;
import android.widget.TextView;

public class PrivateFieldActivity extends Activity {
	@BindFont("test.ttf")
	private TextView foo;
}
