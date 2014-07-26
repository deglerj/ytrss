<%@ taglib prefix="c"	uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" 	uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" 	uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<jsp:include page="components/header.jsp">
	<jsp:param value="${channel.id == null ? 'Add channel' : channel.name}" name="title"/>
	<jsp:param value="2" name="active"/>
</jsp:include>

<div class="container spread-fieldsets">
	<c:if test="${channel.id != null}">
		<div class="row">
			<div class="col-md-8">
				<h1 style="display:inline-block;">${channel.name}</h1>
			</div>
			<div class="col-md-4 text-right">
				<a class="btn btn-danger" href="channel/${channel.id}/delete" role="button" onclick="return confirm('Are you sure you want to delete channel \'${channel.name}\'?')"><i class="glyphicon glyphicon-trash" style="margin-right: 7px"></i>Delete</a>
			</div>
		</div>
	</c:if>
	<c:if test="${channel.id == null}">
		<h1>Add channel</h1>
	</c:if>
	
	<fieldset>
		<c:if test="${channel.id != null}">
			<legend>Data</legend>
		</c:if>
		<form:form method="post" commandName="channel" cssClass="form-horizontal">
			<div class="form-group">
				<label for="inputName" class="control-label col-xs-2">Name *</label>
				<div class="col-xs-7">
					<form:input path="name" cssClass="form-control" id="inputName" placeholder="Monty Python" type="text"/>
					<span class="help-block">Name used in the generated feeds (doesn't have to be the Youtube name)</span>
				</div>
				<div class="col-xs-2">
					<form:errors path="name" cssClass="form-error"/>
				</div>
			</div>

			<div class="form-group">
				<label for="inputURL" class="control-label col-xs-2">URL *</label>
				<div class="col-xs-7">
					<form:input path="url" cssClass="form-control" id="inputURL" placeholder="http://youtube.com/user/MontyPython" type="url"/>
					<span class="help-block">Full URL for the Youtube channel</span>
				</div>
				<div class="col-xs-2">
					<form:errors path="url" cssClass="form-error"/>
				</div>
			</div>
			
			<div class="form-group">
				<label for="inputInclude" class="control-label col-xs-2">Include</label>
				<div class="col-xs-7">
					<form:input path="includeRegex" cssClass="form-control" id="inputInclude" type="text" placeholder=".*talk.*"/>
					<span class="help-block">Only videos with a name matching this regular expression will be downloaded (case insensitive)</span>
				</div>
				<div class="col-xs-2">
					<form:errors path="includeRegex" cssClass="form-error"/>
				</div>
			</div>
			
			<div class="form-group">
				<label for="inputExclude" class="control-label col-xs-2">Exclude</label>
				<div class="col-xs-7">
					<form:input path="excludeRegex" cssClass="form-control" id="inputExclude" type="text" placeholder=".*cloud.*"/>
					<span class="help-block">Any video with a name matching this regular expression will not be downloaded (case insensitive)</span>
				</div>
				<div class="col-xs-2">
					<form:errors path="excludeRegex" cssClass="form-error"/>
				</div>
			</div>
			
			<div class="form-group">
				<label for="inputMaxVideos" class="control-label col-xs-2">Max. videos</label>
				<div class="col-xs-7">
					<form:input path="maxVideos" cssClass="form-control" id="inputMaxVideos" type="number" placeholder="30"/>
					<span class="help-block">Number of latest videos to check, download and transcode (soft limit, includes excluded videos)</span>
				</div>
				<div class="col-xs-2">
					<form:errors path="maxVideos" cssClass="form-error"/>
				</div>
			</div>
			
			<form:hidden path="securityToken"/>

			<div class="form-group">
				<div class="col-xs-offset-2 col-xs-10">
					<button type="submit" class="btn btn-primary">Save</button>
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
						<a id="feedRSS" href="channel/${channel.id}/feed?type=rss&token=${channel.securityToken}" class="form-control-text">${base}channel/${channel.id}/feed?type=rss&token=${channel.securityToken}</a>
					</div>
				</div>
				
				<div class="form-group">
					<label for="feedAtom" class="control-label col-xs-2">Atom</label>
					<div class="col-xs-10">
						<a id="feedAtom" href="channel/${channel.id}/feed?type=atom&token=${channel.securityToken}" class="form-control-text">${base}channel/${channel.id}/feed?type=atom&token=${channel.securityToken}</a>
					</div>
				</div>
			</div>
		</fieldset>
		
		<fieldset>
			<legend>Downloads</legend>
			
			<jsp:include page="components/videoTable.jsp">
				<jsp:param value="${channel.id}" name="channelID"/>
			</jsp:include>
		</fieldset>
	</c:if>
</div>

<jsp:include page="components/footer.jsp"/>