package io.vertigo.studio.plugins.mda.webservice.model;

/**
 * FreeMarker Model of WebServiceInitializer.
 * @author npiedeloup
 */
public class WebServiceInitializerModelTs {

	private final String jsFileName;
	private final String simpleClassName;

	/**
	 * @param jsFileName
	 * @param simpleClassName
	 */
	public WebServiceInitializerModelTs(final String jsFileName, final String simpleClassName) {
		this.simpleClassName = simpleClassName;
		this.jsFileName = jsFileName;
	}

	/**
	 * @return js FileName
	 */
	public String getJsFileName() {
		return jsFileName;
	}

	/**
	 * @return js class name
	 */
	public String getJsConstName() {
		return simpleClassName;
	}

}
