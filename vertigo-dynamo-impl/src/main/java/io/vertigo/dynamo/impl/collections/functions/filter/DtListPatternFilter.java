package io.vertigo.dynamo.impl.collections.functions.filter;

import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.impl.collections.functions.filter.DtListPatternFilterUtil.FilterPattern;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;

import java.io.Serializable;

/**
 * Filtre de DtList prenant en entrée un String qui doit respecter certains patterns.
 * Syntaxes acceptées :
 * FIELD_NAME:VALUE => FilterByValue.
 * 
 * FIELD_NAME:[MINVALUE TO MAXVALUE]
 * - Le min et max doivent être du même type.
 * - Le caractère * peut être utiliser pour indiquer qu'il n'y a pas de borne max ou min.
 * - Les accolades sont ouvrantes ou fermantes pour indiquer si la valeur est comprise ou non
 * 
 * @author npiedeloup
 * @version $Id: DtListPatternFilter.java,v 1.5 2014/01/20 17:45:43 pchretien Exp $
 * @param <D> Type d'objet
 */
public final class DtListPatternFilter<D extends DtObject> implements DtListFilter<D>, Serializable {

	private static final long serialVersionUID = 6282972172196740177L;

	private final FilterPattern filterPattern;
	private final String[] parsedFilter;
	private DtListFilter<D> subDtListFilter;

	/**
	 * Constructeur.
	 * @param filterString Chaine représentant le filtre
	 */
	public DtListPatternFilter(final String filterString) {
		Assertion.checkArgNotEmpty(filterString);
		//----------------------------------------------------------------------
		FilterPattern foundFilterPattern = null;
		String[] foundParsedFilter = null;

		//On test les patterns dans l'ordre
		for (final FilterPattern filterPatternTemp : FilterPattern.values()) {
			final Option<String[]> parsedFilterOption = DtListPatternFilterUtil.parseFilter(filterString, filterPatternTemp.getPattern());
			if (parsedFilterOption.isDefined()) {
				foundFilterPattern = filterPatternTemp;
				foundParsedFilter = parsedFilterOption.get();
				break;
			}
		}
		//On passe par des objets intermédiaires pour permettre le 'final' sur les attributs de la class
		Assertion.checkArgument(foundFilterPattern != null && foundParsedFilter != null, "La chaine de filtrage ne respecte pas la syntaxe.\nFiltre: {0}.", filterString);
		this.filterPattern = foundFilterPattern;
		this.parsedFilter = foundParsedFilter;
	}

	/** {@inheritDoc} */
	public boolean accept(final D dto) {
		if (subDtListFilter == null) {
			subDtListFilter = DtListPatternFilterUtil.createDtListFilterForPattern(filterPattern, parsedFilter, DtObjectUtil.findDtDefinition(dto));
		}
		return subDtListFilter.accept(dto);
	}
}
