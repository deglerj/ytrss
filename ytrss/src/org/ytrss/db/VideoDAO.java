package org.ytrss.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.ytrss.youtube.VideoPage;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.eventbus.EventBus;

@Component
@Transactional
public class VideoDAO {

	private static class InsertStatementCreator implements PreparedStatementCreator {

		private final Video	video;

		public InsertStatementCreator(final Video video) {
			this.video = video;
		}

		@Override
		public PreparedStatement createPreparedStatement(final Connection con) throws SQLException {
			final PreparedStatement stmt = con
					.prepareStatement(
							"INSERT INTO \"VIDEO\" (\"CHANNEL_FK\", \"YOUT_ID\", \"NAME\", \"UPLOADED\", \"DISCOVERED\", \"STATE\", \"VIDEO_FILE\", \"MP3_FILE\", \"ERROR_MESSAGE\", \"SECURITY_TOKEN\", \"DESCRIPTION\") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
							Statement.RETURN_GENERATED_KEYS);

			stmt.setLong(1, video.getChannelID());
			stmt.setString(2, video.getYoutubeID());
			stmt.setString(3, video.getName());
			stmt.setDate(4, video.getUploaded());
			stmt.setTimestamp(5, video.getDiscovered());
			stmt.setInt(6, video.getState().ordinal());

			if (video.getVideoFile() == null) {
				stmt.setNull(7, Types.LONGVARCHAR);
			}
			else {
				stmt.setString(7, video.getVideoFile());
			}

			if (video.getMp3File() == null) {
				stmt.setNull(8, Types.LONGVARCHAR);
			}
			else {
				stmt.setString(8, video.getMp3File());
			}

			if (video.getErrorMessage() == null) {
				stmt.setNull(9, Types.LONGVARCHAR);
			}
			else {
				stmt.setString(9, video.getErrorMessage());
			}

			if (video.getSecurityToken() == null) {
				stmt.setNull(10, Types.LONGVARCHAR);
			}
			else {
				stmt.setString(10, video.getSecurityToken());
			}

			if (video.getDescription() == null) {
				stmt.setNull(11, Types.LONGVARCHAR);
			}
			else {
				stmt.setString(11, video.getDescription());
			}

			return stmt;
		}

	}

	private static class UpdateStatementCreator implements PreparedStatementCreator {

		private final Video	video;

		public UpdateStatementCreator(final Video video) {
			this.video = video;
		}

		@Override
		public PreparedStatement createPreparedStatement(final Connection con) throws SQLException {
			final PreparedStatement stmt = con
					.prepareStatement("UPDATE \"VIDEO\" SET \"CHANNEL_FK\" = ?, \"YOUT_ID\" = ?, \"NAME\" = ?, \"UPLOADED\" = ?, \"DISCOVERED\" = ?, \"STATE\" = ?, \"VIDEO_FILE\" = ?, \"MP3_FILE\" = ?, \"ERROR_MESSAGE\" = ?, \"DESCRIPTION\" = ? WHERE \"ID\" = ?");

			stmt.setLong(1, video.getChannelID());
			stmt.setString(2, video.getYoutubeID());
			stmt.setString(3, video.getName());
			stmt.setDate(4, video.getUploaded());
			stmt.setTimestamp(5, video.getDiscovered());
			stmt.setInt(6, video.getState().ordinal());

			if (video.getVideoFile() == null) {
				stmt.setNull(7, Types.LONGVARCHAR);
			}
			else {
				stmt.setString(7, video.getVideoFile());
			}

			if (video.getMp3File() == null) {
				stmt.setNull(8, Types.LONGVARCHAR);
			}
			else {
				stmt.setString(8, video.getMp3File());
			}

			if (video.getErrorMessage() == null) {
				stmt.setNull(9, Types.LONGVARCHAR);
			}
			else {
				stmt.setString(9, video.getErrorMessage());
			}

			if (video.getDescription() == null) {
				stmt.setNull(10, Types.LONGVARCHAR);
			}
			else {
				stmt.setString(10, video.getDescription());
			}

			stmt.setLong(11, video.getId());

			return stmt;
		}

	}

