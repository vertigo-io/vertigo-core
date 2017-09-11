package io.vertigo.database.sql.mapper;

import java.util.List;

import io.vertigo.database.impl.sql.SqlAdapterSupplierPlugin;
import io.vertigo.database.sql.data.Mail;
import io.vertigo.util.ListBuilder;

public final class MailAdapterSupplierPlugin implements SqlAdapterSupplierPlugin {

	@Override
	public List<SqlAdapter> getAdapters() {
		return new ListBuilder<SqlAdapter>()
				.add(new SqlAdapter<Mail, String>() {
					@Override
					public Mail toJava(final String sqlValue) {
						return sqlValue == null ? null : new Mail(sqlValue);
					}

					@Override
					public String toSql(final Mail mail) {
						return mail != null ? mail.toString() : null;
					}

					@Override
					public Class<Mail> getJavaDataType() {
						return Mail.class;
					}

					@Override
					public Class<String> getSqlDataType() {
						return String.class;
					}
				}).build();
	}
}
