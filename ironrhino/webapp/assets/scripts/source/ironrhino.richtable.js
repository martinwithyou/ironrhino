Richtable = {
	getBaseUrl : function(form) {
		form = form || $('form.richtable');
		var action = form.attr('action');
		var entity = form.data('entity');
		var url;
		if (action.indexOf('/') == 0 || action.indexOf('://') > 0)
			url = entity ? action.substring(0, action.lastIndexOf('/') + 1)
					+ entity : action;
		else
			url = entity ? entity : form.attr('action');
		if (form.data('actionbaseurl'))
			url = CONTEXT_PATH + form.data('actionbaseurl');
		var p = url.indexOf('?');
		if (p > 0)
			url = url.substring(0, p);
		p = url.indexOf(';');
		if (p > 0)
			url = url.substring(0, p);
		if (url.indexOf('/') != 0 && url.indexOf('://') < 0) {
			var hash = document.location.hash;
			if (hash.indexOf('!') == 1)
				url = CONTEXT_PATH
						+ hash.substring(2, hash.lastIndexOf('/') + 1) + url;
		}
		return url;
	},
	getPathParams : function() {
		var url = document.location.href;
		var p = url.indexOf('?');
		if (p > 0)
			url = url.substring(0, p);
		p = url.indexOf(';');
		if (p > 0)
			return url.substring(p);
		else
			return '';
	},
	getUrl : function(type, id, includeParams, form) {
		form = form || $('form.richtable');
		var url = Richtable.getBaseUrl(form) + '/' + type
				+ Richtable.getPathParams();
		if (id) {
			url += (url.indexOf('?') > 0 ? '&' : '?');
			if (typeof id == 'string') {
				url += 'id=' + id;
			} else {
				var ids = [];
				for (var i = 0; i < id.length; i++)
					ids.push('id=' + id[i]);
				url += ids.join('&');
			}
		}
		var data = $('input[type="hidden"],input[name="keyword"]', form)
				.serialize();
		if (includeParams && data)
			url += (url.indexOf('?') > 0 ? '&' : '?') + data;
		return url;
	},
	reload : function(form, pushstate) {
		form = form || $('form.richtable');
		if (form.parents('.ui-dialog,.tab-content').length)
			pushstate = false;
		if (pushstate && typeof history.pushState != 'undefined') {
			var url = form.attr('action');
			var params = form.serializeArray();
			if (params) {
				$.map(params, function(v, i) {
							if (v.name == 'resultPage.pageNo')
								v.name = 'pn';
							else if (v.name == 'resultPage.pageSize')
								v.name = 'ps';
							else if (v.name == 'check') {
								v.name = '';
								v.value = '';
							} else if (v.name == 'keyword' && !v.value) {
								v.name = '';
								v.value = '';
							}
						});
				var param = $.param(params).replace(/(&=)/g, '');
				if (param)
					url += (url.indexOf('?') > 0 ? '&' : '?') + param;
			}
			var location = document.location.href;
			history.replaceState({
						url : location
					}, '', location);
			history.pushState(url, '', url);
		}
		$(form).submit();
	},
	open : function(url, reloadonclose, useiframe, form) {
		form = form || $('form.richtable');
		reloadonclose = reloadonclose || false;
		useiframe = useiframe || false;
		var win = $('#_window_');
		if (!win.length)
			win = $('<div id="_window_"></div>').appendTo(document.body)
					.dialog();
		if (!useiframe) {
			// ajax replace
			var target = win.get(0);
			target.onsuccess = function() {
				if (typeof $.fn.mask != 'undefined')
					win.unmask();
				Dialog.adapt(win);
				if (url.indexOf('?') > 0)
					url = url.substring(0, url.indexOf('?'));
				var pathname = document.location.pathname;
				var hash = document.location.hash;
				if (hash.indexOf('!') == 1)
					pathname = CONTEXT_PATH + hash.substring(2);
				$('#_window_ form.ajax').each(function() {
					var inputform = $(this);
					$(':input:visible', inputform).filter(function(i) {
						return $(this).attr('name')
								&& !($(this).val() || $(this).hasClass('date') || $(this)
										.prop('tagName') == 'BUTTON');
					}).eq(0).focus();
					if (!inputform.hasClass('keepopen')) {
						$(':input', inputform).change(function(e) {
									if (!inputform.hasClass('nodirty'))
										inputform.addClass('dirty');
								});
						$(inputform).addClass('dontreload');
						var create = url.lastIndexOf('input') == url.length - 5;
						if (create) {
							if ($('input[type="hidden"][name="id"]', inputform)
									.val())
								create = false;
							if ($(
									'input[type="hidden"][name="'
											+ (form.data('entity') || form
													.attr('action')) + '.id"]',
									inputform).val())
								create = false;
						}
						if (create) {
							$('button[type="submit"]', inputform)
									.addClass('btn-primary')
									.after(' <button type="submit" class="btn save_and_create">'
											+ MessageBundle
													.get('save.and.create')
											+ '</button>');
							$('.save_and_create', inputform).click(function() {
										$('form.ajax').addClass('reset');
									});
						}
					}
					var action = inputform.attr('action');
					if (action.indexOf('http') != 0 && action.indexOf('/') != 0) {
						action = pathname
								+ (pathname.indexOf('/') == (pathname.length - 1)
										? ''
										: '/') + action;
						inputform.attr('action', action);
					}
					if (inputform.hasClass('view')
							&& !(inputform.data('replacement')))
						inputform.data('replacement', '_window_:content');
					if (!inputform.hasClass('view')
							&& !inputform.hasClass('keepopen')) {
						$('button[type=submit]', inputform).click(function(e) {
							inputform[0].onsuccess = function() {
								$(this).removeClass('dirty');
								$(this).removeClass('dontreload');
								if (!$(e.target).closest('button')
										.hasClass('save_and_create'))
									// setTimeout(function() {
									$('#_window_').dialog('close');
								// }, 1000);

							};
						});
					}
				});
				$('#_window_ a').each(function() {
					var href = $(this).attr('href');
					if (href && href.indexOf('http') != 0
							&& href.indexOf('/') != 0
							&& href.indexOf('javascript:') != 0) {
						href = pathname
								+ (pathname.indexOf('/') == (pathname.length - 1)
										? ''
										: '/') + href;
						this.href = href;
					}
				});
			};
			ajax({
						url : url,
						data : {
							'decorator' : 'simple'
						},
						cache : false,
						target : target,
						replacement : '_window_:content',
						quiet : true
					});
		} else {
			// embed iframe
			win.html('<iframe style="width:100%;height:550px;border:0;"/>');
			url += (url.indexOf('?') > 0 ? '&' : '?') + 'decorator=simple&'
					+ Math.random();
			var iframe = $('#_window_ > iframe')[0];
			iframe.src = url;
			iframe.onload = function() {
				Dialog.adapt(win, iframe);
			}
		}
		if (!useiframe)
			if (win.html() && typeof $.fn.mask != 'undefined')
				win.mask(MessageBundle.get('ajax.loading'));
			else
				win.html('<div style="text-align:center;">'
						+ MessageBundle.get('ajax.loading') + '</div>');
		var opt = {
			minHeight : 600,
			width : 700,
			// modal : true,
			closeOnEscape : false,
			close : function() {
				if (reloadonclose
						&& ($('#_window_ form.ajax').hasClass('forcereload') || !$('#_window_ form.ajax')
								.hasClass('dontreload')))
					Richtable.reload(form);
				win.html('').dialog('destroy').remove();
			},
			beforeClose : function(event, ui) {
				if ($('form', win).hasClass('dirty')) {
					return confirm(MessageBundle.get('confirm.exit'));
				}
			}
		};
		if ($.browser.msie && $.browser.version <= 8)
			opt.height = 600;
		win.dialog(opt);
		win.dialog('open');
		win.closest('.ui-dialog').css('z-index', '2000');
		$('.ui-dialog-titlebar-close', win.closest('.ui-dialog')).blur();
	},
	enter : function(event) {
		var btn = event.target;
		var form = $(btn).closest('form');
		if ($(btn).prop('tagName') != 'BUTTON' || $(btn).prop('tagName') != 'A')
			btn = $(btn).closest('.btn');
		if (btn.attr('onclick') || btn.hasClass('raw'))
			return;
		var url = Richtable.getBaseUrl(form) + Richtable.getPathParams();
		var tr = $(btn).closest('tr');
		var id = tr.data('rowid')
				|| $('input[type="checkbox"]:eq(0)', tr).val();
		url += (url.indexOf('?') > 0 ? '&' : '?') + 'parentId=' + id;
		document.location.href = url;
	},
	click : function(event) {
		var btn = event.target;
		var form = $(btn).closest('form');
		if ($(btn).prop('tagName') != 'BUTTON' || $(btn).prop('tagName') != 'A')
			btn = $(btn).closest('button,a');
		if (btn.attr('onclick') || btn.hasClass('raw'))
			return;
		var idparams;
		var tr = $(btn).closest('tr');
		var id = tr.data('rowid')
				|| $('input[type="checkbox"]:eq(0)', tr).val();
		if (id) {
			idparams = 'id=' + id;
		} else {
			var arr = [];
			$('form.richtable tbody input[type="checkbox"]').each(function() {
				if (this.checked) {
					var _id = $(this).closest('tr').data('rowid') || this.value;
					arr.push('id=' + _id);
				}
			});
			idparams = arr.join('&');
		}
		var action = $(btn).data('action');
		var view = $(btn).data('view');
		if (action == 'reload')
			Richtable.reload(form);
		else if (action == 'enter')
			Richtable.enter(event);
		else if (action == 'save')
			Richtable.save(event);
		else if (action) {
			if (!idparams) {
				Message.showMessage('no.selection');
				return false;
			}
			if (action == 'delete') {
				$.alerts.confirm(MessageBundle.get('confirm.delete'),
						MessageBundle.get('select'), function(b) {
							if (b) {
								var url = Richtable.getBaseUrl(form) + '/'
										+ action + Richtable.getPathParams();
								url += (url.indexOf('?') > 0 ? '&' : '?')
										+ idparams;
								ajax({
											url : url,
											type : 'POST',
											dataType : 'json',
											success : function() {
												Richtable.reload(form)
											}
										});
							}
						});
			} else {
				var url = Richtable.getBaseUrl(form) + '/' + action
						+ Richtable.getPathParams();
				url += (url.indexOf('?') > 0 ? '&' : '?') + idparams;
				ajax({
							url : url,
							type : 'POST',
							dataType : 'json',
							success : function() {
								Richtable.reload(form)
							}
						});
			}
		} else {
			var options = (new Function("return "
					+ ($(btn).data('windowoptions') || '{}')))();
			var url = $(btn).attr('href');
			if (view) {
				url = Richtable.getUrl(view, id, !id || options.includeParams,
						form);
			} else {
				if (!$(btn).hasClass('noid')) {
					if (!id) {
						// from bottom
						if (!idparams) {
							Message.showMessage('no.selection');
							return false;
						}
						if (!url)
							return true;
						url += (url.indexOf('?') > 0 ? '&' : '?') + idparams;
					}
				}
			}
			var reloadonclose = typeof(options.reloadonclose) == 'undefined'
					? (view != 'view')
					: options.reloadonclose;
			Richtable.open(url, reloadonclose, options.iframe, form);
			delete options.iframe;
			delete options.reloadonclose;
			for (var key in options)
				$('#_window_').dialog('option', key, options[key]);
			Dialog.adapt($('#_window_'));
			return false;
		}
	},
	save : function(event) {
		var form = $(event.target).closest('form');
		var modified = false;
		var theadCells = $('.richtable thead:eq(0) th');
		$.each($('.richtable tbody')[0].rows, function() {
			var row = this;
			if ($('td.edited', row).length) {
				modified = true;
				var params = {};
				var entity = form.data('entity') || form.attr('action');
				params[entity + '.id'] = $(this).data('rowid')
						|| $('input[type="checkbox"]:eq(0)', this).val();;
				$.each(row.cells, function(i) {
							var theadCell = $(theadCells[i]);
							var name = theadCell.data('cellname');
							if (!name || !$(this).hasClass('edited')
									&& theadCell.hasClass('excludeIfNotEdited'))
								return;
							var value = $(this).data('cellvalue')
									|| $(this).text();
							params[name] = value;
						});
				var url = Richtable.getBaseUrl(form) + '/save'
						+ Richtable.getPathParams();
				ajax({
							url : url,
							type : 'POST',
							data : params,
							dataType : 'json',
							headers : {
								'X-Edit' : 'cell'
							},
							onsuccess : function() {
								$('td', row).removeClass('edited')
										.removeData('oldvalue');
								$('.btn[data-action="save"]', form)
										.removeClass('btn-primary').hide();
							}
						});
			}
		});
		if (!modified) {
			Message.showMessage('no.modification');
			return false;
		}
	},
	editCell : function(cell, type, templateId) {
		var cell = $(cell);
		if (cell.hasClass('editing'))
			return;
		var value = cell.data('cellvalue');
		if (value === undefined)
			value = $.trim(cell.text());
		else
			value = '' + value;
		if (cell.data('oldvalue') === undefined)
			cell.data('oldvalue', value);
		cell.addClass('editing');
		var template = '';
		if (templateId) {
			template = $('#' + templateId).text();
		} else {
			if (type == 'textarea') {
				template = '<textarea type="text" class="text"/>';
			} else if (type == 'date')
				template = '<input type="text" class="text date"/>';
			else if (type == 'boolean')
				template = '<select><option value="true">'
						+ MessageBundle.get('true')
						+ '</option><option value="false">'
						+ MessageBundle.get('false') + '</option></select>';
			else
				template = '<input type="text" class="text"/>';
		}
		cell.html(template);
		$(':input', cell).blur(function() {
					if (!$(this).hasClass('date'))
						Richtable.updateCell(this);
				});
		var select = $('select', cell);
		if (value != undefined && select.length) {
			var arr = $('option', select).toArray();
			for (var i = 0; i < arr.length; i++) {
				if (arr[i].value == value || $(arr[i]).text() == value) {
					$(arr[i]).prop('selected', true);
					break;
				}
			};
			select.focus();
		} else {
			$('input.date', cell).datepicker({
						dateFormat : 'yy-mm-dd',
						onClose : function() {
							Richtable.updateCell(this)
						}
					});
			$(':input', cell).val(value).focus();
		}
	},
	updateCell : function(cellEdit) {
		var ce = $(cellEdit);
		var cell = ce.parent();
		var value = ce.val();
		var label = value;
		var editType = ce.prop('tagName');
		if (editType == 'SELECT')
			label = $('option:selected', ce).text();
		else if (editType == 'CHECKBOX' || editType == 'RADIO')
			label = ce.next().text();
		Richtable.updateValue(cell, value, label);
	},
	updateValue : function(cell, value, label) {
		if (cell.data('oldvalue') === undefined)
			cell.data('oldvalue', '' + cell.data('cellvalue'));
		cell.removeClass('editing');
		cell.data('cellvalue', value);
		if (typeof label != 'undefined')
			cell.text(label);
		if (cell.data('oldvalue') != cell.data('cellvalue'))
			cell.addClass('edited');
		else
			cell.removeClass('edited');
		var savebtn = $('.btn[data-action="save"]', cell.closest('form'));
		$('td.edited', cell.closest('form')).length ? savebtn
				.addClass('btn-primary').show() : savebtn
				.removeClass('btn-primary').hide();
	},
	enhance : function(table) {
		var t = $(table);
		var theadCells = $('thead:eq(0) th', t);
		$('tbody:eq(0) tr', t).each(function() {
					var cells = this.cells;
					if (!$(this).data('readonly'))
						theadCells.each(function(i) {
									var cellEdit = $(this).data('celledit');
									if (!cellEdit)
										return;
									var ar = cellEdit.split(',');
									$(cells[i]).unbind(ar[0]).bind(ar[0],
											function() {
												Richtable.editCell(this, ar[1],
														ar[2]);
											});
								});
				});

		var need = false;
		var classes = {};
		$('th', t).each(function(i) {
					var arr = [];
					var tt = $(this);
					var cls = tt.attr('class');
					if (cls)
						$.each(cls.split(/\s+/), function(i, v) {
									if (v.indexOf('hidden-') == 0)
										arr.push(v);
								});
					if (arr.length) {
						need = true;
						classes['' + i] = arr;
					}
				});
		$('tbody tr', t).each(function() {
					$('td', $(this)).each(function(i) {
								var arr = classes['' + i];
								var tt = $(this);
								if (arr) {
									$.each(arr, function(i, v) {
												tt.addClass(v);
											});
								}
							});
				});
	}
};
Initialization.richtable = function() {
	$(document).on('click',
			'.richtable .action button.btn,form.richtable a[rel="richtable"]',
			Richtable.click).on('click',
			'.richtable .firstPage:not(.disabled) a', function(event) {
				var form = $(this).closest('form');
				$('.inputPage', form).val(1);
				Richtable.reload(form, true);
				return false;
			}).on('click', '.richtable .prevPage:not(.disabled) a',
			function(event) {
				var form = $(this).closest('form');
				$('.inputPage', form).val(function(i, v) {
							return parseInt(v) - 1
						});
				Richtable.reload(form, true);
				return false;
			}).on('click', '.richtable .nextPage:not(.disabled) a',
			function(event) {
				var form = $(this).closest('form');
				$('.inputPage', form).val(function(i, v) {
							return parseInt(v) + 1
						});
				Richtable.reload(form, true);
				return false;
			}).on('click', '.richtable .lastPage:not(.disabled) a',
			function(event) {
				var form = $(this).closest('form');
				$('.inputPage', form).val($('.totalPage strong', form).text());
				Richtable.reload(form, true);
				return false;
			}).on('change', '.richtable .inputPage', function(event) {
				var form = $(event.target).closest('form');
				Richtable.reload(form, true);
				event.preventDefault();
			}).on('change', '.richtable select.pageSize', function(event) {
				var form = $(event.target).closest('form');
				$('.inputPage', form).val(1);
				Richtable.reload(form, true);
			}).on('keydown', '.richtable input[name="keyword"]',
			function(event) {
				var form = $(event.target).closest('form');
				if (event.keyCode == 13) {
					$('.inputPage', form).val(1);
					Richtable.reload(form, true);
					return false;
				}
			}).on('click', '.richtable .icon-search', function(event) {
				var form = $(event.target).closest('form');
				$('.inputPage', form).val(1);
				Richtable.reload(form, true);
				return false;
			}).on('click', '.richtable .more', function(event) {
		var form = $(event.target).closest('form');
		if (!$('li.nextPage', form).length)
			return;
		$('.inputPage', form).val(function(i, v) {
					return parseInt(v) + 1
				});
		$.ajax({
			url : $(form).attr('action'),
			type : $(form).attr('method'),
			data : form.serialize(),
			success : function(data) {
				var html = data.replace(/<script(.|\s)*?\/script>/g, '');
				var div = $('<div/>').html(html);
				var append = false;
				$('table.richtable tbody:eq(0) tr', div).each(function(i, v) {
					if (!append) {
						var id = $(v).data('rowid')
								|| $(
										'input[type="checkbox"],input[type="radio"]',
										v).prop('value');
						if (id) {
							var rows = $('table.richtable tbody:eq(0) tr', form);
							var exists = false;
							for (var i = rows.length - 1; i >= 0; i--) {
								if (($(rows[i]).data('rowid') || $(
										'input[type="checkbox"],input[type="radio"]',
										rows[i]).prop('value')) == id) {
									exists = true;
									break;
								}
							}
							if (!exists)
								append = true;
						} else {
							append = true;
						}
					}
					if (append) {
						$(v).appendTo($('table.richtable tbody', form));
						_observe(v);
					}
				});
				if (append)
					Richtable.enhance($('table.richtable', form));
				$('.pageSize', form)
						.val($('table.richtable tbody tr', form).length);
				$('div.pagination', form).replaceWith($('div.pagination', div));
				$('div.pagination ul', form).hide();
				$('div.status', form).replaceWith($('div.status', div));
			}
		});

	});
}
Observation.richtable = function(container) {
	$('table.richtable', container)
			.on(
					'change',
					'input[type="checkbox"][name="check"],input[type="checkbox"]:not([name])',
					function() {
						var checked = 0;
						if ($(this).attr('name') === undefined) {
							checked = this.checked
									? $('tbody tr', $(this)
													.closest('table.richtable')).length
									: 0;
						} else {
							$('tbody tr', $(this).closest('table.richtable'))
									.each(function() {
										if ($(
												'td:eq(0) input[type="checkbox"]',
												this).is(':checked'))
											checked++;
									});
						}
						var form = $(this).closest('form.richtable');
						var selected = $(
								'.toolbar .btn[data-shown="selected"]', form);
						var singleselected = $(
								'.toolbar .btn[data-shown="singleselected"]',
								form);
						var multiselected = $(
								'.toolbar .btn[data-shown="multiselected"]',
								form);
						checked > 0
								? selected.addClass('btn-primary').show()
								: selected.removeClass('btn-primary').hide();
						checked == 1 ? singleselected.addClass('btn-primary')
								.show() : singleselected
								.removeClass('btn-primary').hide();
						checked > 1 ? multiselected.addClass('btn-primary')
								.show() : multiselected
								.removeClass('btn-primary').hide();
					});
	$('table.richtable', container).each(function() {
				Richtable.enhance(this);
			});
};