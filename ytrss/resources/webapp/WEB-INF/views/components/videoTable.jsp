<%@ taglib prefix="c"	uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" 	uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" 	uri="http://www.springframework.org/tags" %>

<table class="table">
	<tr>
		<th>Uploaded</th>
		<c:if test="${param.showChannels}"><th>Channel</th></c:if>
		<th>Name</th>
		<th>Status</th>
	</tr>
	<c:forEach var="video" items="${videos}">
		<tr>
			<td>
				<fmt:formatDate value="${video.uploaded}"/>
			</td>
			<c:if test="${param.showChannels}">
			<td>
				<a href="channel/${video.channelID}">
					${channels.get(video.channelID).name}
				</a>
			</td> 
			</c:if>
			<td>
				<a href="https://www.youtube.com/watch?v=${video.youtubeID}">
					${video.name}
				</a>
			</td>						
			<c:if test="${video.state.ordinal() == 0}">
				<td><span class="label label-info table-state-label"><i class="glyphicon glyphicon-info-sign"></i> New</span></td>
			</c:if>
			<c:if test="${video.state.ordinal() == 1}">
				<td><span class="label label-default table-state-label"><i class="glyphicon glyphicon-time"></i> DL enqueued</span></td>
			</c:if>
			<c:if test="${video.state.ordinal() == 2}">
				<td><span class="label label-info table-state-label"><i class="glyphicon glyphicon-download"></i>Downloading</span></td>
			</c:if>
			<c:if test="${video.state.ordinal() == 3}">
				<td><span class="label label-danger table-state-label"><i class="glyphicon glyphicon-warning-sign"></i>DL failed</span></td>
			</c:if>
			<c:if test="${video.state.ordinal() == 4}">
				<td><span class="label label-default table-state-label"><i class="glyphicon glyphicon-time"></i> TRC enqueued</span></td>
			</c:if>
			<c:if test="${video.state.ordinal() == 5}">
				<td><span class="label label-info table-state-label"><i class="glyphicon glyphicon-record"></i> Transconding</span></td>
			</c:if>
			<c:if test="${video.state.ordinal() == 6}">
				<td><span class="label label-danger table-state-label"><i class="glyphicon glyphicon-warning-sign"></i>TRC failed</span></td>
			</c:if>
			<c:if test="${video.state.ordinal() == 7}">
				<td><a href="download?id=${video.id}&token=${video.securityToken}" class="label label-success table-state-label"><i class="glyphicon glyphicon-download-alt"></i> Ready</a></td>
			</c:if>
		</tr>
	</c:forEach>
</table>
<div class="text-right text-info" style="font-size: 13px">
	Next update in: ${updateCountdown}
</div>