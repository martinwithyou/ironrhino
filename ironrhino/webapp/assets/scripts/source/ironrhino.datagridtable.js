(function($) {
	$.fn.datagridTable = function() {
		$('button.add', this).click(addRow);
		$('button.remove', this).click(removeRow);
	};

	var addRow = function(event) {
		var event = event || window.event;
		var row = $(event.srcElement || event.target).closest('tr');
		var r = row.clone(true);
		$('*', r).removeAttr('id');
		$('span.info', r).html('');
		row.after(r);
		$(':input', r).val('');
		$(':input', r).eq(0).focus();
		rename();
	};
	var removeRow = function(event) {
		var event = event || window.event;
		var row = $(event.srcElement || event.target).closest('tr');
		if ($('tr', row.parent()).length > 1) {
			row.remove();
			rename(row.closest('tbody'));
		}
	};
	var rename = function(tbody) {
		$('tr', tbody).each(function(i) {
			$('input', this).each(function() {
				var name = $(this).attr('name');
				var j = name.indexOf('[');
				if (j < 0)
					return;
				name = name.substring(0, j + 1) + i
						+ name.substring(name.indexOf(']'));
				$(this).attr('name', name);
			});
		});
	}
})(jQuery);

Observation.datagridTable = function(container) {
	$('table.datagrid', container).datagridTable();
};