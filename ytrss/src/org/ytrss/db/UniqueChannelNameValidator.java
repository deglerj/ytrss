package org.ytrss.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.google.common.base.Strings;

@Component
public class UniqueChannelNameValidator implements Validator {

	@Autowired
	private ChannelDAO	channelDAO;

	@Override
	public boolean supports(final Class<?> clazz) {
		return Channel.class.equals(clazz);
	}

	@Override
	public void validate(final Object target, final Errors errors) {
		final Channel channel = (Channel) target;
		for (final Channel other : channelDAO.findAll()) {
			if ((channel.getId() == null || !channel.getId().equals(other.getId())) && Strings.nullToEmpty(channel.getName()).equals(other.getName())) {
				errors.rejectValue("name", "nonUniqueChannelName", "must be unique");
			}
		}
	}

}
