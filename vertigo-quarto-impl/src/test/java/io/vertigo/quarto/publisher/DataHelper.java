package io.vertigo.quarto.publisher;

import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.quarto.publisher.mock.Address;
import io.vertigo.quarto.publisher.mock.Enquete;
import io.vertigo.quarto.publisher.mock.Enqueteur;
import io.vertigo.quarto.publisher.mock.MisEnCause;
import io.vertigo.quarto.publisher.mock.Ville;

final class DataHelper {
	static Enquete createEnquete() {
		final Enquete enquete = new Enquete();
		enquete.setCodeEnquete("EN_C" + (int) (Math.random() * 100) + "_" + (int) (Math.random() * 100000));
		enquete.setEnqueteTerminee(Math.random() > 0.5);
		enquete.setFait("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur pretium urna pulvinar massa placerat imperdiet. Curabitur vestibulum dui eget nibh consequat eget ultrices velit iaculis. Ut justo ipsum, euismod nec pulvinar sit amet, consectetur in dui. Ut sed ligula ligula. Phasellus libero enim, congue nec volutpat dignissim, pulvinar luctus urna.\n\tLorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur pretium urna pulvinar massa placerat imperdiet. Curabitur vestibulum dui eget nibh consequat eget ultrices velit iaculis. Ut justo ipsum, euismod nec pulvinar sit amet, consectetur in dui. Ut sed ligula ligula. Phasellus libero enim, congue nec volutpat dignissim, pulvinar luctus urna.\n\t1-\tLorem ipsum dolor\n\t2-\tLorem ipsum dolor\n\t3-\tLorem ipsum dolor");
		enquete.setSiGrave(Math.random() > 0.5);
		return enquete;
	}

	static Enqueteur createEnqueteur() {
		final Enqueteur enqueteur = new Enqueteur();
		enqueteur.setNom("Durey");
		enqueteur.setPrenom("Matthieu");
		return enqueteur;
	}

	static Address createAdresse() {
		final Address address = new Address();
		address.setRue("12, Avenue General Leclerc");
		address.setVille(createVille());
		return address;
	}

	static Ville createVille() {
		final Ville ville = new Ville();
		ville.setNom("Paris");
		ville.setCodePostal("75020");
		return ville;
	}

	static DtList<? extends MisEnCause> createMisEnCauseList() {
		final DtList<MisEnCause> reportData = new DtList<>(MisEnCause.class);
		final int size = 20;
		for (int i = 0; i < size; i++) {
			reportData.add(createMisEnCause());
		}
		return reportData;
	}

	static MisEnCause createMisEnCause() {
		final MisEnCause misEnCause = new MisEnCause();
		misEnCause.setSiHomme(Math.random() > 0.5);
		misEnCause.setNom("Hibulaire");
		misEnCause.setPrenom("Pat");
		final DtList<Address> adresses = new DtList<>(Address.class);
		final int size = 5;
		for (int i = 0; i < size; i++) {
			adresses.add(createAdresse());
		}
		misEnCause.setAdressesConnues(adresses);
		return misEnCause;
	}
}
