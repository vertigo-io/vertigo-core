package io.vertigo.commons.command;

import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.AbstractTestCaseJU5;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.app.config.NodeConfig;
import io.vertigo.commons.CommonsFeatures;
import io.vertigo.commons.command.data.TestCommand;

public class CommandManagerTest extends AbstractTestCaseJU5 {

	@Override
	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.addModule(new CommonsFeatures()
						.withCommand()
						.build())
				.addModule(ModuleConfig.builder("commands-test")
						//---Services
						.addComponent(TestCommand.class)
						.build())
				.build();
	}

	@Inject
	private CommandManager commandManager;

	@Test
	public void testCommandDefinition() {
		final List<CommandDefinition> availableCommands = commandManager.searchCommands("/t/r");
		Assertions.assertFalse(availableCommands.isEmpty());
	}

	@Test
	public void testReplyCommandDefinition() {
		final CommandDefinition command = commandManager.findCommand("/t/repeat");
		Assertions.assertEquals("/t/repeat", command.getCommand());
	}

	@Test
	public void testReplyCommand() {
		final CommandResponse<String> commandResponse = commandManager.executeCommand("/t/repeat", "something");
		Assertions.assertEquals(CommandResponseStatus.OK, commandResponse.getStatus());
		Assertions.assertEquals("something", commandResponse.getPayload());
	}

}
