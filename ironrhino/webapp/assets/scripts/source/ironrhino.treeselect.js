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
			var treeoptions = {
				idproperty : 'id',
				separator : '',
				full : true,
				cache : true
			}
			$.extend(treeoptions, (new Function("return "
							+ (current.data('options') || '{}')))());
			current.data('treeoptions', treeoptions);
			var nametarget = null;
			if (treeoptions.name) {
				nametarget = find(treeoptions.name);
				var remove = nametarget.children('a.remove');
				if (remove.length) {
					remove.click(function(event) {
								val(treeoptions.name, nametarget
												.is(':input,td')
												? ''
												: MessageBundle.get('select'));
								val(treeoptions.id, '');
								$(this).remove();
								event.stopPropagation();
								return false;
							});
				} else if (val(treeoptions.name)) {
					$('<a class="remove" href="#">&times;</a>')
							.appendTo(nametarget).click(function(event) {
								current = $(event.target).closest('.listpick');
								val(pickoptions.name, nametarget
												.is(':input,td')
												? ''
												: MessageBundle.get('pick'));
								val(pickoptions.id, '');
								$(this).remove();
								event.stopPropagation();
								return false;
							});
				}
			}
			var func = function(event) {
				current = $(event.target).closest('.treeselect');
				if (!treeoptions.cache)
					$('#_tree_window').remove();
				if (!$('#_tree_window').length) {
					$('<div id="_tree_window" title="'
							+ MessageBundle.get('select')
							+ '"><div id="_tree_"></div></div>')
							.appendTo(document.body);
					$('#_tree_window').dialog({
						width : current.data('treeoptions').width || 500,
						minHeight : current.data('treeoptions').minHeight
								|| 500
					});
					if (nametarget && nametarget.length)
						treeoptions.value = val(treeoptions.name) || '';
					if (treeoptions.type != 'treeview') {
						treeoptions.click = function(treenode) {
							doclick(treenode, treeoptions);
						};
						$('#_tree_').treearea(treeoptions);
					} else {
						var options = {
							url : treeoptions.url,
							click : function() {
								var treenode = $(this).closest('li')
										.data('treenode');
								doclick(treenode, treeoptions);
							},
							collapsed : true,
							placeholder : MessageBundle.get('ajax.loading'),
							unique : true,
							separator : treeoptions.separator,
							value : treeoptions.value,
							root : treeoptions.root
						};
						if (!treeoptions.cache)
							options.url = options.url + '?r=' + Math.random();
						$('#_tree_').treeview(options);
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

	function doclick(treenode, treeoptions) {
		if (treeoptions.name) {
			var nametarget = find(treeoptions.name);
			var name = treeoptions.full || false
					? treenode.fullname
					: treenode.name;
			val(treeoptions.name, name);
			if (nametarget.is(':input')) {
				var form = nametarget.closest('form');
				if (!form.hasClass('nodirty'))
					form.addClass('dirty');
			} else {
				$('<a class="remove" href="#">&times;</a>')
						.appendTo(nametarget).click(function(event) {
							val(treeoptions.name, nametarget.is(':input,td')
											? ''
											: MessageBundle.get('select'));
							val(treeoptions.id, '');
							$(this).remove();
							event.stopPropagation();
							return false;
						});
			}
		}
		if (treeoptions.id) {
			var idtarget = find(treeoptions.id);
			var id = treenode[treeoptions.idproperty];
			val(treeoptions.id, id);
			if (idtarget.is(':input')) {
				var form = idtarget.closest('form');
				if (!form.hasClass('nodirty'))
					form.addClass('dirty');
			}
		}
		$('#_tree_window').dialog('close');
		if (treeoptions.select)
			treeoptions.select(treenode);
	}

})(jQuery);

Observation.treeselect = function(container) {
	$('.treeselect', container).treeselect();
};