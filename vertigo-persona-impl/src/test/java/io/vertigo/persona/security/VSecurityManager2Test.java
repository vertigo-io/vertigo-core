/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.persona.security;

import java.util.Locale;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.persona.security.SecurityNames.DossierOperations;
import io.vertigo.persona.security.SecurityNames.DossierPermissions;
import io.vertigo.persona.security.SecurityNames.Permissions;
import io.vertigo.persona.security.metamodel.Permission2;
import io.vertigo.persona.security.metamodel.PermissionName;
import io.vertigo.persona.security.metamodel.Role;
import io.vertigo.persona.security.model.Dossier;

/**
 * @author pchretien
 */
public final class VSecurityManager2Test extends AbstractTestCaseJU4 {

	private static final long DEFAULT_REG_ID = 1L;
	private static final long DEFAULT_DEP_ID = 2L;
	private static final long DEFAULT_COM_ID = 3L;
	private static final long DEFAULT_UTI_ID = 1000L;
	private static final long DEFAULT_TYPE_ID = 10L;
	private static final double DEFAULT_MONTANT_MAX = 100d;

	private long currentDosId = 1;

	@Inject
	private VSecurityManager securityManager;

	@Override
	protected String[] getManagersXmlFileName() {
		return new String[] { "./managers-test2.xml", };
	}

	@Test
	public void testCreateUserSession() {
		final UserSession userSession = securityManager.createUserSession();
		Assert.assertEquals(Locale.FRANCE, userSession.getLocale());
		Assert.assertEquals(TestUserSession.class, userSession.getClass());
	}

