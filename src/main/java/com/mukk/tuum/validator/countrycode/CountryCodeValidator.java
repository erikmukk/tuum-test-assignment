package com.mukk.tuum.validator.countrycode;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Locale;
import java.util.Set;

public class CountryCodeValidator implements ConstraintValidator<CountryCodeConstraint, String> {

    private static final Set<String> ISO_3CODE_COUNTRIES = Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA3);
    private static final String VALIDATION_MESSAGE = "invalid value '%s'. expected 3-letter country code ('USA', 'EST', ...)";

    @Override
    public void initialize(CountryCodeConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value != null) {
            final var upperCaseValue = value.toUpperCase();
            context.disableDefaultConstraintViolation();
            context
                    .buildConstraintViolationWithTemplate(String.format(VALIDATION_MESSAGE, upperCaseValue))
                    .addConstraintViolation();
            return ISO_3CODE_COUNTRIES.contains(upperCaseValue);
        }
        return false;
    }
}
