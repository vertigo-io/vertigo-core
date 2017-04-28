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
import io.vertigo.persona.security.metamodel.Permission;
import io.vertigo.persona.security.metamodel.Role;
import io.vertigo.persona.security.model.Dossier;

/**
 * @author pchretien
 */
public final class VSecurityManagerTest extends AbstractTestCaseJU4 {

	private static final long REG_ID_IDF = 1L;
	private static final long DEP_ID_YVELINES = 2L;
	private static final long COM_ID_VERSAILLE = 3L;
	private static final long UTI_ID = 1000L;
	private static final long TYP_ID_1 = 1L;

	private long currentDosId = 1;

	@Inject
	private VSecurityManager securityManager;

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
		final Permission admUsr = getPermission("PRM_ADMUSR");
		final Permission admPro = getPermission("PRM_ADMPRO");

		final UserSession userSession = securityManager.createUserSession()
				.addPermission(admUsr)
				.addPermission(admPro);
		try {
			securityManager.startCurrentUserSession(userSession);
			final boolean canAdmUsr = securityManager.hasPermission(SecurityNames.Permissions.PRM_ADMUSR);
			Assert.assertTrue(canAdmUsr);
			final boolean canAdmPro = securityManager.hasPermission(SecurityNames.Permissions.PRM_ADMPRO);
			Assert.assertTrue(canAdmPro);

			final boolean canAdmApp = securityManager.hasPermission(SecurityNames.Permissions.PRM_ADMAPP);
			Assert.assertFalse(canAdmApp);
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

		final Permission dossierRead = getPermission("PRM_DOSSIER_READ");
		final UserSession userSession = securityManager.createUserSession()
				.addPermission(dossierRead);
		try {
			securityManager.startCurrentUserSession(userSession);
			final boolean canReadDossier = securityManager.hasPermission(SecurityNames.DossierPermissions.PRM_DOSSIER_READ);
			Assert.assertTrue(canReadDossier);

			//read -> MONTANT<=${montantMax} or UTI_ID_OWNER=${utiId}
			boolean canReadThisDossier = securityManager.isAuthorized(dossier, SecurityNames.DossierOperations.READ);
			Assert.assertTrue(canReadThisDossier);

			canReadThisDossier = securityManager.isAuthorized(dossierTooExpensive, SecurityNames.DossierOperations.READ);
			Assert.assertTrue(canReadThisDossier);

			canReadThisDossier = securityManager.isAuthorized(dossierOtherUser, SecurityNames.DossierOperations.READ);
			Assert.assertTrue(canReadThisDossier);

			canReadThisDossier = securityManager.isAuthorized(dossierOtherUserAndTooExpensive, SecurityNames.DossierOperations.READ);
			Assert.assertFalse(canReadThisDossier);

		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	private Dossier createDossier() {
		final Dossier dossier = new Dossier();
		dossier.setDosId(++currentDosId);
		dossier.setRegId(REG_ID_IDF);
		dossier.setDepId(DEP_ID_YVELINES);
		dossier.setComId(COM_ID_VERSAILLE);
		dossier.setTypId(TYP_ID_1);
		dossier.setTitre("Dossier de test #" + currentDosId);
		dossier.setMontant(100d);
		dossier.setUtiIdOwner(UTI_ID);
		dossier.setEtaCd("CRE");
		return dossier;
	}

	@Test
	public void testNoWriterRole() {
		//TODO
	}

	private Permission getPermission(final String name) {
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		return definitionSpace.resolve(name, Permission.class);
	}

}
