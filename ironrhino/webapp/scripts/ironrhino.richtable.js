var ECSideUtil = {};
ECSideUtil.getNextElement = function(node) {
	var tnode = node.nextSibling;
	while (tnode != null) {
		if (tnode.nodeType == 1) {
			return tnode;
		}
		tnode = tnode.nextSibling;
	}
	return null;
};
ECSideUtil.getPosLeft = function(elm) {
	var left = elm.offsetLeft;
	while ((elm = elm.offsetParent) != null) {
		left += elm.offsetLeft;
	}
	return left;
};
ECSideUtil.getPosRight = function(elm) {
	return ECSideUtil.getPosLeft(elm) + elm.offsetWidth;
};
ECSideUtil.replaceAll = function(exstr, ov, value) {
	var gc = ECSideUtil.escapeRegExp(ov);
	if (gc == null || gc == '') {
		return exstr;
	}
	var reReplaceGene = "/" + gc + "/gm";
	var r = null;
	var cmd = "r=exstr.replace(" + reReplaceGene + ","
			+ ECSideUtil.escapeString(value) + ")";
	eval(cmd);
	return r;
};
ECSideUtil.escapeRegExp = function(str) {
	return !str ? '' + str : ('' + str).replace(/\\/gm, "\\\\").replace(
			/([\f\b\n\t\r[\^$|?*+(){}])/gm, "\\$1");
};
ECSideUtil.escapeString = function(str) {
	return !str ? '' + str
			: ('"' + ('' + str).replace(/(["\\])/g, '\\$1') + '"').replace(
					/[\f]/g, "\\f").replace(/[\b]/g, "\\b").replace(/[\n]/g,
					"\\n").replace(/[\t]/g, "\\t").replace(/[\r]/g, "\\r");
};
ECSideUtil.Dragobj = null;
ECSideUtil.DragobjSibling = null;
ECSideUtil.DragobjBodyCell = null;
ECSideUtil.DragobjBodyCellSibling = null;
ECSideUtil.StartResize = function(event) {
	event = event || window.event;
	var obj = event.srcElement || event.target;
	obj.focus();
	document.body.style.cursor = "e-resize";
	var sibling = ECSideUtil.getNextElement(obj.parentNode);
	var dx = event.screenX;
	obj.parentTdW = obj.parentNode.clientWidth;
	obj.siblingW = sibling.clientWidth;
	obj.mouseDownX = dx;
	obj.totalWidth = obj.siblingW + obj.parentTdW;
	obj.oldSiblingRight = ECSideUtil.getPosRight(sibling);
	ECSideUtil.Dragobj = obj;
	ECSideUtil.DragobjSibling = sibling;
	ECSideUtil.MinColWidth = $('#' + ECSideX.id).attr('minColWidth') || '30';
	ECSideUtil.Dragobj.style.backgroundColor = "#3366ff";
	ECSideUtil.Dragobj.parentTdW -= ECSideUtil.Dragobj.mouseDownX;
	var cellIndex = ECSideUtil.Dragobj.parentNode.cellIndex;
	try {
		ECSideUtil.DragobjBodyCell = $('#' + ECSideX.id + ' tbody')[0].rows[0].cells[cellIndex];
		ECSideUtil.DragobjBodyCellSibling = ECSideUtil
				.getNextElement(ECSideUtil.DragobjBodyCell);
	} catch (e) {
		ECSideUtil.DragobjBodyCell = null;
	}
};
ECSideUtil.DoResize = function(event) {
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
			ECSideUtil.DragobjBodyCellSibling.width = newSiblingWidth + "px";
		} catch (e) {
		}
	}
};
ECSideUtil.EndResize = function(event) {
	if (ECSideUtil.Dragobj == null) {
		return false;
	}
	ECSideUtil.Dragobj.mouseDownX = 0;
	document.body.style.cursor = "";
	ECSideUtil.Dragobj.style.backgroundColor = "";
	ECSideUtil.Dragobj = null;
	ECSideUtil.DragobjSibling = null;

};

ECSideUtil.editCell = function(cellObj, editType, templateId) {
	if (cellObj.getAttribute("editing") == "true")
		return;
	cellObj.setAttribute("editing", "true");
	var template = document.getElementById(templateId);
	var templateText = $.browser.msie ? template.value : template.textContent;

	var text = $.browser.msie ? cellObj.innerText : cellObj.textContent;
	var value = cellObj.getAttribute("cellValue");
	value = value == null ? text : value;
	var name = cellObj.getAttribute("cellName");
	if (templateText.indexOf("name=\"\"") > 0) {
		templateText = ECSideUtil.replaceAll(templateText, "name=\"\"",
				"name=\"" + name + "\"");
	}
	if (editType == "input") {
		cellObj.innerHTML = ECSideUtil.replaceAll(templateText, "value=\"\"",
				"value=\"" + value + "\"");
	} else if (editType == "select") {
		cellObj.innerHTML = ECSideUtil
				.replaceAll(templateText, "value=\"" + value + "\"", "value=\""
						+ value + "\" selected=\"selected\"");
	} else if (editType == "checkbox" || editType == "radio") {
		cellObj.innerHTML = ECSideUtil.replaceAll(templateText, "value=\""
				+ value + "\"", "value=\"" + value + "\" checked=\"checked\"");
	}
	$('input,select,checkbox', cellObj).focus();
};
ECSideUtil.updateCell = function(cellEditObj, editType) {
	var cellObj = cellEditObj.parentNode;
	var value = '';
	if (editType == "input") {
		value = cellEditObj.value;
		cellObj.innerHTML = cellEditObj.value;
	} else if (editType == "select") {
		value = cellEditObj.options[cellEditObj.selectedIndex].value;
		cellObj.innerHTML = cellEditObj.options[cellEditObj.selectedIndex].text;
	} else if (editType == "checkbox" || editType == "radio") {
		value = cellEditObj.value;
		cellObj.innerHTML = cellEditObj.nextSibling.nodeValue;
	}
	cellObj.setAttribute("cellValue", value);
	cellObj.setAttribute("edited", "true");
	cellObj.parentNode.setAttribute("edited", "true");
	cellObj.setAttribute("editing", "false");
	$(cellObj).addClass("editedCell");
};

Richtable = {
	getBaseUrl : function() {
		var url = document.location.href;
		if (url.indexOf('?') > 0)
			url = url.substring(0, url.indexOf('?'));
		return url;
	},
	getUrl : function(type, id, includeParams) {
		var url = eval('Richtable.' + type + 'Url');
		if (!url)
			url = Richtable.getBaseUrl() + '/' + type;
		if (includeParams)
			url += document.location.search;
		url += (url.indexOf('?') > 0 ? '&' : '?') + 'decorator=simple';
		if (id) {
			if (url.indexOf('{id}') > 0)
				url = url.replace('{id}', id);
			else
				url += '&id=' + id;
		}
		return url;
	},
	reload : function() {
		$('form.richtable').submit();
	},
	input : function(id) {
		Richtable.open(Richtable.getUrl('input', id), true);
	},
	view : function(id) {
		if (!id)
			return;
		Richtable.open(Richtable.getUrl('view', id));
	},
	open : function(url, reloadonclose) {
		if ($('#_window_').length == 0)
			$(
					'<div id="_window_" class="base" title=""><iframe style="width:600px;height:600px;"/></div>')
					.appendTo(document.body);
		url += (url.indexOf('?') > 0 ? '&' : '?') + Math.random();
		$('#_window_ > iframe')[0].src = url;
		if ($('#_window_').attr('_dialoged_')) {
			$("#_window_").dialog('open');
			return;
		}
		$('#_window_').attr('_dialoged_', true);
		$("#_window_").dialog( {
			width :630,
			height :660,
			close :(reloadonclose ? function() {
				Richtable.reload();
			} : null)
		});
	},
	enter : function(parentId, url) {
		if (!url)
			url = Richtable.getBaseUrl();
		if (parentId) {
			if (url.indexOf('{parentId}') > 0)
				url = url.replace('{parentId}', parentId);
			else
				url += (url.indexOf('?') > 0 ? '&' : '?') + 'parentId='
						+ parentId;
		}
		document.location.href = url;
	},
	save : function(id) {
		var arr = [];
		if (id)
			arr[0] = id;
		else
			$.each($('form.richtable tbody')[0].rows, function() {
				if ($(this).attr("edited") == "true")
					arr.push($(this).attr('rowid'))
			});
		$.each(arr, function() {
			var rows = $('form.richtable tbody')[0].rows;
			var row;
			for ( var i = 0; i < rows.length; i++)
				if ($(rows[i]).attr('rowid') == this)
					row = rows[i];
			if (row && row.getAttribute('edited') == 'true') {
				var params = {};
				var entity = Richtable.getBaseUrl();
				entity = entity.substring(entity.lastIndexOf('/') + 1);
				params[entity + '.id'] = this;
				$.each(row.cells,
						function() {
							var name = $(this).attr("cellName");
							if (!name || name == 'null'
									|| $(this).attr('edited') != 'true'
									&& $(this).hasClass('include_if_edited'))
								return;
							var value = $(this).attr('cellValue');
							if (!value || value == 'null')
								value = window.isIE ? this.innerText
										: this.textContent;
							params[name] = value;
						});
				url = Richtable.getBaseUrl() + '/save';
				ajax( {
					url :url,
					type :'POST',
					data :params,
					dataType :'json'
				});
			}
		});
	},
	del : function(id) {
		url = Richtable.getBaseUrl() + '/delete';
		if (id) {
			url += (url.indexOf('?') > 0 ? '&' : '?') + 'id=' + id;
		} else {
			var arr = [];
			$('form.richtable tbody input[type="checkbox"]').each( function() {
				if (this.checked)
					arr.push('id=' + $(this).closest('tr').attr('rowid'));
			});
			if (arr.length == 0)
				return;
			url += (url.indexOf('?') > 0 ? '&' : '?') + arr.join('&');
		}

		ajax( {
			url :url,
			type :'POST',
			dataType :'json',
			complete :Richtable.reload
		});
	},
	execute : function(operation, id) {
		var url = Richtable.getBaseUrl() + '/' + operation;
		url += (url.indexOf('?') > 0 ? '&' : '?') + 'id=' + id;
		ajax( {
			url :url,
			type :'POST',
			dataType :'json',
			complete :Richtable.reload
		});
	},
	updatePasswordCell : function(cellEditObj) {
		var cellObj = cellEditObj.parentNode;
		cellObj.innerHTML = '********';
		cellObj.setAttribute("cellValue", cellEditObj.value);
		cellObj.setAttribute("edited", "true");
		cellObj.parentNode.setAttribute("edited", "true");
		cellObj.setAttribute("editing", "false");
		ECSideUtil.addClass(cellObj, "editedCell");
	}
};
Observation.richtable = function() {
	if ($('form.richtable').length > 0) {
		$('form.richtable .pageNav.firstPage').click( function() {
			$('form.richtable input.jumpPageInput').val(1);
			Richtable.reload()
		});
		$('form.richtable .pageNav.prevPage').click(
				function() {
					$('form.richtable input.jumpPageInput').val(
							parseInt($('form.richtable input.jumpPageInput')
									.val()) - 1);
					Richtable.reload()
				});
		$('form.richtable .pageNav.nextPage').click(
				function() {
					$('form.richtable input.jumpPageInput').val(
							parseInt($('form.richtable input.jumpPageInput')
									.val()) + 1);
					Richtable.reload()
				});
		$('form.richtable .pageNav.lastPage').click(
				function() {
					$('form.richtable input.jumpPageInput').val(
							$("form.richtable .totalPage").text());
					Richtable.reload()
				});
		$('form.richtable .pageNav.jumpPage').click( function() {
			Richtable.reload()
		});
		$('form.richtable input[name="resultPage.pageNo"]').keydown(
				function(event) {
					if (event.keyCode && event.keyCode == 13) {
						Richtable.reload()
					}
				});
		$('form.richtable select[name="resultPage.pageSize"]').change(
				function() {
					Richtable.reload()
				});
	}
};
Initialization.richtable = function() {
	if ($('form.richtable').length > 0) {
		var canResizeColWidth = $('form.richtable').attr('canResizeColWidth');
		if (canResizeColWidth == "true") {
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
	if (document.location.href.indexOf('decorator=simple') > 0) {
		if (!$('form.ajax').hasClass('keepopen')) {
			$('form.ajax button[type="submit"]')
					.click(
							function() {
								$('form.ajax')
										.attr('onsuccess',
												'if(window.parent!=window){window.parent._close_window_=true;}');
							});
		}
		var create = document.location.href.indexOf('input') > 0;
		if (create) {
			if ($('form.ajax input[type="hidden"][name="id"]').val())
				create = false;
			var entity = Richtable.getBaseUrl();
			entity = entity.substring(0, entity.lastIndexOf('/'));
			entity = entity.substring(entity.lastIndexOf('/') + 1);
			if ($('form.ajax input[type="hidden"][name="' + entity + '.id"]')
					.val())
				create = false;
		}
		if (create) {
			$('form.ajax button[type="submit"]')
					.after(
							'<button type="submit" class="btn save_and_create"><span><span>' + MessageBundle
									.get('save.and.create') + '</span></span></button>');
			$('form.ajax .save_and_create').click( function() {
				$('form.ajax').addClass('reset');
			});
		}
	}
};
_close_window_ = false;
setInterval( function() {
	if (_close_window_ && $('#_window_')) {
		$("#_window_").dialog('close');
		_close_window_ = false;
		Richtable.reload();
	}
}, 2000);