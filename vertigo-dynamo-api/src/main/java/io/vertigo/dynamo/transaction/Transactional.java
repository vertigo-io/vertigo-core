package io.vertigo.dynamo.transaction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation à poser sur les méthodes des implémentations des services.
 * 
 * @author prahmoune
 * @version $Id: Transactional.java,v 1.2 2013/07/18 17:29:09 npiedeloup Exp $
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Transactional {
	// vide
}
