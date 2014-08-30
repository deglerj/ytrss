package org.ytrss.db;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ytrss.URLs;
import org.ytrss.youtube.ChannelPage;

public class ChannelURLValidator implements ConstraintValidator<ChannelURL, String> {

	private static Logger	log	= LoggerFactory.getLogger(ChannelURLValidator.class);

	@Override
	public void initialize(final ChannelURL constraintAnnotation) {
		// Nothing to do here
	}

	@Override
	public boolean isValid(final String value, final ConstraintValidatorContext context) {
		try {
			final String url = URLs.cleanUpURL(value) + "/videos";
			final ChannelPage page = URLs.openPage(url, s -> new ChannelPage(s));
			page.getProfileImage();
			page.getContentGridEntries(30);
			return true;
		}
		catch (final Throwable t) {
			log.info("Validating channel URL \"" + value + "\" failed with exception (expected for invalid URLs)", t);
			return false;
		}
	}

}
