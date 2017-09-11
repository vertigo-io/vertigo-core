package io.vertigo.database.impl.sql;

import java.util.List;

import io.vertigo.core.component.Plugin;
import io.vertigo.database.sql.mapper.SqlAdapter;

public interface SqlAdapterSupplierPlugin extends Plugin {

	List<SqlAdapter> getAdapters();
}
