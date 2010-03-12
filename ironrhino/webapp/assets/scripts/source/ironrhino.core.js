(function() {
	var d = document.domain.split('.');
	try {
		if (d.length > 2)
			document.domain = d[d.length - 2] + '.' + d[d.length - 1];
	} catch (e) {
		if (d.length > 3)
			document.domain = d[d.length - 3] + '.' + d[d.length - 2] + '.'
					+ d[d.length - 1];
	}
	$.ajaxSettings.traditional = true;
	var $ajax = $.ajax;
	$.ajax = function(options) {
		options.url = UrlUtils.makeSameOrigin(options.url);
		var temp = options.beforeSend;
		options.beforeSend = function(xhr) {
			if (options.header)
				for (var key in options.header)
					xhr.setRequestHeader(key, options.header[key]);
			if (temp)
				temp(xhr);
		}
		return $ajax(options);
	}
})();

var HISTORY_ENABLED = true;

if (typeof(Initialization) == 'undefined')
	Initialization = {};
if (typeof(Observation) == 'undefined')
	Observation = {};
var CONTEXT_PATH = $('meta[name="context_path"]').attr('content') || '';
function _init() {
	var array = [];
	for (var key in Initialization) {
		if (typeof(Initialization[key]) == 'function')
			array.push(key);
	}
	array.sort();
	for (var i = 0; i < array.length; i++)
		Initialization[array[i]].call(this);
	_observe();
}
function _observe(container) {
	if (!container)
		container = document;
	var array = [];
	for (var key in Observation) {
		if (typeof(Observation[key]) == 'function')
			array.push(key);
	}
	array.sort();
	for (var i = 0; i < array.length; i++)
		Observation[array[i]].call(this, container);
}
$(_init);

Initialization.common = function() {
	if (typeof dwr != 'undefined') {
		dwr.engine.setPreHook(Indicator.show);
		dwr.engine.setPostHook(Indicator.hide);
	}
	$(document).ajaxStart(function() {
				Indicator.show()
			});
	$(document).ajaxError(function() {
				Indicator.showError()
			});
	$(document).ajaxSuccess(function(ev, xhr) {
				Indicator.hide();
				var url = xhr.getResponseHeader("X-Redirect-To");
				if (url) {
					top.location.href = UrlUtils.absolutize(url);
					return;
				}
			});
};

Indicator = {
	text : '',
	show : function(iserror) {
		if (!$('#indicator').length)
			$('<div id="indicator"></div>').appendTo(document.body);
		var ind = $('#indicator');
		if (iserror && ind.hasClass('loading'))
			ind.removeClass('loading');
		if (!iserror && !ind.hasClass('loading'))
			ind.addClass('loading');
		ind.html(Indicator.text || MessageBundle.get('ajax.loading'));
		ind.show();
	},
	showError : function() {
		Indicator.text = MessageBundle.get('ajax.error');
		Indicator.show(true);
	},
	hide : function() {
		Indicator.text = '';
		if ($('#indicator'))
			$('#indicator').hide()
	}
};

UrlUtils = {
	isSameOrigin : function(a, b) {
		b = b || document.location.href;
		if (UrlUtils.isAbsolute(a)) {
			var index = a.indexOf('://');
			if (a.indexOf(':80/') > 0)
				a = a.replace(':80/', '/');
			var ad = a.substring(0, a.indexOf('/', index + 3));
			if (b.indexOf(':80/') > 0)
				b = b.replace(':80/', '/');
			var bd = b.substring(0, b.indexOf('/', b.indexOf('://') + 3));
			if (ad != bd)
				return false;
		}
		return true;
	},
	makeSameOrigin : function(url, referrer) {
		referrer = referrer || document.location.href;
		if (!UrlUtils.isSameOrigin(url, referrer))
			return referrer.substring(0, referrer.indexOf('/', referrer
									.indexOf('://')
									+ 3))
					+ CONTEXT_PATH + '/webproxy/' + url;
		else
			return url;
	},
	isAbsolute : function(a) {
		var index = a.indexOf('://');
		return (index == 4 || index == 5);
	},
	absolutize : function(url) {
		if (UrlUtils.isAbsolute(url))
			return url;
		var a = document.location.href;
		var index = a.indexOf('://');
		if (url.length == 0 || url.indexOf('/') == 0) {
			return a.substring(0, a.indexOf('/', index + 3)) + CONTEXT_PATH
					+ url;
		} else {
			return a.substring(0, a.lastIndexOf('/') + 1) + url;
		}
	}
}

