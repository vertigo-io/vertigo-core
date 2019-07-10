package io.vertigo.commons.command;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vertigo.core.definition.Definition;
import io.vertigo.core.definition.DefinitionPrefix;
import io.vertigo.lang.Assertion;
import io.vertigo.util.StringUtil;

@DefinitionPrefix("Cmd")
public final class CommandDefinition implements Definition {

	private static final Pattern COMMAND_PATTERN = Pattern.compile("(\\/[a-zA-Z0-9]+)+");

	private final String name;
	private final String command;
	private final String description;
	private final Function<Object[], CommandResponse> action;
	private final List<CommandParam> commandParams;

	public CommandDefinition(
			final String command,
			final String description,
			final List<CommandParam> commandParams,
			final Function<Object[], CommandResponse> action) {
		Assertion.checkArgNotEmpty(command);
		Assertion.checkArgNotEmpty(description);
		Assertion.checkState(COMMAND_PATTERN.matcher(command).matches(), "handle '{0}' must respect the pattern '{1}'", command, COMMAND_PATTERN);
		Assertion.checkNotNull(action);
		commandParams
				.forEach(commandParam -> {
					final Type type = commandParam.getType();
					if (type instanceof Class) {
						Assertion.checkState(String.class.isAssignableFrom((Class) type), "Only ParamUID and String params are allowed for command");
					} else if (type instanceof ParameterizedType) {
						Assertion.checkState(GenericUID.class.isAssignableFrom((Class) ((ParameterizedType) type).getRawType()), "Only ParamUID and String params are allowed for command");
					}
				});

		//---
		this.command = command;
		this.description = description;
		this.commandParams = commandParams;
		final String[] commandParts = command.substring(1).split("\\/"); // we remove the first slash
		name = "Cmd"
				+ StringUtil.first2UpperCase(commandParts[0])
				+ Stream.of(commandParts).skip(1).collect(Collectors.joining("$"));
		this.action = action;
	}

	@Override
	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getCommand() {
		return command;
	}

	public List<CommandParam> getParams() {
		return commandParams;
	}

	public Function<Object[], CommandResponse> getAction() {
		return action;
	}

}
