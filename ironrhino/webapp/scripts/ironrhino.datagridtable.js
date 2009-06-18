( function($) {
	$.fn.datagridTable = function() {
		$('button.add_row', this).each( function() {
			this.onclick = DataGridTable.addRow
		});
		$('button.delete_row', this).each( function() {
			this.onclick = DataGridTable.deleteRow
		});
		$('button.reset', this).each( function() {
			this.onclick = function() {
				DataGridTable.clear(table);
				return true
			}
		});
	};
	var DataGridTable = {
		addRow : function(event) {
			event = event || window.event;
			row = $(event.srcElement || event.target).closest('tr').get(0);
			table = $(row).closest('table').get(0);
			cells = table.tHead.rows[0].cells;
			html = '<tr>';
			for ( var i = 0; i < cells.length; i++) {
				if (i != cells.length - 1) {
					if ($(cells[i]).attr('name'))
						html += '<td><input type="text"/></td>';
					else
						html += '<td></td>';
				} else {
					html += '<td><button type="button" class="delete_row">delete</button><button type="button" class="add_row">add</button></td>';
				}
			}
			html += '</tr>';
			if (row.parentNode.tagName.toLowerCase() == 'tfoot')
				$(html).appendTo($(table.tBodies[0]));
			else
				$(row).after($(html));
			$('button.add_row', table).each( function() {
				this.onclick = DataGridTable.addRow
			});
			$('button.delete_row', table).each( function() {
				this.onclick = DataGridTable.deleteRow
			});
			DataGridTable.setRows(table);
		},
		deleteRow : function(event) {
			event = event || window.event;
			row = $(event.srcElement || event.target).closest('tr').get(0);
			table = $(row).closest('table').get(0);
			$(row).remove();
			DataGridTable.setRows(table);
		},
		setRows : function(table) {
			cells = table.tHead.rows[0].cells;
			array = [];
			for ( var i = 0; i < cells.length; i++)
				if ($(cells[i]).attr('name'))
					array.push($(cells[i]).attr('name'));
			rows = table.tBodies[0].rows;
			for ( var i = 0; i < rows.length; i++) {
				var inputs = $('input[type=text]', rows[i]);
				for ( var j = 0; j < array.length; j++)
					inputs.get(j).name = array[j].replace('#index', i);
			}
		},
		clear : function(table) {
			$('input[type=text]', table).each( function() {
				this.name = ''
			});
		}
	};
})(jQuery);

Observation.datagridTable = function(container) {
	$('table.datagrid', container).datagridTable();
};