(function($) {
	function submit(form) {
		var pushstate = false;
		if (form.hasClass('history'))
			pushstate = true;
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
		form.submit();
	}
	$(function() {
				$(document).on('click',
						'.toolbar .pagination .firstPage:not(.disabled) a',
						function(event) {
							var form = $(this).closest('form');
							$('.inputPage', form).val(1);
							submit(form);
							return false;
						}).on('click',
						'.toolbar .pagination .prevPage:not(.disabled) a',
						function(event) {
							var form = $(this).closest('form');
							$('.inputPage', form).val(function(i, v) {
										return parseInt(v) - 1
									});
							submit(form);
							return false;
						}).on('click',
						'.toolbar .pagination .nextPage:not(.disabled) a',
						function(event) {
							var form = $(this).closest('form');
							$('.inputPage', form).val(function(i, v) {
										return parseInt(v) + 1
									});
							submit(form);
							return false;
						}).on('click',
						'.toolbar .pagination .lastPage:not(.disabled) a',
						function(event) {
							var form = $(this).closest('form');
							$('.inputPage', form).val($('.totalPage strong',
									form).text());
							submit(form);
							return false;
						}).on('change', '.toolbar .pagination .inputPage',
						function(event) {
							var form = $(event.target).closest('form');
							submit(form);
							event.preventDefault();
						}).on('change', '.toolbar .pagination select.pageSize',
						function(event) {
							var form = $(event.target).closest('form');
							$('.inputPage', form).val(1);
							submit(form);
						}).on('keydown', '.toolbar input[name="keyword"]',
						function(event) {
							var form = $(event.target).closest('form');
							if (event.keyCode == 13) {
								$('.inputPage', form).val(1);
								submit(form);
								return false;
							}
						}).on('click', '.toolbar .icon-search',
						function(event) {
							var form = $(event.target).closest('form');
							$('.inputPage', form).val(1);
							submit(form);
							return false;
						});

			});
})(jQuery);