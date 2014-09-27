package org.ytrss.controllers;

import static argo.jdom.JsonNodeFactories.array;
import static argo.jdom.JsonNodeFactories.field;
import static argo.jdom.JsonNodeFactories.number;
import static argo.jdom.JsonNodeFactories.object;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.ytrss.Ripper;
import org.ytrss.db.Channel;
import org.ytrss.db.ChannelDAO;
import org.ytrss.db.ServerStateChangeEvent;
import org.ytrss.db.Video;
import org.ytrss.db.VideoDAO;

import argo.format.CompactJsonFormatter;
import argo.format.JsonFormatter;
import argo.jdom.JdomParser;
import argo.jdom.JsonField;
import argo.jdom.JsonNode;
import argo.jdom.JsonNodeFactories;
import argo.jdom.JsonRootNode;
import argo.saj.InvalidSyntaxException;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

@Controller
public class VideosWebsocketController extends TextWebSocketHandler {

	private final Map<WebSocketSession, Long>	sessions		= Collections.synchronizedMap(Maps.newHashMap());

	@Autowired
	private ChannelDAO							channelDAO;

	@Autowired
	private VideoDAO							videoDAO;

	@Autowired
	private Ripper								ripper;

	private final JsonFormatter					jsonFormatter	= new CompactJsonFormatter();

	private final JdomParser					jsonParser		= new JdomParser();

	@Subscribe
	public void onServerStateChanged(final ServerStateChangeEvent event) throws UnsupportedEncodingException {
		sendUpdates();
	}

	@Autowired
	public void subscribe(final EventBus eventBus) {
		eventBus.register(this);
	}

	@Override
	protected void handleTextMessage(final WebSocketSession session, final TextMessage message) throws Exception {
		final Long channelID = getChannelID(message);

		sendUpdate(session, channelID);

		sessions.put(session, channelID);
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

	private Long getChannelID(final TextMessage message) throws InvalidSyntaxException {
		final String payload = message.getPayload();

		final JsonRootNode jsonRequest = jsonParser.parse(payload);
		if (jsonRequest.isNumberValue("channelID")) {
			return Long.parseLong(jsonRequest.getNumberValue("channelID"));
		}
		else {
			return null;
		}
	}

	private void sendUpdate(final WebSocketSession session, final Long channelID) throws UnsupportedEncodingException {
		final List<Video> videos = channelID == null ? videoDAO.findAll() : videoDAO.findByChannelID(channelID);

		final List<JsonField> responseFields = Lists.newArrayList();

		final JsonField videosField = field("videos", array(Lists.transform(videos, this::createVideoNode)));
		responseFields.add(videosField);

		final JsonField lastUpdateField = field("countdown", number(ripper.getCountdown()));
		responseFields.add(lastUpdateField);

		final JsonRootNode jsonResponse = object(responseFields);
		final String response = jsonFormatter.format(jsonResponse);
		// Encode as "ISO-8859-1" to avoid problems with special characters
		final String encodedResponse = new String(response.getBytes("UTF-8"), "ISO-8859-1");

		try {
			session.sendMessage(new TextMessage(encodedResponse));
		}
		catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void sendUpdates() throws UnsupportedEncodingException {
		for (final Iterator<Entry<WebSocketSession, Long>> iterator = sessions.entrySet().iterator(); iterator.hasNext();) {
			final Entry<WebSocketSession, Long> entry = iterator.next();

			final WebSocketSession session = entry.getKey();

			if (session.isOpen()) {
				sendUpdate(session, entry.getValue());
			}
			else {
				iterator.remove();
			}
		}
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
