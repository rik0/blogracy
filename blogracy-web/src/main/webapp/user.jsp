<%@ page import="net.blogracy.model.hashes.Hashes"%>
<%@ page import="net.blogracy.model.users.Users"%>
<%@ page import="net.blogracy.controller.FileSharing"%>
<%@ page import="net.blogracy.config.Configurations"%>
<%
	String userHash = request.getParameter("user");
	if (userHash == null || userHash.length() == 0) {
		userHash = Configurations.getUserConfig().getUser().getHash()
				.toString();
	} else if (userHash.length() != 32) {
		userHash = Hashes.hash(userHash); // TODO: remove
	}

	pageContext.setAttribute("application", "Blogracy");
	pageContext.setAttribute("user",
			Users.newUser(Hashes.fromString(userHash)));
	pageContext.setAttribute("feed", FileSharing.getFeed(userHash));
	pageContext.setAttribute("friends", Configurations.getUserConfig()
			.getFriends());
	pageContext.setAttribute("localUser", Configurations
			.getUserConfig().getUser());
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<title>${application}</title>
<meta name="description" content="">
<meta name="author" content="">

<!-- Le HTML5 shim, for IE6-8 support of HTML elements -->
<!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->

<!-- Le styles -->
<!-- Bootstrap CSS Toolkit styles -->
<link href="/css/bootstrap.css" rel="stylesheet" />

<!-- Bootstrap styles for responsive website layout, supporting different screen sizes -->
<!--  <link rel="stylesheet" href="http://blueimp.github.com/cdn/css/bootstrap-responsive.min.css">-->

<!-- CSS to style the file input field as button and adjust the Bootstrap progress bars -->
<link rel="stylesheet" href="css/jquery.fileupload-ui.css">

<script src="scripts/jquery-1.7.js"></script>
<script src="scripts/jquery.form.js"></script>
<script src="scripts/bootstrap-alerts.js"></script>

<script type="text/javascript">
	// wait for the DOM to be loaded
	jQuery(function() {
		jQuery('#message-send')
				.ajaxForm(
						{
							url : '/fileupload',
							clearForm : true,
							type : 'POST',
							success : function() {
								console.log(arguments);
							},
							error : function(request, status, statusMessage) {
								var serverSideException = JSON
										.parse(request.responseText);
								var errorMessage = '<div class="alert-message block-message error"><a class="close" href="#">x</a>'
										+ '<p><strong>'
										+ serverSideException.errorMessage
										+ '</strong></p>'
										+ '<pre>'
										+ serverSideException.errorTrace
												.join("\n")
										+ '</pre>'
										+ '</div>';
								jQuery(errorPlace).html(errorMessage);
								jQuery(".alert-message").alert();
							}

						});
	});
</script>
<!--  image gallery loading script -->
<script type="text/javascript">
	// Load existing files into imageUploader
	$(function() {
		$('#imageUpload').each(function() {
			var that = this;
			$.getJSON(this.action, {
				user : "${user.localNick}"
			}, function(result) {
				if (result && result.length) {
					$(that).fileupload('option', 'done').call(that, null, {
						result : result
					});
				}
			});
		});
	});
</script>

<style type="text/css">
/* Override some defaults */
html,body {
	background-color: #eee;
}

body {
	padding-top: 40px;
	/* 40px to make the container go all the way to the bottom of the topbar */
}

.container>footer p {
	text-align: center; /* center align it with the container */
}

.container {
	width: 820px;
	/* downsize our container to make the content feel a bit tighter and more cohesive.
                           * NOTE: this removes two full columns from the grid, meaning you only go to
                           * 14 columns and not 16.
                           */
}

.blogracy-thumbnail {
	max-width: 80px;
	max-height: 80px;
}

/* The white background content wrapper */
.content {
	background-color: #fff;
	padding: 20px;
	margin: 0 -20px;
	/* negative indent the amount of the padding to maintain the grid system */
	-webkit-border-radius: 0 0 6px 6px;
	-moz-border-radius: 0 0 6px 6px;
	border-radius: 0 0 6px 6px;
	-webkit-box-shadow: 0 1px 2px rgba(0, 0, 0, .15);
	-moz-box-shadow: 0 1px 2px rgba(0, 0, 0, .15);
	box-shadow: 0 1px 2px rgba(0, 0, 0, .15);
}

/* Page header tweaks */
.page-header {
	background-color: #f5f5f5;
	padding: 20px 20px 10px;
	margin: -20px -20px 20px;
}

/* Styles you shouldn't keep as they are for displaying this base example only */
.content .span10,.content .span4 {
	min-height: 200px;
}

