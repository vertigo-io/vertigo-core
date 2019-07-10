package io.vertigo.commons.command;

import java.util.List;

import io.vertigo.core.component.Component;

public interface CommandManager extends Component {

	List<CommandDefinition> searchCommands(final String prefix);

	CommandDefinition findCommand(final String command);

	<P extends Object> CommandResponse<P> executeCommand(String command, String... commandParams);
}
