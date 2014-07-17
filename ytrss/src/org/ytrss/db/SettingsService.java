package org.ytrss.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class SettingsService {

	@Autowired
	private JdbcTemplate		jdbcTemplate;

	@Autowired
	private ConversionService	conversionService;

	public <T> T getSetting(final String name, final Class<T> clazz) {
		final String value = getValue(name);
		return conversionService.convert(value, clazz);
	}

	public <T> T getSetting(final String name, final T defaultValue, final Class<T> clazz) {
		if (isSettingAvailable(name)) {
			final String value = getValue(name);
			return conversionService.convert(value, clazz);
		}
		else {
			setSetting(name, defaultValue);
			return defaultValue;
		}
	}

	public <T> void setSetting(final String name, final T value) {
		final String convertedValue = conversionService.convert(value, String.class);

		if (isSettingAvailable(name)) {
			updateSetting(name, convertedValue);
		}
		else {
			createSetting(name, convertedValue);
		}
	}

	private void createSetting(final String name, final String value) {
		jdbcTemplate.update("INSERT INTO \"SETTING\" (\"NAME\", \"VALUE\") VALUES (?, ?)", name, value);
	}

	private String getValue(final String name) {
		return jdbcTemplate.query("SELECT \"VALUE\" FROM \"SETTING\" WHERE \"NAME\" = ?", (ResultSetExtractor<String>) rs -> {
			rs.next();
			return rs.getString(1);
		}, name);
	}

	private boolean isSettingAvailable(final String name) {
		final long count = jdbcTemplate.query("SELECT COUNT(*) FROM \"SETTING\" WHERE \"NAME\" = ?", (ResultSetExtractor<Long>) rs -> {
			rs.next();
			return rs.getLong(1);
		}, name);
		return count == 1;
	}

	private void updateSetting(final String name, final String value) {
		jdbcTemplate.update("UPDATE \"SETTING\" SET \"VALUE\" = ? WHERE \"NAME\" = ?", value, name);
	}

}
