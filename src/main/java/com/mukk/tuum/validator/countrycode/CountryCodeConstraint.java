package com.mukk.tuum.validator.countrycode;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = CountryCodeValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface CountryCodeConstraint {
    String message() default "invalid country code";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
