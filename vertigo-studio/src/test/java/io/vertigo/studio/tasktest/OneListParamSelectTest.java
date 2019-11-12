package io.vertigo.studio.tasktest;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import io.vertigo.studio.data.tasktest.DaoTestClass;

public class OneListParamSelectTest extends DaoTestClass {

	@Inject
	private DaoPAO daoPAO;

	@Test
	public void check_oneListParamSelect_Ok() {
		check().semantics(() -> daoPAO.oneListParamSelect(dum().dumList(java.lang.Integer.class)));
	}
}
