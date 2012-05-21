(function($) {
	$.fn.multiautocomplete = function(options) {
		if (!options)
			options = {};
		$(this).each(function() {
			var t = $(this);
			var source = options.source;
			if (!source)
				source = t.data('source');
			if (source.indexOf('[') > 0)
				source = $.parseJSON(source);
			var local = source.constructor == Array;
			var delimiter = options.delimiter;
			if (!delimiter)
				delimiter = t.data('delimiter') || ',';
			t.autocomplete({
						source : local ? source : function(request, response) {
							$.ajax({
										global : false,
										url : source,
										dataType : 'json',
										data : {
											term : extractLast(request.term,
													delimiter)
										},
										success : response
									});
						},
						search : function() {
							if (local)
								return true;
							var term = extractLast(this.value, delimiter);
							if (term.length < 1) {
								return false;
							}
						},
						focus : function() {
							return false;
						},
						select : options.select ? options.select : function(
								event, ui) {
							var terms = split(this.value, delimiter);
							terms.pop();
							terms.push(ui.item.value);
							terms.push('');
							this.value = terms.join(delimiter);
							return false;
						}
					});
		});

		return this;
	};
	function split(val, delimiter) {
		return val.split(new RegExp(delimiter + '\s*', 'gi'));
	}
	function extractLast(term, delimiter) {
		return split(term, delimiter).pop();
	}
})(jQuery);

Observation.multiautocomplete = function(container) {
	$(':input.multiautocomplete', container).multiautocomplete();
};