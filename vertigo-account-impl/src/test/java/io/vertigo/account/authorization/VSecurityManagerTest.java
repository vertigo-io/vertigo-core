/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.account.authorization;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.AbstractTestCaseJU5;
import io.vertigo.account.authorization.SecurityNames.GlobalAuthorizations;
import io.vertigo.account.authorization.SecurityNames.RecordAuthorizations;
import io.vertigo.account.authorization.SecurityNames.RecordOperations;
import io.vertigo.account.authorization.metamodel.Authorization;
import io.vertigo.account.authorization.metamodel.AuthorizationName;
import io.vertigo.account.authorization.metamodel.Role;
import io.vertigo.account.authorization.model.Record;
import io.vertigo.account.data.TestUserSession;
import io.vertigo.account.security.UserSession;
import io.vertigo.account.security.VSecurityManager;
import io.vertigo.app.config.NodeConfig;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.database.impl.sql.vendor.postgresql.PostgreSqlDataBase;
import io.vertigo.database.sql.vendor.SqlDialect;
import io.vertigo.dynamo.criteria.CriteriaCtx;
import io.vertigo.lang.Tuples.Tuple2;

/**
 * @author pchretien
 */
public final class VSecurityManagerTest extends AbstractTestCaseJU5 {

	private static final long DEFAULT_REG_ID = 1L;
	private static final long DEFAULT_DEP_ID = 2L;
	private static final long DEFAULT_COM_ID = 3L;
	private static final long DEFAULT_UTI_ID = 1000L;
	private static final long DEFAULT_TYPE_ID = 10L;
	private static final double DEFAULT_MONTANT_MAX = 100d;

	private long currentDosId = 1;

	@Inject
	private VSecurityManager securityManager;

	@Inject
	private AuthorizationManager authorizationManager;

	@Override
	protected NodeConfig buildNodeConfig() {
		return MyNodeConfig.config();
	}

	@Test
	public void testCreateUserSession() {
		final UserSession userSession = securityManager.createUserSession();
		Assertions.assertEquals(Locale.FRANCE, userSession.getLocale());
		Assertions.assertEquals(TestUserSession.class, userSession.getClass());
	}

