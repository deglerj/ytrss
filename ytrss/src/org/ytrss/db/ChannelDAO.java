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
public class ChannelDAO {

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

}