	@Autowired
	private JdbcTemplate			jdbcTemplate;

	private final RowMapper<Video>	rowMapper	= (rs, rowNum) -> {
													final Video video = new Video();
													video.setId(rs.getLong("id"));
													video.setChannelID(rs.getLong("channel_fk"));
													video.setYoutubeID(rs.getString("yout_id"));
													video.setName(rs.getString("name"));
													video.setUploaded(rs.getDate("uploaded"));
													video.setDiscovered(rs.getTimestamp("discovered"));
													video.setState(VideoState.values()[rs.getInt("state")]);
													video.setVideoFile(rs.getString("video_file"));
													video.setMp3File(rs.getString("mp3_file"));
													video.setErrorMessage(rs.getString("error_message"));
													video.setSecurityToken(rs.getString("security_token"));
													video.setDescription(rs.getString("description"));
													return video;
												};

	@Autowired
	private EventBus				eventBus;

	public Video create(final Channel channel, final VideoPage videoPage) {
		final Video video = new Video();
		video.setChannelID(channel.getId());
		video.setDiscovered(new Timestamp(System.currentTimeMillis()));
		video.setName(videoPage.getTitle());
		video.setState(VideoState.NEW);
		video.setYoutubeID(videoPage.getVideoID());
		video.setUploaded(videoPage.getUploaded());
		video.setDescription(videoPage.getDescription());
		persist(video);
		return video;
	}

	public void delete(final long id) {
		jdbcTemplate.update("DELETE FROM \"VIDEO\" WHERE \"ID\" = ? ", id);

		eventBus.post(new ServerStateChangeEvent());
	}

	@Transactional(readOnly = true)
	public List<Video> findAll() {
		return jdbcTemplate.query("SELECT * FROM \"VIDEO\" ORDER BY \"UPLOADED\" DESC, \"DISCOVERED\" DESC", rowMapper);
	}

	@Transactional(readOnly = true)
	public List<Video> findByChannelID(final long channelID) {
		return jdbcTemplate.query("SELECT * FROM \"VIDEO\" WHERE \"CHANNEL_FK\" = ? ORDER BY \"UPLOADED\" DESC, \"DISCOVERED\" DESC", rowMapper, channelID);
	}

	@Transactional(readOnly = true)
	public Video findById(final long id) {
		return jdbcTemplate.queryForObject("SELECT * FROM \"VIDEO\" WHERE \"ID\" = ?", rowMapper, id);
	}

	@Transactional(readOnly = true)
	public List<Video> findByState(final VideoState... states) {
		if (states.length == 0) {
			return findAll();
		}

		final Iterable<Integer> ordinals = Iterables.transform(Arrays.asList(states), input -> input.ordinal());

		String query = "SELECT * FROM \"VIDEO\" WHERE \"STATE\" IN (";
		query += Joiner.on(", ").join(ordinals);
		query += ") ORDER BY \"UPLOADED\" DESC, \"DISCOVERED\" DESC";
		return jdbcTemplate.query(query, rowMapper);
	}

	@Transactional(readOnly = true)
	public Video findByYoutubeID(final String youtubeID) {
		final List<Video> result = jdbcTemplate.query("SELECT * FROM \"VIDEO\" WHERE \"YOUT_ID\" = ? ", rowMapper, youtubeID);
		return Iterables.getOnlyElement(result, null);
	}

	public void persist(final Video video) {
		if (video.getId() == null && Strings.isNullOrEmpty(video.getSecurityToken())) {
			video.setSecurityToken(createSecurityToken());
		}

		final KeyHolder keyHolder = new GeneratedKeyHolder();

		jdbcTemplate.update(video.getId() == null ? new InsertStatementCreator(video) : new UpdateStatementCreator(video), keyHolder);

		if (video.getId() == null) {
			video.setId(keyHolder.getKey().longValue());
		}

		eventBus.post(new ServerStateChangeEvent());
	}

	private String createSecurityToken() {
		final String uuid = UUID.randomUUID().toString();
		// Only keep characters and digits
		return uuid.replaceAll("[^\\w\\d]", "");
	}

}
