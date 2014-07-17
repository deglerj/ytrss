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
			<div class="col-xs-offset-2 col-xs-10">
				<button type="submit" class="btn btn-primary">Save</button>
			</div>
		</div>
	</form:form>
</div>

<jsp:include page="components/footer.jsp"/>