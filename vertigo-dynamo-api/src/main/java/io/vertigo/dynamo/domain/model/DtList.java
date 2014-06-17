package io.vertigo.dynamo.domain.model;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.metamodel.DefinitionReference;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe de stockage des listes.
 * Une dtList est une liste constituée avec un seul type d'objet.
 * Les objets null ne sont pas autorisés.
 *
 * @author  fconstantin
 * @param <D> Type du DtObject
 */
public final class DtList<D extends DtObject> extends AbstractList<D> implements Serializable {
	private static final long serialVersionUID = -8059200549636099190L;

	private DtListURI uri;

	/** Reéférence vers la Définition. */
	private DefinitionReference<DtDefinition> dtDefinitionRef;

	/** List des dto contenus. */
	private final List<D> dtObjects = new ArrayList<>();

	/**
	 * Constructeur.
	 * @param dtDefinition Définition de DT
	 */
	public DtList(final DtDefinition dtDefinition) {
		this.dtDefinitionRef = new DefinitionReference<>(dtDefinition);
	}

	/**
	 * Constructeur.
	 * @param dtObjectClass Type d'objet
	 */
	public DtList(final Class<? extends DtObject> dtObjectClass) {
		this(DtObjectUtil.findDtDefinition(dtObjectClass));
	}

	/** {@inheritDoc} */
	@Override
	public D get(final int row) {
		return dtObjects.get(row);
	}

	/** {@inheritDoc} */
	@Override
	public D set(final int row, final D object) {
		//Implementation de set, pour que la collection soit modifiable 
		//Et donc pour que le Collections.sort(List<?> ) fonctionne
		return dtObjects.set(row, object);
	}

	/** {@inheritDoc} */
	@Override
	public int size() {
		return dtObjects.size();
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		final StringBuilder buf = new StringBuilder();
		buf.append("(def=").append(getDefinition()).append(", size=").append(dtObjects.size());
		if (dtObjects.size() > 50) {
			buf.append(" show only the 50 firsts");
		}
		buf.append(")\n");
		for (int i = 0; i < Math.min(dtObjects.size(), 50); i++) { //pas plus de 50 elements dans le toString
			buf.append("\tRow #").append(i).append(" : ");
			buf.append(get(i)).append('\n');
		}

		return buf.toString();
	}

	//==========================================================================

	/** {@inheritDoc} */
	@Override
	public boolean add(final D dto) {
		Assertion.checkNotNull(dto);
		final DtDefinition foundDtDefinition = DtObjectUtil.findDtDefinition(dto);
		Assertion.checkArgument(getDefinition().equals(foundDtDefinition), "Ne peut pas inserer un dto '{0}' dans une collection '{1}'", foundDtDefinition, getDefinition());
		//---------------------------------------------------------------------
		return dtObjects.add(dto);
	}

	/** {@inheritDoc} */
	@Override
	public D remove(final int row) {
		return dtObjects.remove(row);
	}

	/** {@inheritDoc} */
	@Override
	public List<D> subList(final int start, final int end) {
		throw new UnsupportedOperationException();
	}

	//==========================================================================
	//================================ Méthodes supplémentaires=================
	//==========================================================================

	/**
	 * @return Définition de la liste.
	 */
	public DtDefinition getDefinition() {
		return dtDefinitionRef.get();
	}

	/**
	 * @return URI de la ressource
	 */
	public DtListURI getURI() {
		/*
		 * if (uri == null) {
		 * uri = broker.Helper.createURI(this);
		 * }
		 */
		return uri;
	}

	/**
	 * Définit l'uri de la collection.
	 * @param newUri DtListURI
	 */
	public void setURI(final DtListURI newUri) {
		if (this.uri == null) {
			this.uri = newUri;
		} else {
			throw new IllegalAccessError("URI déjà fixée");
		}
	}
}
