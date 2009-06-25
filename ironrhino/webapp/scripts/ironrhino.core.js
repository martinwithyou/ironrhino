var HISTORY_ENABLED = true;

if (typeof (Initialization) == 'undefined')
	Initialization = {};
if (typeof (Observation) == 'undefined')
	Observation = {};
var CONTEXT_PATH = '';

function _init() {
	CONTEXT_PATH = $('meta[name="context_path"]').attr('content') || '';
	if (CONTEXT_PATH == '/')
		CONTEXT_PATH = '';

	var array = [];

	for ( var key in Initialization) {
		if (typeof (Initialization[key]) == 'function')
			array.push(key);
	}
	array.sort();
	for ( var i = 0; i < array.length; i++)
		Initialization[array[i]].call(this);
	_observe();
}
function _observe(container) {
	var array = [];
	for ( var key in Observation) {
		if (typeof (Observation[key]) == 'function')
			array.push(key);
	}
	array.sort();
	for ( var i = 0; i < array.length; i++)
		Observation[array[i]].call(this, container);
}
$(_init);

Observation.common = function(container) {
	$('input.autocomplete_off').attr('autocomplete', 'off');
	$('ul.nav>li', container).hover( function() {
		$("ul", this).fadeIn("fast");
	}, function() {
	});
	if ($.browser.msie) {
		$('ul.nav>li', container).each( function() {
			if ($('ul>li', this).length > 0)
				$(this).hover( function() {
					$(this).addClass("sfHover");
				}, function() {
					$(this).removeClass("sfHover");
				})
		});
	}
	$('div.tabs', container).each( function() {
		$(this).tabs().tabs('select', $(this).attr('tab'))
	});
	$('.round_corner,.corner', container).css( {
		padding :'5px',
		margin :'5px'
	}).each( function() {
		$(this).corner();
	});
	if (typeof $.datepicker != 'undefined')
		$('input.date', container).datepicker( {
			dateFormat :'yy-mm-dd'
		});
	$('input.captcha', container)
			.focus(
					function() {
						if ($(this).attr('_captcha_'))
							return;
						$(this)
								.after(
										'<img class="captcha" src="' + CONTEXT_PATH + '/captcha.jpg"/>');
						$('img.captcha', container).click(Captcha.refresh);
						$(this).attr('_captcha_', true);
					});
	if (typeof $.EmptyOnClick != 'undefined')
		$('input.emptyonclick, textarea.emptyonclick').emptyonclick();
	$('.action_error,.action_message,.field_error').prepend('<div class="close" onclick="$(this.parentNode).remove()"></div>');
};

Initialization.common = function() {
	if ($.browser.msie)
		window.attachEvent('onunload', function() {
			CollectGarbage()
		});
	if (typeof dwr != 'undefined') {
		dwr.engine.setPreHook(Indicator.show);
		dwr.engine.setPostHook(Indicator.hide);
	}
	$().ajaxStart( function() {
		Indicator.show()
	});
	$().ajaxError( function() {
		Indicator.showError()
	});
	$().ajaxSuccess( function(ev, xhr) {
		Indicator.hide();
		var url = xhr.getResponseHeader("X-Redirect-To");
		if (url) {
			top.location.href = url;
			return;
		}
	});
	if ($('#q').length > 0)
		$("#q").autocomplete(CONTEXT_PATH + "/search/suggest?decorator=none", {
			minChars :3,
			delay :1000
		});
};

MessageBundle = {
	'en' : {
		'indicator.loading' :'loading...',
		'indicator.error' :'network error,please try later',
		'required' :'please input value',
		'selection.required' :'please select',
		'email' :'this field must be a valid email',
		'integer' :'this field must be integer',
		'double' :'this field must be double',
		'confirm.delete' :'are you sure to delete?',
		'save.and.create' :'save and add',
		'add' :'add',
		'remove' :'remove'
	},
	'zh-cn' : {
		'indicator.loading' :'正在加载...',
		'indicator.error' :'网络故障,请稍后再试',
		'required' :'必填项,请填写',
		'selection.required' :'必填项,请选择',
		'email' :'email不合法',
		'integer' :'请填写整数',
		'double' :'请填写数字',
		'confirm.delete' :'确定要删除?',
		'save.and.create' :'保存并新建',
		'add' :'添加',
		'remove' :'删除'
	},
	get : function() {
		var key = arguments[0];
		var lang = (navigator.language || navigator.browserLanguage || '')
				.toLowerCase();
		if (!MessageBundle[lang])
			lang = 'en';
		var msg = MessageBundle[lang][key];
		if (typeof (msg) == 'undefined')
			msg = key;
		for ( var i = 1; i < arguments.length; i++)
			msg = msg.replace('{' + i + '}', arguments[i]);
		return msg;
	}
};

