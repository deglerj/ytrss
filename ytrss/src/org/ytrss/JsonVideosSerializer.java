package org.ytrss;

import static argo.jdom.JsonNodeFactories.array;
import static argo.jdom.JsonNodeFactories.field;
import static argo.jdom.JsonNodeFactories.number;
import static argo.jdom.JsonNodeFactories.object;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.ytrss.db.Channel;
import org.ytrss.db.ChannelDAO;
import org.ytrss.db.Video;

import argo.format.CompactJsonFormatter;
import argo.format.JsonFormatter;
import argo.jdom.JsonField;
import argo.jdom.JsonNode;
import argo.jdom.JsonNodeFactories;
import argo.jdom.JsonRootNode;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

@Component
public class JsonVideosSerializer {

	private final JsonFormatter	jsonFormatter	= new CompactJsonFormatter();

	@Autowired
	private ChannelDAO			channelDAO;

	@Autowired
	private Ripper				ripper;

	public String serialize(final List<Video> videos) throws UnsupportedEncodingException {
		final List<JsonField> responseFields = Lists.newArrayList();

		final JsonField videosField = field("videos", array(Lists.transform(videos, this::createVideoNode)));
		responseFields.add(videosField);

		final JsonField lastUpdateField = field("countdown", number(ripper.getCountdown()));
		responseFields.add(lastUpdateField);

		final JsonRootNode jsonResponse = object(responseFields);
		final String response = jsonFormatter.format(jsonResponse);
		// Encode as "ISO-8859-1" to avoid problems with special characters
		final String encodedResponse = new String(response.getBytes("UTF-8"), "ISO-8859-1");

		return encodedResponse;
	}

	private JsonNode createVideoNode(final Video video) {
		final Channel channel = channelDAO.findById(video.getChannelID());

		final String uploaded = SimpleDateFormat.getDateInstance().format(video.getUploaded());
		//@formatter:off
		return object(
				field("id", number(video.getId())),
				field("uploaded", string(uploaded, true)),
				field("channelName", string(channel.getName(), true)),
				field("channelID", number(channel.getId())),
				field("name", string(video.getName(), true)),
				field("youtubeID", string(video.getYoutubeID(), false)),
				field("state", number(video.getState().ordinal())),
				field("errorMessage", string(video.getErrorMessage(), true)),
				field("securityToken", string(video.getSecurityToken(), false))
				);
		//@formatter:on
	}

	private JsonNode string(final String string, final boolean escapeHtml) {
		final String nonNull = Strings.nullToEmpty(string);
		if (escapeHtml) {
			return JsonNodeFactories.string(StringEscapeUtils.escapeHtml4(nonNull));
		}
		else {
			return JsonNodeFactories.string(nonNull);
		}
	}

}
