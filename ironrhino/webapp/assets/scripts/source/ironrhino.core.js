(function() {
	var d = document.domain;
	if (!d.match(/^(\d+\.){3}\d+$/)) {
		d = d.split('.');
		try {
			if (d.length > 2)
				document.domain = d[d.length - 2] + '.' + d[d.length - 1];
		} catch (e) {
			if (d.length > 3)
				document.domain = d[d.length - 3] + '.' + d[d.length - 2] + '.'
						+ d[d.length - 1];
		}
	}
	$.ajaxSettings.traditional = true;
	var $ajax = $.ajax;
	$.ajax = function(options) {
		options.url = UrlUtils.makeSameOrigin(options.url);
		var temp = options.beforeSend;
		options.beforeSend = function(xhr) {
			xhr.withCredentials = 'true';
			// xhr.setRequestHeader('X-Requested-With', 'XMLHttpRequest');
			if (options.header)
				for (var key in options.header)
					xhr.setRequestHeader(key, options.header[key]);
			if (temp)
				temp(xhr);
		}
		return $ajax(options);
	}

	if (typeof $.rc4EncryptStr != 'undefined'
			&& ($('meta[name="pe"]').attr('content') != 'false')) {
		var temp = $.param;
		$.param = function(a, traditional) {
			if (jQuery.isArray(a) || a.jquery) {
				jQuery.each(a, function() {
							if (/password$/.test(this.name.toLowerCase())) {
								try {
									var key = $.cookie('T');
									key = key.substring(15, 25);
									this.value = $
											.rc4EncryptStr(
													encodeURIComponent(this.value
															+ key), key);
								} catch (e) {
								}
							}
						});

			}
			return temp(a, traditional);
		}
	}

})();

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
	extractDomain : function(a) {
		if (UrlUtils.isAbsolute(a)) {
			a = a.replace(/:\d+/, '');
			a = a.substring(a.indexOf('://') + 3);
			var i = a.indexOf('/');
			if (i > 0)
				a = a.substring(0, i);
			return a;
		} else {
			return document.location.hostname;
		}
	},
	isSameDomain : function(a, b) {
		b = b || document.location.href;
		var ad = UrlUtils.extractDomain(a);
		var bd = UrlUtils.extractDomain(b);
		return ad == bd;
	},
	isSameOrigin : function(a, b) {
		b = b || document.location.href;
		var ad = UrlUtils.extractDomain(a);
		var bd = UrlUtils.extractDomain(b);
		if ($.browser.msie && ad != bd)
			return false;
		var arra = ad.split('.');
		var arrb = bd.split('.');
		return (arra[arra.length - 1] == arrb[arrb.length - 1] && arra[arra.length
				- 2] == arrb[arrb.length - 2]);
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
		if (!a)
			return false;
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
	compose : function(message, className) {
		return '<div class="'
				+ className
				+ '"><span class="close" onclick="$(this.parentNode).remove()"></span>'
				+ message + '</div>';
	},
	showMessage : function() {
		Message.showActionMessage(MessageBundle.get.apply(this, arguments));
	},
	showActionMessage : function(messages, target) {
		if (!messages)
			return;
		if (typeof messages == 'string') {
			var a = [];
			a.push(messages);
			messages = a;
		}
		if (typeof $.fn.jnotifyInizialize != 'undefined') {
			if (!$('#notification').length)
				$('<div id="notification"><div>').prependTo(document.body)
						.jnotifyInizialize({
									oneAtTime : false,
									appendType : 'append'
								}).css({
									'position' : 'absolute',
									'marginTop' : '40px',
									'right' : '40px',
									'width' : '250px',
									'min-height' : '50px',
									'z-index' : '9999'
								});
			for (var i = 0; i < messages.length; i++) {
				$('#notification').jnotifyAddMessage({
							text : messages[i],
							permanent : false
						});
			}
			return;
		}
		var html = '';
		for (var i = 0; i < messages.length; i++)
			html += Message.compose(messages[i], 'action_message');
		if (target && target.tagName == 'FORM') {
			if ($('#' + target.id + '_message').length == 0)
				$(target).before('<div id="' + target.id + '_message"></div>');
			$('#' + target.id + '_message').html(html);
		} else {
			if (!$('#message').length)
				$('<div id="message"></div>').prependTo(document.body);
			$('#message').html(html);
		}
	},
	showError : function() {
		Message.showActionError(MessageBundle.get.apply(this, arguments));
	},
	showActionError : function(messages, target) {
		if (!messages)
			return;
		if (typeof messages == 'string') {
			var a = [];
			a.push(messages);
			messages = a;
		}
		if (typeof $.fn.jnotifyInizialize != 'undefined') {
			$('#message').jnotifyInizialize({
						oneAtTime : false
					});
			for (var i = 0; i < messages.length; i++)
				$('#message').jnotifyAddMessage({
							text : messages[i],
							permanent : true,
							type : 'error'
						});
			return;
		}
		if ($.alerts) {
			$.alerts.alert(messages.join('\n'), MessageBundle.get('error'));
			return;
		}
		var html = '';
		for (var i = 0; i < messages.length; i++)
			html += Message.compose(messages[i], 'action_error');
		if (html)
			if (target && target.tagName == 'FORM') {
				if ($('#' + target.id + '_message').length == 0)
					$(target).before('<div id="' + target.id
							+ '_message"></div>');
				$('#' + target.id + '_message').html(html);
			} else {
				if (!$('#message').length)
					$('<div id="message"></div>').prependTo(document.body);

				$('#message').html(html);
			}
	},
	showFieldError : function(field, msg, msgKey) {
		var msg = msg || MessageBundle.get(msgKey);
		if (field && $(field).length)
			$(field).parent().append(Message.compose(msg, 'field_error'));
		else
			Message.showActionError(msg);
	}
};

