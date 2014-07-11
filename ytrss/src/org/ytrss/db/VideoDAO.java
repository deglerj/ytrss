package org.ytrss.db;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class VideoDAO {

	@Autowired
	private DataSource				dataSource;

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
		return video;
	};

	@Transactional(readOnly = true)
	public List<Video> findAll() {
		return jdbcTemplate.query("SELECT * FROM \"VIDEO\" ORDER BY \"DISCOVERED\"", rowMapper);
	}

	@Transactional(readOnly = true)
	public List<Video> findByChannelID(final long channelID) {
		return jdbcTemplate.query("SELECT * FROM \"VIDEO\" WHERE \"CHANNEL_FK\" = ? ORDER BY \"DISCOVERED\"", rowMapper, channelID);
	}
}
