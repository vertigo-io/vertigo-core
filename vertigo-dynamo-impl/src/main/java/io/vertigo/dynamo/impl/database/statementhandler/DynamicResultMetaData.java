package io.vertigo.dynamo.impl.database.statementhandler;

import io.vertigo.dynamo.database.vendor.SQLMapping;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtDefinitionBuilder;
import io.vertigo.dynamo.domain.metamodel.Formatter;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.kernel.Home;
import io.vertigo.kernel.lang.Assertion;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
* Gestion dynamiques des DTO et DTC en sortie.
* Dans le cas des selects avec un type de sortie générique (DTO ou DTC) ;
* il convient de fabriquer dynamiquement, à la volée les DT et DTO, DTC en sortie.
*
* @author  pchretien
*/
final class DynamicResultMetaData implements ResultMetaData {
	private final boolean isDtObject;
	private final SerializableDtDefinition serializableDefinition;

	/**
	 * Constructeur.
	 */
	DynamicResultMetaData(final boolean isDtObject, final SQLMapping mapping, final ResultSet resultSet) throws SQLException {
		Assertion.checkNotNull(mapping);
		Assertion.checkNotNull(resultSet);
		//-----------------------------------------------------------------
		this.isDtObject = isDtObject;
		serializableDefinition = createSerializableDtDefinition(mapping, resultSet);
	}

	/** {@inheritDoc} */
	public DtObject createDtObject() {
		return new DynamicDtObject(serializableDefinition);
	}

	/** {@inheritDoc} */
	public boolean isDtObject() {
		return isDtObject;
	}

	/** {@inheritDoc} */
	public DtDefinition getDtDefinition() {
		return serializableDefinition.getDtDefinition();
	}

	//=========================================================================
	//==============Construction de SerializableDtDefinition===================
	//=========================================================================

	private static SerializableDtDefinition createSerializableDtDefinition(final SQLMapping mapping, final ResultSet resultSet) throws SQLException {
		final ResultSetMetaData metaData = resultSet.getMetaData();
		String fieldName;
		String fieldLabel;
		DataType localDataType;
		final SerializableDtField[] fields = new SerializableDtField[metaData.getColumnCount()];
		for (int i = 1; i <= metaData.getColumnCount(); i++) {
			//On passe les champs en maj pour postgreSQL et SQLServer.
			fieldName = metaData.getColumnName(i).toUpperCase();
			//On vérifie que la colonne possède un nom signifiant
			Assertion.checkArgNotEmpty(fieldName, "Une des colonnes de la requête ne possède ni nom ni alias.");
			//-----------------------------------------------------------------
			fieldLabel = metaData.getColumnLabel(i);
			localDataType = mapping.getDataType(metaData.getColumnType(i));
			fields[i - 1] = new SerializableDtField(fieldName, fieldLabel, localDataType);
		}
		return new SerializableDtDefinition(fields);
	}

	/**
	 * Classe interne décrivant les champs d'une définition.
	 * Permet de serialiser une DT qui par nature n'est pas sérialisable.
	 * @author pchretien
	 */
	private static class SerializableDtField implements Serializable {
		private static final long serialVersionUID = 7086269816597674149L;
		final String name;
		final String label;
		final DataType dataType;

		SerializableDtField(final String fieldName, final String fieldLabel, final DataType dataType) {
			Assertion.checkNotNull(fieldName);
			Assertion.checkNotNull(fieldLabel);
			Assertion.checkNotNull(dataType);
			//-----------------------------------------------------------------
			name = fieldName;
			label = fieldLabel;
			this.dataType = dataType;
		}
	}

	static class SerializableDtDefinition implements Serializable {
		private static final String DT_DYNAMIC = "DT_DYNAMIC_DTO";
		//Map des domaines correspondants aux types primitifs
		private static final Map<DataType, Domain> DOMAIN_MAP = createDomainMap();

		private static final long serialVersionUID = -423652372994923330L;
		private final SerializableDtField[] fields;
		private transient DtDefinition dtDefinition;

		SerializableDtDefinition(final SerializableDtField[] fields) {
			Assertion.checkNotNull(fields);
			//-----------------------------------------------------------------
			this.fields = fields;
		}

