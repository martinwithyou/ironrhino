(function($) {
	$.fn.portal = function() {
		if (arguments.length == 0) {
			this.addClass('clearfix').each(function() {
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
						.addClass('ui-widget ui-widget-content ui-helper-clearfix ui-corner-all')
						.find('.portlet-header')
						.addClass('ui-widget-header ui-corner-all')
						.prepend('<span class="fold ui-icon ui-icon-minusthick"></span>')
						.prepend('<span class="close ui-icon ui-icon-closethick"></span>');
				$('.portlet-header .close', this).click(function() {
							$(this).parents('.portlet:first').hide();
						});
				$('.portlet-header .fold', this).click(function() {
					$(this).toggleClass('ui-icon-minusthick')
							.toggleClass('ui-icon-plusthick');
					$(this).parents('.portlet:first').find('.portlet-content')
							.toggle();
				});
				$('.portlet', this).each(function() {
					if ($('.ajaxpanel', $(this)).length) {
						$('.portlet-header .fold', this)
								.after('<span class="refresh ui-icon ui-icon-refresh"></span>')
								.next().click(function() {
									$('.ajaxpanel', $(this).closest('.portlet'))
											.trigger('load');
								});
					}
				});

				// $('.portal-column', this).disableSelection(); //cannot
				// dropdown selection
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
									+ '</button><button class="btn restore">'
									+ MessageBundle.get('restore')
									+ '</button></div>');
					$('.portal-footer .save', t).click(function() {
								t.portal('layout', 'save');
								Message.showMessage('success');
							});
					$('.portal-footer .restore', t).click(function() {
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
										if (this.id)
											portlets.push('"' + this.id + '"');
									});
							layout.push('[' + portlets.join(',') + ']');
						});
				return '[' + layout.join(',') + ']';
			} else {
				if (arguments[1] == 'save') {
					if (window.localStorage) {
						localStorage[document.location.pathname
								+ '_portal-layout'] = this.portal('layout');
					}
				} else if (arguments[1] == 'restore') {
					delete localStorage[document.location.pathname
							+ '_portal-layout'];
					document.location.reload();
				} else {
					var layout = $.parseJSON(arguments[1]);
					for (var i = 0; i < layout.length; i++) {
						$('.portal-column:eq(' + i + ')', this).each(
								function() {
									var portlets = layout[i];
									for (var j = 0; j < portlets.length; j++) {
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
									t.hide();
							});
				}
				return this;
			}
		}
	};
})(jQuery);

Observation.portal = function(container) {
	$('.portal', container).portal();
};