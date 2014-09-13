var lastVideosUpdate = -1;

var loadingIndicatorVisible = true;

function startVideoTableUpdates(tableId, channelId) {
	var table = $("#" + tableId);
	
	//Insert elements "next update in" and options after table
	$('<div class="text-right"><div class="text-info" style="font-size: 13px; display: inline-block" id="countdown"></div><div class="dropdown" style="display: inline-block; margin-left: 15px"><span class="dropdown-toggle glyphicon glyphicon-th-large" style="cursor:pointer" id="optionMenu" data-toggle="dropdown"></span><ul class="dropdown-menu dropdown-menu-right" role="menu" aria-labelledby="optionMenu"><li role="presentation"><a role="menuitem" tabindex="-1" style="text-decoration: none" href="javascript:void()" onclick="forceUpdate()"><span class="glyphicon glyphicon-repeat"></span>Update now</a></li></ul></div></div>').insertAfter(table);
	//Insert element for "empty table" after table
	$('<div class="text-muted" style="padding-left: 10px; margin-bottom: 20px; display: none;" id="tableEmpty">No videos available</div>').insertAfter(table);
	
	updateTable(table, channelId);	
}

function updateTable(table, channelId) {
	//Create JSON request data
	var reqData = '{"lastUpdate":' + lastVideosUpdate;
	if(channelId != null) {
		reqData += ',"channel":' + channelId;
	}
	reqData += '}';
	
	
	//Request data from server
	$.getJSON("/videos", reqData, function(data){
		if(data.videos){
			lastVideosUpdate = data.lastUpdate;
			
			if(loadingIndicatorVisible) {
				hideLoadingIndicator(table);
				loadingIndicatorVisible = false;
			}
			
			//Update displayed data
			updateTableRowCount(table, data.videos.length);
			updateTableContent(table, data.videos);
			updateTableEmpty(data.videos.length == 0);
		}
		updateCountdown(data.countdown);
		
		//Update again in 1 second
		setTimeout(function () {updateTable(table, channelId);}, 1000);
	})
	//Error? Try again in 1 second
	.error(function() { setTimeout(function () {updateTable(table, channelId);}, 1000); });	
}

function hideLoadingIndicator(table) {
	$(table).find("tr").last().remove();
}

function updateTableEmpty(empty) {
	if(empty){
		$("#tableEmpty").show();
	}
	else {
		$("#tableEmpty").hide();
	}
}

function updateTableRowCount(table, rows) {
	var currentRows = $(table).find("tr").length - 1;
	
	//Not enough rows? Append some
	if(rows > currentRows){		
		//Append additional rows
		for(var i = currentRows; i < rows; i++){
			var tr = document.createElement("tr");
			for(var j = 0; j < 5; j++){
				var td = document.createElement("td");
				tr.appendChild(td);
			}
			table.find("tbody").get(0).appendChild(tr);
		}
	}
	//Too many rows? Remove no longer required rows
	else if(rows < currentRows){
		var trs = $(table).find("tr");
		for(var i = currentRows; i > rows; i--){
			var tr = trs[i-1];
			$(tr).remove();
		}
	}
	
}

