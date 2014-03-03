package io.vertigo.dynamo.plugins.environment.loaders.poweramc.core;

import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.util.StringUtil;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParserFactory;

/**
 * Chargement d'un fichier OOM.
 * Seules les classes et leurs attributs ainsi que les associations sont extraites.
 * @author pchretien
 * @version $Id: LoaderOOM.java,v 1.6 2013/10/22 12:30:19 pchretien Exp $
 */
public final class LoaderOOM {
	private final Map<IdOOM, ObjectOOM> map;

	/**
	 * Constructeur.
	 * @param powerAMCURL URL du fichier PowerAMC
	 */
	public LoaderOOM(final URL powerAMCURL) {
		Assertion.checkNotNull(powerAMCURL);
		//----------------------------------------------------------------------
		map = new LinkedHashMap<>();
		final HandlerOOM handler = new HandlerOOM(map);
		try {
			SAXParserFactory.newInstance().newSAXParser().parse(powerAMCURL.openStream(), handler);
		} catch (final Exception e) {
			throw new VRuntimeException("erreur lors de la lecture du fichier oom : " + powerAMCURL, e);
		}
	}

	/**
	 * Récupération des classes déclarées dans l'OOM.
	 * @return Liste des classes
	 */
	public List<ClassOOM> getClassOOMList() {
		final List<ClassOOM> list = new ArrayList<>();
		for (final ObjectOOM obj : map.values()) {
			//On ne conserve que les classes et les domaines
			if (obj.getType() == TypeOOM.Class) {
				list.add(createClassOOM(obj));
			}
		}
		return java.util.Collections.unmodifiableList(list);
	}

	/**
	 * Récupération des associations déclarées dans l'OOM.
	 * @return Liste des associations
	 */
	public List<AssociationOOM> getAssociationOOMList() {
		final List<AssociationOOM> list = new ArrayList<>();
		for (final ObjectOOM obj : map.values()) {
			if (obj.getType() == TypeOOM.Association) {
				final AssociationOOM associationOOM = buildDynAssociation(obj);
				if (associationOOM != null) {
					list.add(associationOOM);
				}
			}
		}
		return java.util.Collections.unmodifiableList(list);
	}

	private ClassOOM createClassOOM(final ObjectOOM obj) {
		//On recherche les attributs (>DtField) de cet classe(>Dt_DEFINITION)
		final String code = obj.getCode();
		final String packageName = obj.getParent().getPackageName();
		//On recherche les PrimaryIdentifiers :
		//La class possède 
		//- une liste des identifiers qui référencent des champs
		//- un bloc <c:PrimaryIdentifier> (non parsé) qui référence les primaryIdentifiers
		//C'est pourquoi on a une double redirection
		final List<IdOOM> pkList = new ArrayList<>();
		for (final IdOOM ref : obj.getRefList()) {
			final ObjectOOM childRef = map.get(ref); //On recherche les references vers identifiers (ceux dans PrimaryIdentifier)
			if (childRef != null && childRef.getType() == TypeOOM.Identifier) {
				pkList.addAll(childRef.getRefList()); //On recherche les champs pointé par l'identifier				
			}
		}

		final List<AttributeOOM> keyAttributes = new ArrayList<>();
		final List<AttributeOOM> fieldAttributes = new ArrayList<>();
		for (final ObjectOOM child : obj.getChildList()) {
			if (child.getType() == TypeOOM.Attribute) {
				if (pkList.contains(child.getId())) {
					final AttributeOOM attributeOOm = createDynAttribute(child, true);
					keyAttributes.add(attributeOOm);
				} else {
					fieldAttributes.add(createDynAttribute(child, false));
				}
			}
		}
		return new ClassOOM(code, packageName, keyAttributes, fieldAttributes);
	}

	private AttributeOOM createDynAttribute(final ObjectOOM obj, final boolean isPK) {
		final String code = obj.getCode();
		final String label = obj.getLabel();
		final boolean persistent = !"0".equals(obj.getPersistent());

		final boolean notNull;
		if (isPK) {
			//La pk est toujours notNull
			notNull = true;
		} else {
			notNull = "1..1".equals(obj.getMultiplicity());
		}

		//Domain
		String domain = null;
		for (final IdOOM ref : obj.getRefList()) {
			final ObjectOOM childRef = map.get(ref);
			if (childRef != null && childRef.getType() == TypeOOM.Domain) {
				Assertion.checkState(domain == null, "domain deja affecté");
				domain = childRef.getCode();
			}
		}
		return new AttributeOOM(code, label, persistent, notNull, domain);
	}

	/**
	 * Création d'une association.
	 * @param obj ObjectOOM
	 * @return Association 
	 */
	private AssociationOOM buildDynAssociation(final ObjectOOM obj) {
		final String code = obj.getCode();
		final String packageName = obj.getParent().getPackageName();

		final String multiplicityA = obj.getRoleAMultiplicity();
		final String multiplicityB = obj.getRoleBMultiplicity();

		//On recherche les objets référencés par l'association.
		ObjectOOM objectB = null;
		ObjectOOM objectA = null;
		for (final IdOOM ref : obj.getRefList()) {
			final ObjectOOM childRef = map.get(ref);
			if (childRef != null && (childRef.getType() == TypeOOM.Class || childRef.getType() == TypeOOM.Shortcut)) {
				if (objectB == null) {
					objectB = childRef;
				} else if (objectA == null) {
					objectA = childRef;
				} else {
					throw new IllegalStateException("objectB and objectA can't be null at the same time.");
				}
			}
		}

		if (objectA == null || objectB == null) {
			throw new IllegalArgumentException("Noeuds de l'association introuvables");
		}
		//Si les roles ne sont pas renseignés ont prend le nom de la table en CamelCase.
		final String roleLabelA = obj.getRoleALabel() != null ? obj.getRoleALabel() : StringUtil.constToCamelCase(objectA.getName(), true);
		final String roleLabelB = obj.getRoleBLabel() != null ? obj.getRoleBLabel() : StringUtil.constToCamelCase(objectB.getName(), true);
		// Si il n'existe pas de libelle pour un role donné alors on utilise le nom de l'objet référencé.
		//Le code du role est déduit du libellé.

		//Attention pamc inverse dans oom les déclarations des objets !!
		final String codeA = objectA.getCode();
		final String codeB = objectB.getCode();

		// associationDefinition.
		//On recherche les attributs (>DtField) de cet classe(>Dt_DEFINITION)

		// navigabilités sont optionnelles; elles sont déduites de la multiplicités quand elles ne sont pas renseignées
		final boolean navigabilityA = obj.getRoleANavigability() == null ? false : obj.getRoleANavigability();
		final boolean navigabilityB = obj.getRoleBNavigability() == null ? true : obj.getRoleBNavigability();

		return new AssociationOOM(code, packageName, multiplicityA, multiplicityB, roleLabelA, roleLabelB, codeA, codeB, navigabilityA, navigabilityB);
	}
}
