package org.ytrss.client;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.ytrss.db.Channel;
import org.ytrss.db.ChannelDAO;
import org.ytrss.db.VideoDAO;

@Controller
public class ChannelController {

	@Autowired
	private ChannelDAO	channelDAO;

	@Autowired
	private VideoDAO	videoDAO;

	@RequestMapping("/channel")
	public String createChannel(final Model model) {
		return showChannel(new Channel(), model);
	}

	@RequestMapping("/channel/{id}")
	public String showChannel(@PathVariable(value = "id") final long id, final Model model) {
		return showChannel(channelDAO.findById(id), model);
	}

	private String showChannel(final Channel channel, final Model model) {
		model.addAttribute("channel", channel);
		model.addAttribute("channels", channelDAO.findAll());

		if (channel.getId() == null) {
			model.addAttribute("videos", Collections.emptyList());
		}
		else {
			model.addAttribute("videos", videoDAO.findByChannelID(channel.getId()));
		}

		return "channel";
	}

}
