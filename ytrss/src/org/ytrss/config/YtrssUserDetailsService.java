package org.ytrss.config;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.ytrss.db.SettingsService;

import com.google.common.collect.Lists;

public class YtrssUserDetailsService implements UserDetailsService {

	private final SettingsService	settingsService;

	public YtrssUserDetailsService(final SettingsService settingsService) {
		this.settingsService = settingsService;
	}

	@Override
	public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
		return new User(username, getPassword(), Lists.newArrayList(new SimpleGrantedAuthority("ADMIN")));
	}

	private String getPassword() {
		return settingsService.getSetting("password", BCrypt.hashpw("password", BCrypt.gensalt()), String.class);
	}

}
