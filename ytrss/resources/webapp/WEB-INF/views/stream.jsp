<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="y" uri="http://github.com/deglerj/ytrss"%>

<c:set var="req" value="${pageContext.request}" />
<c:set var="url">${req.requestURL}</c:set>
<c:set var="uri" value="${req.requestURI}" />
<c:set var="base"
	value="${fn:substring(url, 0, fn:length(url) - fn:length(uri))}${req.contextPath}/" />

<!DOCTYPE html>
<html lang="en">
<head>

<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">

<base href="${base}">

<title>ytrss - Stream ${video.name}</title>

<link href="images/favicon.ico" rel="shortcut icon">

<link href="stream/css/oiplayer.css" rel="stylesheet" type="text/css" />

</head>

<body style="padding: 15px; background-color: #2b3e50">
	
	<audio src="/download?id=${video.id}&token=${video.securityToken}" controls style="width: 100%">
		<div>Your browser does not support the audio-tag. Please consider upgrading.</div>
	</audio>

</body>

</html>