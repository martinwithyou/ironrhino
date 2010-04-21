(function($) {
	$.fn.ajaxpanel = function() {
		$(this).each(function() {
					var t = $(this);
					if(!t.html())
						t.html(MessageBundle.get('ajax.loading'));
					if (t.attr('lazy'))
						t.bind('load', function() {
									ajaxpanel(t)
								});
					else
						ajaxpanel(t);
				});
		return this;
	};
	function ajaxpanel(ele) {
		var options = {
			url : ele.attr('url'),
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
		var r = ele.attr('id') + ':' + (ele.attr('replacement') || 'content');
		options.replacement = r;
		ajax(options);
	}
})(jQuery);

Observation.ajaxpanel = function(container) {
	$('.ajaxpanel', container).ajaxpanel();
};