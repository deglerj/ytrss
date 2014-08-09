<%@ taglib prefix="c"	uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" 	uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" 	uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<jsp:include page="components/header.jsp">
	<jsp:param value="Settings" name="title"/>
	<jsp:param value="3" name="active"/>
</jsp:include>

<div class="container">
	<form:form method="post" commandName="settingsForm" cssClass="form-horizontal">
		<div class="form-group">
			<label for="inputPassword" class="col-lg-2 control-label">Password</label>
			<div class="col-lg-4">
				<form:input path="password" autocomplete="off" cssClass="form-control" id="inputPassword" placeholder="****" type="password"/>
			</div>
			<div class="col-xs-2">
				<form:errors path="password" cssClass="form-error"/>
			</div>
		</div>
		
		<div class="form-group">
			<label for="inputPassword2" class="col-lg-2 control-label">Repeat password</label>
			<div class="col-lg-4">
				<form:input path="password2" autocomplete="off" cssClass="form-control" id="inputPassword2" placeholder="****" type="password"/>
			</div>
			<div class="col-xs-2">
				<form:errors path="password2" cssClass="form-error"/>
			</div>
		</div>
		
		<div class="form-group">
			<label for="inputPort" class="col-lg-2 control-label">Port</label>
			<div class="col-lg-4">
				<form:input path="port" autocomplete="off" cssClass="form-control" id="inputPort" placeholder="8080" type="number"/>
				<span class="help-block">Requires restart</span>
			</div>
			<div class="col-xs-2">
				<form:errors path="port" cssClass="form-error"/>
			</div>
		</div>
		
		<div class="form-group">
			<label for="inputFiles" class="col-lg-2 control-label">Download Directory</label>
			<div class="col-lg-4">
				<form:input path="files" autocomplete="off" cssClass="form-control" id="inputFiles" type="text"/>
				<span class="help-block">Existing files will not be moved</span>
			</div>
			<div class="col-xs-2">
				<form:errors path="files" cssClass="form-error"/>
			</div>
		</div>
		
		<div class="form-group">
			<label for="inputDownloaderThreads" class="col-lg-2 control-label">Max. parallel downloads</label>
			<div class="col-lg-4">
				<form:input path="downloaderThreads" autocomplete="off" cssClass="form-control" id="inputDownloaderThreads" placeholder="2" type="number"/>
				<span class="help-block">Requires restart</span>
			</div>
			<div class="col-xs-2">
				<form:errors path="downloaderThreads" cssClass="form-error"/>
			</div>
		</div>
		
		<div class="form-group">
			<label for="inputTranscoderThreads" class="col-lg-2 control-label">Max. parallel transcodings</label>
			<div class="col-lg-4">
				<form:input path="transcoderThreads" autocomplete="off" cssClass="form-control" id="inputTranscoderThreads" placeholder="2" type="number"/>
				<span class="help-block">Requires restart</span>
			</div>
			<div class="col-xs-2">
				<form:errors path="transcoderThreads" cssClass="form-error"/>
			</div>
		</div>
		
		<div class="form-group">
			<label for="inputBitrate" class="col-lg-2 control-label">MP3 bitrate</label>
			<div class="col-lg-4">
				<form:select path="bitrate" cssClass="form-control" id="inputBitrate" >
					<form:options itemLabel="description"/>
				</form:select>
				<span class="help-block">Existing MP3s will not be affected</span>
			</div>
			<div class="col-xs-2">
				<form:errors path="bitrate" cssClass="form-error"/>
			</div>
		</div>
		
		<div class="form-group">
			<div class="col-xs-offset-2 col-xs-10">
				<button type="submit" class="btn btn-primary">Save</button>
			</div>
		</div>
	</form:form>
</div>

<jsp:include page="components/footer.jsp"/>