<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>Region Map</title>
<#if Parameters.type?if_exists=='satellite'>
	<script src="http://maps.google.com/maps?file=api&amp;v=2"
		type="text/javascript"></script>
<#else>
	<script src="http://ditu.google.com/maps?file=api&amp;v=2"
		type="text/javascript"></script>
</#if>
<script type="text/javascript">
var lat = ${Parameters.lat?default(22.5162)?html};
var lng = ${Parameters.lng?default(114.050128)?html};
var zoom = ${Parameters.zoom?default(8)?html};
var map;
var mgr;
$(window).unload(GUnload);

	function _click(){
		
	}
	Initialization.treeview= function(){
		$("#treeview").treeview({
			url: "${base}/region/children",
			click:_click,
			collapsed: true,
			unique: true

		});
		}
		
Initialization.GLoad=function(){
return;
		map = new GMap2($('map_container'));
		map.setCenter(new GLatLng(lat, lng), zoom);
		map.addControl(new GLargeMapControl());
		map.addControl(new GOverviewMapControl());
<#if Parameters.type?exists&&'satellite'==Parameters.type>
		map.addControl(new GMapTypeControl());
		map.setMapType(G_SATELLITE_MAP);
</#if>
		//map.enableDoubleClickZoom();
		//map.enableScrollWheelZoom();
		mgr = new GMarkerManager(map);
		mark();
		GEvent.addListener(map, 'moveend', mark);
		GEvent.addListener(map, 'singlerightclick', function(point){map.panTo(gpoint2glatlng(point))});
}

function gpoint2glatlng(point){
var lat1=map.getBounds().getNorthEast().lat();
var lng1=map.getBounds().getSouthWest().lng();
var lat2=map.getBounds().getSouthWest().lat();
var lng2=map.getBounds().getNorthEast().lng();
var height=map.getSize().height;
lng1=lng1+(point.x/map.getSize().width)*(lng2-lng1);
lat1=lat1+(point.y/map.getSize().height)*(lat2-lat1);
return new GLatLng(lat1,lng1);
}

function mark(){
		var bounds = map.getBounds();
		var southWest = bounds.getSouthWest();
		var northEast = bounds.getNorthEast();
		GDownloadUrl('${base}/backend/common/region/mark?southWest='+southWest.lat()+','+southWest.lng()+'&northEast='+northEast.lat()+','+northEast.lng()+'&zoom='+map.getZoom(), function(data, responseCode) {
        var markers = eval('('+data+')');
  		for (var i = 0; i < markers.length; i++) 
  			addMarker(markers[i]);
		});
}

function addMarker(region){
		var point = new GLatLng(region.latitude,region.longitude);
    	var marker = new GMarker(point);
    	var level = region.level;
    	if(level<=1)
    		mgr.addMarker(marker);
    	else if(level<=2)
    		mgr.addMarker(marker,5);
    	else
    		mgr.addMarker(marker,7);
    	mgr.refresh();
    	GEvent.addListener(marker, "click", function() {
    		marker.openInfoWindow(document.createTextNode(region.name));
  		});
}

function moveTo(region){
if(region.latitude)
	map.panTo(new GLatLng(region.latitude,region.longitude));
}

function saveLatLng(region){
if(!confirm('map center to '+region.name+'?'))
return;
region.latitude=map.getCenter().lat();
region.longitude=map.getCenter().lng();
var url='${base}/backend/common/region/save?region.id='+region.id+'&region.latitude='+region.latitude+'&region.longitude='+region.longitude;
new $.ajax({url:url,dataType:true,success:function(){addMarker(region)}});
}

function switchTo(type){
url='${base}/backend/common/region/map?'+(type?'type='+type:'')+'&lat='+map.getCenter().lat()+'&lng='+map.getCenter().lng()+'&zoom='+map.getZoom();
window.location.href=url;
}
</script>
</head>
<body>
<div style="float: left; width: 20%;">
<div><a class="link" onclick="moveTo()">move mode</a> <a class="link" onclick="saveLatLng()">mark mode</a></div>
<@s.action var="region" namespace="/" name="region" executeResult="false"/>
<@s.property value="#attr.region.treeViewHtml" escape="false" />
</div>
<div style="float: left; width: 80%;">
<div><#if Parameters.type?exists&&'satellite'==Parameters.type>
	<a class="link" onclick="switchTo('map')">map </a>
<#else>
	<a class="link" onclick="switchTo('satellite')">satellite</a>
</#if></div>
<div id="map_container" style="height: 600px;"></div>
</div>
</body>
</html>
