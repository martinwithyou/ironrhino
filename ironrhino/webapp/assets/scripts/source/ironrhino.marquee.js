(function($) {
	$.fn.marquee = function() {
		this.each(function() {
					var marquee = $(this);
					marquee.mouseenter(function() {
								$(this).attr('stop', 'true');
							}).mouseleave(function() {
								$(this).removeAttr('stop');
							});
					setInterval(function() {
								if (!marquee.attr('stop'))
									$(
											$(':first-child', marquee)
													.attr('tagName')
													+ ':eq(0)', marquee)
											.fadeOut('slow', function() {
												$(this).appendTo(marquee)
														.fadeIn('slow');
											});
							}, marquee.attr('delay') || 3000);
				});
		return this;
	};
})(jQuery);

Observation.marquee = function(container) {
	$('.marquee', container).marquee();
};