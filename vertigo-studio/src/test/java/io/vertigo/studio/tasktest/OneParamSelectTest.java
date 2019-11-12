package io.vertigo.studio.tasktest;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import io.vertigo.studio.data.tasktest.DaoTestClass;

public class OneParamSelectTest extends DaoTestClass {

	@Inject
	private DaoPAO daoPAO;

	@Test
	public void check_oneParamSelect_Ok() {
		check().semantics(() -> daoPAO.oneParamSelect(dum().dum(java.lang.Integer.class)));
	}
}
