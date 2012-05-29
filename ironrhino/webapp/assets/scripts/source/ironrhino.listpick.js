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
							+ (current.data('options') || '{}')))());
			var nametarget = null;
			if (pickoptions.name) {
				nametarget = $('#' + pickoptions.name);
				var remove = nametarget.children('a.remove');
				if (remove.length)
					remove.click(function(event) {
								nametarget.text(MessageBundle.get('select'));
								$('#' + pickoptions.id).val('');
								$(this).remove();
								event.stopPropagation();
								return false;
							});
			}
			var func = function() {

				$('#_pick_window').remove();
				var win = $('<div id="_pick_window" title="'
						+ MessageBundle.get('select') + '"></div>')
						.appendTo(document.body).dialog({
									width : 800,
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
					if (!pickoptions.multiple) {
						$('tbody input[type=radio]', target).live('click',
								function() {
									var id = $(this).val();
									var name = $($(this).closest('tr')[0].cells[pickoptions.nameindex])
											.text();
									if (pickoptions.name) {
										var nametarget = $('#'
												+ pickoptions.name);
										if (nametarget.is(':input')) {
											nametarget.val(name);
											var form = nametarget
													.closest('form');
											if (!form.hasClass('nodirty'))
												form.addClass('dirty');
										} else {
											nametarget.text(name);
											$('<a class="remove" href="#">&times;</a>')
													.appendTo(nametarget)
													.click(function(event) {
														nametarget
																.text(MessageBundle
																		.get('select'));
														$('#' + pickoptions.id)
																.val('');
														$(this).remove();
														event.stopPropagation();
														return false;
													});
										}
									}
									if (pickoptions.id) {
										var idtarget = $('#' + pickoptions.id);
										if (idtarget.is(':input')) {
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

					} else {
						$('button.confirm', target).live('click', function() {
							var checkbox = $('tbody :checked', target);
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
									nametarget
											.val((nametarget.val() + ArrayUtils
													.unique((nametarget.val()
															? separator
															: '')
															+ name)
													.split(separator))
													.join(separator));
									var form = nametarget.closest('form');
									if (!form.hasClass('nodirty'))
										form.addClass('dirty');
								} else {
									var picked = nametarget.data('picked')
											|| '';
									picked = ArrayUtils.unique(((picked
											? picked + separator
											: '') + name).split(separator))
											.join(separator);
									nametarget.data('picked', picked)
											.text(picked);
									$('<a class="remove" href="#">&times;</a>')
											.appendTo(nametarget).click(
													function(event) {
														nametarget
																.text(MessageBundle
																		.get('select'))
																.data('picked',
																		'');
														$('#' + pickoptions.id)
																.val('');
														$(this).remove();
														event.stopPropagation();
														return false;
													});
								}
							}
							if (pickoptions.id) {
								var idtarget = $('#' + pickoptions.id);
								var id = ids.join(separator);
								if (idtarget.is(':input')) {
									idtarget.val(ArrayUtils
											.unique((idtarget.val()
													+ (idtarget.val()
															? separator
															: '') + id)
													.split(separator))
											.join(separator));
									var form = idtarget.closest('form');
									if (!form.hasClass('nodirty'))
										form.addClass('dirty');
								} else
									idtarget.text(id);
							}
							win.dialog('destroy');
							return false;
						});
					}
				};
				var url = pickoptions.url;
				if (url.indexOf('multiple') < 0 && pickoptions.multiple)
					url += (url.indexOf('?') > 0 ? '&' : '?') + 'multiple=true'
				ajax({
							url : url,
							cache : false,
							target : target,
							replacement : '_pick_window:content',
							quiet : true
						});

			};
			current.css('cursor', 'pointer').click(func).keydown(
					function(event) {
						if (event.keyCode == 13) {
							func();
							return false;
						}
					});
		});
		return this;
	};

})(jQuery);

Observation.listpick = function(container) {
	$('.listpick', container).attr('tabindex', '0').listpick();
};