(function($) {
	var current;
	$.fn.listpick = function() {
		$(this).each(function() {
			current = $(this);
			var pickoptions = {
				separator : ',',
				nameindex : 1,
				multiple : false
			}
			$.extend(pickoptions, (new Function("return "
							+ (current.attr('pickoptions') || '{}')))());
			var nametarget = null;
			if (pickoptions.name) {
				nametarget = $('#' + pickoptions.name);
				var close = nametarget.next('a.close');
				if (close.length)
					close.css({
								'cursor' : 'pointer',
								'color' : '#black',
								'margin-left' : '5px',
								'padding' : '0 5px',
								'border' : 'solid 1px #FFC000'
							}).click(function(event) {
								nametarget.text(MessageBundle.get('select'));
								$('#' + pickoptions.id).val('');
								$(this).remove();
								event.stopPropagation();
							});
			}
			current.css('cursor', 'pointer').click(function() {
				$('#_pick_window').remove();
				var win = $('<div id="_pick_window" title="'
						+ MessageBundle.get('select') + '"></div>')
						.appendTo(document.body).dialog({
									width : 600,
									minHeight : 500
								});
				if (win.html() && typeof $.fn.mask != 'undefined')
					win.mask(MessageBundle.get('ajax.loading'));
				else
					win.html('<div style="text-align:center;">'
							+ MessageBundle.get('ajax.loading') + '</div>');
				var target = win.get(0);
				target.onsuccess = function() {
					if (typeof $.fn.mask != 'undefined')
						win.unmask();
					Dialog.adapt(win);
					$('button.confirm', target).live('click', function() {
						var checkbox = $('tbody :checked', target);
						var length = checkbox.length;
						if (!pickoptions.multiple && length > 1) {
							Message.showMessage('select.one');
							return false;
						}
						var ids = [], names = [];
						checkbox.each(function() {
							ids.push($(this).val());
							names
									.push($($(this).closest('tr')[0].cells[pickoptions.nameindex]).text());
						});
						if (pickoptions.name) {
							var nametarget = $('#' + pickoptions.name);
							var name = names.join(pickoptions.separator);
							if (nametarget.is(':input'))
								nametarget.val(name);
							else {
								nametarget.text(name);
								if (!nametarget.next('a.close').length)
									nametarget.after('<a class="close">x</a>')
											.next().css({
														'cursor' : 'pointer',
														'color' : '#black',
														'margin-left' : '5px',
														'padding' : '0 5px',
														'border' : 'solid 1px #FFC000'
													}).click(function(event) {
												nametarget.text(MessageBundle
														.get('select'));
												$('#' + pickoptions.id).val('');
												$(this).remove();
												event.stopPropagation();
											});
							}
						}
						if (pickoptions.id) {
							var idtarget = $('#' + pickoptions.id);
							var id = ids.join(pickoptions.separator);;
							if (idtarget.is(':input'))
								idtarget.val(id);
							else
								idtarget.text(id);
						}
						win.dialog('destroy');
						return false;
					});
				};
				ajax({
							url : pickoptions.url,
							cache : false,
							target : target,
							replacement : '_pick_window:content',
							quiet : true
						});
			});
		});
		return this;
	};

})(jQuery);

Observation.listpick = function(container) {
	$('.listpick', container).listpick();
};