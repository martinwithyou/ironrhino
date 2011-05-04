(function($) {
	$.fn.marquee = function() {
		this.each(function() {
					var marquee = $(this);
					marquee.mouseenter(function() {
								$(this).addClass('stop');
							}).mouseleave(function() {
								$(this).removeClass('stop');
							});
					setInterval(function() {
								if (!marquee.hasClass('stop'))
									$(
											$(':first-child', marquee)
													.prop('tagName')
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