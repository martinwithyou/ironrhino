(function($) {
	$.fn.portal = function() {
		if (arguments.length == 0) {
			this
					.addClass('clearfix')
					.each(
							function() {
								$('.portal-column', this).sortable({
									connectWith : '.portal-column',
									handle : '.portlet-header',
									opacity : 0.6,
									receive : function(event, ui) {
										var col = $(event.target);
										if (col.hasClass('empty'))
											col.removeClass('empty');
									},
									remove : function(event, ui) {
										var col = $(event.target);
										if ($('.portlet', col).length == 0)
											col.addClass('empty');
									}
								});
								$('.portlet', this)
										.find('.portlet-header')
										.append(
												'<div class="portlet-icon"><a class="btn btn-fold"><i class="glyphicon glyphicon-chevron-up"></i></a><a class="btn btn-close"><i class="glyphicon glyphicon-remove"></i></a></div>')
								$('.portlet-header .btn-close', this).click(
										function() {
											$(this).closest('.portlet')
													.remove();
										});
								$('.portlet-header .btn-fold', this)
										.click(
												function() {
													$('i', this)
															.toggleClass(
																	'glyphicon-chevron-up')
															.toggleClass(
																	'glyphicon-chevron-down');
													$(this)
															.parents(
																	'.portlet:first')
															.find(
																	'.portlet-content')
															.toggle();
												});
								$('.portlet', this)
										.each(
												function() {
													if ($('.ajaxpanel', $(this)).length) {
														$(
																'<a class="btn btn-refresh"><i class="glyphicon glyphicon-refresh"></i></a>')
																.insertBefore(
																		$(
																				'.portlet-header .btn-fold',
																				this))
																.click(
																		function() {
																			$(
																					'.ajaxpanel',
																					$(
																							this)
																							.closest(
																									'.portlet'))
																					.trigger(
																							'load');
																		});
													}
												});

								if (window.localStorage) {
									var layout = localStorage[document.location.pathname
											+ '_portal-layout'];
									if (layout)
										$(this).portal('layout', layout);
								}
								var t = $(this);
								if (t.hasClass('savable')) {
									t
											.append('<div class="portal-footer"><button class="btn save">'
													+ MessageBundle.get('save')
													+ '</button></div>');
									if (localStorage
											&& localStorage[document.location.pathname
													+ '_portal-layout'])
										t.find('.portal-footer').append(
												' <button class="btn restore">'
														+ MessageBundle
																.get('restore')
														+ '</button>');
									$('.portal-footer .save', t).click(
											function() {
												t.portal('layout', 'save');
												Message.showMessage('success');
											});
									$('.portal-footer .restore', t).click(
											function() {
												t.portal('layout', 'restore')
											});
								}
							});
			return this;
		}
		if (arguments[0] == 'layout') {
			if (!arguments[1]) {
				var layout = [];
				$('.portal-column', this.eq(0)).each(function() {
					var portlets = [];
					$('.portlet:visible', this).each(function() {
						if ($(this).attr('id'))
							portlets.push('"' + $(this).attr('id') + '"');
					});
					layout.push('[' + portlets.join(',') + ']');
				});
				return '[' + layout.join(',') + ']';
			} else {
				if (arguments[1] == 'save') {
					if (localStorage) {
						localStorage[document.location.pathname
								+ '_portal-layout'] = this.portal('layout');
						var footer = this.eq(0).find('.portal-footer');
						if (!footer.find('.restore').length) {
							footer.append(' <button class="btn restore">'
									+ MessageBundle.get('restore')
									+ '</button>');
							var t = this;
							$('.restore', footer).click(function() {
								t.portal('layout', 'restore')
							});
						}
					}
				} else if (arguments[1] == 'restore') {
					delete localStorage[document.location.pathname
							+ '_portal-layout'];
					document.location.reload();
				} else {
					var layout = $.parseJSON(arguments[1]);
					for ( var i = 0; i < layout.length; i++) {
						$('.portal-column:eq(' + i + ')', this).each(
								function() {
									var portlets = layout[i];
									for ( var j = 0; j < portlets.length; j++) {
										$('#' + portlets[j]).addClass('sorted')
												.appendTo(this).show();
									}
								});
					}
					$('.portlet', this).each(function() {
						var t = $(this);
						if (t.hasClass('sorted'))
							t.removeClass('sorted');
						else
							t.remove();
					});
				}
				return this;
			}
		}
	};
})(jQuery);

Observation._portal = function(container) {
	$('.portal', container).portal();
};