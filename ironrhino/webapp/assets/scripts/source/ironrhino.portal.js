(function($) {
	$.fn.portal = function() {
		if (arguments.length == 0) {
			this.each(function() {
				$('.portal-column', this).sortable({
							connectWith : '.portal-column'
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

				$('.portal-column', this).disableSelection();
				if (window.localStorage) {
					var layout = localStorage[document.location.pathname
							+ '_portal-layout'];
					if (layout)
						$(this).portal('layout', layout);
				}
				var t = $(this);
				if (t.hasClass('savable')) {
					t
							.append('<div class="portal-footer"><button class="btn save"><span><span>'
									+ MessageBundle.get('save')
									+ '</span></span></button><button class="btn restore"><span><span>'
									+ MessageBundle.get('restore')
									+ '</span></span></button></div>');
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
										$('#' + portlets[j]).attr('sorted',
												true).appendTo(this).show();
									}
								});
					}
					$('.portlet', this).each(function() {
								var t = $(this);
								if (t.attr('sorted'))
									t.removeAttr('sorted');
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