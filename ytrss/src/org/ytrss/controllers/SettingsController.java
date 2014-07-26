package org.ytrss.controllers;

import java.io.File;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.ytrss.db.ChannelDAO;
import org.ytrss.db.SettingsService;

import com.google.common.base.Strings;

@Controller
public class SettingsController {

	public static class SettingsForm {

		private String	password;

		private String	password2;

		private Integer	port;

		private String	files;

		private Integer	downloaderThreads;

		private Integer	transcoderThreads;

		public SettingsForm() {
			// Empty default constructor
		}

		public SettingsForm(final String password, final String password2, final Integer port, final String files, final Integer downloaderThreads,
				final Integer transcoderThreads) {
			this.password = password;
			this.password2 = password2;
			this.port = port;
			this.files = files;
			this.downloaderThreads = downloaderThreads;
			this.transcoderThreads = transcoderThreads;
		}

		public Integer getDownloaderThreads() {
			return downloaderThreads;
		}

		public String getFiles() {
			return files;
		}

		public String getPassword() {
			return password;
		}

		public String getPassword2() {
			return password2;
		}

		public Integer getPort() {
			return port;
		}

		public Integer getTranscoderThreads() {
			return transcoderThreads;
		}

		public void setDownloaderThreads(final Integer downloaderThreads) {
			this.downloaderThreads = downloaderThreads;
		}

		public void setFiles(final String files) {
			this.files = files;
		}

		public void setPassword(final String password) {
			this.password = password;
		}

		public void setPassword2(final String password2) {
			this.password2 = password2;
		}

		public void setPort(final Integer port) {
			this.port = port;
		}

		public void setTranscoderThreads(final Integer transcoderThreads) {
			this.transcoderThreads = transcoderThreads;
		}

	}

	@Autowired
	private SettingsService	settingsService;

	private static Logger	log	= LoggerFactory.getLogger(SettingsController.class);

	@Autowired
	private ChannelDAO		channelDAO;

	@RequestMapping(value = "/settings", method = RequestMethod.GET)
	public String getSettings(final Model model) {
		final Integer port = settingsService.getSetting("port", Integer.class);
		final String files = settingsService.getSetting("files", String.class);
		final Integer downloaderThreads = settingsService.getSetting("downloaderThreads", Integer.class);
		final Integer transcoderThreads = settingsService.getSetting("transcoderThreads", Integer.class);

		model.addAttribute("settingsForm", new SettingsForm("", "", port, files, downloaderThreads, transcoderThreads));
		model.addAttribute("channels", channelDAO.findAll());

		return "settings";
	}

	@RequestMapping(value = "/settings", method = RequestMethod.POST)
	public String postSettings(@ModelAttribute @Validated final SettingsForm settingsForm, final BindingResult bindingResult, final Model model,
			final HttpServletRequest request) throws ServletException {
		validatePasswords(settingsForm, bindingResult);
		validatePort(settingsForm.getPort(), bindingResult);
		validateFiles(settingsForm.getFiles(), bindingResult);
		validateDownloaderThreads(settingsForm.getDownloaderThreads(), bindingResult);
		validateTranscoderThreads(settingsForm.getTranscoderThreads(), bindingResult);

		if (bindingResult.hasErrors()) {
			return "settings";
		}

		if (hasPasswordChanged(settingsForm)) {
			updatePassword(settingsForm.getPassword());
			request.logout();
		}

		settingsService.setSetting("files", settingsForm.getFiles());
		settingsService.setSetting("port", settingsForm.getPort());
		settingsService.setSetting("downloaderThreads", settingsForm.getDownloaderThreads());
		settingsService.setSetting("transcoderThreads", settingsForm.getTranscoderThreads());

		return "redirect:/";
	}

	private boolean hasPasswordChanged(final SettingsForm settingsForm) {
		return !Strings.isNullOrEmpty(settingsForm.getPassword()) && !Strings.isNullOrEmpty(settingsForm.getPassword2());
	}

	private void updatePassword(final String password) {
		final String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
		settingsService.setSetting("password", hashedPassword);
	}

	private void validateDownloaderThreads(final Integer downloaderThreads, final BindingResult bindingResult) {
		if (downloaderThreads == null) {
			bindingResult.addError(new FieldError("settingsForm", "downloaderThreads", "must not be empty"));
		}
		else if (downloaderThreads < 1) {
			bindingResult.addError(new FieldError("settingsForm", "downloaderThreads", "must at least be 1"));
		}
	}

	private void validateFiles(final String files, final BindingResult bindingResult) {
		try {
			final File dir = new File(files);
			if (dir.exists()) {
				if (!dir.isDirectory()) {
					bindingResult.addError(new FieldError("settingsForm", "files", "is not a directory"));
				}
			}
			else {
				if (!dir.mkdirs()) {
					bindingResult.addError(new FieldError("settingsForm", "files", "could not create directory"));
				}
			}
		}
		catch (final Throwable t) {
			log.info("Validating directory \"" + files + "\" failed with exception (expected for invalid directories)", t);
			bindingResult.addError(new FieldError("settingsForm", "files", "is not a valid, writeable directory"));
		}
	}

	private void validatePasswords(final SettingsForm settingsForm, final BindingResult bindingResult) {
		final String password = Strings.nullToEmpty(settingsForm.getPassword()).trim();
		final String password2 = Strings.nullToEmpty(settingsForm.getPassword2()).trim();

		if (!password.equals(password2)) {
			bindingResult.addError(new FieldError("settingsForm", "password2", "passwords must match"));
		}
	}

	private void validatePort(final Integer port, final BindingResult bindingResult) {
		if (port == null) {
			bindingResult.addError(new FieldError("settingsForm", "port", "must not be empty"));
		}
		else if (port < 1 || port > 65535) {
			bindingResult.addError(new FieldError("settingsForm", "port", "must be between 1 and 65535"));
		}
	}

	private void validateTranscoderThreads(final Integer transcoderThreads, final BindingResult bindingResult) {
		if (transcoderThreads == null) {
			bindingResult.addError(new FieldError("settingsForm", "transcoderThreads", "must not be empty"));
		}
		else if (transcoderThreads < 1) {
			bindingResult.addError(new FieldError("settingsForm", "transcoderThreads", "must at least be 1"));
		}
	}

}
