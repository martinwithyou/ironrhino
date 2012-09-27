var MODERN_BROWSER = !$.browser.msie || $.browser.version > 8;
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
	if (MODERN_BROWSER)
		$.ajax = function(options) {
			options.url = UrlUtils.makeSameOrigin(options.url);
			options.xhrFields = {
				withCredentials : true
			};
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
		Indicator.text = '';
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
		return '<div class="' + className
				+ '"><a class="close" data-dismiss="alert">&times;</a>'
				+ message + '</div>';
	},
	showMessage : function() {
		Message.showActionMessage(MessageBundle.get.apply(this, arguments));
	},
	showError : function() {
		Message.showActionError(MessageBundle.get.apply(this, arguments));
	},
	showActionError : function(messages, target) {
		Message.showActionMessage(messages, target, true);
	},
	showActionMessage : function(messages, target, error) {
		if (!messages)
			return;
		if (typeof messages == 'string') {
			var a = [];
			a.push(messages);
			messages = a;
		}
		// if ($.alerts) {
		// $.alerts.alert(messages.join('\n'), MessageBundle.get('error'));
		// return;
		// }
		var html = '';
		for (var i = 0; i < messages.length; i++)
			html += Message.compose(messages[i], error
							? 'action-error alert alert-error'
							: 'action-message alert alert-info');
		if (html) {
			var parent = $('#content');
			if (error && target && $(target).parents('#_window_').length)
				parent = $('#_window_');
			if (!$('#message', parent).length)
				$('<div id="message"></div>').prependTo(parent);
			var msg = $('#message', parent);
			if (error && target && $(target).prop('tagName') == 'FORM') {
				if (!$(target).attr('id'))
					$(target).attr('id', 'form' + new Date().getTime());
				var fid = $(target).attr('id');
				if ($('#' + fid + '_message').length == 0)
					$('<div id="' + fid + '_message"></div>')
							.insertBefore(target);
				msg = $('#' + fid + '_message');
			}
			msg.html(html);
			$('html,body').animate({
						scrollTop : msg.offset().top - 20
					}, 100);
		}
	},
	showFieldError : function(field, msg, msgKey) {
		var msg = msg || MessageBundle.get(msgKey);
		if (field && $(field).length) {
			field = $(field);
			var cgroup = field.closest('.control-group');
			cgroup.addClass('error');
			$('.field-error', field.parent()).remove();
			if ($(field).is(':visible')) {
				var prompt = $('<div class="field-error removeonclick"><div class="field-error-content">'
						+ msg + '</div><div>').insertAfter(field);
				$('<div class="field-error-arrow"/>')
						.html('<div class="line10"><!-- --></div><div class="line9"><!-- --></div><div class="line8"><!-- --></div><div class="line7"><!-- --></div><div class="line6"><!-- --></div><div class="line5"><!-- --></div><div class="line4"><!-- --></div><div class="line3"><!-- --></div><div class="line2"><!-- --></div><div class="line1"><!-- --></div>')
						.appendTo(prompt);
				var promptTopPosition, promptleftPosition, marginTopSize;
				var fieldWidth = field.width();
				var promptHeight = prompt.height();
				promptTopPosition = field.position().top;
				promptleftPosition = field.position().left + fieldWidth - 30;
				marginTopSize = -promptHeight;
				prompt.css({
							"top" : promptTopPosition + "px",
							"left" : promptleftPosition + "px",
							"marginTop" : marginTopSize + "px",
							"opacity" : 0
						});
				prompt.animate({
							"opacity" : 0.8
						});
			} else if (cgroup.length && $(field).is('[type="hidden"]')) {
				$('.controls span', cgroup).text('');
				$('<span class="field-error help-inline">' + msg + '</span>')
						.appendTo($('.controls', cgroup));
			} else
				Message.showActionError(msg);
		} else
			Message.showActionError(msg);
	}
};

