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
		document.body.style.cursor = "e-resize";
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
		ECSideUtil.Dragobj.style.backgroundColor = "#3366ff";
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
		var e = event || window.event;
		if (ECSideUtil.Dragobj == null) {
			return true;
		}
		if (!ECSideUtil.Dragobj.mouseDownX) {
			return false;
		}
		document.body.style.cursor = "e-resize";
		var dx = e.screenX;
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
			ECSideUtil.Dragobj.parentNode.style.width = newWidth + "px";
			ECSideUtil.DragobjSibling.style.width = newSiblingWidth + "px";
			try {
				ECSideUtil.DragobjBodyCell.style.width = newWidth + "px";
				ECSideUtil.DragobjBodyCellSibling.style.width = newSiblingWidth
						+ "px";
				ECSideUtil.DragobjBodyCell.width = newWidth + "px";
				ECSideUtil.DragobjBodyCellSibling.width = newSiblingWidth
						+ "px";
			} catch (e) {
			}
		}
	},
	EndResize : function(event) {
		if (ECSideUtil.Dragobj == null) {
			return false;
		}
		ECSideUtil.Dragobj.mouseDownX = 0;
		document.body.style.cursor = "";
		ECSideUtil.Dragobj.style.backgroundColor = "";
		ECSideUtil.Dragobj = null;
		ECSideUtil.DragobjSibling = null;

	}
};

