package org.ytrss.controllers;

import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.ytrss.JsonVideosSerializer;
import org.ytrss.Patterns;
import org.ytrss.Ripper;
import org.ytrss.URLs;
import org.ytrss.db.Channel;
import org.ytrss.db.ChannelDAO;
import org.ytrss.db.Video;
import org.ytrss.db.VideoDAO;
import org.ytrss.youtube.VideoPage;

@Controller
public class SinglesController {

	public static class SinglesForm {

		@NotBlank
		@URL
		private String	url;

		public SinglesForm() {
			// Empty default constructor
		}

		public SinglesForm(final String url) {
			this.url = url;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(final String url) {
			this.url = url;
		}

	}

	@Autowired
	private ChannelDAO				channelDAO;

	@Autowired
	private Ripper					ripper;

	@Autowired
	private JsonVideosSerializer	videosSerializer;

	@Autowired
	private VideoDAO				videoDAO;

	private static Logger			log					= LoggerFactory.getLogger(SinglesController.class);

	private Channel					singlesChannel;

	private static final Pattern	YOUTUBE_ID_PATTERN	= Pattern.compile("v=([\\w\\d]+)");

	@RequestMapping(value = "/singles", method = RequestMethod.GET)
	public String getSingles(final Model model) throws Exception {
		model.addAttribute("singlesForm", new SinglesForm());
		addCommonModelAttributes(model);

		return "singles";
	}

	@RequestMapping(value = "/singles", method = RequestMethod.POST)
	public String postSingles(final Model model, @ModelAttribute @Validated final SinglesForm singlesForm, final BindingResult bindingResult) throws Exception {
		if (!bindingResult.hasErrors()) {
			if (add(singlesForm.getUrl())) {
				singlesForm.setUrl(null);
			}
			else {
				bindingResult.addError(new FieldError("singlesForm", "url", "must be a valid YouTube video URL"));
			}
		}

		model.addAttribute("singlesForm", singlesForm);
		addCommonModelAttributes(model);

		return "singles";
	}

	private boolean add(final String url) {
		try {
			final String internalURL = processUserURL(url);

			final VideoPage page = URLs.openPage(internalURL, s -> new VideoPage(s));
			final Video video = videoDAO.create(singlesChannel, page);
			ripper.download(video);
			return true;
		}
		// Any exception thrown at this point was most likely caused by an invalid URL -> Blame the user :)
		catch (final Exception e) {
			log.error("Could not add single video for URL \"{}\"", url, e);
			return false;
		}
	}

	private void addCommonModelAttributes(final Model model) throws UnsupportedEncodingException {
		model.addAttribute("channel", getSinglesChannel());
		model.addAttribute("channels", channelDAO.findAll());
		model.addAttribute("initialVideos", videosSerializer.serialize(videoDAO.findByChannelID(getSinglesChannel().getId())));
	}

	private Channel getSinglesChannel() {
		if (singlesChannel == null) {
			singlesChannel = channelDAO.findByName("Singles");
		}
		return singlesChannel;
	}

	private String processUserURL(final String url) {
		final String youtubeId = Patterns.getMatchGroup(YOUTUBE_ID_PATTERN, 1, url);
		return "http://youtube.com/watch?v=" + youtubeId + "&gl=gb&hl=en"; // Force locale to make date parsing easier
	}

}
