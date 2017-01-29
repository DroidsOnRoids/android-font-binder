package pl.droidsonroids.fontbinder;

import android.graphics.Typeface;

class ValidActivity_FontBinder {
  static void bind(ValidActivity target) {
    target.testTextView.setTypeface(Typeface.createFromAsset(target.getAssets(), "test.ttf"));
  }
}
