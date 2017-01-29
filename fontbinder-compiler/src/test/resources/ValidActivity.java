package pl.droidsonroids.fontbinder;

import android.app.Activity;
import android.widget.TextView;

public class ValidActivity extends Activity {
	@BindFont("test.ttf")
	TextView testTextView;
}
