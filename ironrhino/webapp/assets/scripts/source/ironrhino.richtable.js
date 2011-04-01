ECSideUtil = {
	Dragobj : null,
	DragobjSibling : null,
	DragobjBodyCell : null,
	DragobjBodyCellSibling : null,
	getPosLeft : function(elm) {
		var left = elm.offsetLeft;
		while ((elm = elm.offsetParent) != null) {
			left += elm.offsetLeft;
		}
		return left;
	},
	getPosRight : function(elm) {
		return ECSideUtil.getPosLeft(elm) + elm.offsetWidth;
	},
	StartResize : function(event) {
		event.preventDefault();
		var obj = event.target;
		obj.focus();
		document.body.style.cursor = 'e-resize';
		var sibling = $(obj.parentNode).next()[0];
		var dx = event.screenX;
		obj.parentTdW = obj.parentNode.clientWidth;
		obj.siblingW = sibling.clientWidth;
		obj.mouseDownX = dx;
		obj.totalWidth = obj.siblingW + obj.parentTdW;
		obj.oldSiblingRight = ECSideUtil.getPosRight(sibling);
		ECSideUtil.Dragobj = obj;
		ECSideUtil.DragobjSibling = sibling;
		ECSideUtil.MinColWidth = $(obj).closest('table').attr('minColWidth')
				|| '40';
		ECSideUtil.Dragobj.style.backgroundColor = '#888';
		ECSideUtil.Dragobj.parentTdW -= ECSideUtil.Dragobj.mouseDownX;
		var cellIndex = ECSideUtil.Dragobj.parentNode.cellIndex;
		try {
			ECSideUtil.DragobjBodyCell = $('tbody', $(obj).closest('table'))[0].rows[0].cells[cellIndex];
			ECSideUtil.DragobjBodyCellSibling = $(ECSideUtil.DragobjBodyCell)
					.next()[0];
		} catch (e) {
			ECSideUtil.DragobjBodyCell = null;
		}
		return false;
	},
	DoResize : function(event) {
		if (ECSideUtil.Dragobj == null) {
			return true;
		}
		event.preventDefault();
		if (!ECSideUtil.Dragobj.mouseDownX) {
			return false;
		}
		document.body.style.cursor = 'e-resize';
		var dx = event.screenX;
		var newWidth = ECSideUtil.Dragobj.parentTdW + dx;
		var newSiblingWidth = 0;
		/* fix different from ie to ff . but I don't know why */
		if ($.browser.msie) {
			newSiblingWidth = ECSideUtil.Dragobj.totalWidth - newWidth - 1;
		} else {
			newSiblingWidth = ECSideUtil.Dragobj.totalWidth - newWidth - 21;
		}
		if (newWidth > ECSideUtil.MinColWidth
				&& newSiblingWidth > ECSideUtil.MinColWidth) {
			ECSideUtil.Dragobj.parentNode.style.width = newWidth + 'px';
			ECSideUtil.DragobjSibling.style.width = newSiblingWidth + 'px';
			try {
				ECSideUtil.DragobjBodyCell.style.width = newWidth + 'px';
				ECSideUtil.DragobjBodyCellSibling.style.width = newSiblingWidth
						+ 'px';
				ECSideUtil.DragobjBodyCell.width = newWidth + 'px';
				ECSideUtil.DragobjBodyCellSibling.width = newSiblingWidth
						+ 'px';
			} catch (e) {
			}
		}
		return false;
	},
	EndResize : function(event) {
		if (ECSideUtil.Dragobj != null) {
			event.preventDefault();
			ECSideUtil.Dragobj.mouseDownX = 0;
			document.body.style.cursor = '';
			ECSideUtil.Dragobj.style.backgroundColor = '';
			ECSideUtil.Dragobj = null;
			ECSideUtil.DragobjSibling = null;
			return false;
		}
		return true;
	}
};

