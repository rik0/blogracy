jQuery(function () {
    jQuery('#comment-send').ajaxForm({
        url: '/commentSender',
        clearForm: true,
        type: 'POST',
        success: function () {
            console.log(arguments);
            location.reload(true);
        },
        error: function (request, status, statusMessage) {
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