Richtable = {
	getBaseUrl : function() {
		var url = document.location.href;
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
	getUrl : function(type, id, includeParams) {
		var url = Richtable.getBaseUrl() + '/' + type
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
	reload : function() {
		$('.richtable').submit();
	},
	open : function(url, reloadonclose, useiframe) {
		reloadonclose = reloadonclose || false;
		useiframe = useiframe || false;
		if (!$('#_window_').length)
			$('<div id="_window_"></div>').appendTo(document.body);
		if (!useiframe) {
			// ajax replace
			$('#_window_').html('');
			var target = $('#_window_').get(0);
			target.onsuccess = function() {
				Dialog.adapt($('#_window_'));
				if (url.indexOf('?') > 0)
					url = url.substring(0, url.indexOf('?'));
				var form = $('#_window_ form.ajax');
				if (!form.hasClass('keepopen')) {
					var create = url.lastIndexOf('input') == url.length - 5;
					if (create) {
						if ($('input[type="hidden"][name="id"]', form).val())
							create = false;
						var entity = Richtable.getBaseUrl();
						entity = entity.substring(entity.lastIndexOf('/') + 1);
						if ($('input[type="hidden"][name="' + entity + '.id"]',
								form).val())
							create = false;
					}
					if (create) {
						$('button[type="submit"]', form)
								.after('<button type="submit" class="btn save_and_create"><span><span>'
										+ MessageBundle.get('save.and.create')
										+ '</span></span></button>');
						$('.save_and_create', form).click(function() {
									$('form.ajax').addClass('reset');
								});
					}
				}
			};
			ajax({
						url : url,
						cache : false,
						target : target,
						replacement : '_window_:content',
						quiet : true
					});
		} else {
			// embed iframe
			$('#_window_')
					.html('<iframe style="width:100%;height:550px;border:0;"/>');
			url += (url.indexOf('?') > 0 ? '&' : '?') + 'decorator=simple&'
					+ Math.random();
			$('#_window_ > iframe')[0].src = url;
		}
		if ($('#_window_').attr('_dialoged_')) {
			$("#_window_").dialog('option', 'close',
					(reloadonclose ? function() {
						Richtable.reload();
					} : null));
			$("#_window_").dialog('open');
			return;
		}
		$('#_window_').attr('_dialoged_', true);
		var opt = {
			minHeight : 600,
			width : 700,
			// modal : true,
			bgiframe : true,
			closeOnEscape : false,
			close : (reloadonclose ? function() {
				Richtable.reload();
			} : null)
		};
		if ($.browser.msie)
			opt.height = 600;
		$("#_window_").dialog(opt);
	},
	enter : function(event) {
		var btn = event.target;
		if ($(btn).attr('tagName') != 'BUTTON' || $(btn).attr('tagName') != 'A')
			btn = $(btn).closest('.btn');
		if (btn.attr('onclick'))
			return;
		var url = Richtable.getBaseUrl() + Richtable.getPathParams();
		url += (url.indexOf('?') > 0 ? '&' : '?') + 'parentId='
				+ $(btn).closest('tr').attr('rowid');
		document.location.href = url;
	},
	click : function(event) {
		var btn = event.target;
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
			Richtable.reload();
		else if (action == 'enter')
			Richtable.enter(event);
		else if (action == 'save')
			Richtable.save(event);
		else if (action) {
			if (action == 'delete'
					&& !confirm(MessageBundle.get('confirm.delete')))
				return;
			if (!idparams) {
				var msg = MessageBundle.get('no.selection');
				if (typeof $.jGrowl != 'undefined')
					$.jGrowl(msg);
				else
					$('#message').html(Message.get(msg, 'action_message'));
				return;
			}
			var url = Richtable.getBaseUrl() + '/' + action
					+ Richtable.getPathParams();
			url += (url.indexOf('?') > 0 ? '&' : '?') + idparams;
			ajax({
						url : url,
						type : 'POST',
						dataType : 'json',
						success : Richtable.reload
					});
		} else {
			var options = eval('(' + ($(btn).attr('windowoptions') || '{}')
					+ ')');
			var url = $(btn).attr('href');
			if (view) {
				url = Richtable.getUrl(view, id, !id);
			} else {
				if (!$(btn).hasClass('noid')) {
					if (!id) {
						// from bottom
						if (!idparams) {
							var msg = MessageBundle.get('no.selection');
							if (typeof $.jGrowl != 'undefined')
								$.jGrowl(msg);
							else
								$('#message').html(Message.get(msg,
										'action_message'));
							return false;
						}
						url += (url.indexOf('?') > 0 ? '&' : '?') + idparams;
					}
				}
			}
			Richtable.open(url, true, options.iframe);
			options.iframe = null;
			for (var key in options)
				$("#_window_").dialog('option', key, options[key]);
			Dialog.adapt($('#_window_'));
			return false;
		}
	},
	save : function(event) {
		var id = $(event.target).closest('tr').attr('rowid');
		var arr = [];
		if (id)
			arr[0] = id;
		else
			$.each($('.richtable tbody')[0].rows, function() {
						if ($(this).attr("edited") == "true")
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
				if (row && $(row).attr('edited') == 'true') {
					modified = true;
					var params = {};
					var entity = Richtable.getBaseUrl();
					entity = entity.substring(entity.lastIndexOf('/') + 1);
					params[entity + '.id'] = this;
					$.each(row.cells, function(i) {
						var theadCell = $(theadCells[i]);
						var name = theadCell.attr("cellName");
						if (!name || $(this).attr('edited') != 'true'
								&& theadCell.hasClass('include_if_edited'))
							return;
						var value = $(this).attr('cellValue') || $(this).text();
						params[name] = value;
					});
					var url = Richtable.getBaseUrl() + '/save'
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
			var msg = MessageBundle.get('no.modification');
			if (typeof $.jGrowl != 'undefined')
				$.jGrowl(msg);
			else
				$('#message').html(Message.get(msg, 'action_message'));
		}
	},
	editCell : function(cell, templateId) {
		var ce = $(cell);
		if (ce.attr("editing") == "true")
			return;
		ce.attr("editing", "true");
		var template = document.getElementById(templateId);
		var templateText = $.browser.msie
				? template.value
				: template.textContent;
		var text = ce.text();
		var value = ce.attr("cellValue");
		value = $.trim(value || text);
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
			$(':input', ce).val(value).focus();
		}
	},
	updateCell : function(cellEdit) {
		var ce = $(cellEdit);
		var cell = ce.parent();
		var editType = ce.attr('tagName');
		if (editType == "INPUT") {
			cell.html(ce.val());
		} else if (editType == "SELECT") {
			cell.html($('option[selected]', ce).text());
		} else if (editType == "CHECKBOX" || editType == "RADIO") {
			cell.html(ce.next().text());
		}
		cell.attr("cellValue", ce.val());
		cell.attr("edited", "true");
		cell.parent().attr("edited", "true");
		cell.removeAttr("editing");
		cell.addClass("editedCell");
	},
	updatePasswordCell : function(cellEdit) {
		var ce = $(cellEdit);
		var cell = ce.parent();
		cell.text('********');
		cell.attr("cellValue", ce.val());
		cell.attr("edited", "true");
		cell.parent().attr("edited", "true");
		cell.attr("editing", "false");
		cell.addClass("editedCell");
	}
};
Observation.richtable = function() {
	if ($('.richtable').length) {
		$('.extendTool button.btn,.action button.btn,a[rel="richtable"]')
				.click(Richtable.click);
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
		$('.richtable .firstPage').click(function() {
					$('.richtable .jumpPageInput').val(1);
					Richtable.reload()
				});
		$('.richtable .prevPage').click(function() {
			$('.richtable .jumpPageInput')
					.val(parseInt($('.richtable .jumpPageInput').val()) - 1);
			Richtable.reload()
		});
		$('.richtable .nextPage').click(function() {
			$('.richtable .jumpPageInput')
					.val(parseInt($('.richtable .jumpPageInput').val()) + 1);
			Richtable.reload()
		});
		$('.richtable .lastPage').click(function() {
			$('.richtable .jumpPageInput').val($(".richtable .totalPage")
					.text());
			Richtable.reload()
		});
		$('.richtable .jumpPage').click(function() {
					Richtable.reload()
				});
		$('.richtable input[name="resultPage.pageNo"]').keydown(
				function(event) {
					if (event.keyCode && event.keyCode == 13) {
						Richtable.reload()
					}
				});
		$('.richtable select[name="resultPage.pageSize"]').change(function() {
					Richtable.reload()
				});
		$('.richtable input[name="keyword"]').keydown(function(event) {
					if (event.keyCode && event.keyCode == 13) {
						$('.richtable .jumpPageInput').val(1);
					}
				}).next().click(function() {
					$('.richtable .jumpPageInput').val(1)
				});

		var pathname = document.location.pathname;
		var form = $('#_window_ form.ajax');
		if (form.length) {
			$(':input', form).eq(0).focus();
			var action = form.attr('action');
			if (action.indexOf('http') != 0 && action.indexOf('/') != 0)
				action = pathname
						+ (pathname.indexOf('/') == (pathname.length - 1)
								? ''
								: '/') + action;
			form.attr('action', action);
			if (form.hasClass('view') && !form.attr('replacement'))
				form.attr('replacement', '_window_:content');
			if (!form.hasClass('keepopen') && !form.hasClass('view')) {
				$('button[type="submit"]', form).click(function() {
							form[0].onsuccess = function() {
								setTimeout(function() {
											$("#_window_").dialog('close');
										}, 1000);

							};
						});
			}
		}
		$('#_window_ a').each(function() {
			var href = $(this).attr('href');
			if (href && href.indexOf('http') != 0 && href.indexOf('/') != 0) {
				href = pathname
						+ (pathname.indexOf('/') == (pathname.length - 1)
								? ''
								: '/') + href;
				this.href = href;
			}
		});
	}
};
Initialization.richtable = function() {
	if ($('.richtable').length) {
		var resizable = $('.richtable').attr('resizable');
		if (resizable == "true") {
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