$(function() {
			var pressed = false;
			var start = undefined;
			var startX, startWidth;

			$('table.resizable span.resizeBar').live('mousedown', function(e) {
						start = $(this).parent();
						pressed = true;
						startX = e.pageX;
						startWidth = start.width();
					});

			$(document).mousemove(function(e) {
				if (pressed) {
					document.body.style.cursor = 'col-resize';
					var newwidth = startWidth + (e.pageX - startX);
					var minColWidth = $(e.target).closest('table')
							.data('minColWidth');
					if (minColWidth && parseInt(minColWidth) > newwidth)
						start.width(parseInt(minColWidth));
					else
						start.width(newwidth);
				}
			});

			$(document).mouseup(function() {
						if (pressed) {
							pressed = false;
							document.body.style.cursor = '';
						}
					});
		});