(function($) {
	$.fn.patterninput = function() {
		return this
				.each(function() {
					var t = $(this);
					var options = $.extend({
						minCoords : 3,
						maxCoords : 20
					}, (new Function("return "
							+ ($(this).data('options') || '{}')))());
					t
							.wrap('<div class="input-append"/>')
							.parent()
							.append(
									'<span class="add-on" style="cursor:pointer;"><i class="glyphicon glyphicon-lock"></i></span>');
					t
							.next('.add-on')
							.click(
									function() {
										$('#pattern-modal').remove();
										var modal = $(
												'<div id="pattern-modal" class="modal" style="z-index:10000;"><div style="padding: 5px 5px 0 0;"><button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button></div><div  id="pattern-modal-body" class="modal-body" style="max-height:600px;"></div></div>')
												.appendTo(document.body).find(
														'button.close').click(
														function() {
															$('#pattern-modal')
																	.remove();
														});
										$('#pattern-modal-body')
												.pattern(
														{
															oncomplete : function(
																	coords) {
																if (coords.length >= options.minCoords
																		&& coords.length <= options.maxCoords) {
																	$(
																			'#pattern-modal')
																			.remove();
																	t
																			.val(JSON
																					.stringify(coords));
																	Form
																			.validate(t);
																	if (t
																			.hasClass('submit'))
																		t
																				.closest(
																						'form')
																				.submit();
																} else
																	Message
																			.showFieldError(
																					t,
																					MessageBundle
																							.get(
																									'pattern.coords.invalid',
																									options.minCoords,
																									options.maxCoords));
															}
														});
									});
				});
	}
})(jQuery);

Observation._patterninput = function(container) {
	$('input.input-pattern', container).patterninput();
};
