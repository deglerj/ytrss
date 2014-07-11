<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

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

<title>YTRSS - Channel Foo</title>

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
					<li><a href="${base}"><i class="glyphicon glyphicon-download"></i> Downloads</a></li>
					<li class="dropdown active">
						<a href="#" class="dropdown-toggle" data-toggle="dropdown"><i class="glyphicon glyphicon-list-alt"></i >Channels<span class="caret"></span></a>
						<ul class="dropdown-menu" role="menu">
							<c:if test="${!channels.isEmpty()}">
								<c:forEach var="channel" items="${channels}">
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

	<div class="container spread-fieldsets">
		<h1>${channel.name}</h1>

		<fieldset>
			<legend>Data</legend>
			<form class="form-horizontal">
				<div class="form-group">
					<label for="inputName" class="control-label col-xs-2">Name</label>
					<div class="col-xs-7">
						<input type="text" class="form-control" id="inputName" placeholder="Monty Python">
					</div>
				</div>

				<div class="form-group">
					<label for="inputURL" class="control-label col-xs-2">URL</label>
					<div class="col-xs-7">
						<input type="url" class="form-control" id="inputURL" placeholder="youtube.com/user/MontyPython">
					</div>
				</div>

				<div class="form-group">
					<div class="col-xs-offset-2 col-xs-10">
						<button type="submit" class="btn btn-primary">Save</button>
					</div>
				</div>
			</form>
		</fieldset>
		
		<fieldset>
			<legend>Feeds</legend>
			
			<div class="form-horizontal">
				<div class="form-group">
					<label for="feedAtom" class="control-label col-xs-2">Atom</label>
					<div class="col-xs-7">
						<a id="feedAtom" href="#" class="form-control-text">http://localhost:8080/ytrss/atom/${channel.id}</a>
					</div>
				</div>

				<div class="form-group">
					<label for="feedRSS" class="control-label col-xs-2">RSS</label>
					<div class="col-xs-7">
						<a id="feedRSS" href="#" class="form-control-text">http://localhost:8080/ytrss/rss/${channel.id}</a>
					</div>
				</div>
			</div>
		</fieldset>
		
		<fieldset>
			<legend>Downloads</legend>
			
			<h4>TODO</h4>
		</fieldset>
	</div>

	<script src="js/jquery-1.11.1.min.js"></script>
	<script src="js/bootstrap.min.js"></script>
</body>
</html>