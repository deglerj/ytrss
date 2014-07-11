package org.ytrss.client;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.ytrss.db.Channel;
import org.ytrss.db.ChannelDAO;
import org.ytrss.db.VideoDAO;

@Controller
public class DownloadsController {

	@Autowired
	private ChannelDAO	channelDAO;

	@Autowired
	private VideoDAO	videoDAO;

	@RequestMapping("/")
	public String showDownloads(final Model model) {
		model.addAttribute("channels", createChannelIDMap());
		model.addAttribute("videos", videoDAO.findAll());

		return "downloads";
	}

	private Map<Long, Channel> createChannelIDMap() {
		final Map<Long, Channel> map = new HashMap<>();
		for (final Channel channel : channelDAO.findAll()) {
			map.put(channel.getId(), channel);
		}
		return map;
	}

}
