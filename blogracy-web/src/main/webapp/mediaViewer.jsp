<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<%@ page import="org.apache.shindig.social.opensocial.model.MediaItem"%>
<%@ page import="net.blogracy.controller.FileSharingImpl"%>
<%@ page import="net.blogracy.controller.MediaController"%>
<%@ page import="net.blogracy.config.Configurations"%>
<%@ page import="net.blogracy.model.users.UserData"%>
<%
	String userId = request.getParameter("uid");
	String albumId = request.getParameter("aid");
	String mediaId = request.getParameter("mid");

	MediaItem media = MediaController.getMediaItemWithCachedImage(userId,
	albumId, mediaId);
	UserData userData = FileSharingImpl.getUserData(userId);
	pageContext.setAttribute("media", media);
	pageContext.setAttribute("uid", userId);
	pageContext.setAttribute("aid", albumId);
	pageContext.setAttribute("localUser", Configurations
	.getUserConfig().getUser());
	pageContext.setAttribute("comments", userData.getCommentsByObjectId(mediaId));
%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Blogracy Media Viewer</title>

<link type="text/css" href="/css/blogracy-mediaViewer.css"
	rel="stylesheet" />
<link type="text/css" href="/css/bootstrap.css" rel="stylesheet"/>

<script type="text/javascript" src="/scripts/jquery-1.8.2.js"></script>
<script type="text/javascript" src="/scripts/jquery.form.js"></script>
<script type="text/javascript" src="/scripts/blogracy-commentSender.js"></script>
</head>
<body>
	<div class="blogracyMedia">
		<img class="blogracyImage" src="${media.url}" />
	</div>
	<div class="commentsPane">
		<div class="commentsList">
		<c:forEach var="comment" items="${comments}">
			<div class="comment">
				<div class="commentator">
					<p>${comment.actor.displayName}</p>
				</div>
				<div class="commentDate">
					<p>${comment.published}</p>
				</div>
				<div class="commentText">
					<p>${comment.object.content}</p>
				</div>
			</div>

			</c:forEach>
		</div>
		<div class="inputComment">
			<form class="span10" id="comment-send">
				<input type="hidden" name="userId" value="${uid}" /> 
				<input type="hidden" name="albumId" value="${aid}" /> 
				<input type="hidden" name="mediaId" value="${media.id}" />
				<fieldset class="form-stacked">
					<div class="clearfix">
						<label for="messageArea">Send a new comment</label>
						<div class="input">
							<textarea class="xxlarge" name="userCommentText" id="messageArea"
								rows="3"></textarea>
						</div>
					</div>
				</fieldset>
				<fieldset>
					<div class="actions">
						<input type="submit" class="btn primary" value="Send comment">&nbsp;
						<button type="reset" class="btn">Cancel</button>
					</div>
				</fieldset>
			</form>
		</div>
	</div>
</body>
</html>