(function($) {
	$.fn.datagridTable = function(options) {
		var onadd = options ? options.onadd : null;
		var onremove = options ? options.onremove : null;
		var onmoveup = options ? options.onmoveup : null;
		var onmovedown = options ? options.onmovedown : null;
		$(this).each(function() {
					if ($(this).hasClass('datagrided'))
						return;
					$(this).addClass('datagrided');
					if ($(this).parents('.datagrided').length)
						return;
					$('tbody tr input:last', this).keydown(function(event) {
								if (event.keyCode == 13) {
									event.preventDefault();
									addRow(event, onadd);
								}
							});
					$('tbody tr input:first', this).keydown(function(event) {
								if (event.keyCode == 8
										&& !$(event.target).val()) {
									event.preventDefault();
									removeRow(event, onremove);
								}
							});
					$('.add', this).click(function(event) {
								addRow(event, onadd)
							});
					$('.remove', this).click(function(event) {
								removeRow(event, onremove)
							});
					$('.moveup', this).click(function(event) {
								moveupRow(event, onmoveup)
							});
					$('.movedown', this).click(function(event) {
								movedownRow(event, onmovedown)
							});
				})

		return this;
	};

	var addRow = function(event, onadd) {
		var row = $(event.target).closest('tr');
		var r = row.clone(true);
		$('*', r).removeAttr('id');
		$('span.info', r).html('');
		$(':input', r).val('').removeAttr('keyupValidate');
		$('input.filterselect', this).next('select').html(function() {
					return $(this).data('innerHTML')
				});
		$('.datagrided tr', r).each(function(i) {
					if (i > 0)
						$(this).remove();
				});
		row.after(r);
		$(':input', r).eq(0).focus();
		rename(row.closest('tbody'));
		if (onadd)
			onadd.apply(r.get(0));
	};
	var removeRow = function(event, onremove) {
		var row = $(event.target).closest('tr');
		if ($('tr', row.parent()).length > 1) {
			$(':input', row.prev()).eq(0).focus();
			var ret;
			if (onremove)
				ret = onremove.apply(row.get(0));
			if (ret !== false) {
				row.remove();
				rename(row.closest('tbody'));
			}
		}
	};
	var moveupRow = function(event, onmoveup) {
		var row = $(event.target).closest('tr');
		if ($('tr', row.parent()).length > 1) {
			if ($(row).prev().length)
				$(row).insertBefore($(row).prev());
			else
				$(row).insertAfter($(row).siblings(':last'));
			rename(row.closest('tbody'));
			if (onmoveup)
				onmoveup.apply(row.get(0));
		}
	};
	var movedownRow = function(event, onmovedown) {
		var row = $(event.target).closest('tr');
		if ($('tr', row.parent()).length > 1) {
			if ($(row).next().length)
				$(row).insertAfter($(row).next());
			else
				$(row).insertBefore($(row).siblings(':first'));
			rename(row.closest('tbody'));
			if (onmovedown)
				onmovedown.apply(row.get(0));
		}
	};
	var rename = function(tbody) {
		var level = $(tbody).parents('table.datagrided').length;
		$(tbody).children('tr').each(function(i) {
			$(':input', this).each(function() {
				var name = $(this).prop('name');
				var j = -1;
				for (var k = 0; k < level; k++)
					j = name.indexOf('[', j + 1);
				if (j < 0)
					return;
				name = name.substring(0, j + 1) + i
						+ name.substring(name.indexOf(']', j));
				$(this).prop('name', name);
			});
		});
	}
})(jQuery);

Observation.datagridTable = function(container) {
	$('table.datagrid', container).datagridTable();
};