Indicator = {
	text :'',
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
};

Message = {
	get : function(message, className) {
		return '<div class="'
				+ className
				+ '"><div class="close" onclick="$(this.parentNode).remove()"></div>'
				+ message + '</div>';
	},
	showError : function(field, errorType) {
		$(field).parent().append(
				Message.get(MessageBundle.get(errorType), 'field_error'));
	}
};

Form = {
	focus : function(form) {
		var arr = $('input,select', form).get();
		for ( var i = 0; i < arr.length; i++) {
			if ($('.field_error', $(arr[i]).parent()).size() > 0) {
				setTimeout( function() {
					$(arr[i]).focus();
				}, 1000);
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
					&& !$(target).val().match(
							/^\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$/)) {
				Message.showError(target, 'email');
				return false;
			} else if ($(target).hasClass('integer') && $(target).val()
					&& !$(target).val().match(/^[-+]?\d*$/)) {
				Message.showError(target, 'integer');
				return false;
			} else if ($(target).hasClass('double') && $(target).val()
					&& !$(target).val().match(/^[-\+]?\d+(\.\d+)?$/)) {
				Message.showError(target, 'double');
				return false;
			} else {
				return true;
			}
		} else {
			var valid = true;
			$('input,select', target).each( function() {
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
		var ret;
		if (typeof (func) == 'function') {
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
	},
	handleResponse : function(data, options) {
		var hasError = false;
		var target = options.target;
		if ((typeof data == 'string')
				&& (data.indexOf('{') == 0 || data.indexOf('[') == 0))
			data = eval('(' + data + ')');
		if (typeof data == 'string') {
			var replacement = {};
			var entries = (options.replacement || $(target).attr('replacement') || 'content')
					.split(',');
			for ( var i = 0; i < entries.length; i++) {
				var entry = entries[i];
				var ss = entry.split(':', 2);
				replacement[ss[0]] = (ss.length == 2 ? ss[1] : ss[0]);
			}
			var div = $("<div/>").append(
					data.replace(/<script(.|\s)*?\/script>/g, ""));
			// others
			for ( var key in replacement) {
				if (!options.silence)
					$('html,body').animate( {
						scrollTop :$('#' + key).offset().top
					}, 100);
				$('#' + key).html(div.find('#' + replacement[key]).html());
				if (!options.silence && (typeof $.effects != 'undefined'))
					$('#' + key).effect('highlight');
				_observe($('#' + key));
			}
			div.remove();
			Ajax.fire(target, 'onsuccess');
		} else {
			window._jsonResult_ = data;
			if (data.fieldErrors || data.actionErrors) {
				hasError = true;
				Ajax.fire(target, 'onerror');
			} else {
				Ajax.fire(target, 'onsuccess');
			}
			var message = '';
			if (data.fieldErrors)
				for (key in data.fieldErrors)
					if (target && target[key])
						$(target[key]).parent().append(
								$(Message.get(data.fieldErrors[key],
										'field_error')));
					else
						message += Message.get(data.fieldErrors[key],
								'action_error');
			if (data.fieldErrors)
				Form.focus(target);
			if (data.actionErrors)
				for ( var i = 0; i < data.actionErrors.length; i++)
					message += Message
							.get(data.actionErrors[i], 'action_error');
			if (data.actionMessages)
				for ( var i = 0; i < data.actionMessages.length; i++)
					message += Message.get(data.actionMessages[i],
							'action_message');
			if (message)
				if (target && target.tagName.toLowerCase() == 'form') {
					if ($('#' + target.id + '_message').length == 0)
						$(target).before(
								'<div id="' + target.id + '_message"></div>');
					$('#' + target.id + '_message').html(message);
				} else {
					if ($('#message').length == 0)
						$('<div id="message"></div>').prependTo(document.body);
					$('#message').html(message);
				}
		}
		if (target && target.tagName.toLowerCase() == 'form') {
			setTimeout( function() {
				$('button[type="submit"]', target).removeAttr('disabled');
				Captcha.refresh()
			}, 100);
			if (!hasError && $(target).hasClass('reset')) {
				if (typeof target.reset == 'function'
						|| (typeof target.reset == 'object' && !target.reset.nodeType))
					target.reset();
			}
		}
		Indicator.text = '';
		Ajax.fire(target, 'oncomplete');
	}
};

function ajax(options) {
	if (!options.data)
		options.data = {};
	$.extend(options.data, {
		_transport_type_ :'XMLHttpRequest',
		_include_query_string_ :'true',
		_result_type_ :options.dataType != 'json' ? '' : 'json'
	});
	$.extend(options, {
		beforeSend : function() {
			Indicator.text = options.indicator;
			Ajax.fire(null, options.onloading);
		},
		success : function(data) {
			Ajax.handleResponse(data, options)
		}
	});
	$.ajax(options);
}

var _init_content_ = '';
var _init_ = true;
Initialization.history = function() {
	if (HISTORY_ENABLED && (typeof $.history != 'undefined')) {
		_init_content_ = $('#content').html();
		$.history.init( function(hash) {
			if (hash && hash != '#') {
				if ($.browser.mozilla && hash.indexOf('%') == 0)
					return;
				hash = decodeURIComponent(hash);
				_init_ = false;
				if (CONTEXT_PATH)
					hash = CONTEXT_PATH + hash;
				ajax( {
					url :hash,
					cache :true,
					success :Ajax.handleResponse
				});
			} else if (!hash || hash == '#') {
				if (_init_content_ && !_init_) {
					$('#content').html(_init_content_);
					_observe($('#content'));
				}
			}
		});
	}
};

Observation.ajax = function(container) {
	$('a.ajax,form.ajax', container)
			.each(
					function() {
						var target = this;
						try {
							var _options = $(target).attr('options') ? eval('(' + $(
									target).attr('options') + ')')
									: null;
							if (_options)
								$.each(_options, function(key, value) {
									$(target).attr(key, value);
								});
						} catch (e) {
						}

						if (this.tagName.toLowerCase() == 'form') {
							var options = {
								data : {
									_transport_type_ :'XMLHttpRequest',
									_include_query_string_ :'true',
									_result_type_ :$(this).hasClass('view') ? ''
											: 'json'
								},
								beforeSubmit : function() {
									if (!Ajax.fire(target, 'onprepare'))
										return false;
									$('.action_message,.action_error').remove();
									$('.field_error', target).remove();
									if (!Form.validate(target))
										return false;
									Indicator.text = $(target)
											.attr('indicator');
									$('button[type="submit"]', target).attr(
											'disabled', true);
									Ajax.fire(target, 'onloading');
								},
								error : function() {
									Form.focus(target);
									if (target
											&& target.tagName.toLowerCase() == 'form')
										setTimeout( function() {
											$('button[type="submit"]', target)
													.removeAttr('disabled');
										}, 100);
									Ajax.fire(target, 'onerror');
								},
								success : function(data) {
									Ajax.handleResponse(data, {
										'target' :target
									});
								}
							};
							$(this).bind('submit', function() {
								$(this).ajaxSubmit(options);
								return false;
							});
							$('input,select', this).blur( function() {
								Form.validate(this)
							});
							return;
						} else {
							$(this)
									.click(
											function() {
												if (HISTORY_ENABLED
														&& $(this).hasClass(
																'view')
														&& !$(this).attr(
																'replacement')) {
													var hash = this.href;
													hash = hash.substring(hash
															.indexOf('//') + 2);
													hash = hash.substring(hash
															.indexOf('/'));
													if (CONTEXT_PATH)
														hash = hash
																.substring(CONTEXT_PATH.length);
													hash = encodeURIComponent(hash);
													$.history.load(hash);
													return false;
												}
												if (!Ajax.fire(target,
														'onprepare'))
													return false;
												$
														.ajax( {
															url :this.href,
															type :$(this).attr(
																	'method') || 'GET',
															data : {
																_transport_type_ :'XMLHttpRequest',
																_include_query_string_ :'true',
																_result_type_ :$(
																		this)
																		.hasClass(
																				'view') ? ''
																		: 'json'
															},
															cache :$(this)
																	.hasClass(
																			'cache'),
															beforeSend : function() {
																$(
																		'.action_message,.action_error')
																		.remove();
																Indicator.text = $(
																		target)
																		.attr(
																				'indicator');
																Ajax
																		.fire(
																				target,
																				'onloading');
															},
															error : function() {
																Ajax
																		.fire(
																				target,
																				'onerror');
															},
															success : function(
																	data) {
																Ajax
																		.handleResponse(
																				data,
																				{
																					'target' :target
																				})
															}
														});
												return false;
											});
						}
					});
};

var Region = {
	textid :'',
	isfull :'',
	hiddenid :'',
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
			$(
					'<div id="region_window" class="flora" title="请选择"><div id="region_tree"></div></div>')
					.appendTo(document.body);
			$("#region_window").dialog( {
				width :350,
				height :600
			});
			$("#region_tree").treeview( {
				url :CONTEXT_PATH + '/region/children',
				click :_click,
				collapsed :true,
				unique :true
			});
		} else {
			$("#region_window").dialog('open');
		}
	}
};

Captcha = {
	refresh : function() {
		$('img.captcha').each( function() {
			var src = this.src;
			if (src.lastIndexOf('?') > 0)
				src = src.substring(0, src.lastIndexOf('?'));
			this.src = src + '?' + Math.random();
		});
		$('input.captcha').val('');
	}
};
