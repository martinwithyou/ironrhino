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
		ECSideUtil.MinColWidth = $('#' + Richtable.id).attr('minColWidth')
				|| '30';
		ECSideUtil.Dragobj.style.backgroundColor = '#3366ff';
		ECSideUtil.Dragobj.parentTdW -= ECSideUtil.Dragobj.mouseDownX;
		var cellIndex = ECSideUtil.Dragobj.parentNode.cellIndex;
		try {
			ECSideUtil.DragobjBodyCell = $('#' + Richtable.id + ' tbody')[0].rows[0].cells[cellIndex];
			ECSideUtil.DragobjBodyCellSibling = $(ECSideUtil.DragobjBodyCell)
					.next()[0];
		} catch (e) {
			ECSideUtil.DragobjBodyCell = null;
		}
	},
	DoResize : function(event) {
		if (ECSideUtil.Dragobj == null) {
			return true;
		}
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
	},
	EndResize : function(event) {
		if (ECSideUtil.Dragobj == null) {
			return false;
		}
		ECSideUtil.Dragobj.mouseDownX = 0;
		document.body.style.cursor = '';
		ECSideUtil.Dragobj.style.backgroundColor = '';
		ECSideUtil.Dragobj = null;
		ECSideUtil.DragobjSibling = null;

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
	reload : function(form) {
		form = form || $('form.richtable');
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
				var inputform = $('#_window_ form.ajax');
				if (inputform.length) {
					$(':input:visible', inputform).filter(function(i) {
								return !$(this).val();
							}).eq(0).focus();
					if (!inputform.hasClass('keepopen')) {
						$(':input', inputform).change(function(e) {
									inputform.attr('dirty', true);
								});
						$(inputform).attr('dontreload', true);
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
								$(this).removeAttr('dirty');
								$(this).removeAttr('dontreload');
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
				$('#_window_ ').html('');
				if (reloadonclose
						&& !$('#_window_ form.ajax').attr('dontreload'))
					Richtable.reload(form);
				win.dialog('destroy');
			},
			beforeclose : function(event, ui) {
				if ($('form', win).attr('dirty')) {
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
		url += (url.indexOf('?') > 0 ? '&' : '?') + 'parentId='
				+ $(btn).closest('tr').attr('rowid');
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
		var id = $(btn).closest('tr').attr('rowid');
		if (id) {
			idparams = 'id=' + id;
		} else {
			var arr = [];
			$('form.richtable tbody input[type="checkbox"]').each(function() {
						if (this.checked) {
							var _id = $(this).closest('tr').attr('rowid');
							arr.push('id=' + _id);
						}
					});
			idparams = arr.join('&');
		}
		var action = $(btn).attr('action');
		var view = $(btn).attr('view');
		if (action == 'reload')
			Richtable.reload(form);
		else if (action == 'enter')
			Richtable.enter(event);
		else if (action == 'save')
			Richtable.save(event);
		else if (action) {
			if (!idparams) {
				Message.showMessage('no.selection');
				return;
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
		var id = $(event.target).closest('tr').attr('rowid');
		var form = $(event.target).closest('form');
		var arr = [];
		if (id)
			arr[0] = id;
		else
			$.each($('.richtable tbody')[0].rows, function() {
						if ($(this).attr('edited') == 'true')
							arr.push($(this).attr('rowid'))
					});
		var modified = false;
		if (arr.length > 0) {
			var theadCells = $('.richtable thead:eq(0) td');
			$.each(arr, function() {
				var rows = $('.richtable tbody')[0].rows;
				var row;
				for (var i = 0; i < rows.length; i++)
					if ($(rows[i]).attr('rowid') == this)
						row = rows[i];
				if (row && $(row).attr('edited')) {
					modified = true;
					var params = {};
					var entity = Richtable.getBaseUrl(form);
					entity = entity.substring(entity.lastIndexOf('/') + 1);
					params[entity + '.id'] = this;
					$.each(row.cells, function(i) {
						var theadCell = $(theadCells[i]);
						var name = theadCell.attr('cellName');
						if (!name || $(this).attr('edited') != 'true'
								&& theadCell.hasClass('excludeIfNotEdited'))
							return;
						var value = $(this).attr('cellValue') || $(this).text();
						params[name] = value;
					});
					var url = Richtable.getBaseUrl(form) + '/save'
							+ Richtable.getPathParams();
					ajax({
								url : url,
								type : 'POST',
								data : params,
								dataType : 'json'
							});
				}
			});
		}
		if (!modified) {
			Message.showMessage('no.modification');
		}
	},
	editCell : function(cell, templateId) {
		var ce = $(cell);
		if (ce.attr('editing'))
			return;
		ce.attr('editing', 'true');
		var template = document.getElementById(templateId);
		var templateText = $.browser.msie
				? template.value
				: template.textContent;
		var text = ce.text();
		var value = ce.attr('cellValue');
		value = $.trim(value || text);
		ce.attr('oldValue', value);
		ce.html(templateText);
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
		cell.removeAttr('editing');
		cell.attr('cellValue', ce.val());
		var editType = ce.attr('tagName');
		if (editType == 'INPUT') {
			cell.html(ce.val());
		} else if (editType == 'SELECT') {
			cell.html($('option[selected]', ce).text());
		} else if (editType == 'CHECKBOX' || editType == 'RADIO') {
			cell.html(ce.next().text());
		}
		if (cell.attr('oldValue') != cell.attr('cellValue')) {
			cell.attr('edited', 'true');
			cell.parent().attr('edited', 'true');
			cell.addClass('editedCell');
		}
		cell.removeAttr('oldValue');
	},
	updatePasswordCell : function(cellEdit) {
		var ce = $(cellEdit);
		var cell = ce.parent();
		cell.text('********');
		cell.attr('cellValue', ce.val());
		cell.attr('edited', 'true');
		cell.parent().attr('edited', 'true');
		cell.removeAttr('editing');
		cell.addClass('editedCell');
	}
};
Observation.richtable = function() {
	if ($('.richtable').length) {
		$('.action button.btn,a[rel="richtable"]').click(Richtable.click);
		var theadCells = $('.richtable thead:eq(0) td');
		var rows = $('.richtable tbody:eq(0) tr').each(function() {
					var cells = this.cells;
					theadCells.each(function(i) {
								var cellEdit = $(this).attr('cellEdit');
								if (!cellEdit)
									return;
								var ar = cellEdit.split(',');
								var template = ar[1]
										|| 'rt_edit_template_input';
								$(cells[i]).bind(ar[0], function() {
											Richtable.editCell(this, template);
										});
							});
				});
		$('.richtable .resizeBar').mousedown(ECSideUtil.StartResize);
		$('.richtable .resizeBar').mouseup(ECSideUtil.EndResize);
		$('.richtable .firstPage').click(function(event) {
					var form = $(event.target).closest('form');
					$('.inputPage', form).val(1);
					try{
						var url = $(event.target).attr('href');
						history.pushState(url,'',url);
					}catch(e){};
					Richtable.reload(form);
					return false;
				});
		$('.richtable .prevPage').click(function(event) {
					var form = $(event.target).closest('form');
					$('.inputPage', form).val(function(i, v) {
								return parseInt(v) - 1
							});
					try{
						var url = $(event.target).attr('href');
						history.pushState(url,'',url);
					}catch(e){};
					Richtable.reload(form);
					return false;
				});
		$('.richtable .nextPage').click(function(event) {
					var form = $(event.target).closest('form');
					$('.inputPage', form).val(function(i, v) {
								return parseInt(v) + 1
							});
					try{
						var url = $(event.target).attr('href');
						history.pushState(url,'',url);
					}catch(e){};
					Richtable.reload(form);
					return false;
				});
		$('.richtable .lastPage').click(function(event) {
					var form = $(event.target).closest('form');
					$('.inputPage', form).val($('.totalPage', form).text());
					try{
						var url = $(event.target).attr('href');
						history.pushState(url,'',url);
					}catch(e){};
					Richtable.reload(form);
					return false;
				});
		$('.richtable .inputPage').change(function(event) {
					var form = $(event.target).closest('form');
					Richtable.reload(form);
					event.preventDefault();
				});
		$('.richtable select.pageSize').change(function(event) {
					var form = $(event.target).closest('form');
					$('.inputPage', form).val(1);
					Richtable.reload(form);
				});
		$('.richtable input[name="keyword"]').keydown(function(event) {
					var form = $(event.target).closest('form');
					if (event.keyCode == 13) {
						$('.inputPage', form).val(1);
					}
				}).next().click(function() {
					$('.inputPage', form).val(1);
				});
	}
};
Initialization.richtable = function() {
	if ($('.richtable').length) {
		var resizable = $('.richtable').attr('resizable');
		if (resizable) {
			document.onmousemove = ECSideUtil.DoResize;
			document.onmouseup = ECSideUtil.EndResize;
			document.body.ondrag = function() {
				return false;
			};
			document.body.onselectstart = function() {
				return ECSideUtil.Dragobj == null;
			};
		}
	}
};