Richtable = {
	getBaseUrl : function(form) {
		form = form || $('form.richtable');
		var entity = form.attr('entity');
		var url = entity ? entity : form[0].action;
		var p = url.indexOf('?');
		if (p > 0)
			url = url.substring(0, p);
		p = url.indexOf(';');
		if (p > 0)
			url = url.substring(0, p);
		if (url.indexOf('/') != 0) {
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
		if (includeParams && document.location.search)
			url += (url.indexOf('?') > 0 ? '&' : '?')
					+ document.location.search.substring(1);
		return url;
	},
	reload : function(form, dontpushstate) {
		form = form || $('form.richtable');
		if (!dontpushstate && typeof history.pushState != 'undefined') {
			var url = form.attr('action');
			var param = form.serialize();
			if (param) {
				param = param.replace('resultPage.pageNo', 'pn');
				param = param.replace('resultPage.pageSize', 'ps');
				param = param.replace(/&keyword=$/, '');
				url += (url.indexOf('?') > 0 ? '&' : '?') + param;
			}
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
				$('#_window_ form').css('padding-top','25px');	
				var inputform = $('#_window_ form.ajax');
				if (inputform.length) {
					$(':input:visible', inputform).filter(function(i) {
								return !$(this).val();
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
											+ (form.attr('entity') || form
													.attr('action')) + '.id"]',
									inputform).val())
								create = false;
						}
						if (create) {
							$('button[type="submit"]', inputform)
									.after('<button type="submit" class="btn save_and_create"><span><span>'
											+ MessageBundle
													.get('save.and.create')
											+ '</span></span></button>');
							$('.save_and_create', inputform).click(function() {
										$('form.ajax').addClass('reset');
									});
						}
					}
					var pathname = document.location.pathname;
					var action = inputform.attr('action');
					if (action.indexOf('http') != 0 && action.indexOf('/') != 0)
						action = pathname
								+ (pathname.indexOf('/') == (pathname.length - 1)
										? ''
										: '/') + action;
					inputform.attr('action', action);
					if (inputform.hasClass('view')
							&& !inputform.attr('replacement'))
						inputform.attr('replacement', '_window_:content');
					if (!inputform.hasClass('view')) {
						$('button[type=submit]', inputform).click(function(e) {
							inputform[0].onsuccess = function() {
								$(this).removeClass('dirty');
								$(this).removeClass('dontreload');
								if (!$(e.target).closest('button')
										.hasClass('save_and_create'))
									setTimeout(function() {
												$('#_window_').dialog('close');
											}, 1000);

							};
						});
					}
				}
				$('#_window_ a').each(function() {
					var href = $(this).attr('href');
					if (href && href.indexOf('http') != 0
							&& href.indexOf('/') != 0) {
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
			$('#_window_ > iframe')[0].src = url;
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
			bgiframe : true,
			closeOnEscape : false,
			close : function() {
				if (reloadonclose
						&& !$('#_window_ form.ajax').hasClass('dontreload'))
					Richtable.reload(form, true);
				$('#_window_ ').html('');
				win.dialog('destroy');
			},
			beforeclose : function(event, ui) {
				if ($('form', win).hasClass('dirty')) {
					return confirm(MessageBundle.get('confirm.exit'));
				}
			}
		};
		if ($.browser.msie && $.browser.version <= 8)
			opt.height = 600;
		win.dialog(opt);
		win.dialog('open');
	},
	enter : function(event) {
		var btn = event.target;
		var form = $(btn).closest('form');
		if ($(btn).attr('tagName') != 'BUTTON' || $(btn).attr('tagName') != 'A')
			btn = $(btn).closest('.btn');
		if (btn.attr('onclick'))
			return;
		var url = Richtable.getBaseUrl(form) + Richtable.getPathParams();
		var tr = $(btn).closest('tr');
		var id = tr.attr('rowid')
				|| $('input[type="checkbox"]:eq(0)', tr).val();
		url += (url.indexOf('?') > 0 ? '&' : '?') + 'parentId=' + id;
		document.location.href = url;
	},
	click : function(event) {
		var btn = event.target;
		var form = $(btn).closest('form');
		if ($(btn).attr('tagName') != 'BUTTON' || $(btn).attr('tagName') != 'A')
			btn = $(btn).closest('.btn');
		if (btn.attr('onclick'))
			return;
		var idparams;
		var tr = $(btn).closest('tr');
		var id = tr.attr('rowid')
				|| $('input[type="checkbox"]:eq(0)', tr).val();
		if (id) {
			idparams = 'id=' + id;
		} else {
			var arr = [];
			$('form.richtable tbody input[type="checkbox"]').each(function() {
				if (this.checked) {
					var _id = $(this).closest('tr').attr('rowid') || this.value;
					arr.push('id=' + _id);
				}
			});
			idparams = arr.join('&');
		}
		var action = $(btn).attr('action');
		var view = $(btn).attr('view');
		if (action == 'reload')
			Richtable.reload(form, true);
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
												Richtable.reload(form, true)
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
								Richtable.reload(form, true)
							}
						});
			}
		} else {
			var options = (new Function("return "
					+ ($(btn).attr('windowoptions') || '{}')))();
			var url = $(btn).attr('href');
			if (view) {
				url = Richtable.getUrl(view, id, !id, form);
			} else {
				if (!$(btn).hasClass('noid')) {
					if (!id) {
						// from bottom
						if (!idparams) {
							Message.showMessage('no.selection');
							return false;
						}
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
		var theadCells = $('.richtable thead:eq(0) td');
		$.each($('.richtable tbody')[0].rows, function() {
			var row = this;
			if ($(row).hasClass('edited')) {
				modified = true;
				var params = {};
				var entity = Richtable.getBaseUrl(form);
				entity = entity.substring(entity.lastIndexOf('/') + 1);
				params[entity + '.id'] = $(this).attr('rowid')
						|| $('input[type="checkbox"]:eq(0)', this).val();;
				$.each(row.cells, function(i) {
							var theadCell = $(theadCells[i]);
							var name = theadCell.attr('cellName');
							if (!name || !$(this).hasClass('edited')
									&& theadCell.hasClass('excludeIfNotEdited'))
								return;
							var value = $(this).attr('cellValue')
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
							onsuccess : function() {
								$(row).removeClass('edited');
								$('td', row).removeClass('edited')
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
		var ce = $(cell);
		if (ce.hasClass('editing'))
			return;
		ce.addClass('editing');
		var value = ce.attr('cellValue');
		value = $.trim(value || ce.text());
		ce.attr('oldValue', value);
		var template = '';
		if (templateId) {
			template = $('#' + templateId).text();
		} else {
			if (type == 'textarea') {
				template = '<textarea type="text" class="text" value="" style="width: 100%;"/>';
			} else if (type == 'date')
				template = '<input type="text" class="text date" value="" style="width: 100%;"/>';
			else if (type == 'boolean')
				template = '<select style="width: 100%;"><option value="true">'
						+ MessageBundle.get('true')
						+ '</option><option value="false">'
						+ MessageBundle.get('false') + '</option></select>';
			else
				template = '<input type="text" class="text" value="" style="width: 100%;"/>';
		}
		ce.html(template);
		$(':input',ce).blur(function(){Richtable.updateCell(this)});
		var select = $('select', ce);
		if (value && select.length) {
			var arr = $('option', select).toArray();
			for (var i = 0; i < arr.length; i++) {
				if (arr[i].value == value || $(arr[i]).text() == value) {
					$(arr[i]).attr('selected', true);
					break;
				}
			};
			select.focus();
		} else {
			$('input.date', ce).datepicker({
						dateFormat : 'yy-mm-dd',
						onSelect : function() {
							Richtable.updateCell(this)
						}
					});
			$(':input', ce).val(value).focus();
		}
	},
	updateCell : function(cellEdit) {
		var ce = $(cellEdit);
		var cell = ce.parent();
		cell.removeClass('editing');
		cell.attr('cellValue', ce.val());
		var editType = ce.attr('tagName');
		if (editType == 'SELECT')
			cell.text($('option:selected', ce).text());
		else if (editType == 'CHECKBOX' || editType == 'RADIO')
			cell.text(ce.next().text());
		else
			cell.text(ce.val());
		if (cell.attr('oldValue') != cell.attr('cellValue')) {
			cell.addClass('edited');
			cell.parent().addClass('edited');
		}
		cell.removeAttr('oldValue');
	},
	updatePasswordCell : function(cellEdit) {
		var ce = $(cellEdit);
		var cell = ce.parent();
		cell.text('********');
		cell.attr('cellValue', ce.val());
		cell.addClass('edited').removeClass('editing');
		cell.parent().addClass('edited');

	}
};
Observation.richtable = function(container) {
	if ($('.richtable', container).length) {
		$('.action button.btn,a[rel="richtable"]', container)
				.click(Richtable.click);
		var theadCells = $('table.richtable thead:eq(0) td', container);
		var rows = $('table.richtable tbody:eq(0) tr', container).each(
				function() {
					var cells = this.cells;
					theadCells.each(function(i) {
								var cellEdit = $(this).attr('cellEdit');
								if (!cellEdit)
									return;
								var ar = cellEdit.split(',');
								$(cells[i]).bind(ar[0], function() {
											Richtable.editCell(this, ar[1],
													ar[2]);
										});
							});
				});
		$('.firstPage', container).click(function(event) {
					var form = $(event.target).closest('form');
					$('.inputPage', form).val(1);
					Richtable.reload(form);
					return false;
				});
		$('.prevPage', container).click(function(event) {
					var form = $(event.target).closest('form');
					$('.inputPage', form).val(function(i, v) {
								return parseInt(v) - 1
							});
					Richtable.reload(form);
					return false;
				});
		$('.nextPage', container).click(function(event) {
					var form = $(event.target).closest('form');
					$('.inputPage', form).val(function(i, v) {
								return parseInt(v) + 1
							});
					Richtable.reload(form);
					return false;
				});
		$('.lastPage', container).click(function(event) {
					var form = $(event.target).closest('form');
					$('.inputPage', form).val($('.totalPage', form).text());
					Richtable.reload(form);
					return false;
				});
		$('.inputPage', container).change(function(event) {
					var form = $(event.target).closest('form');
					Richtable.reload(form);
					event.preventDefault();
				});
		$('select.pageSize', container).change(function(event) {
					var form = $(event.target).closest('form');
					$('.inputPage', form).val(1);
					Richtable.reload(form);
				});
		$('input[name="keyword"]', container).keydown(function(event) {
					var form = $(event.target).closest('form');
					if (event.keyCode == 13) {
						$('.inputPage', form).val(1);
						Richtable.reload(form);
						return false;
					}
				}).next().click(function(event) {
					var form = $(event.target).closest('form');
					$('.inputPage', form).val(1);
					Richtable.reload(form);
					return false;
				});

		if ($('table.richtable', container).hasClass('resizable')) {
			$('.resizeBar', container).mousedown(ECSideUtil.StartResize);
			$('.resizeBar', container).mouseup(ECSideUtil.EndResize);
			$(document).mousemove(ECSideUtil.DoResize);
			$(document).mouseup(ECSideUtil.EndResize);
			$(document.body).bind('drag', function() {
						return false;
					}).bind('selectstart', function() {
						return ECSideUtil.Dragobj == null;
					});
		}
	}
};