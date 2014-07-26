package org.ytrss.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.ytrss.db.ChannelDAO;

@Controller
public class StartController {

	@Autowired
	private ChannelDAO	channelDAO;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String getStart(final Model model) {
		model.addAttribute("channels", channelDAO.findAll());

		return "start";
	}
}
