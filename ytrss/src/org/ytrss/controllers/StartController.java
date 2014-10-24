package org.ytrss.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.ytrss.JsonVideosSerializer;
import org.ytrss.db.ChannelDAO;
import org.ytrss.db.VideoDAO;

@Controller
public class StartController {

	@Autowired
	private ChannelDAO				channelDAO;

	@Autowired
	private VideoDAO				videoDAO;

	@Autowired
	private JsonVideosSerializer	videosSerializer;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String getStart(final Model model) throws Exception {
		model.addAttribute("channels", channelDAO.findAll());

		model.addAttribute("initialVideos", videosSerializer.serialize(videoDAO.findAll()));

		return "start";
	}
}
