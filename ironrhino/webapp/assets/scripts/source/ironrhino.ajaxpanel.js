(function($) {
	$.fn.ajaxpanel = function() {
		$(this).each(function() {
					var t = $(this);
					if (!t.html())
						t.html(MessageBundle.get('ajax.loading'));
					t.bind('load', function() {
								ajaxpanel(t)
							});
					if (t.attr('timeout')) {
						setTimeout(function() {
									ajaxpanel(t);
								}, parseInt(t.attr('timeout')));
					} else if (t.attr('interval')) {
						setInterval(function() {
									ajaxpanel(t);
								}, parseInt(t.attr('interval')));
					} else if (!t.attr('manual'))
						ajaxpanel(t);
				});
		return this;
	};
	function ajaxpanel(ele) {
		var options = {
			url : ele.attr('url') || document.location.href,
			global : false,
			quiet : true,
			success : function(data) {
				if (typeof data != 'string') {
					ele.empty();
					$('#' + ele.attr('tmpl')).render(data).appendTo(ele);
					_observe(ele);
				}
			}
		};
		if (ele.attr('url'))
			options.replacement = ele.attr('id') + ':'
					+ (ele.attr('replacement') || 'content');
		else
			options.replacement = ele.attr('id');
		ajax(options);
	}
})(jQuery);

Observation.ajaxpanel = function(container) {
	$('.ajaxpanel', container).ajaxpanel();
};