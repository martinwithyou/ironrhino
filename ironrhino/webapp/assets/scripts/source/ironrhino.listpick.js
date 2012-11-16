(function($) {
	var current;
	function find(expr) {
		var i = expr.indexOf('@');
		if (i == 0)
			return current;
		else if (i > 0)
			expr = expr.substring(0, i);
		return (expr == 'this') ? current : $(expr);
	}
	function val(expr, val) {// expr #id #id@attr .class@attr @attr
		if (!expr)
			return;
		if (arguments.length > 1) {
			var i = expr.indexOf('@');
			if (i < 0) {
				var ele = expr == 'this' ? current : $(expr);
				if (ele.is(':input')) {
					ele.val(val);
					Form.validate(ele);
				} else
					ele.text(val);
			} else if (i == 0) {
				current.attr(expr.substring(i + 1), val);
			} else {
				var selector = expr.substring(0, i);
				var ele = selector == 'this' ? current : $(selector);
				ele.attr(expr.substring(i + 1), val);
			}
		} else {
			var i = expr.indexOf('@');
			if (i < 0) {
				var ele = expr == 'this' ? current : $(expr);
				if (ele.is(':input'))
					return ele.val();
				else
					return ele.contents().filter(function() {
								return this.nodeType == Node.TEXT_NODE;
							}).text();
			} else if (i == 0) {
				return current.attr(expr.substring(i + 1));
			} else {
				var selector = expr.substring(0, i);
				var ele = selector == 'this' ? current : $(selector);
				return ele.attr(expr.substring(i + 1));
			}
		}
	}
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
				nametarget = find(pickoptions.name);
				var remove = nametarget.children('a.remove');
				if (remove.length)
					remove.click(function(event) {
								val(pickoptions.name, nametarget.is(':input')
												? ''
												: MessageBundle.get('select'));
								val(pickoptions.id, '');
								$(this).remove();
								event.stopPropagation();
								return false;
							});
			}
			var func = function(event) {
				current = $(event.target).closest('.listpick');;
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
										val(pickoptions.name, name);
										var nametarget = find(pickoptions.name);
										if (nametarget.is(':input')) {
											var form = nametarget
													.closest('form');
											if (!form.hasClass('nodirty'))
												form.addClass('dirty');
										} else {
											$('<a class="remove" href="#">&times;</a>')
													.appendTo(nametarget)
													.click(function(event) {
														val(
																pickoptions.name,
																nametarget
																		.is(':input')
																		? ''
																		: MessageBundle
																				.get('select'));
														val(pickoptions.id, '');
														$(this).remove();
														event.stopPropagation();
														return false;
													});
										}
									}
									if (pickoptions.id) {
										val(pickoptions.id, id);
										var idtarget = find(pickoptions.id);
										if (idtarget.is(':input')) {
											var form = idtarget.closest('form');
											if (!form.hasClass('nodirty'))
												form.addClass('dirty');
										}
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
								var nametarget = find(pickoptions.name);
								var name = names.join(separator);
								if (nametarget.is(':input')) {
									var _names = val(pickoptions.name) || '';
									val(
											pickoptions.name,
											ArrayUtils
													.unique((_names
															+ (_names
																	? separator
																	: '') + name)
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
									nametarget.data('picked', picked);
									val(pickoptions.name, picked);
									$('<a class="remove" href="#">&times;</a>')
											.appendTo(nametarget).click(
													function(event) {
														val(
																pickoptions.name,
																nametarget
																		.is(':input')
																		? ''
																		: MessageBundle
																				.get('pick'));
														val(pickoptions.id, '');
														nametarget.data(
																'picked', null);
														$(this).remove();
														event.stopPropagation();
														return false;
													});
								}
							}
							if (pickoptions.id) {
								var idtarget = find(pickoptions.id);
								var id = ids.join(separator);
								var _ids = val(pickoptions.id) || '';
								val(pickoptions.id, ArrayUtils.unique((_ids
												+ (_ids ? separator : '') + id)
												.split(separator))
												.join(separator));
								if (idtarget.is(':input')) {
									var form = idtarget.closest('form');
									if (!form.hasClass('nodirty'))
										form.addClass('dirty');
								}
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
							func(event);
							return false;
						}
					});
		});
		return this;
	};

})(jQuery);

Observation.listpick = function(container) {
	$('.listpick', container).listpick();
};