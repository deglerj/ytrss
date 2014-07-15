<%@ taglib prefix="c"	uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" 	uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" 	uri="http://www.springframework.org/tags" %>

<jsp:include page="components/header.jsp">
	<jsp:param value="Downloads" name="title"/>
	<jsp:param value="1" name="active"/>
</jsp:include>

<div class="container">
	<p class="text-right">
		<a class="btn btn-lg btn-primary" href="channel" role="button"><i class="glyphicon glyphicon-plus" style="margin-right: 7px"></i>Add a channel</a>
	</p>
	<p>
		<jsp:include page="components/videoTable.jsp">
			<jsp:param value="true" name="showChannels"/>
		</jsp:include>
	</p>
</div>

<jsp:include page="components/footer.jsp"/>