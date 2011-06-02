(function($) {
	if (!window.BlobBuilder && window.WebKitBlobBuilder)
		window.BlobBuilder = window.WebKitBlobBuilder;
	$.ajaxupload = function(files, options) {
		if (!files)
			return false;
		var _options = {
			url : document.location.href,
			name : 'file'
		};
		options = options || {};
		$.extend(_options, options);
		options = _options;
		var xhr = new XMLHttpRequest();
		var boundary = 'xxxxxxxxx';
		xhr.open('POST', options.url, true);
		xhr.setRequestHeader('Content-Type', 'multipart/form-data, boundary='
						+ boundary);
		xhr.onreadystatechange = function() {
			if (xhr.readyState == 4) {
				if ((xhr.status >= 200 && xhr.status <= 200)
						|| xhr.status == 304) {
					if (xhr.responseText != '') {
						if (typeof options['success'] != 'undefined')
							options['success'](xhr);
					}
				}
			}
		}
		if (typeof FileReader != 'undefined'
				&& typeof BlobBuilder != 'undefined') {
			for (var i = 0; i < files.length; i++) {
				var f = files[i];
				var reader = new FileReader();
				reader.sourceFile = f;
				var completed = 0;
				var boundary = 'xxxxxxxxx';
				var body = new BlobBuilder();
				reader.onload = function(evt) {
					var f = evt.target.sourceFile;
					var bb = new BlobBuilder();
					bb.append('--');
					bb.append(boundary);
					bb.append('\r\n');
					bb.append('Content-Disposition: form-data; name=');
					bb.append(options.name);
					bb.append('; filename=');
					bb.append(f.name);
					bb.append('\r\n');
					bb.append('Content-Type: ');
					bb.append(f.type);
					bb.append('\r\n\r\n');
					bb.append(evt.target.result);
					bb.append('\r\n');
					body.append(bb.getBlob());
					completed++;
					if (completed == files.length) {
						body.append('--');
						body.append(boundary);
						body.append('--');
						if (typeof options['beforeSend'] != 'undefined')
							options['beforeSend']();
						xhr.send(body.getBlob());
					}
				};
				reader.readAsArrayBuffer(f);
			}
			return true;
		}
		body = compose(files, options.name, boundary);
		if (body) {
			if (typeof options['beforeSend'] != 'undefined')
				options['beforeSend']();
			if (xhr.sendAsBinary)
				xhr.sendAsBinary(body);
			else
				xhr.send(body);
				return true;
		} else {
			xhr.abort();
		}
		return false;
	}

	function compose(files, name, boundary) {
		if (typeof FileReaderSync != 'undefined') {
			var bb = new BlobBuilder();
			var frs = new FileReaderSync();
			var files = event.data;
			for (var i = 0; i < files.length; i++) {
				bb.append('--');
				bb.append(boundary);
				bb.append('\r\n');
				bb.append('Content-Disposition: form-data; name=');
				bb.append(name);
				bb.append('; filename=');
				bb.append(files[i].name);
				bb.append('\r\n');
				bb.append('Content-Type: ');
				bb.append(files[i].type);
				bb.append('\r\n\r\n');
				bb.append(frs.readAsArrayBuffer(files[i]));
				bb.append('\r\n');
			}
			bb.append('--');
			bb.append(boundary);
			bb.append('--');
			return bb.getBlob();
		} else if (files[0].getAsBinary) {
			var body = '';
			for (var i = 0; i < files.length; i++) {
				body += '--' + boundary + '\r\n';
				body += 'Content-Disposition: form-data; name=' + name
						+ '; filename=' + files[i].name + '\r\n';
				body += 'Content-Type: ' + files[i].type + '\r\n\r\n';
				body += files[i].getAsBinary() + '\r\n';
			}
			body += '--' + boundary + '--';
			return body;
		} else {
			return null;
		}
	}

})(jQuery);
