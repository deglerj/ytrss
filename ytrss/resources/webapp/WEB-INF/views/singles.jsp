<%@ taglib prefix="c"	uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" 	uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" 	uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<jsp:include page="components/header.jsp">
	<jsp:param value="Singles" name="title"/>
	<jsp:param value="3" name="active"/>
</jsp:include>

<div class="container spread-fieldsets">
	<div class="row">
		<div class="col-md-8">
			<h1 style="display:inline-block;">Singles</h1>
		</div>
	</div>
	
	<fieldset>
		<legend>Add a single YouTube video</legend>
		<form:form method="post" commandName="singlesForm" cssClass="form-horizontal">
			<div class="form-group">
				<label for="inputURL" class="control-label col-xs-2">URL</label>
				<div class="col-xs-7">
					<form:input path="url" cssClass="form-control" id="inputURL" placeholder="http://youtube.com/watch?v=sor9GzivGbk" type="text"/>
				</div>
				<div class="col-xs-2">
					<form:errors path="url" cssClass="form-error"/>
				</div>
			</div>
			
			<div class="form-group">
				<div class="col-xs-offset-2 col-xs-10">
					<button type="submit" class="btn btn-primary">Add</button>
				</div>
			</div>
		</form:form>
	</fieldset>
	
	<c:if test="${channel.id != null}">
		<fieldset>
			<legend>Feeds</legend>
			
			<c:set var="req" value="${pageContext.request}" />
			<c:set var="url">${req.requestURL}</c:set>
			<c:set var="uri" value="${req.requestURI}" />
			<c:set var="base" value="${fn:substring(url, 0, fn:length(url) - fn:length(uri))}${req.contextPath}/" />
			<div class="form-horizontal">
				<div class="form-group">
					<label for="feedRSS" class="control-label col-xs-2">RSS</label>
					<div class="col-xs-10">
						<a id="feedRSS" href="singles/feed?type=rss&token=${channel.securityToken}" class="form-control-text">${base}singles/feed?type=rss&token=${channel.securityToken}</a>
					</div>
				</div>
				
				<div class="form-group">
					<label for="feedAtom" class="control-label col-xs-2">Atom</label>
					<div class="col-xs-10">
						<a id="feedAtom" href="singles/feed?type=atom&token=${channel.securityToken}" class="form-control-text">${base}singles/feed?type=atom&token=${channel.securityToken}</a>
					</div>
				</div>
			</div>
		</fieldset>
		
		<fieldset>
			<legend>Downloads</legend>
			<jsp:include page="components/videoTable.jsp">
				<jsp:param value="${channel.id}" name="channelID"/>
				<jsp:param value="${initialVideos}" name="initialVideos"/>
			</jsp:include>
		</fieldset>
	</c:if>
</div>

<jsp:include page="components/footer.jsp"/>