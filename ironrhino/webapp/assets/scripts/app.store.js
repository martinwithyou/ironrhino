$.extend($.jStore.defaults, {
			flash : CONTEXT_PATH + '/assets/images/jStore.Flash.html'
		});

/*
Initialization.store = function() {
	$.jStore.load();
	setTimeout(function() {
		$.jStore.use('flash', CONTEXT_PATH, 'cart');
		try {
			var counter = ($.jStore.get('counter') || 0) * 1, original = counter;
			counter++;
			alert('Original Value: ' + original + "\n" + 'New Value: '
					+ counter + "\n" + 'Setting Value: '
					+ $.jStore.set('counter', counter) + "\n"
					+ 'Fetching Value: ' + $.jStore.get('counter'));
		} catch (e) {
		}
		if (counter >= 5) {
			var lastValue = $.jStore.remove('counter');
		}
	}, 2000);
};
*/