<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="req" value="${pageContext.request}" />
<c:set var="url">${req.requestURL}</c:set>
<c:set var="uri" value="${req.requestURI}" />
<c:set var="base" value="${fn:substring(url, 0, fn:length(url) - fn:length(uri))}${req.contextPath}/" />

<!DOCTYPE html>
<html lang="en">
<head>

<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">

<base href="${base}">

<title>ytrss - Downloads</title>

<link href="images/favicon.ico" rel="shortcut icon">
<link href="css/bootstrap.min.css" rel="stylesheet">
<link href="css/ytrss.css" rel="stylesheet">
<!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
<!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
<!--[if lt IE 9]>
	<script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
	<script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
<![endif]-->

</head>

<body>

	<div class="navbar navbar-default navbar-fixed-top spread-nav" role="navigation">
		<div class="container">
		<div class="navbar-header">
          <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
        </div>
			<div class="navbar-collapse collapse">
				<ul class="nav navbar-nav">
					<li class="active"><a href="${base}"><i class="glyphicon glyphicon-download"></i> Downloads</a></li>
					<li class="dropdown">
						<a href="#" class="dropdown-toggle" data-toggle="dropdown"><i class="glyphicon glyphicon-list-alt"></i >Channels<span class="caret"></span></a>
						<ul class="dropdown-menu" role="menu">
							<c:if test="${!channels.isEmpty()}">
								<c:forEach var="channel" items="${channels.values()}">
									<li><a href="channel/${channel.id}">${channel.name}</a></li>
								</c:forEach>
								<li class="divider"></li>
							</c:if>
							<li><a href="channel"><i class="glyphicon glyphicon-plus"></i>Add</a></li>
						</ul>
					</li>
				</ul>
				<ul class="nav navbar-nav navbar-right">
					<li><a href="https://github.com/deglerj/ytrss"><i class="glyphicon glyphicon-globe"></i> ytrss 0.0.1</a></li>
				</ul>
			</div>
		</div>
	</div>

	<div class="container">
		<p class="text-right">
			<a class="btn btn-lg btn-primary" href="channel" role="button"><i class="glyphicon glyphicon-plus" style="margin-right: 7px"></i>Add a channel</a>
		</p>
		<p>
			<table class="table">
				<tr>
					<th>Uploaded</th>
					<th>Channel</th>
					<th>Name</th>
					<th>Status</th>
				</tr>
				<c:forEach var="video" items="${videos}">
					<tr>
						<td>
							<fmt:formatDate value="${video.uploaded}"/>
						</td>
						<td>
							<a href="channel/${video.channelID}">
								${channels.get(video.channelID).name}
							</a>
						</td> 
						<td>
							<a href="https://www.youtube.com/watch?v=${video.youtubeID}">
								${video.name}
							</a>
						</td>
						<c:if test="${video.state.ordinal() == 0}">
							<td><span class="label label-info table-state-label"><i class="glyphicon glyphicon-info-sign"></i> New</span></td>
						</c:if>
						<c:if test="${video.state.ordinal() == 1}">
							<td><span class="label label-info table-state-label"><i class="glyphicon glyphicon-info-sign"></i> Downloading</span></td>
						</c:if>
						<c:if test="${video.state.ordinal() == 2}">
							<td><span class="label label-info table-state-label"><i class="glyphicon glyphicon-info-sign"></i> Transcoding</span></td>
						</c:if>
						<c:if test="${video.state.ordinal() == 3}">
							<td><a href="#" class="label label-success table-state-label"><i class="glyphicon glyphicon-download-alt"></i> Ready</a></td>
						</c:if>
						<c:if test="${video.state.ordinal() > 3}">
							<td><a href="#" class="label label-danger table-state-label"><i class="glyphicon glyphicon-warning-sign"></i> Error</a></td>
						</c:if>
					</tr>
				</c:forEach>
			</table>
		</p>
	</div>

	<script src="js/jquery-1.11.1.min.js"></script>
	<script src="js/bootstrap.min.js"></script>
</body>
</html>