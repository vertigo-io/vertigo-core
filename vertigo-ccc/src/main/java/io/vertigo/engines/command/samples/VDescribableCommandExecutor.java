package io.vertigo.engines.command.samples;

import io.vertigo.kernel.Home;
import io.vertigo.kernel.command.VCommand;
import io.vertigo.kernel.command.VCommandExecutor;
import io.vertigo.kernel.component.ComponentInfo;
import io.vertigo.kernel.component.Describable;
import io.vertigo.kernel.lang.Assertion;

import java.util.List;

public final class VDescribableCommandExecutor implements VCommandExecutor<List<ComponentInfo>> {
	public List<ComponentInfo> exec(VCommand command) {
		Assertion.checkNotNull(command);
		//Assertion.checkArgument(command.getName());
		System.out.println(">>> find:" + command.getName());
		System.out.println(">>> Home:" + Home.getComponentSpace().keySet());
		//---------------------------------------------------------------------
		Object component = Home.getComponentSpace().resolve(command.getName(), Object.class);

		//			if (component instanceof Describable) {
		return Describable.class.cast(component).getInfos();
		//		}
	}
}
