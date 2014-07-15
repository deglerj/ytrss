package org.ytrss.client;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.ytrss.Ripper;
import org.ytrss.db.Channel;
import org.ytrss.db.ChannelDAO;
import org.ytrss.db.Video;
import org.ytrss.db.VideoDAO;
import org.ytrss.db.VideoState;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.sun.syndication.feed.synd.SyndEnclosure;
import com.sun.syndication.feed.synd.SyndEnclosureImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;

@Controller
public class ChannelController {

	@Autowired
	private ChannelDAO	channelDAO;

	@Autowired
	private VideoDAO	videoDAO;

	@Autowired
	private Ripper		ripper;

	@ModelAttribute
	public Channel channel() {
		return new Channel();
	}

	@RequestMapping(value = "/channel/{id}/delete", method = RequestMethod.GET)
	public String deleteChannel(@PathVariable(value = "id") final long id) {
		channelDAO.delete(id);

		return "redirect:/";
	}

	@RequestMapping(value = "/channel/{id}", method = RequestMethod.GET)
	public String getChannel(@PathVariable(value = "id") final long id, final Model model) {
		final Channel channel = channelDAO.findById(id);
		model.addAttribute("channel", channel);

		addCommonModelAttributes(channel, model);

		return "channel";
	}

	@RequestMapping(value = "/channel/{id}/feed", method = RequestMethod.GET)
	public void getFeed(@PathVariable("id") final long id, @RequestParam("token") final String token, @RequestParam("type") final String type,
			final HttpServletResponse response, final HttpServletRequest request) {

		checkArgument("rss".equals(type) || "atom".equals(type), "Type must be \"rss\" or \"atom\"");

		final Channel channel = channelDAO.findById(id);
		checkArgument(token.equals(channel.getSecurityToken()), "Token mismatch");

		final String requestURL = request.getRequestURL().toString();

		final List<Video> videos = videoDAO.findByChannelID(channel.getId());
		videos.removeIf(v -> v.getState() != VideoState.READY);
		final List<SyndEntry> entries = Lists.transform(videos, v -> createSyndEntry(v, requestURL));

		final SyndFeed feed = createFeed(channel, type, requestURL, entries);

		setFeedHeaders(channel, type, response);

		try {
			final SyndFeedOutput feedOutput = new SyndFeedOutput();
			feedOutput.output(feed, response.getWriter());
		}
		catch (IOException | FeedException e) {
			throw Throwables.propagate(e);
		}
	}

	@RequestMapping(value = "/channel", method = RequestMethod.GET)
	public String getNewChannel(final Model model) {
		final Channel channel = new Channel();
		model.addAttribute("channel", channel);

		addCommonModelAttributes(channel, model);

		return "channel";
	}

	@RequestMapping(value = "/channel/{id}", method = RequestMethod.POST)
	public String postChannel(@ModelAttribute @Validated final Channel channel, final BindingResult bindingResult, final Model model) {
		if (!bindingResult.hasErrors()) {
			channelDAO.persist(channel);
			ripper.start();
		}

		addCommonModelAttributes(channel, model);

		return "channel";
	}

	@RequestMapping(value = "/channel", method = RequestMethod.POST)
	public String postNewChannel(@ModelAttribute @Validated final Channel channel, final BindingResult bindingResult, final Model model) {
		if (bindingResult.hasErrors()) {
			addCommonModelAttributes(channel, model);
			return "channel";
		}

		channelDAO.persist(channel);
		ripper.start();

		return "redirect:/channel/" + channel.getId();
	}

	private void addCommonModelAttributes(final Channel channel, final Model model) {
		model.addAttribute("channels", createChannelIDMap());

		if (channel.getId() == null) {
			model.addAttribute("videos", Collections.emptyList());
		}
		else {
			model.addAttribute("videos", videoDAO.findByChannelID(channel.getId()));
		}
	}

	private Map<Long, Channel> createChannelIDMap() {
		final Map<Long, Channel> map = new HashMap<>();
		for (final Channel channel : channelDAO.findAll()) {
			map.put(channel.getId(), channel);
		}
		return map;
	}

	private SyndFeed createFeed(final Channel channel, final String type, final String requestURL, final List<SyndEntry> entries) {
		final SyndFeed feed = new SyndFeedImpl();

		feed.setAuthor("ytrss");
		feed.setDescription("ytrss feed for Youtube channel \"" + channel.getName() + "\"");

		feed.setLink(requestURL + "?type=" + type + "&token=" + channel.getSecurityToken());
		feed.setTitle(channel.getName());

		feed.setEntries(entries);

		if ("rss".equals(type)) {
			feed.setFeedType("rss_2.0");
		}
		else {
			feed.setFeedType("atom_1.0");
		}

		return feed;
	}

	private SyndEntry createSyndEntry(final Video video, final String requestURL) {
		final String baseURL = requestURL.replaceAll("channel/\\d+/feed.*", "");
		final String downloadURL = baseURL + "download?id=" + video.getId() + "&token=" + video.getSecurityToken();

		final SyndEntry entry = new SyndEntryImpl();
		entry.setLink(downloadURL);
		entry.setTitle(video.getName());
		entry.setPublishedDate(video.getUploaded());

		final SyndEnclosure enclosure = new SyndEnclosureImpl();
		enclosure.setUrl(downloadURL);
		enclosure.setType("audio/mpeg");
		entry.setEnclosures(Lists.newArrayList(enclosure));

		return entry;
	}

	private void setFeedHeaders(final Channel channel, final String type, final HttpServletResponse response) {
		String fileName = channel.getName().replaceAll("[^\\w\\d]", "");

		if ("rss".equals(type)) {
			response.setContentType("application/rss+xml");
			fileName += ".rss";
		}
		else {
			response.setContentType("application/atom+xml");
			fileName += ".atom";
		}

		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
	}

}
