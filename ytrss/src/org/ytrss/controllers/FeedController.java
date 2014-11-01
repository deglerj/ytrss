package org.ytrss.controllers;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.ytrss.URLs;
import org.ytrss.db.Channel;
import org.ytrss.db.ChannelDAO;
import org.ytrss.db.Video;
import org.ytrss.db.VideoDAO;
import org.ytrss.db.VideoState;
import org.ytrss.youtube.ChannelPage;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.sun.syndication.feed.module.itunes.FeedInformation;
import com.sun.syndication.feed.module.itunes.FeedInformationImpl;
import com.sun.syndication.feed.module.mediarss.MediaModule;
import com.sun.syndication.feed.module.mediarss.MediaModuleImpl;
import com.sun.syndication.feed.module.mediarss.types.Metadata;
import com.sun.syndication.feed.module.mediarss.types.Thumbnail;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEnclosure;
import com.sun.syndication.feed.synd.SyndEnclosureImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;

@Controller
public class FeedController {

	private static Logger	log	= LoggerFactory.getLogger(FeedController.class);

	@Autowired
	private ChannelDAO		channelDAO;

	@Autowired
	private VideoDAO		videoDAO;

	@RequestMapping(value = "/channel/{id}/feed", method = RequestMethod.GET)
	public void getFeed(@PathVariable("id") final long id, @RequestParam("token") final String token, @RequestParam("type") final String type,
			final HttpServletResponse response, final HttpServletRequest request) {
		final Channel channel = channelDAO.findById(id);
		getFeed(channel, token, type, response, request, feed -> {
			addChannelPageThumbnail(channel, feed);
			return null;
		});
	}

	@RequestMapping(value = "/singles/feed", method = RequestMethod.GET)
	public void getSinglesFeed(@RequestParam("token") final String token, @RequestParam("type") final String type, final HttpServletResponse response,
			final HttpServletRequest request) {
		final Channel channel = channelDAO.findByName("Singles");
		getFeed(channel, token, type, response, request, feed -> {
			addSinglesThumbnail(feed, request);
			return null;
		});
	}

	@SuppressWarnings("unchecked")
	private void addChannelPageThumbnail(final Channel channel, final SyndFeed feed) {
		try {
			final String url = URLs.cleanUpURL(channel.getUrl()) + "/videos";
			final ChannelPage page = URLs.openPage(url, s -> new ChannelPage(s));
			final String profileImage = page.getProfileImage();

			final Thumbnail thumbnail = new Thumbnail(new URI(profileImage));

			final Metadata metadata = new Metadata();
			metadata.setThumbnail(new Thumbnail[] { thumbnail });

			final MediaModule mediaModule = new MediaModuleImpl();
			((MediaModuleImpl) mediaModule).setMetadata(metadata);
			feed.getModules().add(mediaModule);

			final FeedInformation itunesModule = new FeedInformationImpl();
			itunesModule.setImage(new URL(profileImage));
			feed.getModules().add(itunesModule);
		}
		catch (final URISyntaxException | MalformedURLException e) {
			// Create feed anyway, but log exception
			log.warn("Could not add thumbnail to feed", e);
		}
	}

	@SuppressWarnings("unchecked")
	private void addSinglesThumbnail(final SyndFeed feed, final HttpServletRequest request) {
		try {
			final String hostBaseUrl = request.getRequestURL().toString().replace("/singles/feed", "");
			final String thumbnailUrl = hostBaseUrl + "/images/singles_thumbnail.png";

			final Thumbnail thumbnail = new Thumbnail(new URI(thumbnailUrl));
			final Metadata metadata = new Metadata();
			metadata.setThumbnail(new Thumbnail[] { thumbnail });

			final MediaModule mediaModule = new MediaModuleImpl();
			((MediaModuleImpl) mediaModule).setMetadata(metadata);
			feed.getModules().add(mediaModule);

			final FeedInformation itunesModule = new FeedInformationImpl();
			itunesModule.setImage(new URL(thumbnailUrl));
			feed.getModules().add(itunesModule);
		}
		catch (final URISyntaxException | MalformedURLException e) {
			// Create feed anyway, but log exception
			log.warn("Could not add thumbnail to singles feed", e);
		}
	}

	private SyndEntry createSyndEntry(final Video video, final String requestURL) {
		final String baseURL = requestURL.replaceAll("channel/\\d+/feed.*", "");
		final String downloadURL = baseURL + "download?id=" + video.getId() + "&token=" + video.getSecurityToken();

		final SyndEntry entry = new SyndEntryImpl();
		entry.setLink(downloadURL);
		entry.setTitle(video.getName());
		entry.setPublishedDate(video.getUploaded());

		final SyndContentImpl description = new SyndContentImpl();
		description.setValue(Strings.isNullOrEmpty(video.getDescription()) ? video.getName() : video.getDescription());
		entry.setDescription(description);

		final SyndEnclosure enclosure = new SyndEnclosureImpl();
		enclosure.setUrl(downloadURL);
		enclosure.setType("audio/mpeg");
		entry.setEnclosures(Lists.newArrayList(enclosure));

		return entry;
	}

	private SyndFeed generateFeed(final Channel channel, final String requestURL, final String type) {
		final List<Video> videos = videoDAO.findByChannelID(channel.getId());
		videos.removeIf(v -> v.getState() != VideoState.READY);
		final List<SyndEntry> entries = Lists.transform(videos, v -> createSyndEntry(v, requestURL));

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

	private void getFeed(final Channel channel, final String token, final String type, final HttpServletResponse response, final HttpServletRequest request,
			final Function<SyndFeed, Void> customizeFeed) {
		checkArgument(token.equals(channel.getSecurityToken()), "Token mismatch");

		checkArgument("rss".equals(type) || "atom".equals(type), "Type must be \"rss\" or \"atom\"");

		final String requestURL = request.getRequestURL().toString();

		final SyndFeed feed = generateFeed(channel, requestURL, type);
		customizeFeed.apply(feed);
		setHeaders(channel, type, response);

		try {
			final SyndFeedOutput feedOutput = new SyndFeedOutput();
			feedOutput.output(feed, response.getWriter());
		}
		catch (IOException | FeedException e) {
			log.error("Error writing feed to HTTP response", e);
			throw Throwables.propagate(e);
		}
	}

	private void setHeaders(final Channel channel, final String type, final HttpServletResponse response) {
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
