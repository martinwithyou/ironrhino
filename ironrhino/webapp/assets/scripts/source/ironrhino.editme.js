(function($) {
	$.fn.editme = function() {
		return $(this).attr('contenteditable', 'true').keyup(function() {
					$(this).attr('edited', 'true')
				}).blur(blur);
	};
	function blur() {
		var t = $(this);
		var url = t.attr('url') || document.location.href;
		var name = t.attr('name') || 'content';
		var data = {};
		data[name] = t.html();
		if (t.attr('edited'))
			$.alerts.confirm(MessageBundle.get('confirm.save'), MessageBundle
							.get('select'), function(b) {
						if (b) {
							ajax({
										url : url,
										type : 'POST',
										data : data,
										global : false,
										success : function() {
											t.removeAttr('edited');
										}
									});
						}
					});

	}
})(jQuery);

Observation.editme = function(container) {
	$('.editme', container).editme();
};