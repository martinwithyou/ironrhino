( function($) {
	$.fn.checkbox = function() {
		checkbox(this);
	};
	function checkbox(container) {
		$('input[type=checkbox]', container).each(
				function() {
					this.onclick = function(event) {
						if (!this.name) {
							var b = this.checked;
							$('input[type=checkbox][name]', this.form).each(
									function() {
										this.checked = b;
										var tr = $(this).closest('tr');
										if (tr) {
											if (b)
												tr.addClass('selected');
											else
												tr.removeClass('selected');
										}
									});
						} else {
							if (!(event || window.event).shiftKey) {
								var tr = $(this).closest('tr');
								if (tr) {
									if (this.checked)
										tr.addClass('selected');
									else
										tr.removeClass('selected');
								}
							} else {
								var boxes = $('input[type=checkbox][name]',
										this.form);
								var start = -1, end = -1, checked = false;
								for ( var i = 0; i < boxes.length; i++) {
									if ($(boxes[i]).attr('lastClicked')) {
										checked = boxes[i].checked;
										start = i;
									}
									if (boxes[i] == this) {
										end = i;
									}
								}
								if (start > end) {
									var tmp = end;
									end = start;
									start = tmp;
								}
								for ( var i = start; i <= end; i++) {
									boxes[i].checked = checked;
									tr = $(boxes[i]).closest('tr');
									if (tr) {
										if (boxes[i].checked)
											tr.addClass('selected');
										else
											tr.removeClass('selected');
									}
								}
							}
							$('input[type=checkbox]', this.form).each(
									function() {
										this.removeAttribute('lastClicked')
									});
							$(this).attr('lastClicked', 'true');
						}
					}
				});

		$('a.delete_selected').each(
				function() {
					this.onprepare = function() {
						var params = [];
						$('input[type=checkbox]', $(this).closest('form'))
								.each(
										function() {
											if (this.name && this.checked) {
												params.push(this.name + '='
														+ this.value)
											}
										});
						var url = $(this).attr('_href');
						if (!url) {
							url = this.href;
							$(this).attr('_href', url);
						}
						url += (url.indexOf('?') > 0 ? '&' : '?')
								+ params.join('&');
						this.href = url;
						return true;
					};
					this.onsuccess = function() {
						$(this).attr('href', $(this).attr('_href'));
					}
				});
	}
})(jQuery);

Observation.checkbox = function(container) {
	$(container).checkbox();
};