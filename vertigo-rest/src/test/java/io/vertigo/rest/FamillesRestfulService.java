package io.vertigo.rest;

import io.vertigo.dynamock.domain.famille.Famille;
import io.vertigo.kernel.exception.VUserException;
import io.vertigo.kernel.lang.MessageText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

//basé sur http://www.restapitutorial.com/lessons/httpmethods.html
//et javax.ws.rs : http://docs.oracle.com/javaee/6/api/index.html?javax/ws/rs/package-summary.html

//@Path("familles")
public final class FamillesRestfulService implements RestfulService {

	private final Map<Long, Famille> familles = new HashMap<Long, Famille>();

	public FamillesRestfulService() {
		appendFamille(1L, "Martin");
		appendFamille(2L, "Dubois");
		appendFamille(3L, "Petit");
		appendFamille(4L, "Durant");
		appendFamille(5L, "Leroy");
		appendFamille(6L, "Moreau");
		appendFamille(7L, "Lefebvre");
		appendFamille(8L, "Garcia");
		appendFamille(9L, "Roux");
		appendFamille(10L, "Fournier");
	}

	private void appendFamille(final long famId, final String label) {
		final Famille famille = new Famille();
		famille.setFamId(famId);
		famille.setLibelle(label);
		familles.put(famId, famille);
	}

	@GET("/familles")
	public List<Famille> readList() {
		//offset + range ?
		//code 200
		return new ArrayList<Famille>(familles.values());
	}

	@GET("/familles/{famId}")
	public Famille read(@PathParam("famId") final long famId) {
		final Famille famille = familles.get(famId);
		if (famille == null) {
			//404 ?
			throw new VUserException(new MessageText("Famille n°" + famId + " inconnue", null));
		}
		//200
		return famille;
	}

	//@POST is non-indempotent
	@POST("/familles")
	public long insert(final Famille famille) {
		if (famille.getFamId() != null) {
			throw new VUserException(new MessageText("Famille n°" + famille.getFamId() + " déjà crée", null));
		}
		if (famille.getLibelle() == null || famille.getLibelle().isEmpty()) {
			throw new VUserException(new MessageText("Libelle obligatoire", null));
		}
		final long nextId = getNextId();
		famille.setFamId(nextId);
		familles.put(nextId, famille);
		//code 201 + location header : GET route
		return nextId;
	}

	//PUT is indempotent : ID obligatoire ?
	@PUT("/familles")
	public void update(final Famille famille) {
		if (famille.getLibelle() == null || famille.getLibelle().isEmpty()) {
			//400
			throw new VUserException(new MessageText("Libelle obligatoire", null));
		}
		if (famille.getFamId() != null) {
			familles.put(famille.getFamId(), famille);
			//200
		} else {
			final long nextId = getNextId();// si on crée l'id, nous ne sommes plus idempotent
			famille.setFamId(nextId);
			familles.put(nextId, famille);
			//201
		}
	}

	@DELETE("/familles/{famId}")
	public void delete(@PathParam("famId") final long famId) {
		if (!familles.containsKey(famId)) {
			//404
			throw new VUserException(new MessageText("Famille n°" + famId + " inconnue", null));
		}
		if (famId < 5) {
			//401
			throw new VUserException(new MessageText("Vous n'avez pas les droits nécessaires", null));
		}
		familles.remove(famId);
	}

	private long getNextId() {
		final long nextId = UUID.randomUUID().getMostSignificantBits();
		if (familles.containsKey(nextId)) {
			return getNextId();
		}
		return nextId;
	}
}