		public synchronized DtDefinition getDtDefinition() {
			//synchronizer, car lasy loading
			if (dtDefinition == null) {
				final DtDefinitionBuilder dtDefinitionBuilder = new DtDefinitionBuilder(DT_DYNAMIC)//
						.withPersistent(false)//
						.withDynamic(true);

				for (final SerializableDtField field : fields) {
					//On considére le champ nullable et non persistent
					dtDefinitionBuilder.withDataField(field.name, field.label, getDomain(field.dataType), false, false, false, false);
				}
				dtDefinition = dtDefinitionBuilder.build();
			}
			return dtDefinition;
		}

		private static Map<DataType, Domain> createDomainMap() {
			final DataType[] dataTypes = DataType.values();
			final Map<DataType, Domain> map = new HashMap<>(dataTypes.length);
			//Initialisation de la map.
			final Formatter formatter = Home.getDefinitionSpace().resolve(Formatter.FMT_DEFAULT, Formatter.class);
			for (final DataType dataType : dataTypes) {
				final Domain newDomain = new Domain("DO_DYN", dataType, formatter);
				map.put(dataType, newDomain);
			}
			return map;
		}

		private static Domain getDomain(final DataType dataType) {
			final Domain domain = DOMAIN_MAP.get(dataType);
			Assertion.checkNotNull(domain);
			return domain;
		}
	}

	public boolean isPrimitive() {
		return false;
	}
}
//
//
//static DtObject createDtObject(final SerializableDtDefinition serializableDefinition) {
//	final InvocationHandler proxy = new DtObjectProxy(serializableDefinition);
//	return ClassUtil.newProxyInstance(DynamicDtObject.class.getClassLoader(), new Class<?>[] { DtObject.class }, proxy);
//}
//
////>>>>>A Supprimer 
//static DtList<DtObject> createDtList(final SerializableDtDefinition serializableDefinition) {
//	final InvocationHandler proxy = new DtListProxy(serializableDefinition);
//	return ClassUtil.newProxyInstance(DynamicDtObject.class.getClassLoader(), new Class<?>[] { DtList.class }, proxy);
//}
//
////<<<<<
////
////	static DtList<DtObject> createDtList(final SerializableDtDefinition serializableDefinition) {
////		final InvocationHandler proxy = new DtListProxy(serializableDefinition);
////		return ClassUtil.newProxyInstance(DynamicDtObject.class.getClassLoader(), new Class<?>[] { DtList.class }, proxy);
////	}
//
//private abstract static class Proxy<X extends Serializable> implements InvocationHandler, Serializable {
//	private static final long serialVersionUID = 3293356087603800037L;
//	private final SerializableDtDefinition serializableDefinition;
//	private final Serializable obj;
//
//	Proxy(final SerializableDtDefinition serializableDefinition, final X obj) {
//		Assertion.notNull(serializableDefinition);
//		Assertion.notNull(obj);
//		//-----------------------------------------------------------------
//		this.serializableDefinition = serializableDefinition;
//		this.obj = obj;
//	}
//
//	/** {@inheritDoc} */
//	public final Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
//		if ("getDefinition".equals(method.getName())) {
//			return serializableDefinition.getDtDefinition();
//		}
//		return ClassUtil.invoke(obj, method, args);
//	}
//}
//
//private static class DtObjectProxy extends Proxy<DtObject> {
//	private static final long serialVersionUID = -7739264524038869379L;
//
//	DtObjectProxy(final SerializableDtDefinition serializableDefinition) {
//		super(serializableDefinition, new DynamicDtObject(serializableDefinition));
//	}
//}
//
////>>>>>Je pense que l'on peut supprimer.
//private static class DtListProxy extends Proxy<DtList<DtObject>> {
//	private static final long serialVersionUID = -8457613639266451664L;
//
//	DtListProxy(final SerializableDtDefinition serializableDefinition) {
//		super(serializableDefinition, new DtList<DtObject>(serializableDefinition.getDtDefinition()));
//	}
//}
//
////
////	private static class DtListProxy extends Proxy<DtList<DtObject>> {
////		private static final long serialVersionUID = -8457613639266451664L;
////
////		DtListProxy(final SerializableDtDefinition serializableDefinition) {
////			super(serializableDefinition, new DtList(serializableDefinition.getDtDefinition()));
////		}
////	}
////<<<<<<<<<
