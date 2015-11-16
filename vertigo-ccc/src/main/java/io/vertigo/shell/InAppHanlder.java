package io.vertigo.shell;

import java.io.IOException;

public interface InAppHanlder {
	void handleUserInput(final String line) throws IOException;
}
