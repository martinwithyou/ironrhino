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
								$('#' + treeoptions.id).val('');
								$(this).remove();
								event.stopPropagation();
							});
			}
			current.css('cursor', 'pointer').click(function() {
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
			if (nametarget.is(':input'))
				nametarget.val(name);
			else {
				nametarget.text(name);
				if (!nametarget.next('a.close').length)
					nametarget.after('<a class="close">x</a>').next().css({
								'cursor' : 'pointer',
								'color' : '#black',
								'margin-left' : '5px',
								'padding' : '0 5px',
								'border' : 'solid 1px #FFC000'
							}).click(function(event) {
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
			if (idtarget.is(':input'))
				idtarget.val(id);
			else
				idtarget.text(id);
		}
		$('#_tree_window').dialog('close');
		if (treeoptions.select)
			treeoptions.select(treenode);
	}

})(jQuery);

Observation.treeselect = function(container) {
	$('.treeselect', container).treeselect();
};