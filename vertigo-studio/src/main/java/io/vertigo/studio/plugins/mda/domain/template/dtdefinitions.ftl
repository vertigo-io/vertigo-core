package ${packageName};

import java.util.Arrays;
import java.util.Iterator;
import io.vertigo.dynamo.domain.metamodel.DtFieldName;

/**
 * Attention cette classe est générée automatiquement !
 */
public final class ${classSimpleName} implements Iterable<Class<?>> {

	/**
	 * Enumération des DtDefinitions.
	 */
	public enum Definitions {
<#list dtDefinitions as dtDefinition>
		/** Objet de données ${dtDefinition.classSimpleName}. */
		${dtDefinition.classSimpleName}(${dtDefinition.classCanonicalName}.class),
</#list>
		;

		private final Class<?> clazz;

		private Definitions(final Class<?> clazz) {
			this.clazz = clazz;
		}

		/** 
		 * Classe associée.
		 * @return Class d'implémentation de l'objet 
		 */
		public Class<?> getDtClass() {
			return clazz;
		}
	}

<#list dtDefinitions as dtDefinition>
	/**
	 * Enumération des champs de ${dtDefinition.classSimpleName}.
	 */
	public enum ${dtDefinition.classSimpleName}Fields implements DtFieldName<${dtDefinition.classCanonicalName}> {
		<#list dtDefinition.fields as dtField>
		/** Propriété '${dtField.label.display}'. */
		${dtField.getName()},
		</#list>
	}

</#list>
	/** {@inheritDoc} */
	@Override
	public Iterator<Class<?>> iterator() {
		return new Iterator<Class<?>>() {
			private Iterator<Definitions> it = Arrays.asList(Definitions.values()).iterator();

			/** {@inheritDoc} */
			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			/** {@inheritDoc} */
			@Override
			public Class<?> next() {
				return it.next().getDtClass();
			}

			/** {@inheritDoc} */
			@Override
			public void remove() {
				//unsupported
			}
		};
	}
}
