(function($) {
	$.fn.datagridTable = function() {
		$('tr input:last', this).keydown(function(event) {
					if (event.keyCode && event.keyCode == 13) {
						if (event.preventDefault) {
							event.preventDefault();
							addRow(event);
						}
					}
				});
		$('tr input:first', this).keydown(function(event) {
			if (event.keyCode && (event.keyCode == 8 && !$(event.target).val())) {
				if (event.preventDefault) {
					event.preventDefault();
					removeRow(event);
				}
			}
		});
		$('button.add', this).click(addRow);
		$('button.remove', this).click(removeRow);
		return this;
	};

	var addRow = function(event) {
		var event = event || window.event;
		var row = $(event.srcElement || event.target).closest('tr');
		var r = row.clone(true);
		$('*', r).removeAttr('id');
		$('span.info', r).html('');
		row.after(r);
		$(':input', r).val('').removeAttr('keyupValidate');
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
			$(':input', this).each(function() {
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