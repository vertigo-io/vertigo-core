package io.vertigo.labs.geocoder;

import io.vertigo.kernel.lang.Assertion;

/**
 * Objet permettant de d�finir un emplacement par ses coordonn�es.
 * @author spoitrenaud
 */
public final class GeoLocation {
	public static final GeoLocation UNDEFINED = new GeoLocation();
	private final String countryCode;
	private final String level1;//r�gion  
	private final String level2; //d�partement
	private final String locality;

	private final double latitude;
	private final double longitude;

	//	private final String accuracy;

	/**
	 * Constructeur public
	 * Notamment utilis� pour la d�s�rialisation.
	 */
	public GeoLocation() {
		latitude = Float.NaN;
		longitude = Float.NaN;
		//		accuracy = null;
		this.countryCode = null;
		this.level1 = null;
		this.level2 = null;
		this.locality = null;
	}

	public GeoLocation(final double latitude, final double longitude) {
		this(latitude, longitude, null, null, null, null);
	}

	public GeoLocation(final double latitude, final double longitude, String countryCode, String level1, String level2, String locality) {
		//	Assertion.notEmpty(accuracy);
		Assertion.checkNotNull(latitude);
		Assertion.checkNotNull(longitude);
		Assertion.checkArgument((!Double.isNaN(latitude) && !Double.isNaN(longitude)), "UNDEFINDED or defined");
		//----------------------------------------------------------------
		//		this.accuracy = accuracy;
		this.latitude = latitude;
		this.longitude = longitude;
		this.countryCode = countryCode;
		this.level1 = level1;
		this.level2 = level2;
		this.locality = locality;
	}

	/**
	 * @return Latitude du r�sultat
	 */
	public double getLatitude() {
		Assertion.checkArgument(!Double.isNaN(latitude), "Utiliser isUndefined");
		//---------------------------------------------------------------------
		return latitude;
	}

	/**
	 * @return Longitude du r�sultat
	 */
	public double getLongitude() {
		Assertion.checkArgument(!Double.isNaN(longitude), "Utiliser isUndefined");
		return longitude;
	}

	public boolean isUndefined() {
		return Double.isNaN(latitude);
	}

	public String getLocality() {
		return locality;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public String getLevel1() {
		return level1;
	}

	public String getLevel2() {
		return level2;
	}

	@Override
	public String toString() {
		if (Double.isNaN(latitude)) {
			return "UNDEFINDED";
		}
		return "lat:" + latitude + " ; lng:" + longitude;
	}
}
