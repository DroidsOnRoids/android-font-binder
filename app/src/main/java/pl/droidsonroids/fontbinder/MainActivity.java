package pl.droidsonroids.fontbinder;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

public class MainActivity extends Activity {
	@BindFont("a")
	TextView textA;

	@BindFont("b4")
	TextView textB;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MainActivityBinder.bind(this);
	}
}
