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
									width : 650,
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
									.push($($(this).closest('tr')[0].cells[pickoptions.nameindex])
											.text());
						});
						var separator = pickoptions.separator;
						if (pickoptions.name) {
							var nametarget = $('#' + pickoptions.name);
							var name = names.join(separator);
							if (nametarget.is(':input')) {
								if (pickoptions.multiple)
									nametarget.val((nametarget.val()
											+ (nametarget.val()
													? separator
													: '') + name)
											.split(separator).unique()
											.join(separator));
								else
									nametarget.val(name);
								var form = nametarget.closest('form');
								if (!form.hasClass('nodirty'))
									form.addClass('dirty');
							} else {
								if (pickoptions.multiple) {
									nametarget.text(((nametarget
											.hasClass('dirty') ? nametarget
											.text()
											+ separator : '') + name)
											.split(separator).unique()
											.join(separator)).addClass('dirty');
								} else
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
							var id = ids.join(separator);
							if (idtarget.is(':input')) {
								if (pickoptions.multiple)
									idtarget
											.val((idtarget.val()
													+ (idtarget.val()
															? separator
															: '') + id)
													.split(separator).unique()
													.join(separator));
								else
									idtarget.val(id);
								var form = idtarget.closest('form');
								if (!form.hasClass('nodirty'))
									form.addClass('dirty');
							} else
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