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
												this.toDataURL(),
												parseInt(this
														.getAttribute('data-timestamp')));
							destroy(video, this);
						});
					});
					video.addEventListener('dblclick', function(e) {
								var canvas = document.createElement('canvas');
								canvas.getContext('2d').drawImage(video, 0, 0);
								if (options.onsnapshot)
									options.onsnapshot(canvas.toDataURL(),
											new Date().getTime());
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
/*
 * JavaScript Canvas to Blob 2.0.5
 * https://github.com/blueimp/JavaScript-Canvas-to-Blob
 * 
 * Copyright 2012, Sebastian Tschan https://blueimp.net
 * 
 * Licensed under the MIT license: http://www.opensource.org/licenses/MIT
 * 
 * Based on stackoverflow user Stoive's code snippet:
 * http://stackoverflow.com/q/4998908
 */

/* jslint nomen: true, regexp: true */
/* global window, atob, Blob, ArrayBuffer, Uint8Array, define */

(function(window) {
	'use strict';
	var CanvasPrototype = window.HTMLCanvasElement
			&& window.HTMLCanvasElement.prototype, hasBlobConstructor = window.Blob
			&& (function() {
				try {
					return Boolean(new Blob());
				} catch (e) {
					return false;
				}
			}()), hasArrayBufferViewSupport = hasBlobConstructor
			&& window.Uint8Array && (function() {
				try {
					return new Blob([new Uint8Array(100)]).size === 100;
				} catch (e) {
					return false;
				}
			}()), BlobBuilder = window.BlobBuilder || window.WebKitBlobBuilder
			|| window.MozBlobBuilder || window.MSBlobBuilder, dataURLtoBlob = (hasBlobConstructor || BlobBuilder)
			&& window.atob
			&& window.ArrayBuffer
			&& window.Uint8Array
			&& function(dataURI) {
				var byteString, arrayBuffer, intArray, i, mimeString, bb;
				if (dataURI.split(',')[0].indexOf('base64') >= 0) {
					// Convert base64 to raw binary data held in a string:
					byteString = atob(dataURI.split(',')[1]);
				} else {
					// Convert base64/URLEncoded data component to raw binary
					// data:
					byteString = decodeURIComponent(dataURI.split(',')[1]);
				}
				// Write the bytes of the string to an ArrayBuffer:
				arrayBuffer = new ArrayBuffer(byteString.length);
				intArray = new Uint8Array(arrayBuffer);
				for (i = 0; i < byteString.length; i += 1) {
					intArray[i] = byteString.charCodeAt(i);
				}
				// Separate out the mime component:
				mimeString = dataURI.split(',')[0].split(':')[1].split(';')[0];
				// Write the ArrayBuffer (or ArrayBufferView) to a blob:
				if (hasBlobConstructor) {
					return new Blob([hasArrayBufferViewSupport
									? intArray
									: arrayBuffer], {
								type : mimeString
							});
				}
				bb = new BlobBuilder();
				bb.append(arrayBuffer);
				return bb.getBlob(mimeString);
			};
	if (window.HTMLCanvasElement && !CanvasPrototype.toBlob) {
		if (CanvasPrototype.mozGetAsFile) {
			CanvasPrototype.toBlob = function(callback, type, quality) {
				if (quality && CanvasPrototype.toDataURL && dataURLtoBlob) {
					callback(dataURLtoBlob(this.toDataURL(type, quality)));
				} else {
					callback(this.mozGetAsFile('blob', type));
				}
			};
		} else if (CanvasPrototype.toDataURL && dataURLtoBlob) {
			CanvasPrototype.toBlob = function(callback, type, quality) {
				callback(dataURLtoBlob(this.toDataURL(type, quality)));
			};
		}
	}
	if (typeof define === 'function' && define.amd) {
		define(function() {
					return dataURLtoBlob;
				});
	} else {
		window.dataURLtoBlob = dataURLtoBlob;
	}
}(this));