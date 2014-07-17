package org.ytrss.controllers;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.ytrss.db.SettingsService;

import com.google.common.base.Strings;

@Controller
public class SettingsController {

	public static class SettingsForm {

		private String	password;

		private String	password2;

		public SettingsForm() {
			// Empty default constructor
		}

		public SettingsForm(final String password, final String password2) {
			this.password = password;
			this.password2 = password2;
		}

		public String getPassword() {
			return password;
		}

		public String getPassword2() {
			return password2;
		}

		public void setPassword(final String password) {
			this.password = password;
		}

		public void setPassword2(final String password2) {
			this.password2 = password2;
		}

	}

	@Autowired
	private SettingsService	settingsService;

	@RequestMapping(value = "/settings", method = RequestMethod.GET)
	public String getSettings(final Model model) {
		model.addAttribute("settingsForm", new SettingsForm("", ""));

		return "settings";
	}

	@RequestMapping(value = "/settings", method = RequestMethod.POST)
	public String postSettings(@ModelAttribute @Validated final SettingsForm settingsForm, final BindingResult bindingResult, final Model model,
			final HttpServletRequest request) throws ServletException {
		validatePasswords(settingsForm, bindingResult);

		if (bindingResult.hasErrors()) {
			return "settings";
		}

		if (hasPasswordChanged(settingsForm)) {
			updatePassword(settingsForm.getPassword());
			request.logout();
		}

		return "redirect:/";
	}

	private boolean hasPasswordChanged(final SettingsForm settingsForm) {
		return !Strings.isNullOrEmpty(settingsForm.getPassword()) && !Strings.isNullOrEmpty(settingsForm.getPassword2());
	}

	private void updatePassword(final String password) {
		final String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
		settingsService.setSetting("password", hashedPassword);
	}

	private void validatePasswords(final SettingsForm settingsForm, final BindingResult bindingResult) {
		final String password = Strings.nullToEmpty(settingsForm.getPassword()).trim();
		final String password2 = Strings.nullToEmpty(settingsForm.getPassword2()).trim();

		if (!password.equals(password2)) {
			bindingResult.addError(new FieldError("settingsForm", "password2", "passwords must match"));
		}
	}

}
