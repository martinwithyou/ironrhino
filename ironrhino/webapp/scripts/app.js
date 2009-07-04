Observation.cart = function() {
	$('img.product_list').addClass('draggable').each(function() {
				var code = this.alt;
				$(this).draggable({
							helper : 'clone',
							opacity : 0.5,
							zIndex : 10
						});
			});
	$("#cart_items").droppable({
				accept : 'img.product_list',
				hoverClass : 'ondrop',
				drop : function(event, ui) {
					var code = ui.draggable.attr('alt');
					ajax({
								url : CONTEXT_PATH + '/cart/add/' + code,
								type : 'POST',
								replacement : 'cart_items'
							});
				}
			});
};

Initialization.categoryTree = function() {
	$('a.category').each(function() {
				this.onprepare = function() {
					$('a.category').each(function() {
								$(this).removeClass('selected')
							});
					$(this).addClass('selected');
				};
			});
};

function login() {
	// http和https跨域不好处理
	// if ($('#_login_window_').length == 0)
	// $(
	// '<div id="_login_window_" title=""><iframe
	// style="width:400px;height:240px;border:no;"/></div>')
	// .appendTo(document.body);
	// if ($('#_login_window_').attr('_dialoged_')) {
	// $("#_login_window_").dialog('open');
	// return;
	// }
	// var url = $('#login_link').attr('href');
	// url += (url.indexOf('?') > 0 ? '&' : '?')+'decorator=simple';
	// $('#_login_window_ > iframe')[0].src = url;
	// $('#_login_window_').attr('_dialoged_', true);
	// $("#_login_window_").dialog( {
	// width :430,
	// height :300,
	// close : function(){document.location.href=document.location.href}
	// });
};
