package org.ytrss.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.UUID;

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
			final PreparedStatement stmt = con.prepareStatement(
					"INSERT INTO \"CHANNEL\" (\"NAME\", \"URL\", \"EXCLUDE_REGEX\", \"INCLUDE_REGEX\", \"SECURITY_TOKEN\") VALUES (?, ?, ?, ?, ?)",
					Statement.RETURN_GENERATED_KEYS);

			stmt.setString(1, channel.getName());
			stmt.setString(2, channel.getUrl());

			if (channel.getExcludeRegex() == null) {
				stmt.setNull(3, Types.LONGVARCHAR);
			}
			else {
				stmt.setString(3, channel.getExcludeRegex());
			}

			if (channel.getIncludeRegex() == null) {
				stmt.setNull(4, Types.LONGVARCHAR);
			}
			else {
				stmt.setString(4, channel.getIncludeRegex());
			}

			if (channel.getSecurityToken() == null) {
				stmt.setNull(5, Types.LONGVARCHAR);
			}
			else {
				stmt.setString(5, channel.getSecurityToken());
			}

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
			final PreparedStatement stmt = con
					.prepareStatement("UPDATE \"CHANNEL\" SET \"NAME\" = ?, \"URL\" = ?, \"EXCLUDE_REGEX\" = ?, \"INCLUDE_REGEX\" = ?, \"SECURITY_TOKEN\" = ? WHERE \"ID\" = ?");

			stmt.setString(1, channel.getName());
			stmt.setString(2, channel.getUrl());

			if (channel.getExcludeRegex() == null) {
				stmt.setNull(3, Types.LONGVARCHAR);
			}
			else {
				stmt.setString(3, channel.getExcludeRegex());
			}

			if (channel.getIncludeRegex() == null) {
				stmt.setNull(4, Types.LONGVARCHAR);
			}
			else {
				stmt.setString(4, channel.getIncludeRegex());
			}

			if (channel.getSecurityToken() == null) {
				stmt.setNull(5, Types.LONGVARCHAR);
			}
			else {
				stmt.setString(5, channel.getSecurityToken());
			}

			stmt.setLong(6, channel.getId());

			return stmt;
		}

	}

	@Autowired
	private DataSource					dataSource;

	@Autowired
	private JdbcTemplate				jdbcTemplate;

	private final RowMapper<Channel>	rowMapper	= (rs, rowNum) -> {
														final Channel channel = new Channel();
														channel.setId(rs.getLong("id"));
														channel.setName(rs.getString("name"));
														channel.setUrl(rs.getString("url"));
														channel.setExcludeRegex(rs.getString("exclude_regex"));
														channel.setIncludeRegex(rs.getString("include_regex"));
														channel.setSecurityToken(rs.getString("security_token"));
														return channel;
													};

	public void delete(final long id) {
		jdbcTemplate.update("DELETE FROM \"VIDEO\" WHERE \"CHANNEL_FK\" = ? ", id);
		jdbcTemplate.update("DELETE FROM \"CHANNEL\" WHERE \"ID\" = ? ", id);

		// TODO delete videos' files
	}

	@Transactional(readOnly = true)
	public List<Channel> findAll() {
		return jdbcTemplate.query("SELECT * FROM \"CHANNEL\" ORDER BY \"NAME\"", rowMapper);
	}

	@Transactional(readOnly = true)
	public Channel findById(final long id) {
		return jdbcTemplate.queryForObject("SELECT * FROM \"CHANNEL\" WHERE \"ID\" = ?", rowMapper, id);
	}

	public void persist(final Channel channel) {
		if (channel.getId() == null && channel.getSecurityToken() == null) {
			channel.setSecurityToken(createSecurityToken());
		}

		final KeyHolder keyHolder = new GeneratedKeyHolder();

		jdbcTemplate.update(channel.getId() == null ? new InsertStatementCreator(channel) : new UpdateStatementCreator(channel), keyHolder);

		if (channel.getId() == null) {
			channel.setId(keyHolder.getKey().longValue());
		}
	}

	private String createSecurityToken() {
		final String uuid = UUID.randomUUID().toString();
		// Only keep characters and digits
		return uuid.replaceAll("[^\\w\\d]", "");
	}
}