function updateTableContent(table, videos) {
	var showChannels = window.showChannels != false; //May be null

	//Loop through table rows and videos and update each table cell
	$(table).find("tbody").find("tr").each(function(i) {
		var video = videos[i];		
		var tds = $(this).find("td");
		
		$(tds[0]).text(video.uploaded);

		if(showChannels){
			$(tds[1]).html('<a href="channel/' + video.channelID + '">' + video.channelName + '</a>');
		}
		
		$(tds[showChannels ? 2 : 1]).html('<a href="http://youtube.com/watch?v=' + video.youtubeID + '">' + video.name + '</a>');
		
		var state = '';
		switch(video.state) {
		case 3: case 6:
			state += '<a class="table-state-label" href="#" onclick="window.alert(\'' + video.errorMessage + '\')">'; break;
		case 7:
			state += '<a class="table-state-label" href="download?id=' + video.id + '&token=' + video.securityToken + '">'; break;
		}
		state += '<span class="label table-state-label ';
		switch(video.state) {
		case 0: case 2: case 5:
			state += 'label-info'; break;
		case 3: case 6:
			state += 'label-danger'; break;
		case 7:
			state += 'label-success'; break;
		default:
			state += 'label-default'; break;
		}
		state += '">';
		state += '<i class="glyphicon ';
		switch(video.state) {
		case 0: 
			state += 'glyphicon-info-sign'; break;
		case 1: case 4: 
			state += 'glyphicon-time'; break;
		case 2:
			state += 'glyphicon-download'; break;
		case 3: case 6:
			state += 'glyphicon-warning-sign'; break; 
		case 5:
			state += 'glyphicon-record'; break;
		case 7:
			state += 'glyphicon-ok'; break;
		case 8:
			state += 'glyphicon glyphicon-remove'; break;	
		}
		state += '"></i>';
		switch(video.state) {
		case 0: 
			state += 'New'; break;
		case 1: 
			state += 'DL enqueued'; break;
		case 2: 
			state += 'Downloading'; break;
		case 3: 
			state += 'DL failed'; break;
		case 4: 
			state += 'TRC enqueued'; break;
		case 5: 
			state += 'Transcoding'; break;
		case 6: 
			state += 'TRC failed'; break;
		case 7: 
			state += 'Ready'; break;
		case 8:
			state += 'Deleted'; break;
		}
		state += '</span>';
		if(video.state == 3 ||video.state == 6 || video.state == 7) {
			state += '</a>';
		}
		$(tds[showChannels ? 3 : 2]).html(state);
		
		var showOptions = false;
		var options = '<div class="dropdown"><span class="dropdown-toggle glyphicon glyphicon-th-large" style="cursor:pointer" id="videoOptionMenu' + video.id +'" data-toggle="dropdown"></span><ul class="dropdown-menu dropdown-menu-right" role="menu" aria-labelledby="videoOptionMenu' + video.id + '">';		
		if(video.state == 7){
			options += '<li role="presentation"><a role="menuitem" tabindex="-1" style="text-decoration: none" href="javascript:void()" onclick="stream(' + video.id + ')"><span class="glyphicon glyphicon-play"></span> Stream</a></li>';
		}
		if(video.state == 4 || video.state == 6 || video.state == 7 || video.state == 8){
			options += '<li role="presentation"><a role="menuitem" tabindex="-1" style="text-decoration: none" href="javascript:void()" onclick="resetVideo(' + video.id + ')"><span class="glyphicon glyphicon-repeat"></span> Reset</a></li>';
			showOptions = true;
		}
		if(video.state != 2 && video.state != 5 && video.state != 8){
			options += '<li role="presentation"><a role="menuitem" tabindex="-1" style="text-decoration: none" href="javascript:void()" onclick="deleteVideo(' + video.id + ')"><span class="glyphicon glyphicon-trash"></span> Delete</a></li>';
			showOptions = true;
		}
		options += '</ul></div>';
		
		$(tds[showChannels ? 4 : 3]).html(showOptions ? options : '<span class="glyphicon glyphicon-th-large" style="opacity:0.05;filter:alpha(opacity=05)"></span>');
	});
}

function resetVideo(id) {
	$.get('/videos/reset?id=' + id);
}

function forceUpdate() {
	$.get('/videos/forceUpdate');
}

function deleteVideo(id) {
	$.get('/videos/delete?id=' + id);
}

function stream(id) {
	 popup = window.open("/stream?id=" + id, "ytrss - Stream", "width=550,height=100,resizable=yes");
	 popup.focus();
	 return false;
}

function updateCountdown(countdown) {
	var seconds = countdown / 1000;
	
	if(countdown == 0) {
		$("#countdown").text("Updating...");
	}
	else {
		 var minutes = Math.floor(seconds / 60);
		 var remSeconds = Math.floor(seconds % 60);
		 
		 var text = "Next update in: " + minutes;
		 if(minutes == 1) {
			 text +=" minute";
		 }
		 else{
			 text +=" minutes";
		 }
		 text += " and " + remSeconds;
		 if(remSeconds == 1) {
			 text += " second";
		 }
		 else{
			 text += " seconds";
		 }
		 
		 $("#countdown").text(text);
	}
}
