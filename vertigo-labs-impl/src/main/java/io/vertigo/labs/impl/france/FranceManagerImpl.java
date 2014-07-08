package io.vertigo.labs.impl.france;

import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.labs.france.Departement;
import io.vertigo.labs.france.FranceManager;
import io.vertigo.labs.france.Region;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author pchretien
 */
public final class FranceManagerImpl implements FranceManager {
	private final Map<String, Region> regions;
	private final Map<String, Departement> departements;

	public FranceManagerImpl() {
		try {
			regions = Collections.unmodifiableMap(loadRegions());
			departements = Collections.unmodifiableMap(loadDepartements());
		} catch (IOException e) {
			throw new VRuntimeException(e);
		}
	}

	private static Map<String, Region> loadRegions() throws IOException {
		final Map<String, Region> tmpRegions = new LinkedHashMap<>();

		try (InputStream inputStream = FranceManagerImpl.class.getResourceAsStream("reg2012.txt")) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			String line;
			boolean first = true;
			while ((line = reader.readLine()) != null) {
				//On laisse la premi�re ligne
				if (first) {
					first = false;
				} else {
					String[] tokens = line.split("\t");
					Assertion.checkArgument(tokens.length == 5, "txt mal form�");
					String code = tokens[0];
					String label = tokens[4];
					Region region = new Region(code, label);
					tmpRegions.put(code, region);
				}
			}
		}
		return tmpRegions;
	}

	private Map<String, Departement> loadDepartements() throws IOException {
		final Map<String, Departement> tmpDepartements = new LinkedHashMap<>();

		try (InputStream inputStream = FranceManagerImpl.class.getResourceAsStream("depts2012.txt")) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			String line;
			boolean first = true;
			while ((line = reader.readLine()) != null) {
				//On laisse la premi�re ligne
				if (first) {
					first = false;
				} else {
					String[] tokens = line.split("\t");
					Assertion.checkArgument(tokens.length == 6, "txt mal form� pour la ligne {0}, tokens:{1}", line, tokens.length);
					String code = tokens[1];
					String label = tokens[5];
					String codeRegion = tokens[0];
					Departement departement = new Departement(code, label, getRegion(codeRegion));
					tmpDepartements.put(code, departement);
				}
			}
		}
		return tmpDepartements;
	}

	public Collection<Region> getRegions() {
		return regions.values();
	}

	public Collection<Departement> getDepartements() {
		return departements.values();
	}

	@Override
	public Region getRegion(String codeInsee) {
		Region region = regions.get(codeInsee);
		Assertion.checkNotNull(region, "code insee non reconnu :{0}", codeInsee);
		return region;
	}

	@Override
	public Departement getDepartement(String codeInsee) {
		Departement departement = departements.get(codeInsee);
		Assertion.checkNotNull(departement);
		return departement;
	}

}
