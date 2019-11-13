$(function () {
    $('#create-gallery').ajaxForm({
        url: '/ImageGalleryUploader',
        clearForm: true,
        type: 'POST',
        success: function () {
            console.log(arguments);
            location.reload();
        },
        error: function (request, status, statusMessage) {
            var serverSideException = JSON.parse(request.responseText);
            var errorMessage = '<div class="alert-message block-message error"><a class="close" href="#">x</a>' +
                                       '<p><strong>' + serverSideException.errorMessage + '</strong></p>' +
                                        '<pre>' + serverSideException.errorTrace.join("\n") + '</pre>' +
                                       '</div>';
            $(errorPlace).html(errorMessage);
            $(".alert-message").alert();
        }

    });
});

var blogracyGalleryHelper = {
    openDialogWithLink: function (url) {
        var $dialog = $('#pop').load(url)
            .dialog({
                autoOpen: false,
                title: 'Upload Images to Gallery',
                height: 650,
                width: 750,
                modal: true,
                close: function (event, ui) { location.reload(true); $(this).dialog('destroy'); }
            });

        $dialog.dialog('open');
    }
};
