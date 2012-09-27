<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title>${action.getText('region')}${action.getText('map')}</title>
<script type="text/javascript" src="http://maps.google.com/maps/api/js?sensor=true&region=CN"></script> 
<script type="text/javascript"> 

var map;
  
function initialize() {
  map = new google.maps.Map(document.getElementById("map_container"), {
    zoom: ${Parameters.zoom!8},
<#if Parameters.lat??>
    center: new google.maps.LatLng(${Parameters.lat!}, ${Parameters.lng!}),
</#if>
    mapTypeId: google.maps.MapTypeId.ROADMAP
  });
<#if !Parameters.lat??>
  if(navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(function(position) {
      map.setCenter(new google.maps.LatLng(position.coords.latitude,position.coords.longitude));
    }, function() {
       map.setCenter(new google.maps.LatLng(39.8954, 116.4087));
    });
  }
</#if>
	google.maps.event.addListener(map, 'idle', getMarkers);
	setTimeout(getMarkers,1000);
}

function getMarkers(){
	var bounds = map.getBounds();
	var southWest = bounds.getSouthWest();
	var northEast = bounds.getNorthEast();
	var url = '<@url value="/common/region/markers?southWest="/>'+southWest.lat()+','+southWest.lng()+'&northEast='+northEast.lat()+','+northEast.lng()+'&zoom='+map.getZoom();
	$.ajax({
		url:url, 
		global:false,
		dataType:'json',
		success:function(regions) {
			for (var i = 0; i < regions.length; i++) 
				addMarker(regions[i]);
			}
	});
}

function addMarker(region){
		var marker = new google.maps.Marker({
		      position: new google.maps.LatLng(region.coordinate.latitude,region.coordinate.longitude), 
		      map: map, 
		      title:region.name
		}); 
    	google.maps.event.addListener(marker, "click", function() {
	    	  var infowindow = new google.maps.InfoWindow();
		      infowindow.setContent(region.name);
		      infowindow.setPosition(marker.getPosition());
		      infowindow.open(map);
  		});
}

function moveTo(region){
	if(region.coordinate && region.coordinate.latitude){
		map.panTo(new google.maps.LatLng(region.coordinate.latitude,region.coordinate.longitude));
		addMarker(region);
	}else{
		alert('mot mark yet');
	}
}

function mark(region){
	if(!confirm('map center to '+region.name+'?'))
		return;
	region.coordinate = {
		latitude:map.getCenter().lat(),
		longitude:map.getCenter().lng()
	};
	var data = {
		'region.id':region.id,
		'region.coordinate.latitude':region.coordinate.latitude,
		'region.coordinate.longitude':region.coordinate.longitude,
	}	
	$.ajax({url:'<@url value="/common/region/mark"/>',data:data,global:false,success:function(resp){if(resp.actionMessages)addMarker(region)}});
}



$(function(){
	$("#regionTree").treeview({
		url: '<@url value="/region/children"/>'+ '?r=' + Math.random(),
		click:function(){
			var region = $(this).closest('li').data('treenode');
			if($('.moveTo').hasClass('active')){
				moveTo(region);
			}else{
				mark(region);
			}
		},
		collapsed: true,
		unique: true
	});
	initialize();
});
</script> 
</head> 
<body>
<div class="clearfix">
  <div style="float: left; width: 20%;height: 600px;overflow:scroll;">
	<div class="btn-group switch" style="margin-bottom:10px;">
	  <button class="btn active moveTo">${action.getText("move")}</button>
	  <button class="btn mark">${action.getText("mark")}</button>
	</div>
	<div id="regionTree"></div>
	</div>
	<div style="float: left; width: 80%;">
	<div id="map_container" style="height: 600px;"></div>
	</div>
</div>
</body> 
</html></#escape>