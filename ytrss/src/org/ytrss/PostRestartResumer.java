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

@Component
public class PostRestartResumer {

	private static Logger	log	= LoggerFactory.getLogger(PostRestartResumer.class);

	@Autowired
	private Ripper			ripper;

	@Autowired
	private VideoDAO		videoDAO;

	@PostConstruct
	private void resumeAfterRestart() {
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
			if (video.getState() == VideoState.DOWNLOADING || video.getState() == VideoState.DOWNLOADING_ENQUEUED
					|| video.getState() == VideoState.DOWNLOADING_FAILED) {
				log.info("Resuming download of " + video.getName());
				ripper.download(video);
			}
		}
	}

	private void resumeTranscoding() {
		for (final Video video : videoDAO.findAll()) {
			if (video.getState() == VideoState.TRANSCODING || video.getState() == VideoState.TRANSCODING_ENQUEUED
					|| video.getState() == VideoState.TRANSCODING_FAILED) {
				log.info("Resuming transcoding of " + video.getName());
				final File videoFile = new File(video.getVideoFile());
				ripper.transcode(videoFile, video);
			}
		}
	}

}
