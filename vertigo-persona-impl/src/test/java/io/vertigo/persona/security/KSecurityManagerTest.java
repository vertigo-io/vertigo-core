package io.vertigo.persona.security;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.dynamo.TestUtil;
import io.vertigo.dynamock.domain.famille.Famille;
import io.vertigo.kernel.Home;
import io.vertigo.kernel.lang.Option;
import io.vertigo.persona.impl.security.BeanResourceNameFactory;
import io.vertigo.persona.security.KSecurityManager;
import io.vertigo.persona.security.UserSession;
import io.vertigo.persona.security.model.Permission;
import io.vertigo.persona.security.model.Role;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author pchretien
 * $Id: KSecurityManagerTest.java,v 1.6 2014/02/27 10:25:32 pchretien Exp $
 */
public final class KSecurityManagerTest extends AbstractTestCaseJU4 {
	@Inject
	private KSecurityManager securityManager;

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
			Assert.assertEquals(true, securityManager.getCurrentUserSession().isDefined());
			Assert.assertEquals(userSession, securityManager.getCurrentUserSession().get());
		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testAuthenticate() {
		final UserSession userSession = securityManager.createUserSession();
		Assert.assertEquals(false, userSession.isAuthenticated());
		userSession.authenticate();
	}

	@Test
	public void testNoUserSession() {
		final Option<UserSession> userSession = securityManager.getCurrentUserSession();
		Assert.assertEquals(true, userSession.isEmpty());
	}

	@Test
	public void testResetUserSession() {
		final UserSession userSession = securityManager.createUserSession();
		try {
			securityManager.startCurrentUserSession(userSession);
			Assert.assertEquals(true, securityManager.getCurrentUserSession().isDefined());
			//
		} finally {
			securityManager.stopCurrentUserSession();
		}
		Assert.assertEquals(false, securityManager.getCurrentUserSession().isDefined());
	}

	@Test(expected = NullPointerException.class)
	public void testRole() {
		final Role admin = createRole("R_ADMIN");
		final Role user = createRole("R_USER");
		Home.getDefinitionSpace().put(admin, Role.class);
		Home.getDefinitionSpace().put(user, Role.class);

		final Role r1 = Home.getDefinitionSpace().resolve(admin.getName(), Role.class);
		Assert.assertTrue(admin.equals(r1));
		final Role r2 = Home.getDefinitionSpace().resolve("R_SECRETARY", Role.class);
		nop(r2);
	}

