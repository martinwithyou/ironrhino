// extend the plugin
(function($) {

	// define the new for the plugin ans how to call it
	$.fn.truncatable = function(options) {
		// set default options
		var defaults = {
			limit : 100
		};

		// call in the default otions
		var options = $.extend(defaults, options);

		// act upon the element that is passed into the design
		return this.each(function(i) {
					// check length of text to what out maximum is
					if ($(this).text().length > defaults.limit) {
						var splitText = $(this).html().substr(defaults.limit);
						var hiddenText = '<span class="hiddenText_' + i
								+ '" style="display:none">' + splitText
								+ '</span>'
						$(this).html($(this).text().substr(0, defaults.limit))
								.append('<a class="more_' + i
										+ '" href="#">...<a/>' + hiddenText)
								.bind('click', function() {
											$('.hiddenText_' + i).show();
											$('.more_' + i).hide();
											return false;
										});
					}
				});
	};
	// end the plugin call
})(jQuery);
