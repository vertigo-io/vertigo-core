/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.kernel.util;

import io.vertigo.core.lang.MessageText;
import io.vertigo.core.lang.Option;
import io.vertigo.kernel.exception.VUserException;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test de l'utilitaire de manipulation des classes.
 *
 * @author pchretien
 */
public final class ClassUtilTest {
	private static void nop(final Object o) {
		//NOP
	}

	@Test
	public void testNewInstance() {
		final String className = StringBuilder.class.getCanonicalName();
		final Object created = ClassUtil.newInstance(className);
		Assert.assertEquals(StringBuilder.class, created.getClass());
	}

	@Test
	public void testNewInstanceWithType() {
		final String className = StringBuilder.class.getCanonicalName();
		final StringBuilder created = ClassUtil.newInstance(className, StringBuilder.class);
		nop(created);
		Assert.assertEquals(StringBuilder.class, created.getClass());
	}

	@Test(expected = Exception.class)
	public void testNewInstanceWithTypeError() {
		final String className = StringBuilder.class.getCanonicalName() + ".";
		final StringBuilder created = ClassUtil.newInstance(className, StringBuilder.class);
		nop(created);
	}

	@Test
	public void testNewInstanceWithClasss() {
		final Object created = ClassUtil.newInstance(StringBuilder.class);
		Assert.assertEquals(StringBuilder.class, created.getClass());
	}

	@Test
	public void testFindConstructor() {
		final Constructor<StringBuilder> constructor = ClassUtil.findConstructor(StringBuilder.class, new Class[] { String.class });
		Assert.assertEquals(constructor.getDeclaringClass(), StringBuilder.class);
	}

	@Test(expected = Exception.class)
	public void testFindConstructorWithError() {
		final Constructor<StringBuilder> constructor = ClassUtil.findConstructor(StringBuilder.class, new Class[] { Date.class });
		nop(constructor);
	}

	@Test
	public void testNewInstanceWithConstructor() {
		final Constructor<StringBuilder> constructor = ClassUtil.findConstructor(StringBuilder.class, new Class[] { String.class });
		final StringBuilder created = ClassUtil.newInstance(constructor, new Object[] { "Hello" });
		Assert.assertEquals("Hello", created.toString());
	}

	@Test(expected = Exception.class)
	public void testNewInstanceFail() {
		final String className = StringBuilder.class.getCanonicalName() + ".";
		final Object created = ClassUtil.newInstance(className);
		nop(created);
	}

	@Test
	public void testClassForName() {
		final String className = StringBuilder.class.getCanonicalName();
		Assert.assertEquals(StringBuilder.class, ClassUtil.classForName(className));
	}

	@Test(expected = Exception.class)
	public void testClassForNameFail() {
		final String className = StringBuilder.class.getCanonicalName() + ".";
		final Class<?> clazz = ClassUtil.classForName(className);
		nop(clazz);
	}

	@Test
	public void testClassForNameWithType() {
		final String className = StringBuilder.class.getCanonicalName();
		Assert.assertEquals(StringBuilder.class, ClassUtil.classForName(className, StringBuilder.class));
	}

	@Test(expected = Exception.class)
	public void testClassForNameWithTypeFail() {
		final String className = StringBuilder.class.getCanonicalName() + ".";
		final Class<?> clazz = ClassUtil.classForName(className, StringBuilder.class);
		nop(clazz);
	}

	@Test
	public void testClassForNameWithSuperType() {
		final String className = StringBuilder.class.getCanonicalName();
		Assert.assertEquals(StringBuilder.class, ClassUtil.classForName(className, Serializable.class));
	}

	@Test
	public void testFindMethod() {
		final Method method = ClassUtil.findMethod(StringBuilder.class, "lastIndexOf", new Class[] { String.class });
		nop(method);
		Assert.assertEquals("lastIndexOf", method.getName());
	}

	@Test(expected = Exception.class)
	public void testFindMethodWithError() {
		final Method method = ClassUtil.findMethod(StringBuilder.class, "lastIndexOf", new Class[] { Date.class });
		nop(method);
	}

	@Test
	public void testAllFields() {
		final Collection<Field> fields = ClassUtil.getAllFields(String.class);
		//On vérifi que la propriété 'value' appartient à la liste
		boolean found = false;
		for (final Field field : fields) {
			if ("value".equals(field.getName())) {
				found = true;
			}
		}
		Assert.assertEquals(true, found);
	}

	@Test
	public void testGetField() throws SecurityException {
		final MyInjected myInjected = new MyInjected();
		Field field;
		field = getField(MyInjected.class, "myLong");
		Assert.assertEquals(1L, ClassUtil.get(myInjected, field));
		//---
		field = getField(MyInjected.class, "myPrivateLong");
		Assert.assertEquals(2L, ClassUtil.get(myInjected, field));
		//---
		field = getField(MyInjected.class, "myFinalLong");
		Assert.assertEquals(3L, ClassUtil.get(myInjected, field));
	}

