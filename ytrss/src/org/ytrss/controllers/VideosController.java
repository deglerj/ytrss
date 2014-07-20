package org.ytrss.controllers;

import static argo.jdom.JsonNodeFactories.array;
import static argo.jdom.JsonNodeFactories.field;
import static argo.jdom.JsonNodeFactories.number;
import static argo.jdom.JsonNodeFactories.object;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ytrss.Dates;
import org.ytrss.Ripper;
import org.ytrss.db.Channel;
import org.ytrss.db.ChannelDAO;
import org.ytrss.db.Video;
import org.ytrss.db.VideoDAO;
import org.ytrss.db.VideoState;

import argo.format.CompactJsonFormatter;
import argo.format.JsonFormatter;
import argo.jdom.JsonField;
import argo.jdom.JsonNode;
import argo.jdom.JsonNodeFactories;
import argo.jdom.JsonRootNode;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

@Controller
public class VideosController {

	@Autowired
	private VideoDAO			videoDAO;

	@Autowired
	private ChannelDAO			channelDAO;

	@Autowired
	private Ripper				ripper;

	private final JsonFormatter	jsonFormatter	= new CompactJsonFormatter();

	@RequestMapping(value = "/videos", method = RequestMethod.GET)
	public @ResponseBody String getVideos(@RequestParam(value = "channel", required = false) final Long channelID) {
		final List<Video> videos = channelID == null ? videoDAO.findAll() : videoDAO.findByChannelID(channelID);

		final String countdown = Dates.formatAsPrettyString(ripper.getCountdown(), TimeUnit.MINUTES, TimeUnit.SECONDS);

		final JsonField videosField = field("videos", array(Lists.transform(videos, this::createVideoNode)));
		final JsonField countdownField = field("countdown", string(countdown, true));
		final JsonRootNode json = object(videosField, countdownField);

		return jsonFormatter.format(json);

	}

	@RequestMapping(value = "/videos/reset", method = RequestMethod.GET)
	public @ResponseBody String resetVideo(@RequestParam("id") final long videoID) {
		final Video video = videoDAO.findById(videoID);

		video.setState(VideoState.NEW);
		video.setErrorMessage(null);
		video.setMp3File(null);
		video.setVideoFile(null);
		videoDAO.persist(video);

		ripper.download(video);

		return "reset";
	}

	private JsonNode createVideoNode(final Video video) {
		final Channel channel = channelDAO.findById(video.getChannelID());

		final String uploaded = SimpleDateFormat.getDateInstance().format(video.getUploaded());
		//@formatter:off
		return object(
				field("id", number(video.getId())),
				field("uploaded", string(uploaded, true)),
				field("channelName", string(channel.getName(), true)),
				field("channelID", number(channel.getId())),
				field("name", string(video.getName(), true)),
				field("youtubeID", string(video.getYoutubeID(), false)),
				field("state", number(video.getState().ordinal())),
				field("errorMessage", string(video.getErrorMessage(), true)),
				field("securityToken", string(video.getSecurityToken(), false))
				);
		//@formatter:on
	}

	private JsonNode string(final String string, final boolean escapeHtml) {
		final String nonNull = Strings.nullToEmpty(string);
		if (escapeHtml) {
			return JsonNodeFactories.string(StringEscapeUtils.escapeHtml4(nonNull));
		}
		else {
			return JsonNodeFactories.string(nonNull);
		}
	}
}
