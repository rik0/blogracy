<%@ page import="net.blogracy.model.hashes.Hashes" %>
<%@ page import="net.blogracy.model.users.Users" %>
<%@ page import="net.blogracy.controller.FileSharing" %>
<%@ page import="net.blogracy.config.Configurations" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%
String localUserHash = Configurations.getUserConfig().getUser().getHash().toString();
String userHash = request.getParameter("user");
if (userHash == null || userHash.length() == 0) {
    userHash = Configurations.getUserConfig().getUser().getHash().toString();
} else if (userHash.length() != 32) {
	  userHash = Hashes.hash(userHash); // TODO: remove
}

//String channel = ChatController.getPrivateChannel(localUserHash, userHash);
//ChatController.getSingleton().joinChannel(channel);

pageContext.setAttribute("localUserHash",  localUserHash);
pageContext.setAttribute("userHash", userHash);
pageContext.setAttribute("application", "Blogracy");
pageContext.setAttribute("user", Users.newUser(Hashes.fromString(userHash)));
pageContext.setAttribute("friends", Configurations.getUserConfig().getFriends());
pageContext.setAttribute("localUser", Configurations.getUserConfig().getUser());
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>${application} - Set Attribute</title>
    <meta name="description" content="">
    <meta name="author" content="">

    <!-- Le HTML5 shim, for IE6-8 support of HTML elements -->
    <!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->

    <!-- Le styles -->
    <link href="/css/bootstrap.css" rel="stylesheet" type="text/css" />
    <link href="/css/lightbox.css" rel="stylesheet" type="text/css" />
    <link href="/css/smoothness/jquery-ui-1.8.20.custom.css" rel="stylesheet" type="text/css" />
    
    <script src="scripts/jquery-1.7.min.js" type="text/javascript"></script>
    <script src="scripts/jquery.form.js" type="text/javascript"></script>

    <script type="text/javascript">
        // wait for the DOM to be loaded
        jQuery(function() {
            jQuery('#attribute-send').ajaxForm({
                url: '/attributeupload',
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
	
    <style type="text/css">
        /* Override some defaults */
        html, body {
            background-color: #eee;
        }

        body {
            padding-top: 40px; /* 40px to make the container go all the way to the bottom of the topbar */
        }

        .container > footer p {
            text-align: center; /* center align it with the container */
        }

        .container {
            width: 820px; /* downsize our container to make the content feel a bit tighter and more cohesive.
                           * NOTE: this removes two full columns from the grid, meaning you only go to
                           * 14 columns and not 16.
                           */
        }

        /* The white background content wrapper */
        .content {
            background-color: #fff;
            padding: 20px;
            margin: 0 -20px; /* negative indent the amount of the padding to maintain the grid system */
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
        .content .span10,
        .content .span4 {
            min-height: 500px;
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
        
        .blogracy-thumbnail {
			      max-width: 80px;
			      max-height: 80px;
			  }
			  .set {
			      padding-top:10px;
			      clear:both;
			  }
		
    </style>

    <!-- Le fav and touch icons -->
    <link rel="shortcut icon" href="images/favicon.ico">
    <link rel="apple-touch-icon" href="images/apple-touch-icon.png">
    <link rel="apple-touch-icon" sizes="72x72" href="images/apple-touch-icon-72x72.png">
    <link rel="apple-touch-icon" sizes="114x114" href="images/apple-touch-icon-114x114.png">
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
            <h1>${user.localNick}
                <small>(UserID)</small>
            </h1>
        </div>
        <div class="row">
            <div id="errorPlace"></div>
        </div>

        <div class="row">
            <div class="span10">
                				
                <form class="span10" id="attribute-send">
                    <input type="hidden" name="user" value="${user.hash}" />
                    <fieldset class="form-stacked">
                        <div class="clearfix">
                            <div class="input">
                            	<h2>Set attributes for your friends</h2>
								<ul id="user-friends-attr">
                    				<c:forEach var="friend" items="${friends}">
					                    <li>
					                    <p>${friend.localNick}
					                    <textarea class="xxlarge" name="${friend.localNick}" rows="1"></textarea>
					                    </p>
					                    </li>
                    				</c:forEach>
                				</ul>
                				<p>
                            	<b>ATTENTION</b>: The attribute must follow this scheme: "attribute:value"
                            	</p>
                            </div>
                        </div>
                    </fieldset>
                    <fieldset>
                        <div class="actions">
                            <input type="submit" class="btn primary" value="Set attribute">&nbsp;
                            <button type="reset" class="btn">Cancel</button>
                        </div>
                    </fieldset>
                </form>
                
            </div>
        </div>
      
    </div>

    <footer>
        <p>&copy; University of Parma 2011</p>
    </footer>

</div> <!-- /container -->

</body>
</html>