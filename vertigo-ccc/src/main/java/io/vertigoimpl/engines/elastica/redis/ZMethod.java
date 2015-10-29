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
package io.vertigoimpl.engines.elastica.redis;

import io.vertigo.core.Home;
import io.vertigo.util.ClassUtil;

import java.io.Serializable;
import java.lang.reflect.Method;

public final class ZMethod implements Serializable {
	private static final long serialVersionUID = -8895268815245300635L;

	//	private final Class beanClass;
	private final String apiClassName;
	private final String[] parameterTypeNames;
	private final String methodName;
	//	private final Class beanClass;
	private final Serializable[] args;

	public ZMethod(final Class<?> apiClass, final Method method, final Object[] args) {
		//	apiClass = apiClass;
		apiClassName = apiClass.getName();
		methodName = method.getName();
		parameterTypeNames = new String[method.getParameterTypes().length];
		for (int i = 0; i < method.getParameterTypes().length; i++) {
			parameterTypeNames[i] = method.getParameterTypes()[i].getName();
		}

		this.args = new Serializable[args.length];
		for (int i = 0; i < args.length; i++) {
			//System.out.println(">>>[i]>>" + args[i].getClass().getSimpleName());
			this.args[i] = (Serializable) args[i];
		}
	}

	private Class<?> getApiClass() {
		return ClassUtil.classForName(apiClassName);
	}

	Object run() {
		final Object component = Home.getApp().getComponentSpace().resolve(getApiClass());

		final Class<?>[] parameterTypes = new Class[parameterTypeNames.length];
		for (int i = 0; i < parameterTypeNames.length; i++) {
			parameterTypes[i] = ClassUtil.classForName(parameterTypeNames[i]);
		}
		final Method method = ClassUtil.findMethod(component.getClass(), methodName, parameterTypes);

		//		ClassUtil.findMethod(clazz, methodName, parameterTypes)
		return ClassUtil.invoke(component, method, (Object[]) args);
	}
	/*
	 * private void writeObject(final ObjectOutputStream oos) throws IOException { oos.writeUTF(method.getName()); oos.write); // default serialization oos.defaultWriteObject();
	 * oos.defaultWriteObject(); // write the object loc.add(location.x); loc.add(location.y); loc.add(location.z); loc.add(location.uid); oos.writeObject(loc); } private void readObject(final
	 * ObjectInputStream ois) throws ClassNotFoundException, IOException { // default deserialization ois.defaultReadObject(); final List loc = (List) ois.readObject(); // Replace with real
	 * deserialization location = new Location(loc.get(0), loc.get(1), loc.get(2), loc.get(3)); // ... more code }
	 */
}
