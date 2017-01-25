package pl.droidsonroids.fontbinder;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.EditText;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.Component;

public class MainActivity extends Activity {
	@Inject
	Object dummy;

	@BindFont("a")
	@BindView(R.id.label)
	TextView label;

	@BindFont("b")
	@BindView(R.id.editText)
	EditText editText;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ButterKnife.bind(this);
	}

}
