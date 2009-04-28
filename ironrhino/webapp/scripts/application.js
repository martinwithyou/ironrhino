var HISTORY_ENABLED = true;

MessageBundle = {};
MessageBundle['en'] = {
	'indicator.loading' : 'loading...',
	'indicator.error' : 'network error,please try later',
	'required' : 'please input value',
	'selection.required' : 'please select',
	'email' : 'this field must be a valid email',
	'integer' : 'this field must be integer',
	'double' : 'this field must be double',
	'confirm.delete' : 'are you sure to delete?',
	'save.and.create' : 'save and add'
}
MessageBundle['zh-cn'] = {
	'indicator.loading' : '正在加载...',
	'indicator.error' : '网络故障,请稍后再试',
	'required' : '必填项,请填写',
	'selection.required' : '必填项,请选择',
	'email' : 'email不合法',
	'integer' : '请填写整数',
	'double' : '请填写数字',
	'confirm.delete' : '确定要删除?',
	'save.and.create' : '保存并新建'
}
MessageBundle.get = function() {
	var key = arguments[0];
	var lang = (navigator.language || navigator.browserLanguage || '')
			.toLowerCase();
	if (!MessageBundle[lang])
		lang = 'en';
	var msg = MessageBundle[lang][key];
	for (var i = 1; i < arguments.length; i++)
		msg = msg.replace('{' + i + '}', arguments[i]);
	return msg;
}

Indicator = {
	text : '',
	show : function(iserror) {
		if ($('#indicator').length == 0)
			$('<div id="indicator"></div>').appendTo(document.body);
		var ind = $('#indicator');
		if (iserror && ind.hasClass('loading'))
			ind.removeClass('loading');
		if (!iserror && !ind.hasClass('loading'))
			ind.addClass('loading');
		ind.html(Indicator.text || MessageBundle.get('indicator.loading'));
		ind.show();
	},
	showError : function() {
		Indicator.text = MessageBundle.get('indicator.error');
		Indicator.show(true);
	},
	hide : function() {
		Indicator.text = '';
		if ($('#indicator'))
			$('#indicator').hide()
	}
}

function getMessage(message, className) {
	return '<div class="'
			+ className
			+ '"><div class="close" onclick="$(this.parentNode).remove()"></div>'
			+ message + '</div>';
}

function validateForm(form) {
	var valid = true;
	$('input', form).each(function() {
		if ($(this).hasClass('required') && !$(this).val()) {
			valid = false;
			$(this).after(getMessage(MessageBundle.get('required'),
					'field_error'));
		}
		if ($(this).hasClass('email')
				&& $(this).val()
				&& !$(this).val()
						.match(/^\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$/)) {
			valid = false;
			$(this)
					.after(getMessage(MessageBundle.get('email'), 'field_error'));
		}
		if ($(this).hasClass('integer') && $(this).val()
				&& !$(this).val().match(/^[-+]?\d*$/)) {
			valid = false;
			$(this).after(getMessage(MessageBundle.get('integer'),
					'field_error'));
		}
		if ($(this).hasClass('double') && $(this).val()
				&& !$(this).val().match(/^[-\+]?\d+(\.\d+)?$/)) {
			valid = false;
			$(this)
					.after(getMessage(MessageBundle.get('double'),
							'field_error'));
		}
	});
	$('select', form).each(function() {
		if ($(this).hasClass('required')) {
			if (!$(this).val()) {
				valid = false;
				$(this).after(getMessage(MessageBundle
						.get('selection.required'), 'field_error'));
			}
		}
	});
	return valid;
}

