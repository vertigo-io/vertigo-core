package io.vertigo.core.definition.dsl.smart;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**Each définition has a prefix
 * @author pchretien
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target({ ElementType.FIELD })
public @interface DslField {
	boolean value();
}