	@Test
	public void testInitCurrentUserSession() {
		final UserSession userSession = securityManager.createUserSession();
		try {
			securityManager.startCurrentUserSession(userSession);
			Assert.assertTrue(securityManager.getCurrentUserSession().isPresent());
			Assert.assertEquals(userSession, securityManager.getCurrentUserSession().get());
		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	public void testAuthenticate() {
		final UserSession userSession = securityManager.createUserSession();
		Assert.assertFalse(userSession.isAuthenticated());
		userSession.authenticate();
	}

	@Test
	public void testNoUserSession() {
		final Optional<UserSession> userSession = securityManager.getCurrentUserSession();
		Assert.assertFalse(userSession.isPresent());
	}

	@Test
	public void testResetUserSession() {
		final UserSession userSession = securityManager.createUserSession();
		try {
			securityManager.startCurrentUserSession(userSession);
			Assert.assertTrue(securityManager.getCurrentUserSession().isPresent());
			//
		} finally {
			securityManager.stopCurrentUserSession();
		}
		Assert.assertFalse(securityManager.getCurrentUserSession().isPresent());
	}

	@Test
	public void testRole() {
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		final Role admin = definitionSpace.resolve("R_ADMIN", Role.class);
		Assert.assertTrue("R_ADMIN".equals(admin.getName()));
		final Role secretary = definitionSpace.resolve("R_SECRETARY", Role.class);
		Assert.assertTrue("R_SECRETARY".equals(secretary.getName()));
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
	public void testAuthorized() {
		final Permission2 admUsr = getPermission(Permissions.PRM_ADMUSR);
		final Permission2 admPro = getPermission(Permissions.PRM_ADMPRO);

		final UserSession userSession = securityManager.<TestUserSession> createUserSession()
				.withSecurityKeys("utiId", DEFAULT_UTI_ID)
				.withSecurityKeys("typId", DEFAULT_TYPE_ID)
				.withSecurityKeys("montantMax", DEFAULT_MONTANT_MAX)
				.addPermission(admUsr)
				.addPermission(admPro);
		try {
			securityManager.startCurrentUserSession(userSession);
			Assert.assertTrue(securityManager.hasPermission(Permissions.PRM_ADMUSR));
			Assert.assertTrue(securityManager.hasPermission(Permissions.PRM_ADMPRO));
			Assert.assertFalse(securityManager.hasPermission(Permissions.PRM_ADMAPP));
		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testAuthorizedOnEntity() {

		final Dossier dossier = createDossier();

		final Dossier dossierTooExpensive = createDossier();
		dossierTooExpensive.setMontant(10000d);

		final Dossier dossierOtherUser = createDossier();
		dossierOtherUser.setUtiIdOwner(2000L);

		final Dossier dossierOtherUserAndTooExpensive = createDossier();
		dossierOtherUserAndTooExpensive.setUtiIdOwner(2000L);
		dossierOtherUserAndTooExpensive.setMontant(10000d);

		final Permission2 dossierRead = getPermission(DossierPermissions.PRM_DOSSIER_READ);
		final UserSession userSession = securityManager.<TestUserSession> createUserSession()
				.withSecurityKeys("utiId", DEFAULT_UTI_ID)
				.withSecurityKeys("typId", DEFAULT_TYPE_ID)
				.withSecurityKeys("montantMax", DEFAULT_MONTANT_MAX)
				.addPermission(dossierRead);
		try {
			securityManager.startCurrentUserSession(userSession);
			final boolean canReadDossier = securityManager.hasPermission(DossierPermissions.PRM_DOSSIER_READ);
			Assert.assertTrue(canReadDossier);

			//read -> MONTANT<=${montantMax} or UTI_ID_OWNER=${utiId}
			Assert.assertTrue(securityManager.isAuthorized(dossier, DossierOperations.READ));
			Assert.assertTrue(securityManager.isAuthorized(dossierTooExpensive, DossierOperations.READ));
			Assert.assertTrue(securityManager.isAuthorized(dossierOtherUser, DossierOperations.READ));
			Assert.assertFalse(securityManager.isAuthorized(dossierOtherUserAndTooExpensive, DossierOperations.READ));

		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testAuthorizedOnEntityGrant() {
		final Dossier dossier = createDossier();

		final Dossier dossierTooExpensive = createDossier();
		dossierTooExpensive.setMontant(10000d);

		final Dossier dossierOtherUser = createDossier();
		dossierOtherUser.setUtiIdOwner(2000L);

		final Dossier dossierOtherUserAndTooExpensive = createDossier();
		dossierOtherUserAndTooExpensive.setUtiIdOwner(2000L);
		dossierOtherUserAndTooExpensive.setMontant(10000d);

		final Dossier dossierArchivedNotWriteable = createDossier();
		dossierArchivedNotWriteable.setEtaCd("ARC");

		final Permission2 dossierCreate = getPermission(DossierPermissions.PRM_DOSSIER_CREATE);
		final UserSession userSession = securityManager.<TestUserSession> createUserSession()
				.withSecurityKeys("utiId", DEFAULT_UTI_ID)
				.withSecurityKeys("typId", DEFAULT_TYPE_ID)
				.withSecurityKeys("montantMax", DEFAULT_MONTANT_MAX)
				.addPermission(dossierCreate);
		try {
			securityManager.startCurrentUserSession(userSession);
			final boolean canCreateDossier = securityManager.hasPermission(DossierPermissions.PRM_DOSSIER_CREATE);
			Assert.assertTrue(canCreateDossier);

			//read -> MONTANT<=${montantMax} or UTI_ID_OWNER=${utiId}
			Assert.assertTrue(securityManager.isAuthorized(dossier, DossierOperations.READ));
			Assert.assertTrue(securityManager.isAuthorized(dossierTooExpensive, DossierOperations.READ));
			Assert.assertTrue(securityManager.isAuthorized(dossierOtherUser, DossierOperations.READ));
			Assert.assertFalse(securityManager.isAuthorized(dossierOtherUserAndTooExpensive, DossierOperations.READ));
			Assert.assertTrue(securityManager.isAuthorized(dossierArchivedNotWriteable, DossierOperations.READ));

			//create -> TYP_ID=${typId} and MONTANT<=${montantMax}
			Assert.assertTrue(securityManager.isAuthorized(dossier, DossierOperations.CREATE));
			Assert.assertFalse(securityManager.isAuthorized(dossierTooExpensive, DossierOperations.CREATE));
			Assert.assertTrue(securityManager.isAuthorized(dossierOtherUser, DossierOperations.CREATE));
			Assert.assertFalse(securityManager.isAuthorized(dossierOtherUserAndTooExpensive, DossierOperations.CREATE));
			Assert.assertTrue(securityManager.isAuthorized(dossierArchivedNotWriteable, DossierOperations.CREATE));

		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testAuthorizedOnEntityOverride() {
		final Dossier dossier = createDossier();

		final Dossier dossierTooExpensive = createDossier();
		dossierTooExpensive.setMontant(10000d);

		final Dossier dossierOtherUser = createDossier();
		dossierOtherUser.setUtiIdOwner(2000L);

		final Dossier dossierOtherUserAndTooExpensive = createDossier();
		dossierOtherUserAndTooExpensive.setUtiIdOwner(2000L);
		dossierOtherUserAndTooExpensive.setMontant(10000d);

		final Permission2 dossierRead = getPermission(DossierPermissions.PRM_DOSSIER_READ_HP);
		final UserSession userSession = securityManager.<TestUserSession> createUserSession()
				.withSecurityKeys("utiId", DEFAULT_UTI_ID)
				.withSecurityKeys("typId", DEFAULT_TYPE_ID)
				.withSecurityKeys("montantMax", DEFAULT_MONTANT_MAX)
				.addPermission(dossierRead);
		try {
			securityManager.startCurrentUserSession(userSession);
			final boolean canReadDossier = securityManager.hasPermission(DossierPermissions.PRM_DOSSIER_READ_HP);
			Assert.assertTrue(canReadDossier);

			//read -> MONTANT<=${montantMax} or UTI_ID_OWNER=${utiId}
			Assert.assertTrue(securityManager.isAuthorized(dossier, DossierOperations.READ));
			Assert.assertTrue(securityManager.isAuthorized(dossierTooExpensive, DossierOperations.READ));
			Assert.assertTrue(securityManager.isAuthorized(dossierOtherUser, DossierOperations.READ));
			Assert.assertTrue(securityManager.isAuthorized(dossierOtherUserAndTooExpensive, DossierOperations.READ));

		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testAuthorizedOnEntityEnumAxes() {
		final Dossier dossier = createDossier();

		final Dossier dossierTooExpensive = createDossier();
		dossierTooExpensive.setMontant(10000d);

		final Dossier dossierOtherUser = createDossier();
		dossierOtherUser.setUtiIdOwner(2000L);

		final Dossier dossierOtherUserAndTooExpensive = createDossier();
		dossierOtherUserAndTooExpensive.setUtiIdOwner(2000L);
		dossierOtherUserAndTooExpensive.setMontant(10000d);

		final Dossier dossierArchivedNotWriteable = createDossier();
		dossierArchivedNotWriteable.setEtaCd("ARC");

		final Permission2 dossierWrite = getPermission(DossierPermissions.PRM_DOSSIER_WRITE);
		final UserSession userSession = securityManager.<TestUserSession> createUserSession()
				.withSecurityKeys("utiId", DEFAULT_UTI_ID)
				.withSecurityKeys("typId", DEFAULT_TYPE_ID)
				.withSecurityKeys("montantMax", DEFAULT_MONTANT_MAX)
				.addPermission(dossierWrite);
		try {
			securityManager.startCurrentUserSession(userSession);
			final boolean canReadDossier = securityManager.hasPermission(DossierPermissions.PRM_DOSSIER_WRITE);
			Assert.assertTrue(canReadDossier);

			//read -> MONTANT<=${montantMax} or UTI_ID_OWNER=${utiId}
			Assert.assertTrue(securityManager.isAuthorized(dossier, DossierOperations.READ));
			Assert.assertTrue(securityManager.isAuthorized(dossierTooExpensive, DossierOperations.READ));
			Assert.assertTrue(securityManager.isAuthorized(dossierOtherUser, DossierOperations.READ));
			Assert.assertFalse(securityManager.isAuthorized(dossierOtherUserAndTooExpensive, DossierOperations.READ));
			Assert.assertTrue(securityManager.isAuthorized(dossierArchivedNotWriteable, DossierOperations.READ));

			//write -> (UTI_ID_OWNER=${utiId} and ETA_CD<ARC) or (TYP_ID=${typId} and MONTANT<=${montantMax} and ETA_CD<ARC)
			Assert.assertTrue(securityManager.isAuthorized(dossier, DossierOperations.WRITE));
			Assert.assertTrue(securityManager.isAuthorized(dossierTooExpensive, DossierOperations.WRITE));
			Assert.assertTrue(securityManager.isAuthorized(dossierOtherUser, DossierOperations.WRITE));
			Assert.assertFalse(securityManager.isAuthorized(dossierOtherUserAndTooExpensive, DossierOperations.WRITE));
			Assert.assertFalse(securityManager.isAuthorized(dossierArchivedNotWriteable, DossierOperations.WRITE));

		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testAuthorizedOnEntityTreeAxes() {
		final Dossier dossier = createDossier();
		dossier.setEtaCd("PUB");

		final Dossier dossierOtherType = createDossier();
		dossierOtherType.setEtaCd("PUB");
		dossierOtherType.setTypId(11L);

		final Dossier dossierOtherEtat = createDossier();
		dossierOtherEtat.setEtaCd("CRE");

		final Dossier dossierOtherUser = createDossier();
		dossierOtherUser.setEtaCd("PUB");
		dossierOtherUser.setUtiIdOwner(2000L);

		final Dossier dossierOtherUserAndTooExpensive = createDossier();
		dossierOtherUserAndTooExpensive.setEtaCd("PUB");
		dossierOtherUserAndTooExpensive.setUtiIdOwner(2000L);
		dossierOtherUserAndTooExpensive.setMontant(10000d);

		final Dossier dossierOtherCommune = createDossier();
		dossierOtherCommune.setEtaCd("PUB");
		dossierOtherCommune.setComId(3L);

		final Dossier dossierDepartement = createDossier();
		dossierDepartement.setEtaCd("PUB");
		dossierDepartement.setComId(null);

		final Dossier dossierOtherDepartement = createDossier();
		dossierOtherDepartement.setEtaCd("PUB");
		dossierOtherDepartement.setDepId(10L);
		dossierOtherDepartement.setComId(null);

		final Dossier dossierRegion = createDossier();
		dossierRegion.setEtaCd("PUB");
		dossierRegion.setDepId(null);
		dossierRegion.setComId(null);

		final Dossier dossierNational = createDossier();
		dossierNational.setEtaCd("PUB");
		dossierNational.setRegId(null);
		dossierNational.setDepId(null);
		dossierNational.setComId(null);

		final Permission2 dossierNotify = getPermission(DossierPermissions.PRM_DOSSIER_NOTIFY);
		final Permission2 dossierWrite = getPermission(DossierPermissions.PRM_DOSSIER_WRITE);
		final UserSession userSession = securityManager.<TestUserSession> createUserSession()
				.withSecurityKeys("utiId", DEFAULT_UTI_ID)
				.withSecurityKeys("typId", DEFAULT_TYPE_ID)
				.withSecurityKeys("montantMax", DEFAULT_MONTANT_MAX)
				.withSecurityKeys("geo", new Long[] { DEFAULT_REG_ID, DEFAULT_DEP_ID, null }) //droit sur tout un département
				.addPermission(dossierNotify);
		try {
			securityManager.startCurrentUserSession(userSession);
			Assert.assertTrue(securityManager.hasPermission(DossierPermissions.PRM_DOSSIER_NOTIFY));

			//grant read -> MONTANT<=${montantMax} or UTI_ID_OWNER=${utiId}
			Assert.assertTrue(securityManager.isAuthorized(dossier, DossierOperations.READ));
			Assert.assertTrue(securityManager.isAuthorized(dossierOtherUser, DossierOperations.READ));
			Assert.assertFalse(securityManager.isAuthorized(dossierOtherUserAndTooExpensive, DossierOperations.READ));

			//notify -> TYP_ID=${typId} and ETA_CD=PUB and GEO<=${geo}
			Assert.assertTrue(securityManager.isAuthorized(dossier, DossierOperations.NOTIFY));
			Assert.assertFalse(securityManager.isAuthorized(dossierOtherType, DossierOperations.NOTIFY));
			Assert.assertFalse(securityManager.isAuthorized(dossierOtherEtat, DossierOperations.NOTIFY));
			Assert.assertTrue(securityManager.isAuthorized(dossierOtherUser, DossierOperations.NOTIFY));
			Assert.assertTrue(securityManager.isAuthorized(dossierOtherUserAndTooExpensive, DossierOperations.NOTIFY));
			Assert.assertTrue(securityManager.isAuthorized(dossierOtherCommune, DossierOperations.NOTIFY));
			Assert.assertTrue(securityManager.isAuthorized(dossierDepartement, DossierOperations.NOTIFY));
			Assert.assertFalse(securityManager.isAuthorized(dossierOtherDepartement, DossierOperations.NOTIFY));
			Assert.assertFalse(securityManager.isAuthorized(dossierRegion, DossierOperations.NOTIFY));
			Assert.assertFalse(securityManager.isAuthorized(dossierNational, DossierOperations.NOTIFY));

			//override write -> TYP_ID=${typId} and ETA_CD=PUB and GEO<=${geo}
			//default write don't apply : (UTI_ID_OWNER=${utiId} and ETA_CD<ARC) or (TYP_ID=${typId} and MONTANT<=${montantMax} and ETA_CD<ARC)
			Assert.assertTrue(securityManager.isAuthorized(dossier, DossierOperations.WRITE));
			Assert.assertFalse(securityManager.isAuthorized(dossierOtherType, DossierOperations.WRITE));
			Assert.assertFalse(securityManager.isAuthorized(dossierOtherEtat, DossierOperations.WRITE));
			Assert.assertTrue(securityManager.isAuthorized(dossierOtherUser, DossierOperations.WRITE));
			Assert.assertTrue(securityManager.isAuthorized(dossierOtherUserAndTooExpensive, DossierOperations.WRITE));
			Assert.assertTrue(securityManager.isAuthorized(dossierOtherCommune, DossierOperations.WRITE));
			Assert.assertTrue(securityManager.isAuthorized(dossierDepartement, DossierOperations.WRITE));
			Assert.assertFalse(securityManager.isAuthorized(dossierOtherDepartement, DossierOperations.WRITE));
			Assert.assertFalse(securityManager.isAuthorized(dossierRegion, DossierOperations.WRITE));
			Assert.assertFalse(securityManager.isAuthorized(dossierNational, DossierOperations.WRITE));

		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testNoWriterRole() {
		//TODO
	}

	private Dossier createDossier() {
		final Dossier dossier = new Dossier();
		dossier.setDosId(++currentDosId);
		dossier.setRegId(DEFAULT_REG_ID);
		dossier.setDepId(DEFAULT_DEP_ID);
		dossier.setComId(DEFAULT_COM_ID);
		dossier.setTypId(DEFAULT_TYPE_ID);
		dossier.setTitre("Dossier de test #" + currentDosId);
		dossier.setMontant(DEFAULT_MONTANT_MAX);
		dossier.setUtiIdOwner(DEFAULT_UTI_ID);
		dossier.setEtaCd("CRE");
		return dossier;
	}

	private Permission2 getPermission(final PermissionName permissionName) {
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		return definitionSpace.resolve(permissionName.name(), Permission2.class);
	}

}
