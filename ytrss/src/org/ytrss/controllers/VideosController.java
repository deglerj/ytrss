package org.ytrss.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ytrss.Ripper;
import org.ytrss.db.Video;
import org.ytrss.db.VideoDAO;
import org.ytrss.db.VideoState;

@Controller
public class VideosController {

	@Autowired
	private VideoDAO	videoDAO;

	@Autowired
	private Ripper		ripper;

	@RequestMapping(value = "/videos/delete", method = RequestMethod.GET)
	public @ResponseBody String deleteVideo(@RequestParam("id") final long videoID) {
		final Video video = videoDAO.findById(videoID);

		video.setState(VideoState.DELETED);
		video.setErrorMessage(null);
		video.setMp3File(null);
		video.setVideoFile(null);
		videoDAO.persist(video);

		return "deleted";
	}

	@RequestMapping(value = "/videos/forceDownload", method = RequestMethod.GET)
	public @ResponseBody String forceDownload(@RequestParam("id") final long videoID) {
		final Video video = videoDAO.findById(videoID);

		ripper.download(video);

		return "downloading";
	}

	@RequestMapping(value = "/videos/forceUpdate", method = RequestMethod.GET)
	public @ResponseBody String forceUpdate() {
		if (ripper.isActive()) {
			return "ignored";
		}
		else {
			ripper.start();
			return "updating";
		}
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

		return "reseted";
	}

}
