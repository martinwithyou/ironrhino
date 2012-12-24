(function($) {

	function check(group) {
		var boxes = $('input[type=checkbox][name]', group);
		var allchecked = boxes.length > 0;
		if (allchecked)
			for (var i = 0; i < boxes.length; i++)
				if (!boxes[i].checked) {
					allchecked = false;
					break;
				}
		$('input[type=checkbox]:not(.normal):not([name])', group).prop(
				'checked', allchecked);
	}

	$.fn.checkbox = function() {
		$('.checkboxgroup', this).each(function() {
					check(this);
				});

		$('input[type=checkbox]', this).change(function(event) {
			if ($(this).hasClass('normal'))
				return;
			var group = $(this).closest('.checkboxgroup');
			if (!group.length)
				group = $(this).closest('form.richtable');
			if (!this.name) {
				var b = this.checked;
				if (group.length)
					$('input[type=checkbox][name]', group).each(function() {
								this.checked = b;
								var tr = $(this).closest('tr');
								if (tr.length) {
									if (b)
										tr.addClass('selected');
									else
										tr.removeClass('selected');
								}
							});
			} else {
				var tr = $(this).closest('tr');
				if (tr) {
					if (group.length && this.checked)
						tr.addClass('selected');
					else
						tr.removeClass('selected');
				}
				var table = $(this).closest('table');
				if (table.hasClass('treeTable')) {
					var checked = this.checked;
					$('tr.child-of-node-' + this.value, table)
							.find('input[type=checkbox]').prop('checked',
									checked).end().each(function() {
										if (checked)
											$(this).addClass('selected');
										else
											$(this).removeClass('selected');
									});
				}
				check(group);
			}
		});
		return this;
	}
})(jQuery);

Observation.checkbox = function(container) {
	$(container).checkbox();
};