Form = {
	focus : function(form) {
		var arr = $(':input:visible', form).get();
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
					Message.showFieldError(target, null, 'selection.required');
				else
					Message.showFieldError(target, null, 'required');
				return false;
			} else if ($(target).hasClass('email')
					&& $(target).val()
					&& !$(target)
							.val()
							.match(/^\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$/)) {
				Message.showFieldError(target, null, 'email');
				return false;
			} else if ($(target).hasClass('integer') && $(target).val()) {
				if ($(target).hasClass('positive')
						&& !$(target).val().match(/^[+]?\d*$/)) {
					Message.showFieldError(target, null, 'integer.positive');
					return false;
				}
				if (!$(target).hasClass('positive')
						&& !$(target).val().match(/^[-+]?\d*$/)) {
					Message.showFieldError(target, null, 'integer');
					return false;
				}
				return true;
			} else if ($(target).hasClass('double') && $(target).val()) {
				if ($(target).hasClass('positive')
						&& !$(target).val().match(/^[+]?\d+(\.\d+)?$/)) {
					Message.showFieldError(target, null, 'double.positive');
					return false;
				}
				if (!$(target).hasClass('positive')
						&& !$(target).val().match(/^[-+]?\d+(\.\d+)?$/)) {
					Message.showFieldError(target, null, 'double');
					return false;
				}
				return true;
			} else {
				return true;
			}
		} else {
			var valid = true;
			$(':input', target).each(function() {
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
	defaultRepacement : 'content',
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
			if (data.indexOf('<title>') >= 0 && data.indexOf('</title>') > 0) {
				Ajax.title = data.substring(data.indexOf('<title>') + 7, data
								.indexOf('</title>'));
				if (options.replaceTitle)
					document.title = Ajax.title;
			}
			var replacement = {};
			var entries = (options.replacement
					|| $(target).attr('replacement')
					|| ($(target).attr('tagName') == 'FORM' ? $(target)
							.attr('id') : null) || Ajax.defaultRepacement)
					.split(',');
			for (var i = 0; i < entries.length; i++) {
				var entry = entries[i];
				var ss = entry.split(':', 2);
				replacement[ss[0]] = (ss.length == 2 ? ss[1] : ss[0]);
			}
			var html = data.replace(/<script(.|\s)*?\/script>/g, '');
			var div = $('<div/>').html(html);
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
				if (options.onerror)
					options.onerror.apply(window);
				Ajax.fire(target, 'onerror', data);
			} else {
				if (options.onsuccess)
					options.onsuccess.apply(window);
				Ajax.fire(target, 'onsuccess', data);
			}
			if (data.fieldErrors) {
				if (target) {
					for (key in data.fieldErrors)
						Message.showFieldError(target[key],
								data.fieldErrors[key]);
					Form.focus(target);
				} else {
					for (key in data.fieldErrors)
						Message.showActionError(data.fieldErrors[key]);
				}
			}
			Message.showActionError(data.actionErrors, target);
			Message.showActionMessage(data.actionMessages, target);
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

var CONTEXT_PATH = $('meta[name="context_path"]').attr('content') || '';

if (typeof(Initialization) == 'undefined')
	Initialization = {};
if (typeof(Observation) == 'undefined')
	Observation = {};
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
	$('.chart,form.ajax,.ajaxpanel', container).each(function(i) {
		if (!this.id)
			this.id = ('a' + (i + Math.random())).replace('.', '').substring(0,
					5);
	});
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
	$(document).ajaxStart(function() {
				Indicator.show()
			});
	$(document).ajaxError(function() {
				Indicator.showError()
			});
	$(document).ajaxSuccess(function(ev, xhr) {
				Indicator.hide();
				var url = xhr.getResponseHeader('X-Redirect-To');
				if (url) {
					top.location.href = UrlUtils.absolutize(url);
					return;
				}
			});
	$.alerts.okButton = MessageBundle.get('confirm');
	$.alerts.cancelButton = MessageBundle.get('cancel');
	$('.menu li').each(function() {
				if ($('a', this).attr('href') == document.location.pathname)
					$(this).addClass('selected');
			});
	$('.menu li a').click(function() {
				$('li', $(this).closest('.menu')).removeClass('selected');
				$(this).closest('li').addClass('selected');
			});
};

var HISTORY_ENABLED = typeof $.history != 'undefined'
		&& ($('meta[name="history_enabled"]').attr('content') != 'false');
var SESSION_HISTORY_SUPPORT = typeof history.pushState != 'undefined';
var _historied_ = false;
Initialization.history = function() {
	if (HISTORY_ENABLED) {
		if (SESSION_HISTORY_SUPPORT) {
			window.onpopstate = function(event) {
				var url = document.location.href;
				if (event.state && _historied_) {
					ajax({
								url : url,
								cache : true,
								replaceTitle : true,
								header : {
									'X-Fragment' : '_'
								}
							});
				} else {
					history.replaceState(url);
					_historied_ = true;
				}
			};
			return;
		}
		$.history.init(function(hash) {
					if ((!hash && !_historied_)
							|| (hash && hash.indexOf('/') < 0))
						return;
					var url = document.location.href;
					if (url.indexOf('#') > 0)
						url = url.substring(0, url.indexOf('#'));
					if (hash) {
						if (UrlUtils.isSameDomain(hash)) {
							if (CONTEXT_PATH)
								hash = CONTEXT_PATH + hash;
						}
						url = hash;

					}
					_historied_ = true;
					ajax({
								url : url,
								cache : true,
								replaceTitle : true,
								header : {
									'X-Fragment' : '_'
								}
							});
				}, {
					unescape : true
				});
	}
}

Observation.common = function(container) {
	$('div.action_error,div.action_message,div.field_error,ul.action_error li,ul.action_message li')
			.prepend('<div class="close" onclick="$(this.parentNode).remove()"></div>');
	var ele = ($(container).attr('tagName') == 'FORM' && $(container)
			.hasClass('focus')) ? container : $('.focus:eq(0)', container);
	if (ele.attr('tagName') != 'FORM') {
		ele.focus();
	} else {
		var arr = $(':input:visible', ele).toArray();
		for (var i = 0; i < arr.length; i++) {
			if (!$(arr[i]).val()) {
				$(arr[i]).focus();
				break;
			}
		}
	}
	$('form', container).each(function() {
				if (!$(this).hasClass('ajax'))
					$(this).submit(function() {
								$('.action_message,.action_error').remove();
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
	if (typeof $.fn.corner != 'undefined' && $.browser.msie
			&& $.browser.version <= 8)
		$('.rounded', container).each(function() {
					$(this).corner($(this).attr('corner'));
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
	if (typeof $.fn.truncatable != 'undefined')
		$('.truncatable', container).each(function() {
					$(this).truncatable({
								limit : $(this).attr('limit') || 100
							});
				});

	if (typeof swfobject != 'undefined') {
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
						wmode : 'transparent'
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
	if (typeof $.fn.cycle != 'undefined')
		$('.cycle').each(function() {
					var options = {
						fx : 'fade',
						pause : 1
					};
					var _options = $.parseJSON($(this).attr('options'));
					if (_options)
						$.extend(options, _options);
					$(this).cycle(options);
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
			$('input', this).keyup(function(event) {
						if (event.keyCode == 13)
							return true;
						if (!$(this).attr('keyupValidate')) {
							$(this).attr('keyupValidate', 'true');
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
					if (UrlUtils.isSameDomain(hash)) {
						hash = hash.substring(hash.indexOf('//') + 2);
						hash = hash.substring(hash.indexOf('/'));
						if (SESSION_HISTORY_SUPPORT) {
							history.pushState(hash, '', hash);
						} else {
							if (CONTEXT_PATH)
								hash = hash.substring(CONTEXT_PATH.length);
							hash = hash.replace(/^.*#/, '');
							$.history.load(hash);
							return false;
						}
					}

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
					header : {}
				};
				var _opt = {
					'target' : target
				};
				if (!$(this).hasClass('view'))
					$.extend(options.header, {
								'X-Data-Type' : 'json'
							});
				if (ids.length > 0) {
					$.extend(options.header, {
								'X-Fragment' : ids.join(',')
							});
				} else {
					$.extend(options.header, {
								'X-Fragment' : '_'
							});
					_opt.replaceTitle = true;
				}

				options.success = function(data) {
					Ajax.handleResponse(data, _opt);
				};
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
