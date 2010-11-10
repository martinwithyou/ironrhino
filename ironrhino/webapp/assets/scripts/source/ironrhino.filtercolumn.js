(function($) {
	$.fn.filtercolumn = function() {
		$(this).each(function() {
			var cells = this.rows[0].cells;
			if (!$('td.filtercolumn', this).length) {
				var td = cells[cells.length - 1];
				$(td)
						.addClass('filtercolumn')
						.prepend('<ul class="filtercolumn"><li>...<ul class="listTarget"></ul></li></ul>');
			}
			var hideInList = [];
			var colsHidden = [];
			for (var i = 0; i < cells.length - 1; i++) {
				var td = $(cells[i]);
				if (td.hasClass('nofilter'))
					hideInList.push(i + 1);
				if (td.hasClass('filtered'))
					colsHidden.push(i + 1);
			}
			hideInList.push(cells.length);
			$(this).columnManager({
						listTarget : $('ul.listTarget', this),
						onClass : 'selecton',
						offClass : 'selectoff',
						hideInList : hideInList,
						colsHidden : colsHidden
					});
			$('ul.filtercolumn', this).clickMenu({
						onClick : function() {
						}
					});
		});
		return this;
	};
})(jQuery);

Observation.filtercolumn = function(container) {
	$('table.filtercolumn', container).filtercolumn();
};