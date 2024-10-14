package com.csci318.microservice.cart.Utils.Annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Our own copy of custom annotations.
// While the idea of annotations like these is nice, JPA entity relationships have runtime effects
// that are undesirable in our design.

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface ManyToOne {
    @SuppressWarnings("rawtypes")
	public Class targetEntity() default Void.class;

    public boolean optional() default false;
}
