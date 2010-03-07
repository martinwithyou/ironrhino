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
		ECSideUtil.MinColWidth = $('#' + Richtable.id).attr('minColWidth') || '30';
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
			if (url.indexOf('{id}') > 0)
				url = url.replace('{id}', id);
			else
				url += 'id=' + id;
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
								.after(
										'<button type="submit" class="btn save_and_create"><span><span>' + MessageBundle
												.get('save.and.create') + '</span></span></button>');
						$('.save_and_create', form).click(function() {
							$('form.ajax').addClass('reset');
						});
					}
				}
			};
			ajax( {
				url : url,
				cache : false,
				target : target,
				replacement : '_window_:content',
				quiet : true
			});
		} else {
			// embed iframe
			$('#_window_')
					.html(
							'<iframe style="width:650px;height:90%;border:0;" onload="Dialog.adapt($(\'#_window_\'),this);"/>');
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
		$("#_window_").dialog( {
			minHeight : 500,
			width : 700,
			modal : true,
			bgiframe : true,
			closeOnEscape : false,
			close : (reloadonclose ? function() {
				Richtable.reload();
			} : null)
		});
	},
	enter : function(parentId, url) {
		if (!url)
			url = Richtable.getBaseUrl() + Richtable.getPathParams();
		if (parentId) {
			if (url.indexOf('{parentId}') > 0)
				url = url.replace('{parentId}', parentId);
			else
				url += (url.indexOf('?') > 0 ? '&' : '?') + 'parentId='
						+ parentId;
		}
		document.location.href = url;
	},
	action : function(event) {
		var btn = event.target;
		if ($(btn).attr('tagName') != 'BUTTON')
			btn = $(btn).closest('button');
		var action = $(btn).attr('class').split(' ')[1];
		if (action == 'input')
			Richtable.input(event);
		else if (action == 'view')
			Richtable.view(event);
		else if (action == 'save')
			Richtable.save(event);
		else if (action == 'del')
			Richtable.del(event);
		else if (action) {
			var url = Richtable.getBaseUrl() + '/' + action
					+ Richtable.getPathParams();
			var id = $(btn).closest('tr').attr('rowid');
			if (id) {
				url += (url.indexOf('?') > 0 ? '&' : '?') + 'id=' + id;
			} else {
				var arr = [];
				$('form.richtable tbody input[type="checkbox"]').each(
						function() {
							if (this.checked)
								arr.push('id=' + $(this).closest('tr').attr(
										'rowid'));
						});
				if (arr.length == 0)
					return;
				url += (url.indexOf('?') > 0 ? '&' : '?') + arr.join('&');
			}

			ajax( {
				url : url,
				type : 'POST',
				dataType : 'json',
				success : Richtable.reload
			});
		}
	},
	input : function(event) {
		var id = $(event.target).closest('tr').attr('rowid');
		Richtable.open(Richtable.getUrl('input', id, !id), true);
	},
	view : function(event) {
		Richtable.open(Richtable.getUrl('view', $(event.target).closest('tr')
				.attr('rowid')));
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
		var theadCells = $('.richtable thead:eq(0) td');
		$.each(arr, function() {
			var rows = $('.richtable tbody')[0].rows;
			var row;
			for ( var i = 0; i < rows.length; i++)
				if ($(rows[i]).attr('rowid') == this)
					row = rows[i];
			if (row && $(row).attr('edited') == 'true') {
				var params = {};
				var entity = Richtable.getBaseUrl();
				entity = entity.substring(entity.lastIndexOf('/') + 1);
				params[entity + '.id'] = this;
				$.each(row.cells,
						function(i) {
							var theadCell = $(theadCells[i]);
							var name = theadCell.attr("cellName");
							if (!name || $(this).attr('edited') != 'true'
									&& theadCell.hasClass('include_if_edited'))
								return;
							var value = $(this).attr('cellValue');
							if (!value)
								value = window.isIE ? this.innerText
										: this.textContent;
							params[name] = value;
						});
				var url = Richtable.getBaseUrl() + '/save'
						+ Richtable.getPathParams();
				ajax( {
					url : url,
					type : 'POST',
					data : params,
					dataType : 'json'
				});
			}
		});
	},
	del : function(event) {
		var url = Richtable.getBaseUrl() + '/delete'
				+ Richtable.getPathParams();
		var id = $(event.target).closest('tr').attr('rowid');
		if (id) {
			url += (url.indexOf('?') > 0 ? '&' : '?') + 'id=' + id;
		} else {
			var arr = [];
			$('form.richtable tbody input[type="checkbox"]').each(function() {
				if (this.checked)
					arr.push('id=' + $(this).closest('tr').attr('rowid'));
			});
			if (arr.length == 0)
				return;
			url += (url.indexOf('?') > 0 ? '&' : '?') + arr.join('&');
		}

		ajax( {
			url : url,
			type : 'POST',
			dataType : 'json',
			success : Richtable.reload
		});
	},
	editCell : function(cell, templateId) {
		var ce = $(cell);
		if (ce.attr("editing") == "true")
			return;
		ce.attr("editing", "true");
		var template = document.getElementById(templateId);
		var templateText = $.browser.msie ? template.value
				: template.textContent;
		var text = ce.text();
		var value = ce.attr("cellValue");
		value = value || text;
		ce.html(templateText);
		$('input,select,checkbox,radio', ce).val(value).focus();
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
		$('.extendTool button.btn,.action button.btn').click(Richtable.action);
		var theadCells = $('.richtable thead:eq(0) td');
		var rows = $('.richtable tbody:eq(0) tr').each(function() {
			var cells = this.cells;
			theadCells.each(function(i) {
				var cellEdit = $(this).attr('cellEdit');
				if (!cellEdit)
					return;
				var ar = cellEdit.split(',');
				var template = ar[1] || 'rt_edit_template_input';
				$(cells[i]).bind(ar[0], function() {
					Richtable.editCell(this, template);
				});
			});
		});
		$('.richtable .reload').click(Richtable.reload);
		$('.richtable .resizeBar').mousedown(ECSideUtil.StartResize);
		$('.richtable .resizeBar').mouseup(ECSideUtil.EndResize);
		$('.richtable .firstPage').click(function() {
			$('.richtable .jumpPageInput').val(1);
			Richtable.reload()
		});
		$('.richtable .prevPage')
				.click(
						function() {
							$('.richtable .jumpPageInput').val(
									parseInt($('.richtable .jumpPageInput')
											.val()) - 1);
							Richtable.reload()
						});
		$('.richtable .nextPage')
				.click(
						function() {
							$('.richtable .jumpPageInput').val(
									parseInt($('.richtable .jumpPageInput')
											.val()) + 1);
							Richtable.reload()
						});
		$('.richtable .lastPage').click(
				function() {
					$('.richtable .jumpPageInput').val(
							$(".richtable .totalPage").text());
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
		$('.richtable input[name="keyword"]').focus().keydown(function(event) {
			if (event.keyCode && event.keyCode == 13) {
				$('.richtable .jumpPageInput').val(1);
			}
		}).next().click(function() {
			$('.richtable .jumpPageInput').val(1)
		});

		var pathname = document.location.pathname;
		var form = $('#_window_ form.ajax');
		if (form.length) {
			var action = form.attr('action');
			if (action.indexOf('http') != 0 && action.indexOf('/') != 0)
				action = pathname
						+ (pathname.indexOf('/') == (pathname.length - 1) ? ''
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
		$('#_window_ a')
				.each(
						function() {
							var href = $(this).attr('href');
							if (href.indexOf('http') != 0
									&& href.indexOf('/') != 0) {
								href = pathname
										+ (pathname.indexOf('/') == (pathname.length - 1) ? ''
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