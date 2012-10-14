(function($) {
	$.fn.ajaxpanel = function() {
		$(this).each(function() {
					var t = $(this);
					t.bind('load', function() {
								ajaxpanel(t)
							});
					if (t.data('timeout')) {
						setTimeout(function() {
									ajaxpanel(t);
								}, parseInt(t.data('timeout')));
					} else if (t.data('interval')) {
						setInterval(function() {
									ajaxpanel(t);
								}, parseInt(t.data('interval')));
					} else if (!t.hasClass('manual'))
						ajaxpanel(t);
				});
		return this;
	};
	function ajaxpanel(ele) {
		var options = {
			url : ele.data('url') || document.location.href,
			global : false,
			quiet : true,
			beforeSend : function() {
				if (!ele.data('quiet'))
					if (typeof $.fn.mask != 'undefined')
						ele.mask(MessageBundle.get('ajax.loading'));
					else
						ele.html('<div style="text-align:center;">'
								+ MessageBundle.get('ajax.loading') + '</div>');
			},
			complete : function() {
				if (!ele.data('quiet') && typeof $.fn.unmask != 'undefined')
					ele.unmask();
			},
			success : function(data) {
				if (typeof data != 'string') {
					ele.empty();
					$.tmpl($('#' + ele.data('tmpl')), data).appendTo(ele);
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