Message = {
	get : function(message, className) {
		return '<div class="'
				+ className
				+ '"><span class="close" onclick="$(this.parentNode).remove()"></span>'
				+ message + '</div>';
	},
	showError : function(field, errorType) {
		$(field).parent().append(Message.get(MessageBundle.get(errorType),
				'field_error'));
	}
};

Form = {
	focus : function(form) {
		var arr = $('input,select', form).get();
		for (var i = 0; i < arr.length; i++) {
			if ($('.field_error', $(arr[i]).parent()).length) {
				setTimeout(function() {
							$(arr[i]).focus();
						}, 50);
				break;
			}
		}
	},
	validate : function(target) {
		if ($(target).attr('tagName') != 'FORM') {
			$('.field_error', $(target).parent()).remove();
			if ($(target).hasClass('required') && !$(target).val()) {
				if ($(target).attr('tagName') == 'SELECT')
					Message.showError(target, 'selection.required');
				else
					Message.showError(target, 'required');
				return false;
			} else if ($(target).hasClass('email')
					&& $(target).val()
					&& !$(target)
							.val()
							.match(/^\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$/)) {
				Message.showError(target, 'email');
				return false;
			} else if ($(target).hasClass('integer') && $(target).val()) {
				if ($(target).hasClass('positive')
						&& !$(target).val().match(/^[+]?\d*$/)) {
					Message.showError(target, 'integer.positive');
					return false;
				}
				if (!$(target).hasClass('positive')
						&& !$(target).val().match(/^[-+]?\d*$/)) {
					Message.showError(target, 'integer');
					return false;
				}
				return true;
			} else if ($(target).hasClass('double') && $(target).val()) {
				if ($(target).hasClass('positive')
						&& !$(target).val().match(/^[+]?\d+(\.\d+)?$/)) {
					Message.showError(target, 'double.positive');
					return false;
				}
				if (!$(target).hasClass('positive')
						&& !$(target).val().match(/^[-+]?\d+(\.\d+)?$/)) {
					Message.showError(target, 'double');
					return false;
				}
				return true;
			} else {
				return true;
			}
		} else {
			var valid = true;
			$('input,select', target).each(function() {
						if (!Form.validate(this))
							valid = false;
					});
			if (!valid)
				Form.focus(target);
			return valid;
		}
	}
};

