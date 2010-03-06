(function($) {
	$.fn.filterselect = function() {
		$(this).keyup(filterselect);
	};
	function filterselect(event) {
		var input = $(event.target);
		var select = input.nextAll('select:eq(0)');
		if (!select.data('innerHTML'))
			select.data('innerHTML', select.html());
		var key = input.val();
		if ($.browser.webkit || event.keyCode == 8) {
			select.html(select.data('innerHTML'));
			if (!key)
				return;
		}
		$('option', select).each(function() {
					if (!$(this).text().match(key))
						$(this).remove();
				});
	}
})(jQuery);

Observation.combox = function(container) {
	$('input.filterselect', container).filterselect();
};