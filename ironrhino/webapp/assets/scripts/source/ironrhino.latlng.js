(function($) {
	$.fn.latlng = function() {
		var t = $(this);
		if (!t.parent('.input-append').length)
			t
					.wrap('<div class="input-append"></div>')
					.after('<span class="add-on"><i class="icon-map-marker" style="cursor:pointer;"></i></span>');
		$('.icon-map-marker', t.next()).click(function() {
			window.latlng_input = $(this).parent().prev();
			if (!$('#_maps_window').length) {
				var win = $('<div id="_maps_window" title="'
						+ MessageBundle.get('select')
						+ '"><div id="_maps_container" style="width:500px;height:400px;"></div></div>')
						.appendTo(document.body).dialog({
									minWidth : 520,
									minHeight : 400
								});
				if (typeof google == 'undefined') {
					var script = document.createElement('script');
					script.src = 'https://www.google.com/jsapi?callback=latlng_loadMaps';
					script.type = 'text/javascript';
					document.getElementsByTagName("head")[0]
							.appendChild(script);
				}

			} else {
				$('#_maps_window').dialog('open');
				if (latlng_input.val())
					latlng_createOrMoveMarker(latlng_input.val());
				else {
					latlng_marker.setMap(null);
					latlng_marker = null;
					latlng_map.setZoom(3);
					latlng_map.setCenter(new google.maps.LatLng(35.6622,
							104.0967));
				}
			}
		});
	};

})(jQuery);

var latlng_input;
var latlng_map;
var latlng_marker;
function latlng_loadMaps() {
	if (typeof google != 'undefined' && typeof google.maps == 'undefined') {
		google.load("maps", "3", {
					other_params : 'sensor=true&region=CN',
					'callback' : latlng_initMaps
				});
	}
}
function latlng_initMaps() {
	latlng_map = new google.maps.Map(
			document.getElementById('_maps_container'), {
				zoom : 3,
				mapTypeId : google.maps.MapTypeId.ROADMAP
			});
	if (latlng_input && $(latlng_input).val())
		latlng_createOrMoveMarker(latlng_input.val());
	else
		latlng_map.setCenter(new google.maps.LatLng(35.6622, 104.0967));
	google.maps.event.addListener(latlng_map, 'click', function(event) {
				latlng_createOrMoveMarker(event.latLng);
				latlng_setLatLng(event.latLng);
			});
}
function latlng_createOrMoveMarker(latLng) {
	if (!latLng)
		return;
	if (typeof latLng == 'string') {
		var arr = latLng.split(',');
		latLng = new google.maps.LatLng(parseFloat(arr[0]), parseFloat(arr[1]));
	}
	if (latlng_marker == null) {
		latlng_marker = new google.maps.Marker({
					position : latLng,
					draggable : true,
					map : latlng_map
				});
		google.maps.event.addListener(latlng_marker, "dragend",
				function(event) {
					latlng_setLatLng(event.latLng);
				});
	} else {
		latlng_marker.setPosition(latLng);
	}
	if (latlng_map.getZoom() < 8)
		latlng_map.setZoom(8);
	latlng_map.setCenter(latLng);
}
function latlng_setLatLng(latLng) {
	$(latlng_input).val(latLng.lat() + ',' + latLng.lng());
}
Observation.latlng = function(container) {
	$('input.latlng', container).latlng();
};