Form = {
	focus : function(form) {
		var arr = $(':input:visible', form).get();
		for (var i = 0; i < arr.length; i++) {
			if ($('.field-error', $(arr[i]).parent()).length) {
				setTimeout(function() {
							$(arr[i]).focus();
						}, 50);
				break;
			}
		}
	},
	validate : function(target) {
		if ($(target).prop('tagName') != 'FORM') {
			$(target).closest('.control-group').removeClass('error');
			$('.field-error', $(target).parent()).fadeIn().remove();
			if ($(target).is(':visible') || $(target).is('[type="hidden"]')
					&& !$(target).prop('disabled')) {
				var value = $(target).val();
				if ($(target).hasClass('required') && !value) {
					if ($(target).prop('tagName') == 'SELECT')
						Message.showFieldError(target, null,
								'selection.required');
					else
						Message.showFieldError(target, null, 'required');
					return false;
				} else if ($(target).hasClass('email')
						&& value
						&& !value
								.match(/^\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$/)) {
					Message.showFieldError(target, null, 'email');
					return false;
				} else if ($(target).hasClass('phone') && value
						&& !value.match(/^[\d-]+$/)) {
					Message.showFieldError(target, null, 'phone');
					return false;
				} else if ($(target).hasClass('integer') && value) {
					if ($(target).hasClass('positive')
							&& !value.match(/^[+]?\d*$/)) {
						Message
								.showFieldError(target, null,
										'integer.positive');
						return false;
					}
					if (!$(target).hasClass('positive')
							&& !value.match(/^[-+]?\d*$/)) {
						Message.showFieldError(target, null, 'integer');
						return false;
					}
					return true;
				} else if ($(target).hasClass('double') && value) {
					if ($(target).hasClass('positive')
							&& !value.match(/^[+]?\d+(\.\d+)?$/)) {
						Message.showFieldError(target, null, 'double.positive');
						return false;
					}
					if (!$(target).hasClass('positive')
							&& !value.match(/^[-+]?\d+(\.\d+)?$/)) {
						Message.showFieldError(target, null, 'double');
						return false;
					}
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
		if (!data)
			return;
		var hasError = false;
		var target = options.target;
		if (target && $(target).parents('div.ui-dialog').length)
			options.quiet = true;
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
					|| ($(target).prop('tagName') == 'FORM' ? $(target)
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
				var r = $('#' + key);
				if (key == Ajax.defaultRepacement && !r.length)
					r = $('body');
				if (!options.quiet && r.length)
					$('html,body').animate({
								scrollTop : r.offset().top - 50
							}, 100);
				var rep = div.find('#' + replacement[key]);
				if (rep.length) {
					r.html(rep.html());
				} else {
					if (div.find('#content').length)
						r.html(div.find('#content').html());
					else if (div.find('body').length)
						r.html(div.find('body').html());
					else
						r.html(html);
				}
				if (!options.quiet && (typeof $.effects != 'undefined'))
					r.effect('highlight');
				_observe(r);
			}
			div.remove();
			Ajax.fire(target, 'onsuccess', data);
		} else {
			Ajax.jsonResult = data;
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
			Message.showActionError(data.actionErrors, target);
			Message.showActionMessage(data.actionMessages, target);

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
		}
		if (target && target.tagName == 'FORM') {
			setTimeout(function() {
						$('button[type="submit"]', target).prop('disabled',
								false);
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

function ajaxOptions(options) {
	options = options || {};
	if (!options.dataType)
		options.dataType = 'text';
	if (!options.headers)
		options.headers = {};
	$.extend(options.headers, {
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
	return options;
}

function ajax(options) {
	$.ajax(ajaxOptions(options));
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
		if (!$(this).attr('id'))
			$(this).attr(
					'id',
					('a' + (i + Math.random())).replace('.', '')
							.substring(0, 5));
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
					var url = UrlUtils.absolutize(url);
					try {
						var href = top.location.href;
						if (href && UrlUtils.isSameDomain(href, url))
							top.location.href = url;
						else
							document.location.href = url;
					} catch (e) {
						document.location.href = url;
					}
					return;
				}
			});
	$('.removeonclick').live('click', function() {
				$(this).remove()
			});
	$('input').live('keyup', $.debounce(500, function(ev) {
						if (!$(this).hasClass('email') && ev.keyCode != 13)
							Form.validate(this);
						return true;
					})).live('blur', function(ev) {
				// if (this.value != this.defaultValue)
				Form.validate(this);
				return true;
			});
	$('select').live('change', function() {
				Form.validate(this);
				return true;
			});
	$('.ui-dialog-titlebar').live('dblclick', function() {
		Dialog.toggleMaximization($('.ui-dialog-content', $(this)
						.closest('.ui-dialog')));
	});
	$('.popover,.tooltip').live('mouseenter', function() {
				$(this).remove()
			});
	$.alerts.okButton = MessageBundle.get('confirm');
	$.alerts.cancelButton = MessageBundle.get('cancel');
	$('.nav li a').each(function() {
				if ($(this).attr('href') == document.location.pathname)
					$(this).closest('li').addClass('active');
			});
	$('.nav li a').click(function() {
				$('li', $(this).closest('.nav')).removeClass('active');
				$(this).closest('li').addClass('active');
			});
	if (document.location.search.indexOf('printpage=true') != -1) {
		window.print();
		window.close();
	}
};

var HISTORY_ENABLED = MODERN_BROWSER
		&& (typeof history.pushState != 'undefined' || typeof $.history != 'undefined')
		&& ($('meta[name="history_enabled"]').attr('content') != 'false');
if (HISTORY_ENABLED) {
	var SESSION_HISTORY_SUPPORT = typeof history.pushState != 'undefined'
			&& document.location.hash.indexOf('#!/') != 0;
	var _historied_ = false;
	Initialization.history = function() {

		if (SESSION_HISTORY_SUPPORT) {
			var url = document.location.href;
			history.replaceState({
						url : url
					}, '', url);
			window.onpopstate = function(event) {
				var url = document.location.href;
				if (event.state) {
					ajax({
								url : url,
								replaceTitle : true,
								replacement : event.state.replacement,
								cache : false,
								success : function() {
									$('.nav li a').each(function() {
										if (this.href == url
												|| url.indexOf(this.href + '?') == 0) {
											$('li', $(this).closest('.nav'))
													.removeClass('active');
											$(this).closest('li')
													.addClass('active');
										}
									});
								},
								headers : {
									'X-Fragment' : '_'
								}
							});
				}
			};
			return;
		}
		$.history.init(function(hash) {
					if ((!hash && !_historied_)
							|| (hash && hash.indexOf('!') < 0))
						return;
					var url = document.location.href;
					if (url.indexOf('#') > 0)
						url = url.substring(0, url.indexOf('#'));
					if (hash.length) {
						hash = hash.substring(1);
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
								success : function() {
									$('.nav li a').each(function() {
										if ($(this).attr('href') == url) {
											$('li', $(this).closest('.nav'))
													.removeClass('active');
											$(this).closest('li')
													.addClass('active');
										}
									});
								},
								headers : {
									'X-Fragment' : '_'
								}
							});
				}, {
					unescape : true
				});
	}
}

Observation.common = function(container) {
	$('.controls .field-error', container).each(function() {
				var text = $(this).text();
				var field = $(':input', $(this).parent());
				$(this).remove();
				Message.showFieldError(field, text);
			});
	var ele = ($(container).prop('tagName') == 'FORM' && $(container)
			.hasClass('focus')) ? container : $('.focus:eq(0)', container);
	if (ele.prop('tagName') != 'FORM') {
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
								$('.action-error').remove();
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
	$('.linkage', container).each(function() {
		var c = $(this);
		c.data('originalclass', c.attr('class'));
		var sw = $('.linkage_switch', c);
		$('.linkage_component', c).show();
		$('.linkage_component', c).not('.' + sw.val()).hide().filter(':input')
				.val('');
		c.attr('class', c.data('originalclass') + ' ' + sw.val());
		sw.change(function() {
					var c = $(this).closest('.linkage');
					var sw = $(this);
					$('.linkage_component', c).show();
					$('.linkage_component', c).not('.' + sw.val()).hide()
							.filter(':input').val('');
					c.attr('class', c.data('originalclass') + ' ' + sw.val());
				});
	});
	$(':input.conjunct', container).change(function() {
				var t = $(this);
				var f = $(this).closest('form');
				var hid = $(':input[type=hidden][name$=".id"]', f);
				var url = f.attr('action');
				if (url.indexOf('/') > -1)
					url = url.substring(0, url.lastIndexOf('/')) + '/input';
				else
					url = 'input';
				var data = {};
				if (hid.val())
					data['id'] = hid.val();
				data[t.attr('name')] = t.val();
				ajax({
							global : false,
							quiet : true,
							method : 'GET',
							url : url,
							data : data,
							replacement : t.attr('replacement')
						});
			});
	if (!$.browser.msie && typeof $.fn.elastic != 'undefined')
		$('textarea.elastic', container).elastic();
	if (typeof $.fn.tabs != 'undefined')
		$('div.tabs', container).each(function() {
					$(this).tabs({
								select : function(event, ui) {
									$(ui.panel).trigger('load');
								}
							}).tabs('select', $(this).data('tab'));

				});
	if (typeof $.fn.datepicker != 'undefined')
		$('input.date', container).datepicker({
					dateFormat : 'yy-mm-dd',
					zIndex : 2000
				});
	$('input.captcha', container).focus(function() {
				if ($(this).data('_captcha_'))
					return;
				$(this).after('<img class="captcha" src="' + this.id + '"/>');
				$('img.captcha', container).click(Captcha.refresh);
				$(this).data('_captcha_', true);
			});
	if (typeof $.fn.treeTable != 'undefined')
		$('.treeTable', container).each(function() {
			$(this).treeTable({
				initialState : $(this).hasClass('expanded')
						? 'expanded'
						: 'collapsed'
			});
		});
	// bootstrap start
	$('.carousel', container).each(function() {
				var t = $(this);
				t.carousel((new Function("return "
						+ (t.data('options') || '{}')))());
			});

	$('.tiped', container).each(function() {
				var t = $(this);
				var options = {
					trigger : t.data('trigger') || 'hover',
					placement : t.data('placement') || 'top'
				};
				if (!t.attr('title') && t.data('tipurl'))
					t.attr('title', MessageBundle.get('ajax.loading'));
				t.bind(options.trigger, function() {
							if (!t.hasClass('_tiped')) {
								t.addClass('_tiped');
								$.ajax({
											url : t.data('tipurl'),
											global : false,
											dataType : 'html',
											success : function(data) {
												t.attr('data-original-title',
														data);
												t.tooltip(options)
														.tooltip('show');
											}
										});
							}
						});
				if (t.is(':input')) {
					options.trigger = 'focus';
					options.placement = 'right';
				}
				t.tooltip(options);
			});
	$('.poped', container).each(function() {
		var t = $(this);
		var options = {
			trigger : t.data('trigger') || 'hover',
			placement : t.data('placement') || 'right',
			title : t.data('title'),
			content : t.data('content')
		};
		if (!options.content && t.data('popurl'))
			options.title = MessageBundle.get('ajax.loading');
		t.bind(options.trigger, function() {
			if (!t.hasClass('_poped')) {
				t.addClass('_poped');
				$.ajax({
							url : t.data('popurl'),
							global : false,
							dataType : 'html',
							success : function(data) {
								if (data.indexOf('<title>') >= 0
										&& data.indexOf('</title>') > 0)
									t.attr('data-original-title',
											data.substring(data
															.indexOf('<title>')
															+ 7,
													data.indexOf('</title>')));
								if (data.indexOf('<body>') >= 0
										&& data.indexOf('</body>') > 0)
									t
											.attr(
													'data-content',
													data
															.substring(
																	data
																			.indexOf('<body>')
																			+ 6,
																	data
																			.indexOf('</body>')));
								t.popover(options).popover('show');
							}
						});
			}
		});
		t.popover(options);
	});
	// bootstrap end
	$('.switch', container).each(function() {
				var t = $(this);
				t.children().css('cursor', 'pointer').click(function() {
							t.children().removeClass('active').css({
										'font-weight' : 'normal'
									});
							$(this).addClass('active').css({
										'font-weight' : 'bold'
									});
						});
			});
	if (typeof swfobject != 'undefined') {
		$('.chart', container).each(function() {
			var t = $(this);
			var id = t.attr('id');
			var width = t.width();
			var height = t.height();
			var data = t.attr('data');
			if (data.indexOf('/') == 0)
				data = document.location.protocol + '//'
						+ document.location.host + data;
			if (!id || !width || !height || !data)
				alert('id,width,height,data all required');
			swfobject.embedSWF(CONTEXT_PATH
							+ '/assets/images/open-flash-chart.swf', id, width,
					height, '9.0.0', CONTEXT_PATH
							+ '/assets/images/expressInstall.swf', {
						'data-file' : encodeURIComponent(data),
						'loading' : MessageBundle.get('ajax.loading')
					}, {
						wmode : 'transparent'
					});
			if (t.data('_interval'))
				clearInterval(parseInt(t.data('_interval')));
			if (t.data('interval')) {
				var _interval = setInterval(function() {
							if (t.data('quiet')) {
								$.ajax({
											global : false,
											url : data,
											dataType : 'text',
											success : function(json) {
												document.getElementById(id)
														.load(json);
											}
										});
							} else {
								document.getElementById(id).reload(data);
							}
						}, parseInt(t.data('interval')));
				t.data('_interval', _interval);
			}
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
	$('a.ajax,form.ajax', container).each(function() {
		var target = this;
		var ids = [];
		var targetId = $(target).attr('id');
		if (typeof targetId != 'string')
			targetId = '';
		var entries = ($(target).attr('replacement') || ($(target)
				.prop('tagName') == 'FORM' ? targetId : '')).split(',');
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
					$('.action-error').remove();
					if (!Form.validate(target))
						return false;
					Indicator.text = $(target).data('indicator');
					$('button[type="submit"]', target).prop('disabled', true);
					Ajax.fire(target, 'onloading');
				},
				error : function() {
					Form.focus(target);
					if (target && target.tagName == 'FORM')
						setTimeout(function() {
									$('button[type="submit"]', target).prop(
											'disabled', false);
								}, 100);
					Ajax.fire(target, 'onerror');
				},
				success : function(data) {
					Ajax.handleResponse(data, {
								'target' : target
							});
				},
				headers : {}
			};
			if (!$(this).hasClass('view'))
				$.extend(options.headers, {
							'X-Data-Type' : 'json'
						});
			if (ids.length > 0)
				$.extend(options.headers, {
							'X-Fragment' : ids.join(',')
						});
			$(this).bind('submit', function() {
						$(this).ajaxSubmit(options);
						return false;
					});
			return;
		} else {
			$(this).click(function() {
				if (!Ajax.fire(target, 'onprepare'))
					return false;
				if (HISTORY_ENABLED
						&& $(this).hasClass('view')
						&& ($(this).hasClass('history') || !$(this)
								.attr('replacement'))) {
					var hash = this.href;
					if (UrlUtils.isSameDomain(hash)) {
						hash = hash.substring(hash.indexOf('//') + 2);
						hash = hash.substring(hash.indexOf('/'));
						if (SESSION_HISTORY_SUPPORT) {
							history.pushState({
										replacement : $(this)
												.attr('replacement'),
										url : hash
									}, '', hash);
						} else {
							if (CONTEXT_PATH)
								hash = hash.substring(CONTEXT_PATH.length);
							hash = hash.replace(/^.*#/, '');
							$.history.load('!' + hash);
							return false;
						}
					}

				}
				var options = {
					url : this.href,
					type : $(this).attr('method') || 'GET',
					cache : $(this).hasClass('cache'),
					beforeSend : function() {
						$('.action-error').remove();
						Indicator.text = $(target).data('indicator');
						Ajax.fire(target, 'onloading');
					},
					error : function() {
						Ajax.fire(target, 'onerror');
					},
					headers : {}
				};
				var _opt = {
					'target' : target
				};
				if (!$(this).hasClass('view'))
					$.extend(options.headers, {
								'X-Data-Type' : 'json'
							});
				if (ids.length > 0) {
					$.extend(options.headers, {
								'X-Fragment' : ids.join(',')
							});
				} else {
					$.extend(options.headers, {
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
			$(d).dialog('option', 'minHeight', height);
			var height = $(doc).height() + 20;
			$(iframe).height(height);
		}
		d.dialog('option', 'position', 'center');
		var height = d.height();
		if (height > 600)
			d.dialog('option', 'position', 'top');
		d.dialog('moveToTop');
	},
	toggleMaximization : function(d) {
		var dialog = $(d).closest('.ui-dialog');
		var orginalWidth = dialog.data('orginal-width');
		if (orginalWidth) {
			dialog.width(orginalWidth);
			dialog.height(dialog.data('orginal-height'));
			dialog.removeData('orginal-width').removeData('orginal-height');
			Dialog.adapt($(d));
		} else {
			dialog.data('orginal-width', dialog.width() + 0.2).data(
					'orginal-height', dialog.height() + 0.2);
			var viewportWidth = $(window).width() - 10;
			var viewportHeight = $(window).height() - 10;
			dialog.width(viewportWidth);
			if ($(d).dialog('option', 'minHeight') < viewportHeight)
				dialog.height(viewportHeight);
			d.dialog('option', 'position', 'top');
		}
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
		$('input.captcha').val('').focus();
	}
};
ArrayUtils = {
	unique : function(arr) {
		if (arr) {
			var arr2 = [];
			var provisionalTable = {};
			for (var i = 0, item; (item = arr[i]) != null; i++) {
				if (!provisionalTable[item]) {
					arr2.push(item);
					provisionalTable[item] = true;
				}
			}
			return arr2;
		}
	}
};