Initialization.upload = function() {
	$(document).on('click', '#files button.reload', function() {
				ajax({
							url : $('#upload_form').attr('action'),
							data : $('#upload_form').serialize(),
							replacement : 'files'
						});
			}).on('click', '#files button.mkdir', function() {
		$.alerts.prompt('', 'newfolder', '', function(t) {
					if (t) {
						var folder = $('#current_folder').text() + t;
						var url = CONTEXT_PATH + '/common/upload/mkdir'
								+ folder;
						ajax({
									url : url,
									dataType : 'json',
									success : function() {
										$('#folder').val(folder);
										$('#files button.reload').click();
									}
								});
					}
				});
	}).on('click', '#files button.delete', function() {
				deleteFiles()
			});
}
Observation.upload = function(container) {
	var upload_form = $('#upload_form', container);
	if (upload_form.length && typeof window.FileReader != 'undefined') {
		$('input[type="file"]', upload_form).change(function() {
					if (uploadFiles(this.files)) {
						$(this).closest('div').remove();
						addMore(1);
						return false;
					}
				});
		upload_form.bind('dragover', function(e) {
					$(this).addClass('drophover');
					return false;
				}).bind('dragleave', function(e) {
					$(this).removeClass('drophover');
					return false;
				}).get(0).ondrop = function(e) {
			e.preventDefault();
			$(this).removeClass('drophover');
			uploadFiles(e.dataTransfer.files);
			return true;
		};
		$(document.body).bind('dragover', function(e) {
					return false;
				})[0].ondrop = function(e) {
			var id = e.dataTransfer.getData('Text');
			var target = $(e.target);
			if (!id || target.is('#upload_form')
					|| target.parents('#upload_form').length)
				return true;
			var i = id.lastIndexOf('/');
			if (i > 0)
				id = id.substring(i + 1);
			if (e.preventDefault)
				e.preventDefault();
			if (e.stopPropagation)
				e.stopPropagation();
			deleteFiles(id);
		}
	}

	$('.uploaditem', container).attr('draggable', true).each(function() {
		var t = $(this);
		this.ondragstart = function(e) {
			e.dataTransfer.effectAllowed = 'copy';
			e.dataTransfer.setData('Text', $(':input:eq(0)', t.closest('tr'))
							.attr('value'));
		};
	});

	$('#more', container).click(function() {
				addMore(1);
			});
};

function addMore(n) {
	var f = $('input[type="file"]:last').parent();
	var r;
	for (var i = 0; i < n; i++) {
		r = f.clone(true);
		f.after(r);
		f = r;
	}
}
function deleteFiles(file) {
	$.alerts.confirm(MessageBundle.get('confirm.delete'), MessageBundle
					.get('select'), function(b) {
				if (b) {
					var options = {
						url : CONTEXT_PATH + '/common/upload/delete',
						dataType : 'json',
						complete : function() {
							$('#files button.reload').click();
						}
					};
					if (file) {
						var data = $('#upload_form').serialize();
						var params = [];
						params.push('id=' + file);
						if (data) {
							var arr = data.split('&');
							for (var i = 0; i < arr.length; i++) {
								var arr2 = arr[i].split('=', 2);
								if (arr2[0] != 'id')
									params.push(arr[i]);
							}
						}
						options.data = params.join('&');
					} else {
						options.data = $('#upload_form').serialize();
					}
					ajax(options);
				}
			});

}
function uploadFiles(files) {
	if (files && files.length)
		return $.ajaxupload(files, {
					url : $('#upload_form').attr('action') + '?'
							+ $('#upload_form').serialize(),
					name : $('#upload_form input[type="file"]').attr('name'),
					beforeSend : Indicator.show,
					success : function(xhr) {
						Ajax.handleResponse(xhr.responseText, {
									replacement : 'files'
								});
						Indicator.hide();
					}
				});
}
