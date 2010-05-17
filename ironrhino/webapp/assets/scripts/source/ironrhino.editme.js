(function($) {
	$.fn.editme = function() {
		var t = $(this);
		t.bind('click', click).bind('blur', blur);
		if ('hover' == t.attr('trigger'))
			t.hover(click, blur);
		else
			t.hover(function() {
						$(this).addClass('editme_hover')
					}, function() {
						$(this).removeClass('editme_hover')
					});
		return this;
	};
	function click() {
		var t = $(this);
		t.unbind('click', click);
		var value = t.text();
		t.attr('_value', value);
		t.html('<input value="' + value + '"/>');
		$('input', this).width(t.width()).blur(function() {
					$(this).parent().trigger('blur')
				}).focus(function() {
					if ($.browser.mozilla) {
						return;
					} else if ($.browser.msie || $.browser.opera) {
						var rng = this.createTextRange();
						rng.text = this.value;
						rng.collapse(false);
					} else {
						// webkit
						try {
							this.select();
							window.getSelection().collapseToEnd();
						} catch (e) {
						}
					}
				}).focus();
	};
	function blur() {
		var oldvalue = $(this).attr('_value');
		var value = $('input', this).val();
		var t = $(this);
		var url = $(this).attr('url');
		var name = $(this).attr('name');
		if (!url || !name) {
			t.text(value);
		} else {
			$.ajax({
						url : url,
						type : 'POST',
						data : {
							name : value
						},
						global : false,
						success : function() {
							t.text(value);
						},
						error : function() {
							t.text(oldvalue);
						}
					});
		}
		t.bind('click', click);
	}
})(jQuery);

Observation.editme = function(container) {
	$('.editme', container).editme();
};