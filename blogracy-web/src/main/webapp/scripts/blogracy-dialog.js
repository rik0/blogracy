var dialogHelper = {
    openDialogWithLink: function (url, title, height, width, modal, reloadParent) {
        var $dialog = $('#pop').load(url)
            .dialog({
                autoOpen: false,
                title: title,
                height: height,
                width: width,
                modal: modal,
                close: function (event, ui) { 
                	if (reloadParent == true)
                		location.reload(true);
                	$(this).dialog('destroy'); }
            });

        $dialog.dialog('open');
    }
};