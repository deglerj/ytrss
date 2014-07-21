package org.ytrss.controllers;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.ytrss.FeedGenerator;
import org.ytrss.Ripper;
import org.ytrss.db.Channel;
import org.ytrss.db.ChannelDAO;
import org.ytrss.db.UniqueChannelNameValidator;
import org.ytrss.db.VideoDAO;

import com.google.common.base.Throwables;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;

@Controller
public class ChannelController {

	@Autowired
	private ChannelDAO					channelDAO;

	@Autowired
	private VideoDAO					videoDAO;

	@Autowired
	private FeedGenerator				generator;

	@Autowired
	private Ripper						ripper;

	private static Logger				log	= LoggerFactory.getLogger(ChannelController.class);

	@Autowired
	private UniqueChannelNameValidator	uniqueChannelNameValidator;

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

		final SyndFeed feed = generator.generateFeed(channel, requestURL, type);
		setFeedHeaders(channel, type, response);

		try {
			final SyndFeedOutput feedOutput = new SyndFeedOutput();
			feedOutput.output(feed, response.getWriter());
		}
		catch (IOException | FeedException e) {
			log.error("Error writing feed to HTTP response", e);
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
	}

	private Map<Long, Channel> createChannelIDMap() {
		final Map<Long, Channel> map = new HashMap<>();
		for (final Channel channel : channelDAO.findAll()) {
			map.put(channel.getId(), channel);
		}
		return map;
	}

	@InitBinder
	private void initBinder(final WebDataBinder binder) {
		binder.addValidators(uniqueChannelNameValidator);
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
