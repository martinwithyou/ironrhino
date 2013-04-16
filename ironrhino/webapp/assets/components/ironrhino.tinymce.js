$(function() {
	$(document).on('click', '.mce-i-image', function() {
				if ($('#page_id').val()) {
					var interval = setInterval(function() {
								if (appendIcon())
									clearInterval(interval);
							}, 200);
				}
			}).on('click', '.mce-i-browse', function() {
		if (!$('#mce-browse-modal').length) {
			var modal = $('<div id="mce-browse-modal" class="modal" style="z-index:65537;height:500px;"><div style="padding: 5px 5px 0 0;"><button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button></div><div  id="mce-browse-modal-body" class="modal-body" style="height:400px;"></div><div  id="mce-browse-modal-footer" class="modal-footer"></div></div>')
					.appendTo(document.body);
			$('button.close', modal).click(function() {
						modal.fadeIn().remove();
					});
			browse();
		}
	});
	$(document.body).bind('dragover', function(e) {
				return false;
			})[0].ondrop = function(e) {
		var id = e.dataTransfer.getData('Text');
		var target = $(e.target);
		if (!id || target.is('#mce-browse-modal-body')
				|| target.parents('#mce-browse-modal-body').length)
			return true;
		var i = id.lastIndexOf('/');
		if (i > 0)
			id = id.substring(i + 1);
		if (e.preventDefault)
			e.preventDefault();
		if (e.stopPropagation)
			e.stopPropagation();
		if (confirm(MessageBundle.get('confirm.delete'))) {
			$.post(CONTEXT_PATH + '/common/upload/delete', {
						folder : '/page/' + $('#page_id').val(),
						id : id
					}, browse);
		}
	}
});

function appendIcon() {
	var combobox = $('.mce-combobox');
	if (combobox.length) {
		combobox
				.addClass('mce-has-open')
				.append('<div class="mce-btn mce-open" tabindex="-1"><button hidefocus="" tabindex="-1"><i class="mce-ico mce-i-browse"></i></button></div>')
				.find('input.mce-textbox').css('width', '254px');
		return true;
	}
	return false;
}

function browse() {
	var pageid = $('#page_id').val();
	var panel = $('#mce-browse-modal-body');
	panel.bind('dragover', function(e) {
				$(this).css('border', '2px dashed #333');
				return false;
			}).bind('dragleave', function(e) {
				$(this).css('border', '0');
				return false;
			}).get(0).ondrop = function(e) {
		e.preventDefault();
		$(this).css('border', '0');
		upload(e.dataTransfer.files);
		return true;
	};
	$.getJSON(CONTEXT_PATH + '/common/page/files/' + pageid
					+ '?suffix=jpg,gif,png,bmp', function(data) {
				var html = '';
				$.each(data, function(key, val) {
					html += '<img src="'
							+ val
							+ '" alt="'
							+ key
							+ '" title="'
							+ key
							+ '" style="margin:5px;width:92px;height:92px;cursor:pointer;"/>';
				});
				panel.html(html);
				$('img', panel).attr('draggable', true).each(function() {
							var t = $(this);
							this.ondragstart = function(e) {
								e.dataTransfer.effectAllowed = 'copy';
								e.dataTransfer.setData('Text', t.attr('title'));
							};
						}).click(function() {
							$('#mce-browse-modal').fadeIn().remove();
							$('.mce-combobox input.mce-textbox').val(this.src);
							$('input.mce-textbox:eq(1)').focus();
						});
				$('#mce-browse-modal-footer')
						.html('<input id="files" type="file" multiple="true"/>')
						.find('input[type="file"]').change(function() {
									upload(this.files);
								});
			});
	return false;
}

function upload(files) {
	$.ajaxupload(files, {
				url : CONTEXT_PATH + '/common/upload?folder=/page/'
						+ $('#page_id').val(),
				success : browse
			});
}