Ajax = {
	fire : function(target, funcName) {
		if (!target)
			return true;
		var func = target[funcName];
		if (typeof func == 'undefined')
			func = $(target).attr(funcName);
		if (typeof func == 'undefined' || !func)
			return true;
		var args = [];
		if (arguments.length > 2)
			for (var i = 2; i < arguments.length; i++)
				args[i - 2] = arguments[i];
		var ret;
		if (typeof(func) == 'function') {
			ret = func.apply(target, args);
		} else {
			if (func.indexOf('return') > -1)
				func = func.replace('return', '');
			target._temp = function() {
				return eval(func)
			};
			try {
				ret = target._temp();
			} catch (e) {
				alert(e);
			}
		}
		if (false == ret)
			return false;
		return true;
	},
	handleResponse : function(data, options) {
		var hasError = false;
		var target = options.target;
		if ((typeof data == 'string')
				&& (data.indexOf('{') == 0 || data.indexOf('[') == 0))
			data = $.parseJSON(data);
		if (typeof data == 'string') {
			if (data.indexOf('<title>') > 0 && data.indexOf('</title>') > 0) {
				Ajax.title = data.substring(data.indexOf('<title>') + 7, data
								.indexOf('</title>'));
				if (options.replaceTitle)
					document.title = Ajax.title;
			}
			var replacement = {};
			var entries = (options.replacement
					|| $(target).attr('replacement')
					|| ($(target).attr('tagName') == 'FORM' ? $(target)
							.attr('id') : null) || 'content').split(',');
			for (var i = 0; i < entries.length; i++) {
				var entry = entries[i];
				var ss = entry.split(':', 2);
				replacement[ss[0]] = (ss.length == 2 ? ss[1] : ss[0]);
			}
			var html = data.replace(/<script(.|\s)*?\/script>/g, "");
			var div = $("<div/>").html(html);
			// others
			for (var key in replacement) {
				if (!options.quiet)
					$('html,body').animate({
								scrollTop : $('#' + key).offset().top - 50
							}, 100);
				if (div.find('#' + replacement[key]).length)
					$('#' + key).html(div.find('#' + replacement[key]).html());
				else {
					var start = html.indexOf('>', html.indexOf('<body')) + 1;
					var end = html.indexOf('</body>');
					if (end > 0)
						$('body').html(html.substring(start, end));
					else
						$('body').html(html.substring(start));
				}
				if (!options.quiet && (typeof $.effects != 'undefined'))
					$('#' + key).effect('highlight');
				_observe($('#' + key));
			}
			div.remove();
			Ajax.fire(target, 'onsuccess', data);
		} else {
			Ajax.jsonResult = data;
			if (data.csrf)
				$('input[name="csrf"]').val(data.csrf);
			if (data.fieldErrors || data.actionErrors) {
				hasError = true;
				Ajax.fire(target, 'onerror', data);
			} else {
				Ajax.fire(target, 'onsuccess', data);
			}
			var message = '';
			if (data.fieldErrors)
				for (key in data.fieldErrors)
					if (target && target[key])
						$(target[key]).parent().append($(Message.get(
								data.fieldErrors[key], 'field_error')));
					else
						message += Message.get(data.fieldErrors[key],
								'action_error');
			if (data.fieldErrors)
				Form.focus(target);
			if (data.actionErrors)
				for (var i = 0; i < data.actionErrors.length; i++)
					message += Message
							.get(data.actionErrors[i], 'action_error');
			if (data.actionMessages)
				for (var i = 0; i < data.actionMessages.length; i++)
					if (typeof $.jGrowl != 'undefined')
						$.jGrowl(data.actionMessages[i]);
					else
						message += Message.get(data.actionMessages[i],
								'action_message');
			if (message)
				if (target && target.tagName == 'FORM') {
					if ($('#' + target.id + '_message').length == 0)
						$(target).before('<div id="' + target.id
								+ '_message"></div>');
					$('#' + target.id + '_message').html(message);
				} else {
					if (!$('#message').length)
						$('<div id="message"></div>').prependTo(document.body);
					$('#message').html(message);
				}
		}
		if (target && target.tagName == 'FORM') {
			setTimeout(function() {
						$('button[type="submit"]', target)
								.removeAttr('disabled');
						Captcha.refresh()
					}, 100);
			if (!hasError && $(target).hasClass('reset')) {
				if (typeof target.reset == 'function'
						|| (typeof target.reset == 'object' && !target.reset.nodeType))
					target.reset();
			}
		}
		Indicator.text = '';
		Ajax.fire(target, 'oncomplete', data);
	},
	jsonResult : null,
	title : ''
};

function ajax(options) {
	if (!options.header)
		options.header = {};
	$.extend(options.header, {
				'X-Data-Type' : options.dataType
			});
	var beforeSend = options.beforeSend;
	options.beforeSend = function(xhr) {
		if (beforeSend)
			beforeSend(xhr);
		Indicator.text = options.indicator;
		Ajax.fire(null, options.onloading);
	}
	var success = options.success;
	options.success = function(data, textStatus, XMLHttpRequest) {
		Ajax.handleResponse(data, options);
		if (success && !(data.fieldErrors || data.actionErrors))
			success(data, textStatus, XMLHttpRequest);
	};
	$.ajax(options);
}
var _history_ = false;
Initialization.history = function() {
	if (!HISTORY_ENABLED || (typeof $.historyInit == 'undefined'))
		return;
	$.historyInit(function(hash) {
				if ((!hash && !_history_) || (hash && hash.indexOf('/') < 0))
					return;
				var url = document.location.href;
				if (url.indexOf('#') > 0)
					url = url.substring(0, url.indexOf('#'));
				if (hash) {
					if (UrlUtils.isSameOrigin(hash)) {
						if (CONTEXT_PATH)
							hash = CONTEXT_PATH + hash;
					}
					url = hash;

				}
				_history_ = true;
				ajax({
							url : url,
							cache : true,
							replaceTitle : true
						});
			}, '');
}

