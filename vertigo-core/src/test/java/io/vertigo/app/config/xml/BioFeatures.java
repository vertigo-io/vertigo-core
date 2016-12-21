package io.vertigo.app.config.xml;

import io.vertigo.app.config.Features;
import io.vertigo.app.config.Param;
import io.vertigo.core.spaces.component.data.BioManager;
import io.vertigo.core.spaces.component.data.BioManagerImpl;
import io.vertigo.core.spaces.component.data.MathManager;
import io.vertigo.core.spaces.component.data.MathManagerImpl;
import io.vertigo.core.spaces.component.data.MathPlugin;

/**
 * A feature for the Bio Module.
 * @author mlaroche
 *
 */
public class BioFeatures extends Features {

	public BioFeatures() {
		super("bio");
	}

	@Override
	protected void buildFeatures() {
		getModuleConfigBuilder()
				.addComponent(BioManager.class, BioManagerImpl.class)
				.addComponent(MathManager.class, MathManagerImpl.class, Param.create("start", "100"))
				.addPlugin(MathPlugin.class, Param.create("factor", "20"));
	}

}
