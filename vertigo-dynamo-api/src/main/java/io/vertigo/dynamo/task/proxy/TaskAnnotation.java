package io.vertigo.dynamo.task.proxy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.vertigo.core.component.proxy.ProxyMethodAnnotation;
import io.vertigo.dynamo.task.model.TaskEngine;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ProxyMethodAnnotation
public @interface TaskAnnotation {
	Class<? extends TaskEngine> taskEngineClass();

	String name();

	String request();

	String dataSpace() default ""; //null is not allowed here
}