Observation.common = function(container) {
	$('div.action_error,div.action_message,div.field_error,ul.action_error li,ul.action_message li')
			.prepend('<div class="close" onclick="$(this.parentNode).remove()"></div>');
	$('.focus:eq(0)', container).focus();
	$('form', container).each(function() {
				if (!$(this).hasClass('ajax'))
					$(this).submit(function() {
								$('.action_message,.action_error').remove();
								$('.field_error', this).remove();
								return Form.validate(this)
							});
			});
	$('input[type="text"]', container).each(function() {
				if (!$(this).attr('autocomplete'))
					$(this).attr('autocomplete', 'off');
				var maxlength = $(this).attr('maxlength');
				if (!maxlength || maxlength > 3000) {
					if ($(this).hasClass('date'))
						$(this).attr('maxlength', '10');
					else if ($(this).hasClass('integer'))
						$(this).attr('maxlength', '11');
					else if ($(this).hasClass('double'))
						$(this).attr('maxlength', '22');
					else
						$(this).attr('maxlength', '255');
				}
			});
	$('ul.nav>li', container).hover(function() {
				$("ul", this).fadeIn("fast");
			}, function() {
			});
	if ($.browser.msie) {
		$('ul.nav>li', container).each(function() {
					if ($('ul>li', this).length)
						$(this).hover(function() {
									$(this).addClass("sfHover");
								}, function() {
									$(this).removeClass("sfHover");
								})
				});
	}
	$('.highlightrow tbody tr').hover(function() {
				$(this).addClass('highlight');
			}, function() {
				$(this).removeClass('highlight');
			});
	if (!$.browser.msie && typeof $.fn.elastic != 'undefined')
		$('textarea').elastic();
	if (typeof $.fn.tabs != 'undefined')
		$('div.tabs', container).each(function() {
					$(this).tabs().tabs('select', $(this).attr('tab'))
				});
	if (typeof $.fn.corner != 'undefined')
		$('.rounded', container).css({
					padding : '5px',
					margin : '5px'
				}).each(function() {
					$(this).corner();
				});
	if (typeof $.fn.datepicker != 'undefined')
		$('input.date', container).datepicker({
					dateFormat : 'yy-mm-dd',
					zIndex : 2000
				});
	$('input.captcha', container).focus(function() {
				if ($(this).attr('_captcha_'))
					return;
				$(this).after('<img class="captcha" src="' + this.id + '"/>');
				$('img.captcha', container).click(Captcha.refresh);
				$(this).attr('_captcha_', true);
			});
	if (typeof $.fn.treeTable != 'undefined')
		$('.treeTable', container).each(function() {
			$(this).treeTable({
				initialState : $(this).hasClass('expanded')
						? 'expanded'
						: 'collapsed'
			});
		});
	if (typeof $.fn.sexyCombo != 'undefined')
		$('select.combox', container).sexyCombo({
					emptyText : MessageBundle.get('select'),
					triggerSelected : true
				});
	if (typeof $.fn.truncatable != 'undefined')
		$('.truncatable', container).each(function() {
					$(this).truncatable({
								limit : $(this).attr('limit') || 100
							});
				});

	if (typeof swfobject.embedSWF != 'undefined') {
		$('.chart', container).each(function() {
			var id = this.id;
			var width = $(this).width();
			var height = $(this).height();
			var data = $(this).attr('data');
			if (data.indexOf('/') == 0)
				data = document.location.protocol + '//'
						+ document.location.host + data;
			data = encodeURIComponent(data);
			if (!id || !width || !height || !data)
				alert('id,width,height,data all required');
			swfobject.embedSWF(CONTEXT_PATH
							+ '/assets/images/open-flash-chart.swf', id, width,
					height, '9.0.0', CONTEXT_PATH
							+ '/assets/images/expressInstall.swf', {
						'data-file' : data
					}, {
						wmode : "transparent"
					});
		});
		window.save_image = function() {
			var content = [];
			content
					.push('<html><head><title>Charts: Export as Image<\/title><\/head><body>');
			$('object[data]').each(function() {
				content.push('<img src="data:image/png;base64,'
						+ this.get_img_binary() + '"/>');
			});
			content.push('<\/body><\/html>');
			var img_win = window.open('', '_blank');
			with (img_win.document) {
				write(content.join(''));
				img_win.document.close();
			}
		}
	}
	if (typeof $.fn.uploadify != 'undefined')
		$('.uploadify').each(function() {
			var options = {
				'uploader' : CONTEXT_PATH + '/assets/images/uploadify.swf',
				'script' : $(this).closest('form')[0].action,
				'cancelImg' : CONTEXT_PATH + '/assets/images/cancel.png',
				// 'folder' : CONTEXT_PATH + '/upload',
				'buttonText' : MessageBundle.get('browse'),
				'wmode' : 'transparent',
				'multi' : true,
				'auto' : true,
				'dipsplayData' : 'percentage'
			};
			var _options = $.parseJSON($(this).attr('options'));
			if (_options)
				$.extend(options, _options);
			if (!options.auto) {
				$(this)
						.after('<div class="uploadify_control"><button class="btn"><span><span>'
								+ MessageBundle.get('upload')
								+ '</span></span></button><button class="btn"><span><span>'
								+ MessageBundle.get('clear')
								+ '</span></span></button></div>');
				var t = this;
				$('div.uploadify_control button', $(this).parent()).eq(0)
						.click(function() {
									$(t).uploadifyUpload()
								}).end().eq(1).click(function() {
									$(t).uploadifyClearQueue()
								});
			}
			options.fileDataName = $(this).attr('name');
			$(this).uploadify(options);
		});

	$('a.ajax,form.ajax', container).each(function() {
		var target = this;
		var ids = [];
		var targetId = $(target).attr('id');
		if (typeof targetId != 'string')
			targetId = '';
		var entries = ($(target).attr('replacement') || ($(target)
				.attr('tagName') == 'FORM' ? targetId : '')).split(',');
		for (var i = 0; i < entries.length; i++) {
			var entry = entries[i];
			var ss = entry.split(':', 2);
			var id = ss.length == 2 ? ss[1] : ss[0];
			if (id)
				ids.push(id);
		}
		if (this.tagName == 'FORM') {
			var options = {
				beforeSubmit : function() {
					if (!Ajax.fire(target, 'onprepare'))
						return false;
					$('.action_message,.action_error').remove();
					$('.field_error', target).remove();
					if (!Form.validate(target))
						return false;
					Indicator.text = $(target).attr('indicator');
					$('button[type="submit"]', target).attr('disabled', true);
					Ajax.fire(target, 'onloading');
				},
				error : function() {
					Form.focus(target);
					if (target && target.tagName == 'FORM')
						setTimeout(function() {
									$('button[type="submit"]', target)
											.removeAttr('disabled');
								}, 100);
					Ajax.fire(target, 'onerror');
				},
				success : function(data) {
					Ajax.handleResponse(data, {
								'target' : target
							});
				},
				header : {}
			};
			if (!$(this).hasClass('view'))
				$.extend(options.header, {
							'X-Data-Type' : 'json'
						});
			if (ids.length > 0)
				$.extend(options.header, {
							'X-Fragment' : ids.join(',')
						});
			$(this).bind('submit', function() {
						$(this).ajaxSubmit(options);
						return false;
					});
			$('input', this).keyup(function() {
						if (!$(this).attr('need')) {
							$(this).attr('need', 'true');
						} else {
							Form.validate(this);
						}
						return true;
					});
			$('select', this).change(function() {
						Form.validate(this);
						return true;
					});
			return;
		} else {
			$(this).click(function() {
				if (!Ajax.fire(target, 'onprepare'))
					return false;
				if (HISTORY_ENABLED && $(this).hasClass('view')
						&& !$(this).attr('replacement')) {
					var hash = this.href;
					if (UrlUtils.isSameOrigin(hash)) {
						hash = hash.substring(hash.indexOf('//') + 2);
						hash = hash.substring(hash.indexOf('/'));
						if (CONTEXT_PATH)
							hash = hash.substring(CONTEXT_PATH.length);
					}
					hash = hash.replace(/^.*#/, '');
					$.historyLoad(hash);
					return false;
				}
				var options = {
					url : this.href,
					type : $(this).attr('method') || 'GET',
					cache : $(this).hasClass('cache'),
					beforeSend : function() {
						$('.action_message,.action_error').remove();
						Indicator.text = $(target).attr('indicator');
						Ajax.fire(target, 'onloading');
					},
					error : function() {
						Ajax.fire(target, 'onerror');
					},
					success : function(data) {
						Ajax.handleResponse(data, {
									'target' : target
								})
					},
					header : {}
				};
				if (!$(this).hasClass('view'))
					$.extend(options.header, {
								'X-Data-Type' : 'json'
							});
				if (ids.length > 0)
					$.extend(options.header, {
								'X-Fragment' : ids.join(',')
							});
				$.ajax(options);
				return false;
			});
		}
	});
};

var Dialog = {
	adapt : function(d, iframe) {
		var useiframe = iframe != null;
		if (!iframe) {
			$(d).dialog('option', 'title', Ajax.title);
		} else {
			var doc = iframe.document;
			if (iframe.contentDocument) {
				doc = iframe.contentDocument;
			} else if (iframe.contentWindow) {
				doc = iframe.contentWindow.document;
			}
			$(d).dialog('option', 'title', doc.title);
		}
		d.dialog('option', 'position', 'center');
		var height = d.height();
		if (height > 600)
			d.dialog('option', 'position', 'top');
		
	}
}

var Region = {
	textid : '',
	isfull : '',
	hiddenid : '',
	select : function(tid, f, hid) {
		Region.textid = tid;
		Region.isfull = f;
		Region.hiddenid = hid;
		var _click = function() {
			if (Region.textid && $('#' + Region.textid)) {
				var name = $(this).text();
				if (Region.isfull) {
					var p = this.parentNode.parentNode.parentNode.parentNode;
					while (p && p.tagName == 'LI') {
						name = $('a span', p).get(0).innerHTML + name;
						p = p.parentNode.parentNode;
					}
				}
				if ($('#' + Region.textid).attr('tagName') == 'INPUT')
					$('#' + Region.textid).val(name);
				else
					$('#' + Region.textid).text(name);
			}
			if (Region.hiddenid && $('#' + Region.hiddenid))
				$('#' + Region.hiddenid).val($(this).closest('li').attr('id'));
			$("#region_window").dialog('close');
		};
		if (!$('#region_window').length) {
			$('<div id="region_window" title="' + MessageBundle.get('select')
					+ '"><div id="region_tree"></div></div>')
					.appendTo(document.body);
			$("#region_window").dialog({
						width : 500,
						minHeight : 500
					});
			$("#region_tree").treeview({
						url : CONTEXT_PATH + '/region/children',
						click : _click,
						collapsed : true,
						placeholder : MessageBundle.get('ajax.loading'),
						unique : true
					});
		} else {
			$("#region_window").dialog('open');
		}
	}
};

Captcha = {
	refresh : function() {
		$('img.captcha').each(function() {
					var src = this.src;
					var i = src.lastIndexOf('&');
					if (i > 0)
						src = src.substring(0, i);
					this.src = src + '&' + Math.random();
				});
		$('input.captcha').val('');
	}
};
