package io.vertigo.publisher.impl.merger.script;

/**
 * Contexte des Tag KScript.
 * @author pchretien, npiedeloup
 * @version $Id: ScriptContext.java,v 1.1 2013/07/11 13:24:48 npiedeloup Exp $
 */
public interface ScriptContext {
	String pop();

	String peek();

	void push(String element);

	boolean empty();
}
