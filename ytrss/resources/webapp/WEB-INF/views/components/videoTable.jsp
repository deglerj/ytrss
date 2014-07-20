<%@ taglib prefix="c"	uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" 	uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" 	uri="http://www.springframework.org/tags" %>

<table class="table" id="videoTable">
	<thead>
		<tr>
			<th>Uploaded</th>
			<c:if test="${param.channelID == null}"><th>Channel</th></c:if>
			<th>Name</th>
			<th>Status</th>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td colspan="4">
				<div class="progress progress-striped active">
	                <div style="width: 100%" class="progress-bar">Loading...</div>
	            </div>
			</td>
		</tr>
	</tbody>
</table>

<c:if test="${param.channelID == null}">
	<script type="text/javascript">
		window.addEventListener("load", function(){
			startVideoTableUpdates("videoTable", null);	
		}, false);
	</script>
</c:if>
<c:if test="${param.channelID != null}">
	<script type="text/javascript">
		window.addEventListener("load", function(){
			startVideoTableUpdates("videoTable", ${param.channelID});	
		}, false);
	</script>
</c:if>