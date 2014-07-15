package org.ytrss.db;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.google.common.base.Strings;

public class RegularExpressionValidator implements ConstraintValidator<RegularExpression, String> {

	@Override
	public void initialize(final RegularExpression constraintAnnotation) {
		// Nothing to do here
	}

	@Override
	public boolean isValid(final String value, final ConstraintValidatorContext context) {
		if (Strings.isNullOrEmpty(value)) {
			return true;
		}

		try {
			Pattern.compile(value);
			return true;
		}
		catch (final PatternSyntaxException e) {
			return false;
		}
	}

}
