(function($) {

	$.fn.decodeqrcode = function() {
		this.click(function(e) {
			var target = $($(e.target).data('target'));
			$.snapshot({
				onsnapshot : function(canvas) {
					$.ajax({
								type : 'post',
								url : CONTEXT_PATH + '/qrcode?decode=true',
								contentType : 'text/plain',
								data : canvas.toDataURL(),
								success : function(data) {
									if (data.actionErrors) {
										Message
												.showActionError(data.actionErrors);
									} else {
										if (target.is(':input'))
											target.val(data);
										else
											target.text(data);
									}
								}
							});
				},
				onerror : function(msg) {
					Message.showError(msg);
				}
			});
		});
		return this;
	};

})(jQuery);

Observation.decodeqrcode = function(container) {
	$('.decodeqrcode', container).decodeqrcode();
};