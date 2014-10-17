package io.vertigo.dynamock.domain;

import java.util.Arrays;
import java.util.Iterator;
import io.vertigo.dynamo.domain.metamodel.DtFieldName;

/**
 * Attention cette classe est générée automatiquement !
 */
public final class DtDefinitions implements Iterable<Class<?>> {
	
	/**
	 * Enumération des DtDefinitions.
	 */
	public enum Definitions {
			/** Objet de données Car. */
			Car(io.vertigo.dynamock.domain.car.Car.class), //
			/** Objet de données Famille. */
			Famille(io.vertigo.dynamock.domain.famille.Famille.class), //
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

	/**
	 * Enumération des champs de Car.
	 */
	public enum CarFields implements DtFieldName {
		/** Propriété 'identifiant de la voiture'. */
		ID, //
		/** Propriété 'Constructeur'. */
		MAKE, //
		/** Propriété 'ModÃ¨le'. */
		MODEL, //
		/** Propriété 'Descriptif'. */
		DESCRIPTION, //
		/** Propriété 'AnnÃ©e'. */
		YEAR, //
		/** Propriété 'KilomÃ©trage'. */
		KILO, //
		/** Propriété 'Prix'. */
		PRICE, //
		/** Propriété 'Type de moteur'. */
		MOTOR_TYPE, //
	}

	/**
	 * Enumération des champs de Famille.
	 */
	public enum FamilleFields implements DtFieldName {
		/** Propriété 'identifiant de la famille'. */
		FAM_ID, //
		/** Propriété 'Libelle'. */
		LIBELLE, //
		/** Propriété 'Libelle'. */
		DESCRIPTION, //
	}

	    
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
