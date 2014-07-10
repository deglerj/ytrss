<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html lang="en">
<head>

<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">

<title>YTRSS</title>

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
					<li class="active"><a href="#"><i class="glyphicon glyphicon-download"></i> Downloads <span class="badge pull-right">42</span></a></li>
					<li class="dropdown">
						<a href="#" class="dropdown-toggle" data-toggle="dropdown"><i class="glyphicon glyphicon-list-alt"></i >Subscriptions<span class="caret"></span></a>
						<ul class="dropdown-menu" role="menu">
							<li><a href="#">Feed 1</a></li>
							<li><a href="#">Feed 2</a></li>
							<li class="divider"></li>
							<li><a href="#">Add subscription</a></li>
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
		<div class="jumbotron">
			<p class="text-right">
				<a class="btn btn-lg btn-primary" href="../../components/#navbar" role="button">Add a subscription &raquo;</a>
			</p>
			<p>
				<table class="table">
					<tr>
						<th>Uploaded</th>
						<th>Feed</th>
						<th>Name</th>
						<th>Status</th>
					</tr>
					<tr>
						<td>10.07.2014</td>
						<td>Rocket Beans TV</td>
						<td>Almost Daily #99: Handwerken</td>
						<td><span class="label label-info table-state-label"><i class="glyphicon glyphicon-info-sign"></i> Downloading</span></td>
					</tr>
					<tr>
						<td>10.07.2014</td>
						<td>Rocket Beans TV</td>
						<td>Almost Daily #99: Handwerken</td>
						<td><span class="label label-info table-state-label"><i class="glyphicon glyphicon-info-sign"></i> Transcoding</span></td>
					</tr>
					<tr>
						<td>10.07.2014</td>
						<td>Rocket Beans TV</td>
						<td>Almost Daily #99: Handwerken</td>
						<td><a href="#" class="label label-success table-state-label"><i class="glyphicon glyphicon-download-alt"></i> Ready</a></td>
					</tr>
					<tr>
						<td>10.07.2014</td>
						<td>Rocket Beans TV</td>
						<td>Almost Daily #99: Handwerken</td>
						<td><a href="#" class="label label-danger table-state-label"><i class="glyphicon glyphicon-warning-sign"></i> Error</a></td>
					</tr>
				</table>
			</p>
		</div>

	</div>

	<script src="js/jquery-1.11.1.min.js"></script>
	<script src="js/bootstrap.min.js"></script>
</body>
</html>