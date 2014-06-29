<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="net.blogracy.controller.addendum.AddendumController" %>
<%@ page import="net.blogracy.controller.addendum.DelegateController" %>
<%@ page import="net.blogracy.controller.FileSharingImpl"%>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="net.blogracy.model.users.User" %>
<%@ page import="net.blogracy.model.hashes.Hashes" %>
    <%
    String channel = request.getParameter("channel");
    String channelHash = Hashes.hash(channel);
    DelegateController controller = AddendumController.getSingleton().getDelegateController(channelHash);
    FileSharingImpl fileSharingImpl =  FileSharingImpl.getSingleton();
    
    Map<User, String> otherUserScore = new HashMap<User, String>();
    for (User user : fileSharingImpl.getDelegates(channelHash))
    	otherUserScore.put(user, String.valueOf(controller.getDelegateScore(user.getHash().toString())));

    pageContext.setAttribute("channel", channel);
    pageContext.setAttribute("delegate", controller.getCurrentDelegate());
    if (controller.getCurrentDelegate()!= null)
    	pageContext.setAttribute("delegateScore", controller.getDelegateScore(controller.getCurrentDelegate().getHash().toString()));
    else
    	pageContext.setAttribute("delegateScore", "NULL");
    pageContext.setAttribute("otherUsers", otherUserScore);
    %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>${channel}'s channel status</title>
</head>
<body>
<div class="channelTitle">
	<span class="channelUser">${channel}'s channel </span>
	<span class="channelStatus">${controller.CurrentState}</span>
</div>
<div class="currentDelegate">
	<span>Delegate: <span class="currentDelegateUser">${delegate.localNick}</span><span> ${delegate.hash} </span>
	<span class="score"> ${delegateScore} </span></span>
</div>
<div class="otherUsers">
	<ul>
	 	<c:forEach var="user" items="${otherUsers.entrySet}">
		<li><span class="otherDelegateUser">${user.key.localNick}</span><span>(${user.key.hash})</span>
		<span class="score"> ${user.value} </span></li>
		</c:forEach>
	</ul>
</div>
</body>
</html>