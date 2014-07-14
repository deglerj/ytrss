package org.ytrss.client;

import static com.google.common.base.Preconditions.checkState;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.ytrss.db.Channel;
import org.ytrss.db.ChannelDAO;
import org.ytrss.db.Video;
import org.ytrss.db.VideoDAO;

import com.google.common.base.Throwables;

@Controller
public class DownloadsController {

	@Autowired
	private ChannelDAO	channelDAO;

	@Autowired
	private VideoDAO	videoDAO;

	@RequestMapping(value = "/download/{id}", method = RequestMethod.GET)
	public void getDownload(@PathVariable(value = "id") final long id, final HttpServletResponse response) {
		final Video video = videoDAO.findById(id);

		final File file = new File(video.getMp3File());
		checkState(file.exists(), "MP3 file for video #" + video.getId() + " does not exist");

		response.setContentType("audio/mpeg");
		response.setContentLength(new Long(file.length()).intValue());
		response.setHeader("Content-Disposition", "attachment; filename=Test.xls");

		try (final InputStream in = new FileInputStream(file); OutputStream out = response.getOutputStream()) {
			IOUtils.copy(in, out);
		}
		catch (final IOException e) {
			throw Throwables.propagate(e);
		}
	}

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String getDownloads(final Model model) {
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