/* Give a quick and non-cross-browser friendly divider */
.content .span4 {
	margin-left: 0;
	padding-left: 19px;
	border-left: 1px solid #eee;
}

.topbar .btn {
	border: 0;
}

.progress {
	background-color: #F7F7F7;
	background-image: -moz-linear-gradient(center top, #F5F5F5, #F9F9F9);
	background-repeat: repeat-x;
	border-radius: 4px 4px 4px 4px;
	box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1) inset;
	height: 18px;
	margin-bottom: 18px;
	overflow: hidden;
}

.progress .bar {
	-moz-box-sizing: border-box;
	-moz-transition: width 0.6s ease 0s;
	background-color: #0E90D2;
	background-image: -moz-linear-gradient(center top, #149BDF, #0480BE);
	background-repeat: repeat-x;
	box-shadow: 0 -1px 0 rgba(0, 0, 0, 0.15) inset;
	color: #FFFFFF;
	font-size: 12px;
	height: 18px;
	text-align: center;
	text-shadow: 0 -1px 0 rgba(0, 0, 0, 0.25);
	width: 0;
}

.progress-striped .bar {
	background-color: #149BDF;
	background-image: -moz-linear-gradient(-45deg, rgba(255, 255, 255, 0.15)
		25%, transparent 25%, transparent 50%, rgba(255, 255, 255, 0.15) 50%,
		rgba(255, 255, 255, 0.15) 75%, transparent 75%, transparent );
	background-size: 40px 40px;
}

.progress.active .bar {
	-moz-animation: 2s linear 0s normal none infinite progress-bar-stripes;
}

.progress-danger .bar {
	background-color: #DD514C;
	background-image: -moz-linear-gradient(center top, #EE5F5B, #C43C35);
	background-repeat: repeat-x;
}

.progress-danger.progress-striped .bar {
	background-color: #EE5F5B;
	background-image: -moz-linear-gradient(-45deg, rgba(255, 255, 255, 0.15)
		25%, transparent 25%, transparent 50%, rgba(255, 255, 255, 0.15) 50%,
		rgba(255, 255, 255, 0.15) 75%, transparent 75%, transparent );
}

.progress-success .bar {
	background-color: #5EB95E;
	background-image: -moz-linear-gradient(center top, #62C462, #57A957);
	background-repeat: repeat-x;
}

.progress-success.progress-striped .bar {
	background-color: #62C462;
	background-image: -moz-linear-gradient(-45deg, rgba(255, 255, 255, 0.15)
		25%, transparent 25%, transparent 50%, rgba(255, 255, 255, 0.15) 50%,
		rgba(255, 255, 255, 0.15) 75%, transparent 75%, transparent );
}

.progress-info .bar {
	background-color: #4BB1CF;
	background-image: -moz-linear-gradient(center top, #5BC0DE, #339BB9);
	background-repeat: repeat-x;
}

.progress-info.progress-striped .bar {
	background-color: #5BC0DE;
	background-image: -moz-linear-gradient(-45deg, rgba(255, 255, 255, 0.15)
		25%, transparent 25%, transparent 50%, rgba(255, 255, 255, 0.15) 50%,
		rgba(255, 255, 255, 0.15) 75%, transparent 75%, transparent );
}

.progress-warning .bar {
	background-color: #FAA732;
	background-image: -moz-linear-gradient(center top, #FBB450, #F89406);
	background-repeat: repeat-x;
}

.progress-warning.progress-striped .bar {
	background-color: #FBB450;
	background-image: -moz-linear-gradient(-45deg, rgba(255, 255, 255, 0.15)
		25%, transparent 25%, transparent 50%, rgba(255, 255, 255, 0.15) 50%,
		rgba(255, 255, 255, 0.15) 75%, transparent 75%, transparent );
}
</style>

<!-- Le fav and touch icons -->
<link rel="shortcut icon" href="images/favicon.ico">
<link rel="apple-touch-icon" href="images/apple-touch-icon.png">
<link rel="apple-touch-icon" sizes="72x72"
	href="images/apple-touch-icon-72x72.png">
<link rel="apple-touch-icon" sizes="114x114"
	href="images/apple-touch-icon-114x114.png">
</head>

<body>

	<div class="topbar">
		<div class="fill">
			<div class="container">
				<a class="brand" href="#">${application}</a>
				<ul class="nav">
					<li class="active"><a href="#">Home</a></li>
					<li><a href="#about">About</a></li>
					<li><a href="#contact">Contact</a></li>
				</ul>
			</div>
		</div>
	</div>

	<div class="container">

		<div class="content">
			<div class="page-header">
				<h1>
					${user.localNick} <small>(hardcoded email)</small>
				</h1>
			</div>
			<div class="row">
				<div id="errorPlace"></div>
			</div>
			<div class="row">

				<div class="span10">
					<h2>Messages</h2>

					<form class="span10" id="message-send">

						<fieldset class="form-stacked">
							<div class="clearfix">
								<label for="messageArea">Send a new message</label>
								<div class="input">
									<textarea class="xxlarge" name="usertext" id="messageArea"
										rows="3"></textarea>
								</div>
							</div>
						</fieldset>
						<fieldset class="form-stacked">
							<div class="clearfix">
								<label for="fileArea">Share a new file</label>
								<div class="input">
									<input class="xylarge" name="userfile" id="fileArea"
										type="file" />
								</div>
							</div>
						</fieldset>
						<fieldset>
							<div class="actions">
								<input type="submit" class="btn primary" value="Send message">&nbsp;
								<button type="reset" class="btn">Cancel</button>
							</div>
						</fieldset>
					</form>

					<div class="span10" id="user-feed">
						<ul>
							<c:forEach var="entry" items="${feed.entries}">
								<li>${entry.description.value}</li>
							</c:forEach>
						</ul>
					</div>
				</div>
				<div class="span4">
					<h3>Local user</h3>
					<ul>
						<li><a href="/user.jsp?user=${localUser.hash}">${localUser.localNick}</a></li>
					</ul>

					<h3>Followers</h3>

					<h3>Followees</h3>
					<ul id="user-friends">
						<c:forEach var="friend" items="${friends}">
							<li><a href="/user.jsp?user=${friend.hash}">${friend.localNick}</a></li>
						</c:forEach>
					</ul>
					<h3>Tags</h3>
				</div>
			</div>
			<div class="row">
				<div class="span10" id="image-gallery">
					<h2>Image Gallery</h2>

					<!-- The file upload form used as target for the file upload widget -->
					<form id="imageUpload" class="span10"
						action="/ImageGalleryUploader" method="POST"
						enctype="multipart/form-data">
						<input type="hidden" name="user" value="${user.hash}" />
						<!-- The fileupload-buttonbar contains buttons to add/delete files and start/cancel the upload -->
						<div class="row fileupload-buttonbar">
							<div class="span7">
								<!-- The fileinput-button span is used to style the file input field as button -->
								<span class="btn btn-success fileinput-button"> <i
									class="icon-plus icon-white"></i> <span>Add files...</span> <input
									type="file" name="files[]" multiple>
								</span>
								<button type="submit" class="btn btn-primary start">
									<i class="icon-upload icon-white"></i> <span>Start
										upload</span>
								</button>
								<button type="reset" class="btn btn-warning cancel">
									<i class="icon-ban-circle icon-white"></i> <span>Cancel
										upload</span>
								</button>
								<button type="button" class="btn btn-danger delete">
									<i class="icon-trash icon-white"></i> <span>Delete</span>
								</button>
								<input type="checkbox" class="toggle">
							</div>
							<div class="span5">
								<!-- The global progress bar -->
								<div
									class="progress progress-success progress-striped active fade">
									<div class="bar" style="width: 0%;"></div>
								</div>
							</div>
						</div>
						<!-- The loading indicator is shown during file processing -->
						<div class="fileupload-loading"></div>
						<br>
						<!-- The table listing the files available for upload/download -->
						<table class="table table-striped">
							<tbody class="files" data-toggle="modal-gallery"
								data-target="#modal-gallery"></tbody>
						</table>
					</form>
				</div>
			</div>

		</div>

		<footer>
			<p>&copy; University of Parma 2011</p>
		</footer>

	</div>
	<!-- /container -->
	<!-- modal-gallery is the modal dialog used for the image gallery -->
	<div id="modal-gallery" class="modal modal-gallery hide fade"
		data-filter=":odd">
		<div class="modal-header">
			<a class="close" data-dismiss="modal">&times;</a>
			<h3 class="modal-title"></h3>
		</div>
		<div class="modal-body">
			<div class="modal-image"></div>
		</div>
		<div class="modal-footer">
			<a class="btn modal-download" target="_blank"> <i
				class="icon-download"></i> <span>Download</span>
			</a> <a class="btn btn-success modal-play modal-slideshow"
				data-slideshow="5000"> <i class="icon-play icon-white"></i> <span>Slideshow</span>
			</a> <a class="btn btn-info modal-prev"> <i
				class="icon-arrow-left icon-white"></i> <span>Previous</span>
			</a> <a class="btn btn-primary modal-next"> <span>Next</span> <i
				class="icon-arrow-right icon-white"></i>
			</a>
		</div>
	</div>
	<!-- The template to display files available for upload -->
	<script id="template-upload" type="text/x-tmpl">
{% for (var i=0, file; file=o.files[i]; i++) { %}
    <tr class="template-upload fade">
        <td class="preview"><span class="fade"></span></td>
        <td class="name"><span>{%=file.name%}</span></td>
        <td class="size"><span>{%=o.formatFileSize(file.size)%}</span></td>
        {% if (file.error) { %}
            <td class="error" colspan="2"><span class="label label-important">{%=locale.fileupload.error%}</span> {%=locale.fileupload.errors[file.error] || file.error%}</td>
        {% } else if (o.files.valid && !i) { %}
            <td>
                <div class="progress progress-success progress-striped active"><div class="bar" style="width:0%;"></div></div>
            </td>
            <td class="start">{% if (!o.options.autoUpload) { %}
                <button class="btn btn-primary">
                    <i class="icon-upload icon-white"></i>
                    <span>{%=locale.fileupload.start%}</span>
                </button>
            {% } %}</td>
        {% } else { %}
            <td colspan="2"></td>
        {% } %}
        <td class="cancel">{% if (!i) { %}
            <button class="btn btn-warning">
                <i class="icon-ban-circle icon-white"></i>
                <span>{%=locale.fileupload.cancel%}</span>
            </button>
        {% } %}</td>
    </tr>
{% } %}
	</script>
	<!-- The template to display files available for download -->
	<script id="template-download" type="text/x-tmpl">
{% for (var i=0, file; file=o.files[i]; i++) { %}
    <tr class="template-download fade">
        {% if (file.error) { %}
            <td></td>
            <td class="name"><span>{%=file.name%}</span></td>
            <td class="size"><span>{%=o.formatFileSize(file.size)%}</span></td>
            <td class="error" colspan="2"><span class="label label-important">{%=locale.fileupload.error%}</span> {%=locale.fileupload.errors[file.error] || file.error%}</td>
        {% } else { %}
            <td class="preview">{% if (file.thumbnail_url) { %}
                <a href="{%=file.url%}" title="{%=file.name%}" rel="gallery" download="{%=file.name%}"><img src="{%=file.thumbnail_url%}" class="blogracy-thumbnail"></a>
            {% } %}</td>
            <td class="name">
                <a href="{%=file.url%}" title="{%=file.name%}" rel="{%=file.thumbnail_url&&'gallery'%}" download="{%=file.name%}">{%=file.name%}</a>
            </td>
            <td class="size"><span>{%=o.formatFileSize(file.size)%}</span></td>
            <td colspan="2"></td>
        {% } %}
        <td class="delete">
            <button class="btn btn-danger" data-type="{%=file.delete_type%}" data-url="{%=file.delete_url%}">
                <i class="icon-trash icon-white"></i>
                <span>{%=locale.fileupload.destroy%}</span>
            </button>
            <input type="checkbox" name="delete" value="1">
        </td>
    </tr>
{% } %}
</script>

	<!-- The jQuery UI widget factory, can be omitted if jQuery UI is already included -->
	<script src="scripts/fileupload/vendor/jquery.ui.widget.js"></script>
	<!-- The Templates plugin is included to render the upload/download listings -->
	<script src="scripts/fileupload/tmpl.min.js"></script>
	<!-- The Iframe Transport is required for browsers without support for XHR file uploads -->
	<script src="scripts/fileupload/jquery.iframe-transport.js"></script>
	<!-- The basic File Upload plugin -->
	<script src="scripts/fileupload/jquery.fileupload.js"></script>

	<!-- The Load Image plugin is included for the preview images and image resizing functionality -->
	<script src="scripts/fileupload/load-image.min.js"></script>
	<!-- The Canvas to Blob plugin is included for image resizing functionality -->
	<script src="scripts/fileupload/canvas-to-blob.min.js"></script>
	<!-- Bootstrap JS and Bootstrap Image Gallery are not required, but included for the demo -->
	<script src="scripts/fileupload/bootstrap.min.js"></script>
	<script src="scripts/fileupload/bootstrap-image-gallery.min.js"></script>

	<!-- The File Upload file processing plugin -->
	<script src="scripts/fileupload/jquery.fileupload-fp.js"></script>
	<!-- The File Upload user interface plugin -->
	<script src="scripts/fileupload/jquery.fileupload-ui.js"></script>
	<!-- The localization script -->
	<script src="scripts/fileupload/locale.js"></script>
	<!-- The main application script -->
	<script src="scripts/fileupload/main.js"></script>


</body>
</html>