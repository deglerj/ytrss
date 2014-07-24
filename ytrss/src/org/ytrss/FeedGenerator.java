package org.ytrss;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.ytrss.db.Channel;
import org.ytrss.db.Video;
import org.ytrss.db.VideoDAO;
import org.ytrss.db.VideoState;
import org.ytrss.youtube.ChannelPage;

import com.google.common.base.Strings;
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

@Component
@SuppressWarnings("unchecked")
public class FeedGenerator {

	@Autowired
	private VideoDAO		videoDAO;

	private static Logger	log	= LoggerFactory.getLogger(FeedGenerator.class);

	public SyndFeed generateFeed(final Channel channel, final String requestURL, final String type) {
		final List<Video> videos = videoDAO.findByChannelID(channel.getId());
		videos.removeIf(v -> v.getState() != VideoState.READY);
		final List<SyndEntry> entries = Lists.transform(videos, v -> createSyndEntry(v, requestURL));

		return createFeed(channel, type, requestURL, entries);
	}

	private SyndFeed createFeed(final Channel channel, final String type, final String requestURL, final List<SyndEntry> entries) {
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

		try {
			final String url = URLs.cleanUpURL(channel.getUrl()) + "/videos";
			final ChannelPage page = URLs.openPage(url, 10, s -> new ChannelPage(s));
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

		return feed;
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

}
