(function($) {
	var current;
	$.fn.selectregion = function() {
		$(this).css('cursor', 'pointer').click(function() {
			current = $(this);
			var _click = function() {
				var full = current.attr('full');
				var regionname = $('#' + current.attr('regionname'));
				var regionid = $('#' + current.attr('regionid'));
				if (regionname.length) {
					var name = $(this).text();
					if (full) {
						var p = this.parentNode.parentNode.parentNode.parentNode;
						while (p && p.tagName == 'LI') {
							name = $('a span', p).get(0).innerHTML + name;
							p = p.parentNode.parentNode;
						}
					}
					if (regionname.attr('tagName') == 'INPUT')
						regionname.val(name);
					else
						regionname.text(name);
				}
				if (regionid.length)
					regionid.val($(this).closest('li').attr('id'));
				$("#region_window").dialog('close');
			};
			if (!$('#region_window').length) {
				$('<div id="region_window" title="'
						+ MessageBundle.get('select')
						+ '"><div id="region_tree"></div></div>')
						.appendTo(document.body);
				$("#region_window").dialog({
							width : 500,
							minHeight : 500
						});
				$("#region_tree").treeview({
							url : CONTEXT_PATH + '/region/children',
							click : _click,
							collapsed : true,
							placeholder : MessageBundle.get('ajax.loading'),
							unique : true
						});
			} else {
				$("#region_window").dialog('open');
			}

		});
		return this;
	};

})(jQuery);

Observation.selectregion = function(container) {
	$('.selectregion', container).selectregion();
};