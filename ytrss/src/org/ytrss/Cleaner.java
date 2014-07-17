package org.ytrss;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.ytrss.db.Channel;
import org.ytrss.db.ChannelDAO;
import org.ytrss.db.Video;
import org.ytrss.db.VideoDAO;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

@Component
public class Cleaner {

	private static final long	MAX_VIDEOS_PER_CHANNEL	= 30;

	private static final long	MIN_FILE_AGE			= TimeUnit.DAYS.toMillis(1);

	@Autowired
	private VideoDAO			videoDAO;

	@Autowired
	private ChannelDAO			channelDAO;

	private static Logger		log						= LoggerFactory.getLogger(Cleaner.class);

	@Scheduled(fixedDelay = 360000)
	private void cleanUp() {
		cleanUpOldVideoEntities();

		cleanUpOrphanedVideoFiles();
		cleanUpOrphanedMP3Files();
	}

	private void cleanUpOldVideoEntities() {
		for (final Channel channel : channelDAO.findAll()) {
			final List<Video> videos = videoDAO.findByChannelID(channel.getId());
			if (videos.size() > MAX_VIDEOS_PER_CHANNEL) {
				videos.subList(30, videos.size()).forEach(v -> channelDAO.delete(v.getId()));
			}
		}
	}

	private void cleanUpOrphanedMP3Files() {
		final List<String> filesToKeep = Lists.transform(videoDAO.findAll(), v -> v.getMp3File());
		filesToKeep.removeIf(s -> Strings.isNullOrEmpty(s));

		final String directory = System.getProperty("user.home") + "/.ytrss/mp3s/";

		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directory))) {
			for (final Path path : directoryStream) {
				if (!isOldFile(path)) {
					continue;
				}

				final String fileName = path.toString();
				if (!filesToKeep.contains(fileName) && fileName.endsWith(".mp3")) {
					try {
						log.info("Cleaning up mp3 file " + fileName);
						Files.delete(path);
					}
					catch (final IOException e) {
						log.warn("Could not delete mp3 file " + fileName, e);
					}
				}
			}
		}
		catch (final IOException e) {
			log.warn("Could not list content of mp3 directory", e);
		}
	}

	private void cleanUpOrphanedVideoFiles() {
		final List<String> filesToKeep = Lists.transform(videoDAO.findAll(), v -> v.getVideoFile());
		filesToKeep.removeIf(s -> Strings.isNullOrEmpty(s));

		final String directory = System.getProperty("user.home") + "/.ytrss/videos/";

		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directory))) {
			for (final Path path : directoryStream) {
				if (!isOldFile(path)) {
					continue;
				}

				final String fileName = path.toString();
				if (!filesToKeep.contains(fileName) && fileName.matches(".+\\.(?:mp4|webm|flv|3gp|avi)")) {
					try {
						log.info("Cleaning up video file " + fileName);
						Files.delete(path);
					}
					catch (final IOException e) {
						log.warn("Could not delete video file " + fileName, e);
					}
				}
			}
		}
		catch (final IOException e) {
			log.warn("Could not list content of video directory", e);
		}
	}

	private boolean isOldFile(final Path path) throws IOException {
		final BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
		final Instant lastModified = attributes.lastModifiedTime() == null ? attributes.creationTime().toInstant() : attributes.lastModifiedTime().toInstant();
		final long age = lastModified.until(Instant.now(), ChronoUnit.MILLIS);
		return age > MIN_FILE_AGE;
	}

}
