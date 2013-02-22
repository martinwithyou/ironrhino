(function($) {

	$.fn.concatimage = function() {
		this.each(function() {
					var t = $(this);
					var target = t.data('target') ? document.getElementById(t
							.data('target')) : this.parentNode();
					var field = document.getElementById(t.data('field'));
					var maximum = parseInt(t.data('maximum'));
					t.click(function() {
								$('<input type="file" multiple/>')
										.appendTo(target).hide().change(
												function() {
													concatenateImages(
															this.files, target,
															field, maximum,
															t.data('error'));
													$(this).remove();
												}).click();
							});
					$(target).bind('dragover', function(e) {
								$(this).addClass('drophover');
								return false;
							}).bind('dragleave', function(e) {
								$(this).removeClass('drophover');
								return false;
							}).get(0).ondrop = function(e) {
						e.preventDefault();
						$(this).removeClass('drophover');
						concatenateImages(e.dataTransfer.files, target, field,
								maximum, t.data('error'));
						return true;
					};
				});
		return this;
	}

	function concatenateImages(files, target, field, maximum, error) {
		if (files.length > maximum) {
			Message.showActionError(MessageBundle.get(error
							|| 'maximum.exceeded', files.length, maximum));
			return;
		}
		window.URL = window.URL || window.webkitURL || window.mozURL;
		$(target).text('');
		var count = files.length;
		for (var i = 0; i < count; i++) {
			var img = document.createElement("img");
			var url = window.URL.createObjectURL(files[i]);
			img.src = url;
			img.onload = function() {
				window.URL.revokeObjectURL(url);
				count--;
				if (count == 0)
					doConcatenateImages(target, field);
			}
			img.onerror = function() {
				this.parentNode.removeChild(this);
				window.URL.revokeObjectURL(url);
				count--;
				if (count == 0)
					doConcatenateImages(target, field);
			}
			target.appendChild(img);
		}
	}

	function doConcatenateImages(target, field) {
		var imgs = target.querySelectorAll("img");
		var maxWidth = 0;
		var height = 0;
		for (var i = 0; i < imgs.length; i++) {
			var img = imgs[i];
			maxWidth = Math.max(img.width, maxWidth);
			height += img.height;
		}
		var canvas = document.createElement("canvas");
		canvas.width = maxWidth;
		canvas.height = height;
		var context = canvas.getContext('2d');
		height = 0;
		for (var i = 0; i < imgs.length; i++) {
			var img = imgs[i];
			context.drawImage(img, 0, height);
			target.removeChild(img);
			height += img.height;
		}
		$(field).val(canvas.toDataURL());
		target.appendChild(canvas);
		$(target).data('times', imgs.length);
	}
})(jQuery);

Observation.concatimage = function(container) {
	$('.concatimage', container).concatimage();
};