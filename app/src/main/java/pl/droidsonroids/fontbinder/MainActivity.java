package pl.droidsonroids.fontbinder;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.EditText;
import android.widget.TextView;

import java.lang.reflect.Field;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends Activity {
	@Inject
	Object dummy;

	@BindFont(value = "test.ttf", bold = true)
	@BindView(R.id.label)
	TextView label;

	@BindFont("b.ttf")
	@BindView(R.id.editText)
	EditText editText;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);
		MainActivity_FontBinder.bind(this);
	}

	private void readBindViewReflectively() {
		for (Field field : getClass().getDeclaredFields()) {
			BindView bindView = field.getAnnotation(BindView.class);
			if (bindView != null) {
				int idRes = bindView.value();
			}
		}
	}

}