	@Test
	public void testAccess() {
		final Role admin = createRole("R_ADMIN");
		final Role user = createRole("R_USER");
		final Role manager = createRole("R_MANAGER");
		final Role secretary = createRole("R_SECRETARY");
		Home.getDefinitionSpace().put(admin, Role.class);
		Home.getDefinitionSpace().put(user, Role.class);
		Home.getDefinitionSpace().put(manager, Role.class);
		Home.getDefinitionSpace().put(secretary, Role.class);

		final UserSession userSession = securityManager.createUserSession();
		userSession.addRole(admin);
		userSession.addRole(manager);
		try {
			securityManager.startCurrentUserSession(userSession);

			final Set<Role> roles = new HashSet<>();
			roles.add(admin);
			roles.add(secretary);
			Assert.assertEquals(true, securityManager.hasRole(userSession, roles));

			roles.clear();
			roles.add(secretary);
			Assert.assertEquals(false, securityManager.hasRole(userSession, roles));

			roles.clear(); //Si aucun droit necessaire alors c'est bon
			Assert.assertEquals(true, securityManager.hasRole(userSession, roles));
		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testNotAuthorized() {
		final Role reader = getRole("R_READER");
		final Role writer = getRole("R_WRITER");

		final UserSession userSession = securityManager.createUserSession();
		userSession.addRole(reader);
		userSession.addRole(writer);
		try {
			securityManager.startCurrentUserSession(userSession);
			final boolean authorized = securityManager.isAuthorized("not", "authorized");
			Assert.assertFalse(authorized);
		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testAuthorized() {
		final Role reader = getRole("R_READER");
		final Role writer = getRole("R_WRITER");

		final UserSession userSession = securityManager.createUserSession();
		userSession.addRole(reader);
		userSession.addRole(writer);
		try {
			securityManager.startCurrentUserSession(userSession);
			final boolean canread = securityManager.isAuthorized("/products/12", "OP_READ");
			Assert.assertTrue(canread);
			final boolean canwrite = securityManager.isAuthorized("/products/12", "OP_WRITE");
			Assert.assertTrue(canwrite);
		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testNoWriterRole() {
		final Role reader = getRole("R_READER");

		final UserSession userSession = securityManager.createUserSession();
		userSession.addRole(reader);
		try {
			securityManager.startCurrentUserSession(userSession);
			final boolean canread = securityManager.isAuthorized("/products/12", "OP_READ");
			Assert.assertTrue(canread);
			final boolean cannotwrite = securityManager.isAuthorized("/products/12", "OP_WRITE");
			Assert.assertFalse(cannotwrite);
		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testAuthorizedAllWithResourceNameFactory() {
		securityManager.registerResourceNameFactory(Famille.class.getSimpleName(), new BeanResourceNameFactory("/famille/${famId}"));
		final Famille famille12 = new Famille();
		famille12.setFamId(12L);

		final Famille famille13 = new Famille();
		famille13.setFamId(13L);

		//Test toutes familles
		final Role readAllFamillies = getRole("R_ALL_FAMILLES");
		final UserSession userSession = securityManager.createUserSession();
		userSession.addRole(readAllFamillies);
		try {
			securityManager.startCurrentUserSession(userSession);
			final boolean canRead12 = securityManager.isAuthorized(Famille.class.getSimpleName(), famille12, "OP_READ");
			Assert.assertTrue(canRead12);
			final boolean canRead13 = securityManager.isAuthorized(Famille.class.getSimpleName(), famille13, "OP_READ");
			Assert.assertTrue(canRead13);
		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testAuthorizedSessionPropertyWithResourceNameFactory() {
		securityManager.registerResourceNameFactory(Famille.class.getSimpleName(), new BeanResourceNameFactory("/famille/${famId}"));
		final Famille famille12 = new Famille();
		famille12.setFamId(12L);

		final Famille famille13 = new Famille();
		famille13.setFamId(13L);

		//Test ma famille
		final Role readMyFamilly = getRole("R_MY_FAMILLE");
		final UserSession userSession = securityManager.createUserSession();
		userSession.addRole(readMyFamilly);
		try {
			securityManager.startCurrentUserSession(userSession);
			final boolean canRead12 = securityManager.isAuthorized(Famille.class.getSimpleName(), famille12, "OP_READ");
			Assert.assertTrue(canRead12);
			final boolean canRead13 = securityManager.isAuthorized(Famille.class.getSimpleName(), famille13, "OP_READ");
			Assert.assertFalse(canRead13);
		} finally {
			securityManager.stopCurrentUserSession();
		}
	}

	@Test
	public void testDescription() {
		final Role admin = createRole("R_ADMIN");
		final Role user = createRole("R_USER");
		final Role manager = createRole("R_MANAGER");
		final Role secretary = createRole("R_SECRETARY");
		Home.getDefinitionSpace().put(admin, Role.class);
		Home.getDefinitionSpace().put(user, Role.class);
		Home.getDefinitionSpace().put(manager, Role.class);
		Home.getDefinitionSpace().put(secretary, Role.class);
		TestUtil.testDescription(securityManager);
	}

	private Role getRole(final String name) {
		return Home.getDefinitionSpace().resolve(name, Role.class);
	}

	private static Role createRole(final String name) {
		final String description = name;
		return new Role(name, description, Collections.<Permission> emptyList());
	}
}
