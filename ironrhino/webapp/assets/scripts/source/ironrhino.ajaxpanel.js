(function($) {
	$.fn.ajaxpanel = function() {
		$(this).each(function() {
					var t = $(this);
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
			beforeSend : function() {
				if (typeof $.fn.mask != 'undefined')
					ele.mask(MessageBundle.get('ajax.loading'));
				else
					ele.html('<div style="text-align:center;">'
							+ MessageBundle.get('ajax.loading') + '</div>');
			},
			complete : function() {
				if (typeof $.fn.unmask != 'undefined')
					ele.unmask();
			},
			success : function(data) {
				if (typeof data != 'string') {
					ele.empty();
					$.tmpl($('#' + ele.attr('tmpl')), data).appendTo(ele);
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
	$('.ajaxpanel .load', container).click(function() {
				$(this).closest('.ajaxpanel').trigger('load');
			});
};