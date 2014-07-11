package org.ytrss.client;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.ytrss.db.Channel;
import org.ytrss.db.ChannelDAO;
import org.ytrss.db.VideoDAO;

@Controller
public class ChannelController {

	@Autowired
	private ChannelDAO	channelDAO;

	@Autowired
	private VideoDAO	videoDAO;

	@ModelAttribute
	public Channel channel() {
		return new Channel();
	}

	@RequestMapping(value = "/channel/{id}/delete", method = RequestMethod.GET)
	public String getChannel(@PathVariable(value = "id") final long id) {
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

		return "redirect:/channel/" + channel.getId();
	}

	private void addCommonModelAttributes(final Channel channel, final Model model) {
		model.addAttribute("channels", channelDAO.findAll());

		if (channel.getId() == null) {
			model.addAttribute("videos", Collections.emptyList());
		}
		else {
			model.addAttribute("videos", videoDAO.findByChannelID(channel.getId()));
		}
	}

}
