<%@ page import="net.blogracy.controller.ChatController" %>

<% ChatController.getSingleton().joinChannel(request.getParameter("channel")); // Blogracy %>


<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <title>Blogracy chat</title>
    <link rel="stylesheet" href="chat.css" type="text/css">

    <script type="text/javascript" src="js/jquery-1.4.2.min.js"></script>
    <script type="text/javascript" src="js/amq_jquery_adapter.js"></script>
    <script type="text/javascript" src="js/amq.js"></script>
    <script type="text/javascript" src="js/chat.js"></script>
    <script type="text/javascript">
        channel = "${param.channel}"; // Blogracy
        jQuery(function() {
            org.activemq.Amq.init({ uri: 'amq', logging: true, timeout: 45, clientId:(new Date()).getTime().toString() });
            org.activemq.Chat.init();
            document.getElementById('joinB').click(); // Blogracy
        });
    </script>

</head>

<body>

<div id="chatroom">
    <div id="chat"></div>

    <div id="members"></div>

    <div id="input">
        <div id="join" class="hidden">
            Username:&nbsp;
            <input id="username" type="text" value="${param.nick}"/> <!-- Blogracy -->
            <button id="joinB">Join</button>
        </div>
        <div id="joined" class="hidden">
            Chat:&nbsp;
            <input id="phrase" type="text" />
            <button id="sendB">Send</button>
            <button id="leaveB">Leave</button>
        </div>
    </div>
</div>

</body>
</html>
