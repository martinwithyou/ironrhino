(function($) {
	var current;
	$.fn.treeselect = function() {
		$(this).css('cursor', 'pointer').click(function() {
			current = $(this);
			var treeoptions = {
				full : true,
				cache : true
			}
			$.extend(treeoptions, (new Function("return "
							+ ($(current).attr('treeoptions') || '{}')))());
			var _click = function() {
				if (treeoptions.name) {
					var nametarget = $('#' + treeoptions.name);
					var full = treeoptions.full || false;
					var name = $(this).text();
					if (full) {
						var p = this.parentNode.parentNode.parentNode.parentNode;
						while (p && p.tagName == 'LI') {
							name = $('a span', p).get(0).innerHTML + name;
							p = p.parentNode.parentNode;
						}
					}
					if (nametarget.is(':input'))
						nametarget.val(name);
					else {
						nametarget.text(name).after('<a>x</a>').next().css({
									'cursor' : 'pointer',
									'color' : '#black',
									'margin-left' : '5px',
									'padding' : '0 5px',
									'border' : 'solid 1px #FFC000'
								}).click(function() {
									nametarget
											.text(MessageBundle.get('select'));
									$('#' + treeoptions.id).val('');
									$(this).remove();
								});
					}
				}
				if (treeoptions.id) {
					var idtarget = $('#' + treeoptions.id);
					var id = $(this).closest('li').attr('id');
					if (idtarget.is(':input'))
						idtarget.val(id);
					else
						idtarget.text(id);
				}
				$("#_tree_window").dialog('close');
			};
			var options = {
				url : treeoptions.url,
				click : _click,
				collapsed : true,
				placeholder : MessageBundle.get('ajax.loading'),
				unique : true
			};
			if (!treeoptions.cache) {
				options.url = options.url + '?r=' + Math.random();
				$('#_tree_window').remove();
			}
			if (!$('#_tree_window').length) {
				$('<div id="_tree_window" title="'
						+ MessageBundle.get('select')
						+ '"><div id="_tree_"></div></div>')
						.appendTo(document.body);
				$('#_tree_window').dialog({
							width : 500,
							minHeight : 500
						});
				if (treeoptions.type != 'treeview') {
					if (treeoptions.name) {
						var nametarget = $('#' + treeoptions.name);
						treeoptions.value = nametarget.is(':input')
								? nametarget.val()
								: nametarget.text();
					}
					treeoptions.click = function(treenode) {
						if (treeoptions.name) {
							var nametarget = $('#' + treeoptions.name);
							var name = treeoptions.full || false
									? treenode.fullname
									: treenode.name;
							if (nametarget.is(':input'))
								nametarget.val(name);
							else {
								nametarget.text(name).after('<a>x</a>').next()
										.css({
													'cursor' : 'pointer',
													'color' : '#black',
													'margin-left' : '5px',
													'padding' : '0 5px',
													'border' : 'solid 1px #FFC000'
												}).click(function() {
											nametarget.text(MessageBundle
													.get('select'));
											$('#' + treeoptions.id).val('');
											$(this).remove();
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
						$("#_tree_window").dialog('close');
					};
					$('#_tree_').treearea(treeoptions);
				} else
					$('#_tree_').treeview(options);
			} else {
				$('#_tree_window').dialog('open');
			}

		});
		return this;
	};

})(jQuery);

Observation.treeselect = function(container) {
	$('.treeselect', container).treeselect();
};