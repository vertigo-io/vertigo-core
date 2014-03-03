package io.vertigo.dynamo.task;

import io.vertigo.dynamo.task.model.TaskEngine;

/**
 * 
 * @author dchallas
 * $Id: TaskEngineMock.java,v 1.1 2013/07/10 15:45:32 npiedeloup Exp $
 */
public final class TaskEngineMock extends TaskEngine {
	/** entier 1. */
	public static final String ATTR_IN_INT_1 = "ATTR_IN_INT_1";
	/** entier 2. */
	public static final String ATTR_IN_INT_2 = "ATTR_IN_INT_2";
	/** entier 3. */
	public static final String ATTR_IN_INT_3 = "ATTR_IN_INT_3";
	/** Somme. */
	public static final String ATTR_OUT = "ATTR_OUT";

	private Integer getValue1() {
		return getValue(ATTR_IN_INT_1);
	}

	private Integer getValue2() {
		return getValue(ATTR_IN_INT_2);
	}

	private Integer getValue3() {
		return getValue(ATTR_IN_INT_3);
	}

	private void setOutput(final Integer result) {
		this.setValue(ATTR_OUT, result);
	}

	/** {@inheritDoc} */
	@Override
	public void execute() {
		final int outPut;
		if ("+".equals(this.getTaskDefinition().getRequest())) {
			outPut = getValue1() + getValue2() + getValue3();
		} else if ("*".equals(this.getTaskDefinition().getRequest())) {
			outPut = getValue1() * getValue2() * getValue3();
		} else {
			throw new IllegalArgumentException("Operateur non reconnu.");
		}

		setOutput(outPut);
	}

}
