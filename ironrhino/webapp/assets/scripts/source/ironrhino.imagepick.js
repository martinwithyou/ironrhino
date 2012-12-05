(function($) {
	$.fn.imagepick = function() {
		this
				.addClass('poped')
				.wrap('<div class="input-append"/>')
				.parent()
				.append('<span class="add-on listpick" style="cursor:pointer;" data-options="{\'url\':\''
						+ CONTEXT_PATH
						+ '/common/upload/pick\',\'id\':\'#'
						+ this.attr('id')
						+ '\',\'width\':400}"><i class="icon-th-list"></i></span>');
		if (this.val())
			$(this).attr('data-content', '<img src="' + this.val() + '"/>');
		this.change(function() {
			if (this.value)
				$(this).attr('data-content', '<img src="' + this.value + '"/>');
		});
		return this;
	}
})(jQuery);

Observation.imagepick = function(container) {
	$('input.imagepick', container).imagepick();
};
