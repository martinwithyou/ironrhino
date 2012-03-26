(function($) {
	var current;
	$.fn.treeselect = function() {
		$(this).each(function() {
			current = $(this);
			var treeoptions = {
				separator : '',
				full : true,
				cache : true
			}
			$.extend(treeoptions, (new Function("return "
							+ (current.attr('treeoptions') || '{}')))());
			var nametarget = null;
			if (treeoptions.name) {
				nametarget = $('#' + treeoptions.name);
				var close = nametarget.children('a.close');
				if (close.length)
					close.click(function(event) {
								nametarget.text(MessageBundle.get('select'));
								$('#' + treeoptions.id).val('');
								$(this).remove();
								event.stopPropagation();
							});
			}
			var func = function() {

				if (!treeoptions.cache)
					$('#_tree_window').remove();
				if (!$('#_tree_window').length) {
					$('<div id="_tree_window" title="'
							+ MessageBundle.get('select')
							+ '"><div id="_tree_"></div></div>')
							.appendTo(document.body);
					$('#_tree_window').dialog({
								width : 500,
								minHeight : 500
							});
					if (nametarget)
						treeoptions.value = nametarget.is(':input')
								? nametarget.val()
								: nametarget.text();
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
							func();
							return false;
						}
					});
		});
		return this;
	};

	function doclick(treenode, treeoptions) {
		if (treeoptions.name) {
			var nametarget = $('#' + treeoptions.name);
			var name = treeoptions.full || false
					? treenode.fullname
					: treenode.name;
			if (nametarget.is(':input')) {
				nametarget.val(name);
				var form = nametarget.closest('form');
				if (!form.hasClass('nodirty'))
					form.addClass('dirty');
			} else {
				nametarget.text(name);
				$('<a class="close">x</a>').appendTo(nametarget).click(
						function(event) {
							nametarget.text(MessageBundle.get('select'));
							$('#' + treeoptions.id).val('');
							$(this).remove();
							event.stopPropagation();
						});
			}
		}
		if (treeoptions.id) {
			var idtarget = $('#' + treeoptions.id);
			var id = treenode.id;
			if (idtarget.is(':input')) {
				idtarget.val(id);
				var form = idtarget.closest('form');
				if (!form.hasClass('nodirty'))
					form.addClass('dirty');
			} else
				idtarget.text(id);
		}
		$('#_tree_window').dialog('close');
		if (treeoptions.select)
			treeoptions.select(treenode);
	}

})(jQuery);

Observation.treeselect = function(container) {
	$('.treeselect', container).attr('tabindex', '0').treeselect();
};