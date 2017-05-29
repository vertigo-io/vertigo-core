package io.vertigo.studio.plugins.mda.webservice.model;

public class WebServiceInitializerModelTs {

	private final String jsFileName;
	private final String simpleClassName;

	public WebServiceInitializerModelTs(final String jsFileName, final String simpleClassName) {
		this.simpleClassName = simpleClassName;
		this.jsFileName = jsFileName;
	}

	public String getJsFileName() {
		return jsFileName;
	}

	public String getJsConstName() {
		return simpleClassName;
	}

}
