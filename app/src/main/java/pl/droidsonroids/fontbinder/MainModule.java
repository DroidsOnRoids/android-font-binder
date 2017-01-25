package pl.droidsonroids.fontbinder;

import dagger.Module;
import dagger.Provides;

@Module
class MainModule {

	@Provides
	public Object provideDummy() {
		return null;
	}
}