	@Test
	public void testSetField() throws SecurityException {
		final MyInjected myInjected = new MyInjected();
		Field field;
		//---
		field = getField(MyInjected.class, "myLong");
		ClassUtil.set(myInjected, field, 10L);
		Assert.assertEquals(10L, ClassUtil.get(myInjected, field));
		//---
		field = getField(MyInjected.class, "myPrivateLong");
		ClassUtil.set(myInjected, field, 20L);
		Assert.assertEquals(20L, ClassUtil.get(myInjected, field));
		//---
		field = getField(MyInjected.class, "myFinalLong");
		ClassUtil.set(myInjected, field, 30L);
		Assert.assertEquals(30L, ClassUtil.get(myInjected, field));
	}

	@Test
	public void testInvoke() {
		final Method addMethod = ClassUtil.findMethod(MyMath.class, "add", new Class[] { long.class, long.class });
		final Object result = ClassUtil.invoke(new MyMath(), addMethod, 4L, 6L);
		Assert.assertEquals(Long.valueOf(10L), result);
	}

	@Test(expected = ArithmeticException.class)
	public void testInvokeWithError() {
		final Method divMethod = ClassUtil.findMethod(MyMath.class, "div", new Class[] { long.class, long.class });
		final Object result = ClassUtil.invoke(new MyMath(), divMethod, 4L, 0L);
		nop(result);
	}

	@Test(expected = VUserException.class)
	public void testInvokeWithException() {
		final Method addMethod = ClassUtil.findMethod(MyMath.class, "kuser", new Class[] { long.class, long.class });
		final Object result = ClassUtil.invoke(new MyMath(), addMethod, 4L, 6L);
		nop(result);
	}

	@Test
	public void testGeneric() throws SecurityException, NoSuchFieldException {
		Field field;
		//---
		field = MyGenerics.class.getField("myOption");
		Assert.assertEquals(Long.class, ClassUtil.getGeneric(field));
		//---
		field = MyGenerics.class.getField("myList");
		Assert.assertEquals(Long.class, ClassUtil.getGeneric(field));
		//---
		field = MyGenerics.class.getField("myList3");
		Assert.assertEquals(Map.class, ClassUtil.getGeneric(field));
	}

	@Test(expected = Exception.class)
	public void testGenericWithError() throws SecurityException, NoSuchFieldException {
		final Field field = MyGenerics.class.getField("myList2");
		final Class<?> generic = ClassUtil.getGeneric(field);
		nop(generic);
	}

	@Test
	public void testConstructorParameterGeneric() throws NoSuchMethodException, SecurityException {
		final Constructor<MyGenerics> constructor = MyGenerics.class.getConstructor(Option.class, List.class, List.class, List.class);
		//---
		Assert.assertEquals(Long.class, ClassUtil.getGeneric(constructor, 0));
		//---
		Assert.assertEquals(Long.class, ClassUtil.getGeneric(constructor, 1));
		//---
		//Assert.assertEquals(Long.class, ClassUtil.getGeneric(constructor, 2));
		//---
		Assert.assertEquals(Map.class, ClassUtil.getGeneric(constructor, 3));
	}

	@Test(expected = Exception.class)
	public void testConstructorParameterGenericWithError() throws NoSuchMethodException, SecurityException {
		final Constructor<MyGenerics> constructor = MyGenerics.class.getConstructor(Option.class, List.class, List.class, List.class);
		//---
		final Class<?> generic = ClassUtil.getGeneric(constructor, 2);
		nop(generic);
	}

	@Test
	public void testGetAllFieldsByAnnotation() throws SecurityException {
		int i = 0;
		Collection<Field> fields;
		//--
		final String[] injectedFieldNames = { "publicValue2", "protectedValue2", "packageValue2", "privateValue2" };
		fields = ClassUtil.getAllFields(MyBean.class, Inject.class);

		Assert.assertEquals(injectedFieldNames.length, fields.size());
		for (final Field field : fields) {
			Assert.assertEquals(injectedFieldNames[i], field.getName());
			i++;
		}
		//--
		i = 0;
		final String[] deprectatedFieldNames = { "publicValue1" };
		fields = ClassUtil.getAllFields(MyBean.class, Deprecated.class);

		Assert.assertEquals(deprectatedFieldNames.length, fields.size());
		for (final Field field : fields) {
			Assert.assertEquals(deprectatedFieldNames[i], field.getName());
			i++;
		}
		//--
		i = 0;
		final String[] injectedFieldNames2 = { "privateValue4", "publicValue2", "protectedValue2", "packageValue2", "privateValue2" };
		fields = ClassUtil.getAllFields(MySubBean.class, Inject.class);

		Assert.assertEquals(injectedFieldNames2.length, fields.size());
		for (final Field field : fields) {
			Assert.assertEquals(injectedFieldNames2[i], field.getName());
			i++;
		}
	}

