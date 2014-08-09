package org.ytrss.db;

import java.io.File;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.ytrss.transcoders.Bitrate;

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

	public <T> void setSetting(final String name, final T value) {
		final String convertedValue = conversionService.convert(value, String.class);

		if (isSettingAvailable(name)) {
			updateSetting(name, convertedValue);
		}
		else {
			createSetting(name, convertedValue);
		}

		if ("files".equals(name)) {
			createFilesDir();
		}
	}

	private void createFilesDir() {
		final String baseDirectory = getSetting("files", String.class);

		final File data = new File(baseDirectory + File.separator + "data");
		if (!data.exists()) {
			data.mkdirs();
		}

		final File videos = new File(baseDirectory + File.separator + "videos");
		if (!videos.exists()) {
			videos.mkdirs();
		}

		final File mp3s = new File(baseDirectory + File.separator + "mp3s");
		if (!mp3s.exists()) {
			mp3s.mkdirs();
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

	private <T> void initDefaultValue(final String name, final T value) {
		if (!isSettingAvailable(name)) {
			setSetting(name, value);
		}

	}

	@PostConstruct
	private void initDefaultValues() {
		initDefaultValue("files", System.getProperty("user.home") + File.separator + ".ytrss");
		createFilesDir();

		initDefaultValue("port", 8080);
		initDefaultValue("password", BCrypt.hashpw("ytrss", BCrypt.gensalt()));
		initDefaultValue("downloaderThreads", 2);
		initDefaultValue("transcoderThreads", 2);
		initDefaultValue("bitrate", Bitrate.CBR_96);
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
