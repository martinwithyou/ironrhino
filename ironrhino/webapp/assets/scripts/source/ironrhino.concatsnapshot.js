(function($) {

	$.fn.concatsnapshot = function() {
		this.click(function() {
					var t = $(this);
					var target = t.data('target') ? document
							.getElementById($(this).data('target')) : this
							.parentNode();
					var times = $(target).data('times');
					if (times && parseInt(times) >= parseInt(t.data('maximum'))) {
						Message.showActionError(MessageBundle.get(t
										.data('error')
										|| 'maximum.exceeded', times, t
										.data('maximum')));
						return false;
					}

					$.snapshot({
								onsnapshot : function(canvas) {
									var times = $(target).data('times');
									times ? $(target).data('times',
											parseInt(times) + 1) : $(target)
											.data('times', 1);
									var image = target.querySelector('canvas');
									if (!image) {
										image = document
												.createElement('canvas');
										image.width = canvas.width;
										image.height = canvas.height;
										target.innerHTML = '';
										target.appendChild(image);
										var context = image.getContext('2d');
										context.drawImage(canvas, 0, 0);
									} else {
										var image2 = image;
										image2.style.display = 'none';
										image = document
												.createElement('canvas');
										image.width = Math.max(image2.width,
												canvas.width);
										image.height = image2.height
												+ canvas.height;
										target.appendChild(image);
										var context = image.getContext('2d');
										context.drawImage(image2, 0, 0);
										context.drawImage(canvas, 0,
												image2.height);
										image2.parentNode.removeChild(image2);
									}
									$('#' + t.data('field')).val(image
											.toDataURL());
								},
								onerror : function(error) {
									Message.showActionError(error);
								}
							});
				});
		return this;
	}
})(jQuery);

Observation.concatsnapshot = function(container) {
	$('.concatsnapshot', container).concatsnapshot();
};