function fire(target, funcName) {
	if (!target)
		return true;
	var func = target[funcName];
	if (typeof func == 'undefined')
		func = $(target).attr(funcName);
	if (typeof func == 'undefined' || !func)
		return true;
	var ret;
	if (typeof(func) == 'function') {
		ret = func.call(target);
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
}

function handleResponse(data, options) {
	var hasError = false;
	var target = options.target;
	if ((typeof data == 'string')
			&& (data.indexOf('{') == 0 || data.indexOf('[') == 0))
		data = eval('(' + data + ')');
	if (typeof data == 'string') {
		var replacement = {};
		var entries = (options.replacement || $(target).attr('replacement') || 'content')
				.split(',');
		for (var i = 0; i < entries.length; i++) {
			var entry = entries[i];
			var ss = entry.split(':', 2);
			replacement[ss[0]] = (ss.length == 2 ? ss[1] : ss[0]);
		}
		var div=$("<div/>").append(data.replace(/<script(.|\s)*?\/script>/g, ""));
		// others
		for (var key in replacement) {
			if (!options.silence)
				$('html,body').animate({
					scrollTop : $('#' + key).offset().top
				}, 100);
			$('#' + key).html(div.find('#' + replacement[key]));
			if (!options.silence && (typeof $.effects != 'undefined'))
				$('#' + key).effect('highlight');
			_observe($('#' + key));
		}
		div.remove();
		fire(target, 'onsuccess');
	} else {
		window._jsonResult_ = data;
		if (data.fieldErrors || data.actionErrors) {
			hasError = true;
			fire(target, 'onerror');
		} else {
			fire(target, 'onsuccess');
		}
		var message = '';
		if (data.fieldErrors)
			for (key in data.fieldErrors)
				if (target && target[key])
					$(target[key]).after($(getMessage(data.fieldErrors[key],
							'field_error')));
				else
					message += getMessage(data.fieldErrors[key], 'action_error');
		if (data.actionErrors)
			for (var i = 0; i < data.actionErrors.length; i++)
				message += getMessage(data.actionErrors[i], 'action_error');
		if (data.actionMessages)
			for (var i = 0; i < data.actionMessages.length; i++)
				message += getMessage(data.actionMessages[i], 'action_message');
		if (message)
			if (target && target.tagName.toLowerCase() == 'form') {
				if ($('#' + target.id + '_message').length == 0)
					$(target).before('<div id="' + target.id
							+ '_message"></div>');
				$('#' + target.id + '_message').html(message);
			} else {
				if ($('#message').length == 0)
					$('<div id="message"></div>').prependTo(document.body);
				$('#message').html(message);
			}
	}
	if (target && target.tagName.toLowerCase() == 'form') {
		setTimeout(function() {
			$('input[type="submit"]', target).removeAttr('disabled');
			refreshCaptcha()
		}, 100);
		if (!hasError && $(target).hasClass('reset')) {
			if (typeof target.reset == 'function'
					|| (typeof target.reset == 'object' && !target.reset.nodeType))
				target.reset();
		}
	}
	Indicator.text = '';
	fire(target, 'oncomplete');
};

function ajax(options) {
	if (!options.data)
		options.data = {};
	$.extend(options.data, {
		_transport_type_ : 'XMLHttpRequest',
		_include_query_string_ : 'true',
		_result_type_ : options.dataType != 'json' ? '' : 'json'
	});
	$.extend(options, {
		beforeSend : function() {
			Indicator.text = options.indicator;
			fire(null, options.onloading);
		},
		success : function(data) {
			handleResponse(data, options)
		}
	});
	$.ajax(options);
}

if (typeof(Initialization) == 'undefined')
	Initialization = {};
if (typeof(Observation) == 'undefined')
	Observation = {};
var CONTEXT_PATH = '';
function _init() {
	CONTEXT_PATH = $('meta[name="context_path"]').attr('content') || '';
	if (CONTEXT_PATH == '/')
		CONTEXT_PATH = '';

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

var _init_content_ = '';
var _init_ = true;
Initialization.history = function() {
	if (HISTORY_ENABLED && (typeof $.history != 'undefined')) {
		_init_content_ = $('#content').html();
		$.history.init(function(hash) {
			if (hash && hash != '#') {
				if ($.browser.mozilla && hash.indexOf('%') == 0)
					return;
				hash = decodeURIComponent(hash);
				_init_ = false;
				if (CONTEXT_PATH)
					hash = CONTEXT_PATH + hash;
				ajax({
					url : hash,
					cache : true,
					success : handleResponse
				});
			} else if (!hash || hash == '#') {
				if (_init_content_ && !_init_) {
					$('#content').html(_init_content_);
					_observe($('#content'));
				}
			}
		});
	}
}

Observation.ajax = function(container) {
	$('a.ajax,form.ajax', container).each(function() {
		var target = this;
		try {
			var _options = $(target).attr('options') ? eval('('
					+ $(target).attr('options') + ')') : null;
			if (_options)
				$.each(_options, function(key, value) {
					$(target).attr(key, value);
				});
		} catch (e) {
		}

		if (this.tagName.toLowerCase() == 'form') {
			var options = {
				data : {
					_transport_type_ : 'XMLHttpRequest',
					_include_query_string_ : 'true',
					_result_type_ : $(this).hasClass('view') ? '' : 'json'
				},
				beforeSubmit : function() {
					if (!fire(target, 'onprepare'))
						return false;
					$('.action_message,.action_error').remove();
					$('.field_error', target).remove();
					if (!validateForm(target))
						return false;
					Indicator.text = $(target).attr('indicator');
					$('input[type="submit"]', target).attr('disabled', true);
					fire(target, 'onloading');
				},
				error : function() {
					if (target && target.tagName.toLowerCase() == 'form')
						setTimeout(function() {
							$('input[type="submit"]', target)
									.removeAttr('disabled');
						}, 100);
					fire(target, 'onerror');
				},
				success : function(data) {
					handleResponse(data, {
						'target' : target
					});
				}
			};
			$(this).ajaxForm(options);
			return;
		} else {
			$(this).click(function() {
				if (HISTORY_ENABLED && $(this).hasClass('view')
						&& !$(this).attr('replacement')) {
					var hash = this.href;
					hash = hash.substring(hash.indexOf('//') + 2);
					hash = hash.substring(hash.indexOf('/'));
					if (CONTEXT_PATH)
						hash = hash.substring(CONTEXT_PATH.length);
					hash = encodeURIComponent(hash);
					$.history.load(hash);
					return false;
				}
				if (!fire(target, 'onprepare'))
					return false;
				$.ajax({
					url : this.href,
					type : $(this).attr('method') || 'GET',
					data : {
						_transport_type_ : 'XMLHttpRequest',
						_include_query_string_ : 'true',
						_result_type_ : $(this).hasClass('view') ? '' : 'json'
					},
					cache : $(this).hasClass('cache'),
					beforeSend : function() {
						$('.action_message,.action_error').remove();
						Indicator.text = $(target).attr('indicator');
						fire(target, 'onloading');
					},
					error : function() {
						fire(target, 'onerror');
					},
					success : function(data) {
						handleResponse(data, {
							'target' : target
						})
					}
				});
				return false;
			});
		}
	});
}

Observation.checkbox = function(container) {
	$('input[type=checkbox]', container).each(function() {
		this.onclick = function(event) {
			if (!this.name) {
				var b = this.checked;
				$('input[type=checkbox][name]', this.form).each(function() {
					this.checked = b;
					var tr = $(this).closest('tr');
					if (tr) {
						if (b)
							tr.addClass('selected');
						else
							tr.removeClass('selected');
					}
				});
			} else {
				if (!(event || window.event).shiftKey) {
					var tr = $(this).closest('tr');
					if (tr) {
						if (this.checked)
							tr.addClass('selected');
						else
							tr.removeClass('selected');
					}
				} else {
					var boxes = $('input[type=checkbox][name]', this.form);
					var start = -1, end = -1, checked = false;
					for (var i = 0; i < boxes.length; i++) {
						if ($(boxes[i]).attr('lastClicked')) {
							checked = boxes[i].checked;
							start = i;
						}
						if (boxes[i] == this) {
							end = i;
						}
					}
					if (start > end) {
						var tmp = end;
						end = start;
						start = tmp;
					}
					for (var i = start; i <= end; i++) {
						boxes[i].checked = checked;
						tr = $(boxes[i]).closest('tr');
						if (tr) {
							if (boxes[i].checked)
								tr.addClass('selected');
							else
								tr.removeClass('selected');
						}
					}
				}
				$('input[type=checkbox]', this.form).each(function() {
					this.removeAttribute('lastClicked')
				});
				$(this).attr('lastClicked', 'true');
			}
		}
	});

	$('a.delete_selected').each(function() {
		this.onprepare = function() {
			var params = [];
			$('input[type=checkbox]', $(this).closest('form'))
					.each(function() {
						if (this.name && this.checked) {
							params.push(this.name + '=' + this.value)
						}
					});
			var url = $(this).attr('_href');
			if (!url) {
				url = this.href;
				$(this).attr('_href', url);
			}
			url += (url.indexOf('?') > 0 ? '&' : '?') + params.join('&');
			this.href = url;
			return true;
		};
		this.onsuccess = function() {
			$(this).attr('href', $(this).attr('_href'));
		}
	});
}

Observation.common = function(container) {
	// beautify buttons
	$('form input[type="submit"],form input[type="button"]', container).each( function() {
		var htm = '<button'+($(this).id?' id="'+$(this).id+'"':"")+' class="btn"><span><span>' + $(this).val() + '</span></span></button>';		
		$(this).replaceWith(htm);
	});
	$('input.autocomplete_off').attr('autocomplete','off');
	$('ul.nav>li', container).hover(function() {
		$("ul", this).fadeIn("fast");
	}, function() {
	});
	if ($.browser.msie) {
		$('ul.nav>li', container).each(function() {
			if ($('ul>li', this).length > 0)
				$(this).hover(function() {
					$(this).addClass("sfHover");
				}, function() {
					$(this).removeClass("sfHover");
				})
		});
	}
	$('div.tabs', container).each(function() {
		$(this).tabs().tabs('select', $(this).attr('tab'))
	});
	$('.round_corner,.corner', container).css({
		padding : '5px',
		margin : '5px'
	}).each(function() {
		$(this).corner();
	});
	if (typeof $.datepicker != 'undefined')
		$('input.date', container).datepicker({
			dateFormat : 'yy-mm-dd'
		});
	$('input.captcha', container).focus(function() {
		if ($(this).attr('_captcha_'))
			return;
		$(this).after('<img class="captcha" src="' + CONTEXT_PATH + '/captcha.jpg"/>');
		$('img.captcha', container).click(refreshCaptcha);
		$(this).attr('_captcha_', true);
	});
	if (typeof $.EmptyOnClick != 'undefined')
		$('input.emptyonclick, textarea.emptyonclick').emptyonclick();
}

Initialization.common = function() {
	if ($.browser.msie)
		window.attachEvent('onunload', function() {
			CollectGarbage()
		});
	if (typeof dwr != 'undefined') {
		dwr.engine.setPreHook(Indicator.show);
		dwr.engine.setPostHook(Indicator.hide);
	}
	$().ajaxStart(function() {
		Indicator.show()
	});
	$().ajaxError(function() {
		Indicator.showError()
	});
	$().ajaxSuccess(function(ev, xhr) {
		Indicator.hide();
		var url = xhr.getResponseHeader("X-Redirect-To");
		if (url) {
			top.location.href = url;
			return;
		}
	});
	if ($('#q').length > 0)
		$("#q").autocomplete(CONTEXT_PATH + "/search/suggest?decorator=none", {
			minChars : 3,
			delay : 1000
		});
}

Initialization.cart = function() {
	$('img.product_list').addClass('draggable').each(function() {
		var code = this.alt;
		$(this).dragable({
			clone : false,
			opacity : 0.5,
			zIndex : -10,
			target : '#cart_items',
			over : function(obj, target) {
				$(target).addClass('ondrop')
			},
			out : function(obj, target) {
				$(target).removeClass('ondrop')
			},
			drop : function(obj, target) {
				$(target).removeClass('ondrop');
				ajax({
					url : CONTEXT_PATH + '/cart/add/' + code,
					type : 'POST',
					replacement : 'cart_items'
				});
			}
		});
	});
}

Initialization.categoryTree = function() {
	$('a.category').each(function() {
		this.onsuccess = function() {
			$('a.category').each(function() {
				$(this).removeClass('selected')
			});
			$(this).addClass('selected');
		};
		this.cache = true;
	});
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
					while (p && p.tagName.toLowerCase() == 'li') {
						name = $('a span', p).get(0).innerHTML + name;
						p = p.parentNode.parentNode;
					}
				}
				if ($('#' + Region.textid)[0].tagName.toLowerCase() == 'input')
					$('#' + Region.textid).val(name);
				else
					$('#' + Region.textid).text(name);
			}
			if (Region.hiddenid && $('#' + Region.hiddenid))
				$('#' + Region.hiddenid).val($(this).closest('li').attr('id'));
			$("#region_window").dialog('close');
		};
		if ($('#region_window').length == 0) {
			$('<div id="region_window" class="flora" title="请选择"><div id="region_tree"></div></div>')
					.appendTo(document.body);
			$("#region_window").dialog({
				width : 350,
				height : 600
			});
			$("#region_tree").treeview({
				url : CONTEXT_PATH + '/region/children',
				click : _click,
				collapsed : true,
				unique : true
			});
		} else {
			$("#region_window").dialog('open');
		}
	}
}

function refreshCaptcha() {
	$('img.captcha').each(function() {
		var src = this.src;
		if (src.lastIndexOf('?') > 0)
			src = src.substring(0, src.lastIndexOf('?'));
		this.src = src + '?' + Math.random();
	});
}
