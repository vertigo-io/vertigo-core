package io.vertigo.labs.france;

import io.vertigo.kernel.component.Manager;

import java.util.Collection;

/**
 * Some data about france. 
 * @author pchretien
 */
public interface FranceManager extends Manager {
	/**
	 * @return List of Regions
	 */
	Collection<Region> getRegions();

	/**
	 * @return list of "Départements"
	 */
	Collection<Departement> getDepartements();

	Region getRegion(String codeInsee);

	Departement getDepartement(String codeInsee);
}
