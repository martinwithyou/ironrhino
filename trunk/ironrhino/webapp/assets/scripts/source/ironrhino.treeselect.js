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
				} else {
					ele.text(val);
					if (ele.parents('.richtable').length
							&& ele.prop('tagName') == 'TD')
						ele.addClass('edited');
				}
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
	$.fn.treeselect = function() {
		$(this).each(function() {
			current = $(this);
			var options = {
				idproperty : 'id',
				separator : '',
				full : true,
				cache : true
			}
			$.extend(options, (new Function("return "
							+ (current.data('options') || '{}')))());
			current.data('_options', options);
			var nametarget = null;
			if (options.name) {
				nametarget = find(options.name);
				var remove = nametarget.children('a.remove');
				if (remove.length) {
					remove.click(function(event) {
								val(options.name, nametarget.is(':input,td')
												? ''
												: MessageBundle.get('select'));
								val(options.id, '');
								$(this).remove();
								event.stopPropagation();
								return false;
							});
				} else {
					var text = val(options.name);
					if (text && text != '...') {
						$('<a class="remove" href="#">&times;</a>')
								.appendTo(nametarget).click(function(event) {
									current = $(event.target)
											.closest('.treeselect');
									val(options.name, nametarget
													.is(':input,td')
													? ''
													: MessageBundle
															.get('select'));
									val(options.id, '');
									$(this).remove();
									event.stopPropagation();
									return false;
								});
					}
				}
			}
			var func = function(event) {
				current = $(event.target).closest('.treeselect');
				if (!options.cache)
					$('#_tree_window').remove();
				if (!$('#_tree_window').length) {
					$('<div id="_tree_window" title="'
							+ MessageBundle.get('select')
							+ '"><div id="_tree_"></div></div>')
							.appendTo(document.body);
					$('#_tree_window').dialog({
								width : current.data('_options').width || 500,
								minHeight : current.data('_options').minHeight
										|| 500
							});
					if (nametarget && nametarget.length)
						options.value = val(options.name) || '';
					if (options.type != 'treeview') {
						options.click = function(treenode) {
							doclick(treenode, options);
						};
						$('#_tree_').treearea(options);
					} else {
						var treeviewoptions = {
							url : options.url,
							click : function() {
								var treenode = $(this).closest('li')
										.data('treenode');
								doclick(treenode, options);
							},
							collapsed : true,
							placeholder : MessageBundle.get('ajax.loading'),
							unique : true,
							separator : options.separator,
							value : options.value,
							root : options.root
						};
						if (!options.cache)
							treeviewoptions.url = treeviewoptions.url + '?r='
									+ Math.random();
						$('#_tree_').treeview(treeviewoptions);
					}
				} else {
					$('#_tree_window').dialog('open');
				}

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

	function doclick(treenode, options) {
		if (options.name) {
			var nametarget = find(options.name);
			var name = options.full || false
					? treenode.fullname
					: treenode.name;
			val(options.name, name);
			if (nametarget.is(':input')) {
				nametarget.trigger('change');
				var form = nametarget.closest('form');
				if (!form.hasClass('nodirty'))
					form.addClass('dirty');
			} else {
				$('<a class="remove" href="#">&times;</a>')
						.appendTo(nametarget).click(function(event) {
							val(options.name, nametarget.is(':input,td')
											? ''
											: MessageBundle.get('select'));
							val(options.id, '');
							$(this).remove();
							event.stopPropagation();
							return false;
						});
			}
		}
		if (options.id) {
			var idtarget = find(options.id);
			var id = treenode[options.idproperty];
			val(options.id, id);
			if (idtarget.is(':input')) {
				idtarget.trigger('change');
				var form = idtarget.closest('form');
				if (!form.hasClass('nodirty'))
					form.addClass('dirty');
			}
		}
		$('#_tree_window').dialog('close');
		if (options.select)
			options.select(treenode);
	}

})(jQuery);

Observation.treeselect = function(container) {
	$('.treeselect', container).treeselect();
};