package io.vertigo.labs.gedcom;

import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.kernel.component.Manager;

public interface GedcomManager extends Manager {
	DtList<Individual> getAllIndividuals();

	//Collection<Family> getFamilies();

	DtList<Individual> getChildren(Individual individual);

}
