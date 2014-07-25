package org.ytrss;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.ytrss.db.Video;
import org.ytrss.db.VideoDAO;
import org.ytrss.db.VideoState;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

@Component
public class PostRestartResumer {

	private static Logger	log	= LoggerFactory.getLogger(PostRestartResumer.class);

	@Autowired
	private Ripper			ripper;

	@Autowired
	private VideoDAO		videoDAO;

	private boolean fileExists(final String file) {
		if (Strings.isNullOrEmpty(file)) {
			return false;
		}
		try {
			return new File(file).exists();
		}
		catch (final Exception e) {
			return false;
		}
	}

	private void fixStates() {
		for (final Video video : videoDAO.findAll()) {
			// Is waiting for encoding but video file is missing? -> Reset to DOWNLOADING_ENQUEUED
			if (hasAnyOfStates(video, VideoState.TRANSCODING, VideoState.TRANSCODING_ENQUEUED, VideoState.TRANSCODING_FAILED)
					&& !fileExists(video.getVideoFile())) {
				log.info(video.getName() + " is marked for transcoding, but the video file is missing. Re-downloading...");
				video.setState(VideoState.DOWNLOADING_ENQUEUED);
				videoDAO.persist(video);
			}
			// Is ready but mp3 file is missing? -> Reset to DOWNLOADING_ENQUEUED or TRANSCODING_ENQUEUED
			else if (video.getState() == VideoState.READY && !fileExists(video.getMp3File())) {
				if (fileExists(video.getVideoFile())) {
					log.info(video.getName() + " is marked as ready, but the mp3 file is missing. Re-transcoding...");
					video.setState(VideoState.TRANSCODING_ENQUEUED);
				}
				else {
					log.info(video.getName() + " is marked as ready, but the mp3 and video files are missing. Re-downloading...");
					video.setState(VideoState.DOWNLOADING_ENQUEUED);
				}
				videoDAO.persist(video);
			}
		}
	}

	private boolean hasAnyOfStates(final Video video, final VideoState... states) {
		return Lists.newArrayList(states).contains(video.getState());
	}

	@PostConstruct
	private void resumeAfterRestart() {
		fixStates();

		final ExecutorService executor = Executors.newFixedThreadPool(2);
		executor.submit(() -> {
			resumeTranscoding();
		});
		executor.submit(() -> {
			resumeDownloads();
		});
	}

	private void resumeDownloads() {
		for (final Video video : videoDAO.findAll()) {
			if (hasAnyOfStates(video, VideoState.DOWNLOADING, VideoState.DOWNLOADING_ENQUEUED, VideoState.DOWNLOADING_FAILED)) {
				log.info("Resuming download of " + video.getName());
				ripper.download(video);
			}
		}
	}

	private void resumeTranscoding() {
		for (final Video video : videoDAO.findAll()) {
			if (hasAnyOfStates(video, VideoState.TRANSCODING, VideoState.TRANSCODING_ENQUEUED, VideoState.TRANSCODING_FAILED)) {
				log.info("Resuming transcoding of " + video.getName());
				final File videoFile = new File(video.getVideoFile());
				ripper.transcode(videoFile, video);
			}
		}
	}
}
