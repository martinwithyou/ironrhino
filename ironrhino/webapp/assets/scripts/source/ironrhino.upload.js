Initialization.upload = function() {
	$(document).on('click', '#files button.reload', function() {
				ajax({
							type : $('#upload_form').attr('method'),
							url : $('#upload_form').attr('action'),
							data : $('#upload_form').serialize(),
							replacement : 'files'
						});
			}).on('click', '#files button.mkdir', function() {
		$.alerts.prompt('', 'newfolder', '', function(t) {
					if (t) {
						var folder = $('#current_folder').text() + t;
						var url = CONTEXT_PATH + '/common/upload/mkdir'
								+ encodeURI(folder);
						ajax({
									url : url,
									dataType : 'json',
									success : function() {
										$('#folder').val(folder);
										$('#files button.reload').click();
										if (typeof history.pushState != 'undefined') {
											var url = CONTEXT_PATH
													+ '/common/upload/list'
													+ encodeURI(folder)
											history.pushState(url, '', url);
										}
									}
								});
					}
				});
	}).on('click', '#files button.capture', function() {
		$.capture({
					oncapture : function(data, timestamp) {
						var filename = 'snapshot_'
								+ new Date(timestamp).format('%Y%m%d%H%M%S')
								+ '.png';
						var file = dataURLtoBlob(data);
						uploadFiles([file], [filename]);
					},
					onerror : function(msg) {
						Message.showError(msg);
					}
				});
	}).on('click', '#files button.delete', function() {
				if (!$('#files tbody input:checked').length)
					Message.showMessage('no.selection');
				else
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
						type : $('#upload_form').attr('method'),
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
function uploadFiles(files, filenames) {
	if (files && files.length) {
		var data = {
			folder : $('#upload_form [name="folder"]').val(),
			autorename : $('#upload_form [name="autorename"]').is(':checked')
		};
		if (filenames && filenames.length)
			data.filename = filenames;
		return $.ajaxupload(files, {
					url : $('#upload_form').attr('action'),
					name : $('#upload_form input[type="file"]').attr('name'),
					data : data,
					beforeSend : Indicator.show,
					success : function(xhr) {
						Indicator.hide();
						var data = xhr.responseText;
						var html = data
								.replace(/<script(.|\s)*?\/script>/g, '');
						var div = $('<div/>').html(html);
						var message = $('#message', div);
						if (message.html()) {
							if ($('.action-error', message).length
									|| !$('#upload_form input[name="pick"]').length)
								if ($('#message').length)
									$('#message').html(message.html());
								else
									$('<div id="message">' + message.html()
											+ '</div>')
											.prependTo($('#content'));
							if ($('.action-error', message).length)
								return;
						}
						$('#files button.reload').trigger('click');
					}
				});
	}
}
