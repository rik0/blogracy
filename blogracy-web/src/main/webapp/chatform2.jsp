<%@ page import="net.blogracy.model.hashes.Hashes" %>
<%@ page import="net.blogracy.model.users.Users" %>
<%@ page import="net.blogracy.controller.FileSharing" %>
<%@ page import="net.blogracy.controller.ChatController" %>
<%@ page import="net.blogracy.config.Configurations" %>
<%
String userHash = request.getParameter("user");
if (userHash == null || userHash.length() == 0) {
    userHash = Configurations.getUserConfig().getUser().getHash().toString();
} else if (userHash.length() != 32) {
	userHash = Hashes.hash(userHash); // TODO: remove
}

pageContext.setAttribute("application", "Blogracy");
pageContext.setAttribute("localUser", Configurations.getUserConfig().getUser());
pageContext.setAttribute("remoteUser", ChatController.getRemoteUser());
pageContext.setAttribute("loc",  Configurations.getUserConfig().getUser().getHash().toString());
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="en">
<head>

	<meta charset="utf-8">
    <title>${application}</title>
    <meta name="description" content="">
    <meta name="author" content="">
	
    <link rel="stylesheet" href="/chat files/chat.css" type="text/css">
    <style type="text/css" media="screen"> 
        @import url(/admin/styles/sorttable.css);
        @import url(/admin/styles/prettify.css);
    </style>

	<script type="text/javascript" src="chat files/jquery-1.js"></script>
	<script type="text/javascript" src="chat files/amq_jquery_adapter.js"></script>
	<script type="text/javascript" src="chat files/amq.js"></script>
	<script type="text/javascript" src="chat files/chat2.js"></script>
	
	<script src="/scripts/jquery-1.7.js"></script>
    <script src="/scripts/jquery.form.js"></script>
    <script src="/scripts/bootstrap-alerts.js"></script>
	
	<script type="text/javascript">
        // wait for the DOM to be loaded
        jQuery(function() {
            jQuery('#message-send').ajaxForm({
                url: '/fileupload',
                clearForm: true,
                type: 'POST',
                success: function() {
                    console.log(arguments);
                },
                error: function(request, status, statusMessage) {
                    var serverSideException = JSON.parse(request.responseText);
                    var errorMessage = '<div class="alert-message block-message error"><a class="close" href="#">x</a>' +
                                       '<p><strong>' + serverSideException.errorMessage + '</strong></p>' +
                                        '<pre>' + serverSideException.errorTrace.join("\n") + '</pre>' +
                                       '</div>';
                    jQuery(errorPlace).html(errorMessage);
                    jQuery(".alert-message").alert();
                }
                
            });
        });

    </script>
	
	<script type="text/javascript">
		window.onload = function() {
			org.activemq.Amq.init({ uri: 'amq', logging: true, timeout: 45, clientId:(new Date()).getTime().toString() });
			org.activemq.Chat.init();
		};
	</script> 
	
</head> 
 
<body>

<div class="white_box">    
    <table border="0"> 
            <tr> 
                <td style="overflow: hidden;" valign="top" width="50%"> 
                    <div class="body-content">

						<div id="chatroom">
							<div id="chat"></div>
							<div id="members" class="hidden"></div>

							<div id="input">
								<div id="join" class="">
									Username:&nbsp;&nbsp;
									<input id="username" type="text" value="${localUser.localNick}" readonly="readonly">
									<button id="joinB">Re-Join</button>
								</div>
								<div id="joined" class="hidden">
									Chat:&nbsp;
									<input id="phrase" type="text">
									<button id="sendB">Send</button>
									<button id="leaveB" disabled="disabled">Leave</button>
								</div>
							</div>
						</div>

                    </div> 
                </td> 
            </tr>  
    </table> 
	<div id="channelName" title="${loc}"></div>
</div> 

</body>
</html>