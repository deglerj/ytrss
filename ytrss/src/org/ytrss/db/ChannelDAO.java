package org.ytrss.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class ChannelDAO {

	private static class InsertStatementCreator implements PreparedStatementCreator {

		private final Channel	channel;

		public InsertStatementCreator(final Channel channel) {
			this.channel = channel;
		}

		@Override
		public PreparedStatement createPreparedStatement(final Connection con) throws SQLException {
			final PreparedStatement stmt = con.prepareStatement("INSERT INTO \"CHANNEL\" (\"NAME\", \"URL\") VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, channel.getName());
			stmt.setString(2, channel.getUrl());
			return stmt;
		}

	}

	private static class UpdateStatementCreator implements PreparedStatementCreator {

		private final Channel	channel;

		public UpdateStatementCreator(final Channel channel) {
			this.channel = channel;
		}

		@Override
		public PreparedStatement createPreparedStatement(final Connection con) throws SQLException {
			final PreparedStatement stmt = con.prepareStatement("UPDATE \"CHANNEL\" SET \"NAME\" = ?, \"URL\" = ? WHERE \"ID\" = ?");
			stmt.setString(1, channel.getName());
			stmt.setString(2, channel.getUrl());
			stmt.setLong(3, channel.getId());
			return stmt;
		}

	}

	@Autowired
	private DataSource					dataSource;

	@Autowired
	private JdbcTemplate				jdbcTemplate;

	private final RowMapper<Channel>	rowMapper	= (rs, rowNum) -> {
		final Channel feed = new Channel();
		feed.setId(rs.getLong("id"));
		feed.setName(rs.getString("name"));
		feed.setUrl(rs.getString("url"));
		return feed;
	};

	@Transactional(readOnly = true)
	public List<Channel> findAll() {
		return jdbcTemplate.query("SELECT * FROM \"CHANNEL\" ORDER BY \"NAME\"", rowMapper);
	}

	@Transactional(readOnly = true)
	public Channel findById(final long id) {
		return jdbcTemplate.queryForObject("SELECT * FROM \"CHANNEL\" WHERE \"ID\" = ?", rowMapper, id);
	}

	public void persist(final Channel channel) {
		final KeyHolder keyHolder = new GeneratedKeyHolder();

		jdbcTemplate.update(channel.getId() == null ? new InsertStatementCreator(channel) : new UpdateStatementCreator(channel), keyHolder);

		if (channel.getId() == null) {
			channel.setId(keyHolder.getKey().longValue());
		}
	}
}
