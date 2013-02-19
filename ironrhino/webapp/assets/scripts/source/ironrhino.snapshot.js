(function($) {
	var videoStream;
	$.snapshot = function(options) {
		options = options || {};
		if (!navigator.getUserMedia)
			navigator.getUserMedia = navigator.oGetUserMedia
					|| navigator.mozGetUserMedia
					|| navigator.webkitGetUserMedia || navigator.msGetUserMedia;
		if (!navigator.getUserMedia) {
			var error = 'getUserMedia() not available from your Web browser!';
			if (options.onerror)
				options.onerror(error);
			else
				alert(error);
			return;
		}
		if (typeof Indicator.show != 'undefined')
			Indicator.show();
		navigator.getUserMedia({
					video : true
				}, function(stream) {
					videoStream = stream;
					if (typeof Indicator.hide != 'undefined')
						Indicator.hide();
					var container = options.container;
					if (!container) {
						$('<div id="snapshot-modal" class="modal" style="z-index:10000;"><div  id="snapshot-modal-body" class="modal-body" style="max-height:600px;"></div></div>')
								.appendTo(document.body);
						container = 'snapshot-modal-body';
					}
					if (typeof container == 'string')
						container = document.getElementById(container);
					var video = document.getElementById('snapshot-video');
					if (!video) {
						video = document.createElement('video');
						video.id = 'snapshot-video';
						video.autoplay = true;
						video.style.cursor = 'pointer';
						video.style.width = '100%';
						container.appendChild(video);
					}
					video.addEventListener('click', function(e) {
						video.style.zIndex = '2000';
						video.style.width = '20%';
						video.style.position = 'absolute';
						var canvas = document.getElementById('snapshot-canvas');
						if (!canvas) {
							canvas = document.createElement('canvas');
							canvas.id = 'snapshot-canvas';
							canvas.style.cursor = 'pointer';
							canvas.style.width = '100%';
							container.appendChild(canvas);
						}
						canvas.width = this.videoWidth;
						canvas.height = this.videoHeight;
						canvas.getContext('2d').drawImage(video, 0, 0);
						canvas.setAttribute('data-timestamp', new Date()
										.getTime());
						canvas.addEventListener('click', function() {
							if (options.onsnapshot)
								options
										.onsnapshot(
												this,
												parseInt(this
														.getAttribute('data-timestamp')));
							destroy(video, this);
						});
					});
					video.addEventListener('dblclick', function(e) {
								var canvas = document.createElement('canvas');
								canvas.getContext('2d').drawImage(video, 0, 0);
								if (options.onsnapshot)
									options.onsnapshot(canvas, new Date()
													.getTime());
								canvas = document
										.getElementById('snapshot-canvas');
								destroy(video, canvas);
							});
					video.onerror = function() {
						if (video)
							destroy(video);
					};
					// stream.onended = noStream;
					if (window.webkitURL)
						video.src = window.webkitURL.createObjectURL(stream);
					else if (video.mozSrcObject !== undefined) {// FF18a
						video.mozSrcObject = stream;
						video.play();
					} else if (navigator.mozGetUserMedia) {// FF16a, 17a
						video.src = stream;
						video.play();
					} else if (window.URL)
						video.src = window.URL.createObjectURL(stream);
					else
						video.src = stream;
				}, function() {
					var error = 'Access to camera was denied!';
					if (options.onerror)
						options.onerror(error);
					else
						alert(error);
				});
	}

	function destroy(video, canvas) {
		if (canvas)
			canvas.parentNode.removeChild(canvas);
		if (videoStream) {
			if (videoStream.stop)
				videoStream.stop();
			else if (videoStream.msStop)
				videoStream.msStop();
			videoStream.onended = null;
			videoStream = null;
		}
		video.onerror = null;
		video.pause();
		if (video.mozSrcObject)
			video.mozSrcObject = null;
		if (window.URL)
			window.URL.revokeObjectURL(video.src);
		video.src = '';
		video.parentNode.removeChild(video);
		var modal = document.getElementById('snapshot-modal');
		if (modal)
			modal.parentNode.removeChild(modal);
	}
})(jQuery);