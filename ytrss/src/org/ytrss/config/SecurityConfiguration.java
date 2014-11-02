package org.ytrss.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.ytrss.db.SettingsService;

@Configuration
@EnableWebSecurity
// Based on: https://github.com/jasonish/jetty-springmvc-jsp-template
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Autowired
	private UserDetailsService	userDetailsService;

	@Bean
	public UserDetailsService getUserDetailsService(final SettingsService settingsService) {
		return new YtrssUserDetailsService(settingsService);
	}

	@Override
	protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(new BCryptPasswordEncoder());
	}

	@Override
	protected void configure(final HttpSecurity http) throws Exception {
		// Allow anonymous access to feed and mp3 downloads
		http.authorizeRequests().antMatchers("/download").permitAll();
		http.authorizeRequests().antMatchers("/channel/**/feed").permitAll();
		http.authorizeRequests().antMatchers("/singles/feed").permitAll();
		http.authorizeRequests().antMatchers("/images/**").permitAll();

		// Require authentication for all other pages
		http.authorizeRequests().anyRequest().hasAuthority("ADMIN");

		// Use HTTP basic authentication
		http.httpBasic();

		// Disable CSRF protection (doesn't seem to work with HTTP basic authentication)
		http.csrf().disable();
	}

}
