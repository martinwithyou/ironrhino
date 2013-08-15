(function($) {
	var BLOCK_COMMENT = new RegExp('/\\*(?:.|[\\n\\r])*?\\*/', 'g');
	var LINE_COMMENT = new RegExp('\r?\n?\\s*--.*\r?(\n|$)', 'g');
	var PARAMETER = new RegExp('(:\\w*)(,|;|\\)|\\s|\\||\\+|$)', 'g');
	$.sqleditor = {
		extractParameters : function(sql) {
			sql = $.sqleditor.clearComments(sql);
			var params = [];
			var result;
			while ((result = PARAMETER.exec(sql))) {
				var param = result[1].substring(1);
				if ($.inArray(param, params) == -1)
					params.push(param);
			}
			return params;
		},
		clearComments : function(sql) {
			return $.trim(sql.replace(BLOCK_COMMENT, '').replace(LINE_COMMENT,
					'\n'));
		},
		highlight : function(sql) {
			return sql
					.replace(BLOCK_COMMENT, '<span class="comment">$&</span>')
					.replace(LINE_COMMENT, '<span class="comment">$&</span>')
					.replace(PARAMETER, '<strong>$&</strong>');
		}
	}
	function preview(input) {
		var t = $(input).hide();
		var p = t.next('div.preview');
		if (!p.length) {
			p = $('<div class="preview"></div>').insertAfter(t);
			if (!(t.prop('readonly') || t.prop('disabled')))
				p.click(function() {
							$(this).hide().prev('.sqleditor:input').show()
									.focus();
						});
		}
		p.width(t.width()).css('height', t.height() + 'px').html($.sqleditor
				.highlight(t.val())).show();

	}
	$.fn.sqleditor = function() {
		$(this).each(function() {
					var t = $(this);
					if (t.is(':input')) {
						preview(t);
						t.blur(function() {
									preview(t)
								});
					} else {
						t.addClass('preview').html($.sqleditor.highlight(t
								.text()));
					}
				});
		return this;
	};
})(jQuery);

Observation.sqleditor = function(container) {
	$('.sqleditor', container).sqleditor();
};