	@Test
	public void testGetAllInterfaces() throws SecurityException {
		Class<?>[] interfaces;
		final Class<?>[] declaredInterfaces = { MyInterface1.class };
		interfaces = ClassUtil.getAllInterfaces(MyBean.class);
		//--
		Assert.assertEquals(declaredInterfaces.length, interfaces.length);
		Assert.assertArrayEquals(declaredInterfaces, interfaces);

		final Class<?>[] inheritedInterfaces = { MyInterface2.class, MyInterface4.class, MyInterface3.class, MyInterface1.class };
		interfaces = ClassUtil.getAllInterfaces(MySubBean.class);
		//--
		Assert.assertEquals(inheritedInterfaces.length, interfaces.length);
		Assert.assertArrayEquals(inheritedInterfaces, interfaces);
	}

	@Test
	public void testGetPropertyName() throws NoSuchMethodException, SecurityException {
		Method method;
		//---
		method = MyPojo.class.getDeclaredMethod("getValue1");
		Assert.assertEquals("value1", ClassUtil.getPropertyName(method));
		//---
		method = MyPojo.class.getDeclaredMethod("getValueCamelCase");
		Assert.assertEquals("valueCamelCase", ClassUtil.getPropertyName(method));
		//---
		method = MyPojo.class.getDeclaredMethod("isValue2");
		Assert.assertEquals("value2", ClassUtil.getPropertyName(method));
	}

	@Test(expected = Exception.class)
	public void testGetPropertyNameSetterWithError() throws NoSuchMethodException, SecurityException {
		Method method;
		//---
		method = MyPojo.class.getDeclaredMethod("setValue1", Long.class);
		final String name = ClassUtil.getPropertyName(method);
		nop(name);
	}

	@Test(expected = Exception.class)
	public void testGetPropertyNameIsWithError() throws NoSuchMethodException, SecurityException {
		Method method;
		//---
		method = MyPojo.class.getDeclaredMethod("isValueLong");
		final String name = ClassUtil.getPropertyName(method);
		nop(name);
	}

	@Test(expected = Exception.class)
	public void testGetPropertyNameHasWithError() throws NoSuchMethodException, SecurityException {
		Method method;
		//---
		method = MyPojo.class.getDeclaredMethod("hasValue2");
		final String name = ClassUtil.getPropertyName(method);
		nop(name);
	}

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------
	public static final class MyInjected {
		public Long myLong = 1L;
		private Long myPrivateLong = 2L;
		private final Long myFinalLong = 3L;

		final void initMyPrivateLong() {
			myPrivateLong = 2L;
			nop(myPrivateLong); //Pour faire croire que la var est utilisée
			nop(myFinalLong); //idem
		}
	}

	public static final class MyGenerics {
		//Tests pour les génériques 
		public Option<Long> myOption;
		public List<Long> myList;
		public List<?> myList2;
		public List<Map<?, ?>> myList3;

		public MyGenerics(final Option<Long> myOption, final List<Long> myList, final List<?> myList2, final List<Map<?, ?>> myList3) {
			//rien
		}
	}

	public static final class MyMath {
		public long div(final long v1, final long v2) {
			return v1 / v2;
		}

		public long add(final long v1, final long v2) {
			return v1 + v2;
		}

		public long kuser(final long v1, final long v2) {
			throw new VUserException(new MessageText("test", null));
		}

	}

	public static class MyBean implements MyInterface1 {
		@Deprecated
		public Long publicValue1;
		@Inject
		public Long publicValue2;
		protected Long protectedValue1;
		@Inject
		protected Long protectedValue2;
		Long packageValue1;
		@Inject
		Long packageValue2;
		private Long privateValue1;
		@Inject
		private Long privateValue2;

		public MyBean() {
			nop(privateValue1);
			nop(privateValue2);
		}
	}

	public static final class MySubBean extends MyBean implements MyInterface2, MyInterface4 {
		private Long privateValue3;
		@Inject
		private Long privateValue4;

		public MySubBean() {
			nop(privateValue3);
			nop(privateValue4);
		}

	}

	public static interface MyInterface1 {
		//rien
	}

	public static interface MyInterface2 {
		//rien
	}

	public static interface MyInterface3 {
		//rien
	}

	public static interface MyInterface4 extends MyInterface3 {
		//rien
	}

	public static class MyPojo {
		public Long getValue1() {
			return 1L;
		}

		public void setValue1(final Long value1) {
			nop(value1);
		}

		public Long getValueCamelCase() {
			return 1L;
		}

		public boolean isValue2() {
			return true;
		}

		public boolean hasValue3() {
			return true;
		}

		public Long isValueLong() {
			return 2L;
		}
	}

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------
	private Field getField(final Class<?> clazz, final String fieldName) {
		final Collection<Field> fields = ClassUtil.getAllFields(clazz);
		for (final Field field : fields) {
			if (fieldName.equals(field.getName())) {
				return field;
			}
		}
		Assert.fail("field " + fieldName + " non trouvé dans " + clazz.getSimpleName());
		return null;
	}

}
