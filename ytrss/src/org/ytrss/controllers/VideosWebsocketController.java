package org.ytrss.controllers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.ytrss.JsonVideosSerializer;
import org.ytrss.db.ServerStateChangeEvent;
import org.ytrss.db.Video;
import org.ytrss.db.VideoDAO;

import argo.jdom.JdomParser;
import argo.jdom.JsonRootNode;
import argo.saj.InvalidSyntaxException;

import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

@Controller
public class VideosWebsocketController extends TextWebSocketHandler {

	private static Logger						log			= LoggerFactory.getLogger(VideosWebsocketController.class);

	private final Map<WebSocketSession, Long>	sessions	= Collections.synchronizedMap(Maps.newHashMap());

	@Autowired
	private VideoDAO							videoDAO;

	@Autowired
	private JsonVideosSerializer				videosSerializer;

	private final JdomParser					jsonParser	= new JdomParser();

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

		final String serialized = videosSerializer.serialize(videos);

		try {
			session.sendMessage(new TextMessage(serialized));
		}
		catch (final IOException e) {
			log.warn("Could not send update to WebsocketSession \"" + session.getId() + "\"", e);
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

}
