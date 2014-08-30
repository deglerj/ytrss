package org.ytrss.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.ytrss.db.Video;
import org.ytrss.db.VideoDAO;

@Controller
public class StreamController {

	@Autowired
	private VideoDAO	videoDAO;

	@RequestMapping(value = "/stream", method = RequestMethod.GET)
	public String getStream(@RequestParam(value = "id") final long id, final Model model) {
		final Video video = videoDAO.findById(id);
		model.addAttribute("video", video);

		return "stream";
	}

}