	@Test
	public void testInitCurrentUserSession() {
		final UserSession userSession = securityManager.createUserSession();
		try {
			securityManager.startCurrentUserSession(userSession);
			Assertions.assertTrue(securityManager.getCurrentUserSession().isPresent());
			Assertions.assertEquals(userSession, securityManager.getCurrentUserSession().get());
		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testAuthenticate() {
		final UserSession userSession = securityManager.createUserSession();
		Assertions.assertFalse(userSession.isAuthenticated());
		userSession.authenticate();
	}

	@Test
	public void testNoUserSession() {
		final Optional<UserSession> userSession = securityManager.getCurrentUserSession();
		Assertions.assertFalse(userSession.isPresent());
	}

	@Test
	public void testResetUserSession() {
		final UserSession userSession = securityManager.createUserSession();
		try {
			securityManager.startCurrentUserSession(userSession);
			Assertions.assertTrue(securityManager.getCurrentUserSession().isPresent());
			//
			authorizationManager.obtainUserAuthorizations().clearSecurityKeys();
			authorizationManager.obtainUserAuthorizations().clearAuthorizations();
			authorizationManager.obtainUserAuthorizations().clearRoles();
		} finally {
			securityManager.stopCurrentUserSession();
		}
		Assertions.assertFalse(securityManager.getCurrentUserSession().isPresent());
	}

	@Test
	public void testRole() {
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		final Role admin = definitionSpace.resolve("R_ADMIN", Role.class);
		Assertions.assertTrue("R_ADMIN".equals(admin.getName()));
		final Role secretary = definitionSpace.resolve("R_SECRETARY", Role.class);
		Assertions.assertTrue("R_SECRETARY".equals(secretary.getName()));
	}

	@Test
	public void testAccess() {
		//TODO
	}

	@Test
	public void testNotAuthorized() {
		//TODO
	}

	@Test
	public void testToString() {
		final Authorization admUsr = getAuthorization(GlobalAuthorizations.ATZ_ADMUSR);
		admUsr.toString();
		final Authorization admPro = getAuthorization(GlobalAuthorizations.ATZ_ADMPRO);
		admPro.toString();
		/*Pour la couverture de code, et 35min de dette technique.... */
	}

	@Test
	public void testAuthorized() {
		final Authorization admUsr = getAuthorization(GlobalAuthorizations.ATZ_ADMUSR);
		final Authorization admPro = getAuthorization(GlobalAuthorizations.ATZ_ADMPRO);

		final UserSession userSession = securityManager.<TestUserSession> createUserSession();
		try {
			securityManager.startCurrentUserSession(userSession);
			authorizationManager.obtainUserAuthorizations().withSecurityKeys("utiId", DEFAULT_UTI_ID)
					.withSecurityKeys("typId", DEFAULT_TYPE_ID)
					.withSecurityKeys("montantMax", DEFAULT_MONTANT_MAX)
					.addAuthorization(admUsr)
					.addAuthorization(admPro);

			Assertions.assertTrue(authorizationManager.hasAuthorization(GlobalAuthorizations.ATZ_ADMUSR));
			Assertions.assertTrue(authorizationManager.hasAuthorization(GlobalAuthorizations.ATZ_ADMPRO));
			Assertions.assertFalse(authorizationManager.hasAuthorization(GlobalAuthorizations.ATZ_ADMAPP));
		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testAuthorizedOnEntity() {

		final Record record = createRecord();

		final Record recordTooExpensive = createRecord();
		recordTooExpensive.setAmount(10000d);

		final Record recordOtherUser = createRecord();
		recordOtherUser.setUtiIdOwner(2000L);

		final Record recordOtherUserAndTooExpensive = createRecord();
		recordOtherUserAndTooExpensive.setUtiIdOwner(2000L);
		recordOtherUserAndTooExpensive.setAmount(10000d);

		final Authorization recordRead = getAuthorization(RecordAuthorizations.ATZ_RECORD$READ);
		final UserSession userSession = securityManager.<TestUserSession> createUserSession();
		try {
			securityManager.startCurrentUserSession(userSession);
			authorizationManager.obtainUserAuthorizations().withSecurityKeys("utiId", DEFAULT_UTI_ID)
					.withSecurityKeys("typId", DEFAULT_TYPE_ID)
					.withSecurityKeys("montantMax", DEFAULT_MONTANT_MAX)
					.addAuthorization(recordRead);

			final boolean canReadRecord = authorizationManager.hasAuthorization(RecordAuthorizations.ATZ_RECORD$READ);
			Assertions.assertTrue(canReadRecord);

			//read -> MONTANT<=${montantMax} or UTI_ID_OWNER=${utiId}
			Assertions.assertTrue(authorizationManager.isAuthorized(record, RecordOperations.READ));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordTooExpensive, RecordOperations.READ));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordOtherUser, RecordOperations.READ));
			Assertions.assertFalse(authorizationManager.isAuthorized(recordOtherUserAndTooExpensive, RecordOperations.READ));

			Assertions.assertFalse(Arrays.asList("READ", "READ2", "READ3").retainAll(authorizationManager.getAuthorizedOperations(record)));
			Assertions.assertFalse(Arrays.asList("READ", "READ2", "READ3").retainAll(authorizationManager.getAuthorizedOperations(recordTooExpensive)));
			Assertions.assertFalse(Arrays.asList("READ", "READ2", "READ3").retainAll(authorizationManager.getAuthorizedOperations(recordOtherUser)));
			Assertions.assertFalse(Arrays.asList("READ", "READ2", "READ3").retainAll(authorizationManager.getAuthorizedOperations(recordOtherUserAndTooExpensive)));

		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testPredicateOnEntity() {

		final Record record = createRecord();

		final Record recordTooExpensive = createRecord();
		recordTooExpensive.setAmount(10000d);

		final Record recordOtherUser = createRecord();
		recordOtherUser.setUtiIdOwner(2000L);

		final Record recordOtherUserAndTooExpensive = createRecord();
		recordOtherUserAndTooExpensive.setUtiIdOwner(2000L);
		recordOtherUserAndTooExpensive.setAmount(10000d);

		final Authorization recordRead = getAuthorization(RecordAuthorizations.ATZ_RECORD$READ);
		final UserSession userSession = securityManager.<TestUserSession> createUserSession();
		try {
			securityManager.startCurrentUserSession(userSession);
			authorizationManager.obtainUserAuthorizations().withSecurityKeys("utiId", DEFAULT_UTI_ID)
					.withSecurityKeys("typId", DEFAULT_TYPE_ID)
					.withSecurityKeys("montantMax", DEFAULT_MONTANT_MAX)
					.withSecurityKeys("geo", new Long[] { DEFAULT_REG_ID, DEFAULT_DEP_ID, null }) //droit sur tout un département
					.addAuthorization(recordRead).addAuthorization(recordRead)
					.addAuthorization(getAuthorization(RecordAuthorizations.ATZ_RECORD$TEST))
					.addAuthorization(getAuthorization(RecordAuthorizations.ATZ_RECORD$TEST2))
					.addAuthorization(getAuthorization(RecordAuthorizations.ATZ_RECORD$TEST3))
					.addAuthorization(getAuthorization(RecordAuthorizations.ATZ_RECORD$WRITE))
					.addAuthorization(getAuthorization(RecordAuthorizations.ATZ_RECORD$CREATE))
					.addAuthorization(getAuthorization(RecordAuthorizations.ATZ_RECORD$DELETE));

			final boolean canReadRecord = authorizationManager.hasAuthorization(RecordAuthorizations.ATZ_RECORD$READ);
			Assertions.assertTrue(canReadRecord);

			final Predicate<Record> readRecordPredicate = authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.READ).toPredicate();
			//read -> MONTANT<=${montantMax} or UTI_ID_OWNER=${utiId}
			Assertions.assertTrue(readRecordPredicate.test(record));
			Assertions.assertTrue(readRecordPredicate.test(recordTooExpensive));
			Assertions.assertTrue(readRecordPredicate.test(recordOtherUser));
			Assertions.assertFalse(readRecordPredicate.test(recordOtherUserAndTooExpensive));

			Assertions.assertEquals("(AMOUNT <= #AMOUNT_0# OR UTI_ID_OWNER = #UTI_ID_OWNER_1#)", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.READ).toString());
			Assertions.assertEquals("(AMOUNT <= #AMOUNT_0# OR UTI_ID_OWNER = #UTI_ID_OWNER_1#)", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.READ2).toString());
			Assertions.assertEquals("((AMOUNT <= #AMOUNT_0# AND (UTI_ID_OWNER is null or UTI_ID_OWNER != #UTI_ID_OWNER_1# )) OR (AMOUNT > #AMOUNT_0# AND UTI_ID_OWNER = #UTI_ID_OWNER_1#))", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.READ3).toString());
			Assertions.assertEquals("false", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.READ_HP).toString());
			Assertions.assertEquals("((UTI_ID_OWNER = #UTI_ID_OWNER_0# AND ETA_CD in ('CRE', 'VAL', 'PUB', 'NOT', 'REA')) OR (TYP_ID = #TYP_ID_1# AND AMOUNT > #AMOUNT_2# AND AMOUNT <= #AMOUNT_3# AND ETA_CD in ('CRE', 'VAL', 'PUB', 'NOT', 'REA')))", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.WRITE).toString());
			Assertions.assertEquals("(TYP_ID = #TYP_ID_0# AND AMOUNT <= #AMOUNT_1#)", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.CREATE).toString());
			Assertions.assertEquals("(TYP_ID = #TYP_ID_0# OR (UTI_ID_OWNER = #UTI_ID_OWNER_1# AND ETA_CD in ('CRE', 'VAL')))", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.DELETE).toString());

			Assertions.assertEquals("(((UTI_ID_OWNER is null or UTI_ID_OWNER != #UTI_ID_OWNER_0# ) AND (AMOUNT < #AMOUNT_1# OR AMOUNT = #AMOUNT_1# OR AMOUNT <= #AMOUNT_1#)) OR (UTI_ID_OWNER = #UTI_ID_OWNER_0# AND (AMOUNT > #AMOUNT_1# OR AMOUNT = #AMOUNT_1# OR AMOUNT >= #AMOUNT_1#)))", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.TEST).toString());
			Assertions.assertEquals("(ETA_CD in ('CRE', 'VAL', 'PUB') OR ETA_CD in ('CRE', 'VAL', 'PUB') OR ETA_CD = #ETA_CD_0# OR (ETA_CD is null or ETA_CD != #ETA_CD_0# ) OR ETA_CD in ('PUB', 'NOT', 'REA', 'ARC') OR ETA_CD in ('PUB', 'NOT', 'REA', 'ARC'))", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.TEST2).toString());
			Assertions.assertEquals("((((((REG_ID = #REG_ID_0# AND DEP_ID = #DEP_ID_1# AND COM_ID is not null) OR (REG_ID = #REG_ID_0# AND DEP_ID = #DEP_ID_1#)) OR (REG_ID = #REG_ID_0# AND DEP_ID = #DEP_ID_1# AND COM_ID is null )) OR ((REG_ID is null or REG_ID != #REG_ID_0# ) AND (DEP_ID is null or DEP_ID != #DEP_ID_1# ) AND COM_ID is not null )) OR (REG_ID = #REG_ID_0# AND DEP_ID is null AND COM_ID is null)) OR (REG_ID = #REG_ID_0# AND (DEP_ID is null OR DEP_ID = #DEP_ID_1#) AND COM_ID is null))", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.TEST3).toString());

			final boolean canReadNotify = authorizationManager.hasAuthorization(RecordAuthorizations.ATZ_RECORD$NOTIFY);
			Assertions.assertFalse(canReadNotify);
			Assertions.assertEquals("false", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.NOTIFY).toString());

		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testSecuritySqlOnEntity() {

		final Record recordTooExpensive = createRecord();
		recordTooExpensive.setAmount(10000d);

		final Record recordOtherUser = createRecord();
		recordOtherUser.setUtiIdOwner(2000L);

		final Record recordOtherUserAndTooExpensive = createRecord();
		recordOtherUserAndTooExpensive.setUtiIdOwner(2000L);
		recordOtherUserAndTooExpensive.setAmount(10000d);

		final Authorization recordRead = getAuthorization(RecordAuthorizations.ATZ_RECORD$READ);
		final UserSession userSession = securityManager.<TestUserSession> createUserSession();
		try {
			securityManager.startCurrentUserSession(userSession);
			authorizationManager.obtainUserAuthorizations().withSecurityKeys("utiId", DEFAULT_UTI_ID)
					.withSecurityKeys("typId", DEFAULT_TYPE_ID)
					.withSecurityKeys("montantMax", DEFAULT_MONTANT_MAX)
					.withSecurityKeys("geo", new Long[] { DEFAULT_REG_ID, DEFAULT_DEP_ID, null }) //droit sur tout un département
					.addAuthorization(recordRead).addAuthorization(recordRead)
					.addAuthorization(getAuthorization(RecordAuthorizations.ATZ_RECORD$TEST))
					.addAuthorization(getAuthorization(RecordAuthorizations.ATZ_RECORD$TEST2))
					.addAuthorization(getAuthorization(RecordAuthorizations.ATZ_RECORD$TEST3))
					.addAuthorization(getAuthorization(RecordAuthorizations.ATZ_RECORD$WRITE))
					.addAuthorization(getAuthorization(RecordAuthorizations.ATZ_RECORD$CREATE))
					.addAuthorization(getAuthorization(RecordAuthorizations.ATZ_RECORD$DELETE));

			final boolean canReadRecord = authorizationManager.hasAuthorization(RecordAuthorizations.ATZ_RECORD$READ);
			Assertions.assertTrue(canReadRecord);

			final SqlDialect sqlDialect = new PostgreSqlDataBase().getSqlDialect();
			final Tuple2<String, CriteriaCtx> readRecordSql = authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.READ).toSql(sqlDialect);
			//read -> MONTANT<=${montantMax} or UTI_ID_OWNER=${utiId}
			Assertions.assertEquals("(AMOUNT <= #AMOUNT_0# OR UTI_ID_OWNER = #UTI_ID_OWNER_1#)", readRecordSql.getVal1());
			Assertions.assertEquals(100.0, readRecordSql.getVal2().getAttributeValue("AMOUNT_0"));
			Assertions.assertEquals(1000L, readRecordSql.getVal2().getAttributeValue("UTI_ID_OWNER_1"));

			Assertions.assertEquals("(AMOUNT <= #AMOUNT_0# OR UTI_ID_OWNER = #UTI_ID_OWNER_1#)", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.READ).toSql(sqlDialect).getVal1());
			Assertions.assertEquals("(AMOUNT <= #AMOUNT_0# OR UTI_ID_OWNER = #UTI_ID_OWNER_1#)", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.READ2).toSql(sqlDialect).getVal1());
			Assertions.assertEquals("((AMOUNT <= #AMOUNT_0# AND (UTI_ID_OWNER is null or UTI_ID_OWNER != #UTI_ID_OWNER_1# )) OR (AMOUNT > #AMOUNT_0# AND UTI_ID_OWNER = #UTI_ID_OWNER_1#))", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.READ3).toSql(sqlDialect).getVal1());
			Assertions.assertEquals("0=1", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.READ_HP).toSql(sqlDialect).getVal1());
			Assertions.assertEquals("((UTI_ID_OWNER = #UTI_ID_OWNER_0# AND ETA_CD in ('CRE', 'VAL', 'PUB', 'NOT', 'REA')) OR (TYP_ID = #TYP_ID_1# AND AMOUNT > #AMOUNT_2# AND AMOUNT <= #AMOUNT_3# AND ETA_CD in ('CRE', 'VAL', 'PUB', 'NOT', 'REA')))", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.WRITE).toSql(sqlDialect).getVal1());
			Assertions.assertEquals("(TYP_ID = #TYP_ID_0# AND AMOUNT <= #AMOUNT_1#)", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.CREATE).toSql(sqlDialect).getVal1());
			Assertions.assertEquals("(TYP_ID = #TYP_ID_0# OR (UTI_ID_OWNER = #UTI_ID_OWNER_1# AND ETA_CD in ('CRE', 'VAL')))", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.DELETE).toSql(sqlDialect).getVal1());

			Assertions.assertEquals("(((UTI_ID_OWNER is null or UTI_ID_OWNER != #UTI_ID_OWNER_0# ) AND (AMOUNT < #AMOUNT_1# OR AMOUNT = #AMOUNT_1# OR AMOUNT <= #AMOUNT_1#)) OR (UTI_ID_OWNER = #UTI_ID_OWNER_0# AND (AMOUNT > #AMOUNT_1# OR AMOUNT = #AMOUNT_1# OR AMOUNT >= #AMOUNT_1#)))", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.TEST).toSql(sqlDialect).getVal1());
			Assertions.assertEquals("(ETA_CD in ('CRE', 'VAL', 'PUB') OR ETA_CD in ('CRE', 'VAL', 'PUB') OR ETA_CD = #ETA_CD_0# OR (ETA_CD is null or ETA_CD != #ETA_CD_0# ) OR ETA_CD in ('PUB', 'NOT', 'REA', 'ARC') OR ETA_CD in ('PUB', 'NOT', 'REA', 'ARC'))", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.TEST2).toSql(sqlDialect).getVal1());
			//TODO
			Assertions.assertEquals("((((((REG_ID = #REG_ID_0# AND DEP_ID = #DEP_ID_1# AND COM_ID is not null) OR (REG_ID = #REG_ID_0# AND DEP_ID = #DEP_ID_1#)) OR (REG_ID = #REG_ID_0# AND DEP_ID = #DEP_ID_1# AND COM_ID is null )) OR ((REG_ID is null or REG_ID != #REG_ID_0# ) AND (DEP_ID is null or DEP_ID != #DEP_ID_1# ) AND COM_ID is not null )) OR (REG_ID = #REG_ID_0# AND DEP_ID is null AND COM_ID is null)) OR (REG_ID = #REG_ID_0# AND (DEP_ID is null OR DEP_ID = #DEP_ID_1#) AND COM_ID is null))", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.TEST3).toSql(sqlDialect).getVal1());

			final boolean canReadNotify = authorizationManager.hasAuthorization(RecordAuthorizations.ATZ_RECORD$NOTIFY);
			Assertions.assertFalse(canReadNotify);
			Assertions.assertEquals("0=1", authorizationManager.getCriteriaSecurity(Record.class, RecordOperations.NOTIFY).toSql(sqlDialect).getVal1());

		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testSecuritySearchOnEntity() {

		final Record recordTooExpensive = createRecord();
		recordTooExpensive.setAmount(10000d);

		final Record recordOtherUser = createRecord();
		recordOtherUser.setUtiIdOwner(2000L);

		final Record recordOtherUserAndTooExpensive = createRecord();
		recordOtherUserAndTooExpensive.setUtiIdOwner(2000L);
		recordOtherUserAndTooExpensive.setAmount(10000d);

		final Authorization recordRead = getAuthorization(RecordAuthorizations.ATZ_RECORD$READ);
		final UserSession userSession = securityManager.<TestUserSession> createUserSession();
		try {
			securityManager.startCurrentUserSession(userSession);
			authorizationManager.obtainUserAuthorizations().withSecurityKeys("utiId", DEFAULT_UTI_ID)
					.withSecurityKeys("typId", DEFAULT_TYPE_ID)
					.withSecurityKeys("montantMax", DEFAULT_MONTANT_MAX)
					.withSecurityKeys("geo", new Long[] { DEFAULT_REG_ID, DEFAULT_DEP_ID, null }) //droit sur tout un département
					.addAuthorization(recordRead)
					.addAuthorization(getAuthorization(RecordAuthorizations.ATZ_RECORD$TEST))
					.addAuthorization(getAuthorization(RecordAuthorizations.ATZ_RECORD$TEST2))
					.addAuthorization(getAuthorization(RecordAuthorizations.ATZ_RECORD$TEST3))
					.addAuthorization(getAuthorization(RecordAuthorizations.ATZ_RECORD$READ_HP))
					.addAuthorization(getAuthorization(RecordAuthorizations.ATZ_RECORD$WRITE))
					.addAuthorization(getAuthorization(RecordAuthorizations.ATZ_RECORD$CREATE))
					.addAuthorization(getAuthorization(RecordAuthorizations.ATZ_RECORD$DELETE));

			final boolean canReadRecord = authorizationManager.hasAuthorization(RecordAuthorizations.ATZ_RECORD$READ);
			Assertions.assertTrue(canReadRecord);

			//read -> MONTANT<=${montantMax} or UTI_ID_OWNER=${utiId}
			Assertions.assertEquals("(+AMOUNT:<=100.0) (+UTI_ID_OWNER:1000)", authorizationManager.getSearchSecurity(Record.class, RecordOperations.READ));
			Assertions.assertEquals("(AMOUNT:<=100.0 UTI_ID_OWNER:1000)", authorizationManager.getSearchSecurity(Record.class, RecordOperations.READ2));
			Assertions.assertEquals("((+AMOUNT:<=100.0 -UTI_ID_OWNER:1000) (+AMOUNT:>100.0 +UTI_ID_OWNER:1000))", authorizationManager.getSearchSecurity(Record.class, RecordOperations.READ3));
			Assertions.assertEquals("(*:*)", authorizationManager.getSearchSecurity(Record.class, RecordOperations.READ_HP));
			Assertions.assertEquals("(+UTI_ID_OWNER:1000 +ETA_CD:('CRE' 'VAL' 'PUB' 'NOT' 'REA')) (+TYP_ID:10 +AMOUNT:>0 +AMOUNT:<=100.0 +ETA_CD:('CRE' 'VAL' 'PUB' 'NOT' 'REA'))", authorizationManager.getSearchSecurity(Record.class, RecordOperations.WRITE));
			Assertions.assertEquals("(+TYP_ID:10 +AMOUNT:<=100.0)", authorizationManager.getSearchSecurity(Record.class, RecordOperations.CREATE));
			Assertions.assertEquals("(+TYP_ID:10) (+UTI_ID_OWNER:1000 +ETA_CD:('CRE' 'VAL'))", authorizationManager.getSearchSecurity(Record.class, RecordOperations.DELETE));

			Assertions.assertEquals("((-UTI_ID_OWNER:1000 +(AMOUNT:<100.0 AMOUNT:100.0 AMOUNT:<=100.0)) (+UTI_ID_OWNER:1000 +(AMOUNT:>100.0 AMOUNT:100.0 AMOUNT:>=100.0)))", authorizationManager.getSearchSecurity(Record.class, RecordOperations.TEST));
			Assertions.assertEquals("(ETA_CD:('CRE' 'VAL' 'PUB') ETA_CD:('CRE' 'VAL' 'PUB') ETA_CD:'PUB' -ETA_CD:'PUB' ETA_CD:('PUB' 'NOT' 'REA' 'ARC') ETA_CD:('PUB' 'NOT' 'REA' 'ARC'))", authorizationManager.getSearchSecurity(Record.class, RecordOperations.TEST2));
			//GEO<${geo} OR GEO<=${geo} OR GEO=${geo} OR GEO!=${geo} OR GEO>${geo} OR GEO>=${geo}
			Assertions.assertEquals("((+REG_ID:1 +DEP_ID:2 +_exists_:COM_ID) (+REG_ID:1 +DEP_ID:2) (+REG_ID:1 +DEP_ID:2 -_exists_:COM_ID) (-REG_ID:1 -DEP_ID:2 _exists_:COM_ID) (+REG_ID:1 -_exists_:DEP_ID -_exists_:COM_ID) (+REG_ID:1 +(DEP_ID:2 -_exists_:DEP_ID) -_exists_:COM_ID))", authorizationManager.getSearchSecurity(Record.class, RecordOperations.TEST3));

			final boolean canReadNotify = authorizationManager.hasAuthorization(RecordAuthorizations.ATZ_RECORD$NOTIFY);
			Assertions.assertFalse(canReadNotify);
			Assertions.assertEquals("", authorizationManager.getSearchSecurity(Record.class, RecordOperations.NOTIFY));
		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testAuthorizedOnEntityGrant() {
		final Record record = createRecord();

		final Record recordTooExpensive = createRecord();
		recordTooExpensive.setAmount(10000d);

		final Record recordOtherUser = createRecord();
		recordOtherUser.setUtiIdOwner(2000L);

		final Record recordOtherUserAndTooExpensive = createRecord();
		recordOtherUserAndTooExpensive.setUtiIdOwner(2000L);
		recordOtherUserAndTooExpensive.setAmount(10000d);

		final Record recordArchivedNotWriteable = createRecord();
		recordArchivedNotWriteable.setEtaCd("ARC");

		final Authorization recordCreate = getAuthorization(RecordAuthorizations.ATZ_RECORD$CREATE);
		final UserSession userSession = securityManager.<TestUserSession> createUserSession();
		try {
			securityManager.startCurrentUserSession(userSession);
			authorizationManager.obtainUserAuthorizations()
					.withSecurityKeys("utiId", DEFAULT_UTI_ID)
					.withSecurityKeys("typId", DEFAULT_TYPE_ID)
					.withSecurityKeys("montantMax", DEFAULT_MONTANT_MAX)
					.addAuthorization(recordCreate);

			final boolean canCreateRecord = authorizationManager.hasAuthorization(RecordAuthorizations.ATZ_RECORD$CREATE);
			Assertions.assertTrue(canCreateRecord);

			//read -> MONTANT<=${montantMax} or UTI_ID_OWNER=${utiId}
			Assertions.assertTrue(authorizationManager.isAuthorized(record, RecordOperations.READ));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordTooExpensive, RecordOperations.READ));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordOtherUser, RecordOperations.READ));
			Assertions.assertFalse(authorizationManager.isAuthorized(recordOtherUserAndTooExpensive, RecordOperations.READ));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordArchivedNotWriteable, RecordOperations.READ));

			//create -> TYP_ID=${typId} and MONTANT<=${montantMax}
			Assertions.assertTrue(authorizationManager.isAuthorized(record, RecordOperations.CREATE));
			Assertions.assertFalse(authorizationManager.isAuthorized(recordTooExpensive, RecordOperations.CREATE));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordOtherUser, RecordOperations.CREATE));
			Assertions.assertFalse(authorizationManager.isAuthorized(recordOtherUserAndTooExpensive, RecordOperations.CREATE));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordArchivedNotWriteable, RecordOperations.CREATE));

		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testAuthorizedOnEntityOverride() {
		final Record record = createRecord();

		final Record recordTooExpensive = createRecord();
		recordTooExpensive.setAmount(10000d);

		final Record recordOtherUser = createRecord();
		recordOtherUser.setUtiIdOwner(2000L);

		final Record recordOtherUserAndTooExpensive = createRecord();
		recordOtherUserAndTooExpensive.setUtiIdOwner(2000L);
		recordOtherUserAndTooExpensive.setAmount(10000d);

		final Authorization recordRead = getAuthorization(RecordAuthorizations.ATZ_RECORD$READ_HP);
		final UserSession userSession = securityManager.<TestUserSession> createUserSession();
		try {
			securityManager.startCurrentUserSession(userSession);
			authorizationManager.obtainUserAuthorizations()
					.withSecurityKeys("utiId", DEFAULT_UTI_ID)
					.withSecurityKeys("typId", DEFAULT_TYPE_ID)
					.withSecurityKeys("montantMax", DEFAULT_MONTANT_MAX)
					.addAuthorization(recordRead);

			final boolean canReadRecord = authorizationManager.hasAuthorization(RecordAuthorizations.ATZ_RECORD$READ_HP);
			Assertions.assertTrue(canReadRecord);

			//read -> MONTANT<=${montantMax} or UTI_ID_OWNER=${utiId}
			Assertions.assertTrue(authorizationManager.isAuthorized(record, RecordOperations.READ));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordTooExpensive, RecordOperations.READ));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordOtherUser, RecordOperations.READ));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordOtherUserAndTooExpensive, RecordOperations.READ));

		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testAuthorizedOnEntityEnumAxes() {
		final Record record = createRecord();

		final Record recordTooExpensive = createRecord();
		recordTooExpensive.setAmount(10000d);

		final Record recordOtherUser = createRecord();
		recordOtherUser.setUtiIdOwner(2000L);

		final Record recordOtherUserAndTooExpensive = createRecord();
		recordOtherUserAndTooExpensive.setUtiIdOwner(2000L);
		recordOtherUserAndTooExpensive.setAmount(10000d);

		final Record recordArchivedNotWriteable = createRecord();
		recordArchivedNotWriteable.setEtaCd("ARC");

		final Authorization recordWrite = getAuthorization(RecordAuthorizations.ATZ_RECORD$WRITE);
		final UserSession userSession = securityManager.<TestUserSession> createUserSession();
		try {
			securityManager.startCurrentUserSession(userSession);
			authorizationManager.obtainUserAuthorizations()
					.withSecurityKeys("utiId", DEFAULT_UTI_ID)
					.withSecurityKeys("typId", DEFAULT_TYPE_ID)
					.withSecurityKeys("montantMax", DEFAULT_MONTANT_MAX)
					.addAuthorization(recordWrite);

			final boolean canReadRecord = authorizationManager.hasAuthorization(RecordAuthorizations.ATZ_RECORD$WRITE);
			Assertions.assertTrue(canReadRecord);

			//read -> MONTANT<=${montantMax} or UTI_ID_OWNER=${utiId}
			Assertions.assertTrue(authorizationManager.isAuthorized(record, RecordOperations.READ));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordTooExpensive, RecordOperations.READ));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordOtherUser, RecordOperations.READ));
			Assertions.assertFalse(authorizationManager.isAuthorized(recordOtherUserAndTooExpensive, RecordOperations.READ));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordArchivedNotWriteable, RecordOperations.READ));

			//write -> (UTI_ID_OWNER=${utiId} and ETA_CD<ARC) or (TYP_ID=${typId} and MONTANT<=${montantMax} and ETA_CD<ARC)
			Assertions.assertTrue(authorizationManager.isAuthorized(record, RecordOperations.WRITE));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordTooExpensive, RecordOperations.WRITE));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordOtherUser, RecordOperations.WRITE));
			Assertions.assertFalse(authorizationManager.isAuthorized(recordOtherUserAndTooExpensive, RecordOperations.WRITE));
			Assertions.assertFalse(authorizationManager.isAuthorized(recordArchivedNotWriteable, RecordOperations.WRITE));

		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testAuthorizedOnEntityTreeAxes() {
		final Record record = createRecord();
		record.setEtaCd("PUB");

		final Record recordOtherType = createRecord();
		recordOtherType.setEtaCd("PUB");
		recordOtherType.setTypId(11L);

		final Record recordOtherEtat = createRecord();
		recordOtherEtat.setEtaCd("CRE");

		final Record recordOtherUser = createRecord();
		recordOtherUser.setEtaCd("PUB");
		recordOtherUser.setUtiIdOwner(2000L);

		final Record recordOtherUserAndTooExpensive = createRecord();
		recordOtherUserAndTooExpensive.setEtaCd("PUB");
		recordOtherUserAndTooExpensive.setUtiIdOwner(2000L);
		recordOtherUserAndTooExpensive.setAmount(10000d);

		final Record recordOtherCommune = createRecord();
		recordOtherCommune.setEtaCd("PUB");
		recordOtherCommune.setComId(3L);

		final Record recordDepartement = createRecord();
		recordDepartement.setEtaCd("PUB");
		recordDepartement.setComId(null);

		final Record recordOtherDepartement = createRecord();
		recordOtherDepartement.setEtaCd("PUB");
		recordOtherDepartement.setDepId(10L);
		recordOtherDepartement.setComId(null);

		final Record recordRegion = createRecord();
		recordRegion.setEtaCd("PUB");
		recordRegion.setDepId(null);
		recordRegion.setComId(null);

		final Record recordNational = createRecord();
		recordNational.setEtaCd("PUB");
		recordNational.setRegId(null);
		recordNational.setDepId(null);
		recordNational.setComId(null);

		final Authorization recordNotify = getAuthorization(RecordAuthorizations.ATZ_RECORD$NOTIFY);
		final UserSession userSession = securityManager.<TestUserSession> createUserSession();
		try {
			securityManager.startCurrentUserSession(userSession);
			authorizationManager.obtainUserAuthorizations()
					.withSecurityKeys("utiId", DEFAULT_UTI_ID)
					.withSecurityKeys("typId", DEFAULT_TYPE_ID)
					.withSecurityKeys("montantMax", DEFAULT_MONTANT_MAX)
					.withSecurityKeys("geo", new Long[] { DEFAULT_REG_ID, DEFAULT_DEP_ID, null }) //droit sur tout un département
					.addAuthorization(recordNotify);

			Assertions.assertTrue(authorizationManager.hasAuthorization(RecordAuthorizations.ATZ_RECORD$NOTIFY));

			//grant read -> MONTANT<=${montantMax} or UTI_ID_OWNER=${utiId}
			Assertions.assertTrue(authorizationManager.isAuthorized(record, RecordOperations.READ));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordOtherUser, RecordOperations.READ));
			Assertions.assertFalse(authorizationManager.isAuthorized(recordOtherUserAndTooExpensive, RecordOperations.READ));
			//grant read2 -> MONTANT<=${montantMax} or UTI_ID_OWNER=${utiId}
			Assertions.assertTrue(authorizationManager.isAuthorized(record, RecordOperations.READ2));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordOtherUser, RecordOperations.READ2));
			Assertions.assertFalse(authorizationManager.isAuthorized(recordOtherUserAndTooExpensive, RecordOperations.READ2));

			//notify -> TYP_ID=${typId} and ETA_CD=PUB and GEO<=${geo}
			Assertions.assertTrue(authorizationManager.isAuthorized(record, RecordOperations.NOTIFY));
			Assertions.assertFalse(authorizationManager.isAuthorized(recordOtherType, RecordOperations.NOTIFY));
			Assertions.assertFalse(authorizationManager.isAuthorized(recordOtherEtat, RecordOperations.NOTIFY));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordOtherUser, RecordOperations.NOTIFY));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordOtherUserAndTooExpensive, RecordOperations.NOTIFY));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordOtherCommune, RecordOperations.NOTIFY));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordDepartement, RecordOperations.NOTIFY));
			Assertions.assertFalse(authorizationManager.isAuthorized(recordOtherDepartement, RecordOperations.NOTIFY));
			Assertions.assertFalse(authorizationManager.isAuthorized(recordRegion, RecordOperations.NOTIFY));
			Assertions.assertFalse(authorizationManager.isAuthorized(recordNational, RecordOperations.NOTIFY));

			//override write -> TYP_ID=${typId} and ETA_CD=PUB and GEO<=${geo}
			//default write don't apply : (UTI_ID_OWNER=${utiId} and ETA_CD<ARC) or (TYP_ID=${typId} and MONTANT<=${montantMax} and ETA_CD<ARC)
			Assertions.assertTrue(authorizationManager.isAuthorized(record, RecordOperations.WRITE));
			Assertions.assertFalse(authorizationManager.isAuthorized(recordOtherType, RecordOperations.WRITE));
			Assertions.assertFalse(authorizationManager.isAuthorized(recordOtherEtat, RecordOperations.WRITE));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordOtherUser, RecordOperations.WRITE));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordOtherUserAndTooExpensive, RecordOperations.WRITE));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordOtherCommune, RecordOperations.WRITE));
			Assertions.assertTrue(authorizationManager.isAuthorized(recordDepartement, RecordOperations.WRITE));
			Assertions.assertFalse(authorizationManager.isAuthorized(recordOtherDepartement, RecordOperations.WRITE));
			Assertions.assertFalse(authorizationManager.isAuthorized(recordRegion, RecordOperations.WRITE));
			Assertions.assertFalse(authorizationManager.isAuthorized(recordNational, RecordOperations.WRITE));

		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testNoWriterRole() {
		//TODO
	}

	private Record createRecord() {
		final Record record = new Record();
		record.setDosId(++currentDosId);
		record.setRegId(DEFAULT_REG_ID);
		record.setDepId(DEFAULT_DEP_ID);
		record.setComId(DEFAULT_COM_ID);
		record.setTypId(DEFAULT_TYPE_ID);
		record.setTitle("Record de test #" + currentDosId);
		record.setAmount(DEFAULT_MONTANT_MAX);
		record.setUtiIdOwner(DEFAULT_UTI_ID);
		record.setEtaCd("CRE");
		return record;
	}

	private Authorization getAuthorization(final AuthorizationName authorizationName) {
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		return definitionSpace.resolve(authorizationName.name(), Authorization.class);
	}

}
