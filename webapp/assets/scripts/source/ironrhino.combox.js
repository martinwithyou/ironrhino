(function($) {
	$.fn.combox = function() {
		this.keydown(function(event) {
					combox(this)
				});
		return this;
	};
	function combox(ele) {
		var name = $(ele).attr('name');
		var value = $(ele).val();
		if ($(ele).attr('tagName') == 'SELECT') {
			var input = $(ele.nextSibling);
			if (name == input.attr('name')) {
				input.attr('disabled', false);
				input.val(value);
				input.show();
			} else {
				var width = $(ele).width();
				var input = $('<input name="' + name + '"/>');
				input.width(width);
				input.blur(function() {
							combox(this)
						});
				$(ele).after(input);
			}
			input.focus();
		} else {
			var select = $(ele.previousSibling);
			var options = $('option', select);
			var has = false;
			for (var i = 0; i < options.length; i++) {
				$(options[i]).attr('selected', false);
				if ($(options[i]).val() == value) {
					has = true;
					$(options[i]).attr('selected', true);
				}
			}
			if (!has)
				select.append('<option value="' + value
						+ '" selected="selected">' + value + '</option>');
			select.show();
			select.attr('disabled', false);
			select.focus();
		}
		$(ele).hide();
		$(ele).attr('disabled', true);
	}
})(jQuery);

Observation.combox = function(container) {
	$('select.combox', container).combox();
};