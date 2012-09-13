(function($) {
	$.fn.filterselect = function() {
		this.each(function() {
					var select = $(this).nextAll('select:eq(0)');
					select.data('innerHTML', select.html());
					$(this).keyup($.debounce(500, filterselect));
				});
		return this;
	};
	function filterselect(event) {
		var input = $(event.target);
		var select = input.nextAll('select:eq(0)');
		var key = input.val();
		if ($.browser.webkit || event.keyCode == 8) {
			select.html(select.data('innerHTML'));
			if (!key)
				return;
		}
		var keys = key.split(/\s+/);
		for (var i = 0; i < keys.length; i++)
			$('option', select).each(function() {
						if (!$(this).text().match(keys[i]))
							$(this).remove();
					});
	}
})(jQuery);

Observation.filterselect = function(container) {
	$('input.filterselect', container).filterselect();
};