<!DOCTYPE html>
<#escape x as x?html><html>
<head>
<title>${action.getText('pageView')}</title>
<!--[if lte IE 8]><script language="javascript" type="text/javascript" src="<@url value="/assets/components/flot/excanvas.js"/>"></script><![endif]-->
<script src="<@url value="/assets/components/flot/jquery.flot.js"/>" type="text/javascript"></script>
<script src="<@url value="/assets/components/flot/jquery.flot.time.js"/>" type="text/javascript"></script>
<script src="<@url value="/assets/components/flot/jquery.flot.pie.js"/>" type="text/javascript"></script>
<script>
function showTooltip(x, y, content) {
	$('#tooltip').remove();
	$('<div id="tooltip">' + content + '</div>').css({
				position : 'absolute',
				display : 'none',
				top : y + 5,
				left : x + 5,
				border : '1px solid #fdd',
				padding : '2px',
				'background-color' : '#fee',
				opacity : 0.80,
				zIndex : 10010
			}).appendTo("body").fadeIn(200);
}

function labelFormatter(label, series) {
		return "<div style='font-size:8pt; text-align:center; padding:2px; color:white;'>" + label + "<br/>" + series.percent.toFixed(1) +"%</div>";
}

Initialization.pageView = function() {
	$('#select_domain').change(function() {
		var domain = $(this).val();
		$('form').each(function(){
			$('input[name="domain"]',this).val(domain);
		});
		$('.ajaxpanel').each(function() {
			var t = $(this);
			var url = t.data('url');
			if(url.indexOf('?')>0){
			var uri = url.substring(0, url.indexOf('?'));
			var query = url.substring(url.indexOf('?') + 1);
			var params = query.split('&');
			var arr = [];
			for (var i = 0; i < params.length; i++) {
				var arr2 = params[i].split('=', 2);
				if (arr2[0] == 'domain') {
					if (arr2[1] == domain)
						return;
				} else {
					arr.push(params[i]);
				}
			}
			if (domain)
				arr.push('domain=' + domain);
			url = uri + '?' + arr.join('&');
			}else if (domain){
				url+='?domain='+domain;
			}
			t.data('url', url);
			t.trigger('load');
		});
	});
}

Observation.pageView = function(container) {

	$('ul.flotlinechart',container).each(function() {
		var ul = $(this);
		var data = [];
		var lies = $('li', ul);
		if (lies.length > 2) {
			lies.each(function() {
						var point = [];
						point.push(parseInt($('span', this).data('time')));
						point.push(parseInt($('strong', this).text()));
						data.push(point);
					});
			$.plot(ul, [data], {
						series : {
							lines : {
								show : true
							},
							points : {
								show : true
							}
						},
						grid : {
							hoverable : true
						},
						xaxis : {
							mode : 'time',
							timeformat : '%m-%d'
						}
					});
			var previousPoint = null;
			ul.bind('plothover', function(event, pos, item) {
						if (item) {
							if (previousPoint != item.dataIndex) {
								previousPoint = item.dataIndex;
								$('#tooltip').remove();
								var x = item.datapoint[0], y = item.datapoint[1];
								var content = '<strong style="margin-right:5px;">'
										+ (ul.hasClass('percent') ? y * 100
												+ '%' : y)
										+ '</strong><span>'
										+ new Date(x).format($(this).data('format')||'%m-%d %H:%M')
										+ '</span>';
								showTooltip(item.pageX, item.pageY, content);
							}
						} else {
							$('#tooltip').remove();
							previousPoint = null;
						}
					});

		}
	});
	
	$('ul.flotbarchart',container).each(function() {
		var ul = $(this);
		var data = [];
		var xticks = [];
		var lies = $('li', ul);
		if (lies.length > 2) {
			lies.each(function() {
						var point = [];
						point.push(parseInt($('span', this).text()));
						point.push(parseInt($('strong', this).text()));
						xticks.push(point[0]);
						data.push(point);
					});
			$.plot(ul, [data], {
						series : {
							bars : {
								show : true
							}
						},
						grid : {
							hoverable : true
						},
						xaxis : {
							ticks: xticks,
							tickLength: 0,
							max: 24
						}
					});
			var previousPoint = null;
			ul.bind('plothover', function(event, pos, item) {
						if (item) {
							if (previousPoint != item.dataIndex) {
								previousPoint = item.dataIndex;
								$('#tooltip').remove();
								var x = item.datapoint[0], y = item.datapoint[1];
								var content = '<strong style="margin-right:5px;">'
										+ y
										+ ' </strong>';
								showTooltip(item.pageX, item.pageY, content);
							}
						} else {
							$('#tooltip').remove();
							previousPoint = null;
						}
					});

		}
	});
	
		$('ul.flotpiechart',container).each(function() {
		var ul = $(this);
		var data = [];
		var lies = $('li', ul);
			lies.each(function() {
						var share = {};
						share.label =$('span', this).text();
						share.data =parseInt($('strong', this).text());
						data.push(share);
					});
			$.plot(ul, data, {
						series: {
				        pie: {
				            show: true,
				            radius: 1,
				            label: {
				                show: true,
				                radius: 2/3,
				                formatter: labelFormatter,
				                threshold: 0.1
				            }
				        }
				    },
				    legend: {
				        show: true
				    },
				    grid: {
				    //hoverable:true,
					clickable: true
					}
					});
					ul.bind('plotclick', function(event, pos, obj) {
					if (!obj) {
						$('#tooltip').remove();
						return;
					}
					showTooltip(pos.pageX,pos.pageY,obj.series.label+': '+obj.series.data[0][1]);
					});
	});

}
</script>
</head>
<body>

<#if domains?? && domains?size gt 0 && !Parameters.domain??>
<div class="clearfix" style="margin-bottom:10px;">
	<span class="pull-right">
	<select id="select_domain">
		<option value=""></option>
		<#list domains as var>
		<option value="${var}"<#if domain?? && domain==var>selected</#if>>${action.getText(var)}</option>
		</#list>
	</select>
	</span>
</div>
</#if>

<div class="row">
<div class="span2 offset2">
<strong>${action.getText('pv')}</strong>
</div>
<div class="span4">
<form class="ajax view form-inline" data-replacement="pv_result">
<@s.hidden name="domain" id=""/>
<span>${action.getText('date')}</span>
<@s.textfield label="%{getText('date')}" theme="simple" id="" name="date" cssClass="date" size="10" maxlength="10"/>
<@s.submit value="%{getText('query')}" theme="simple"/>
</form>
</div>
<div class="span4">
<form class="ajax view form-inline" data-replacement="pv_result">
<@s.hidden name="domain" id=""/>
<span>${action.getText('date')}${action.getText('range')}</span>
<@s.textfield label="%{getText('from')}" theme="simple" id="" name="from" cssClass="date"  size="10" maxlength="10"/>
<i class="icon-arrow-right"></i>
<@s.textfield label="%{getText('to')}" theme="simple" id="" name="to" cssClass="date"  size="10" maxlength="10"/>
<@s.submit value="%{getText('query')}" theme="simple"/>
</form>
</div>
</div>
<div id="pv_result">
<#assign dataurl=getUrl("/common/pageView/pv")/>
<#if request.queryString?has_content>
<#assign dataurl=dataurl+'?'+request.queryString/>
</#if>
<div class="ajaxpanel" data-url="${dataurl}"></div>
</div>

<div class="row" style="padding-top:20px;">
<div class="span2 offset2">
<strong>${action.getText('uip')}</strong>
</div>
<div class="span4 offset4">
<form class="ajax view form-inline" data-replacement="uip_result">
<@s.hidden name="domain" id=""/>
<span>${action.getText('date')}${action.getText('range')}</span>
<@s.textfield label="%{getText('from')}" theme="simple" id="" name="from" cssClass="date"  size="10" maxlength="10"/>
<i class="icon-arrow-right"></i>
<@s.textfield label="%{getText('to')}" theme="simple" id="" name="to" cssClass="date"  size="10" maxlength="10"/>
<@s.submit value="%{getText('query')}" theme="simple"/>
</form>
</div>
</div>
<div id="uip_result">
<#assign dataurl=getUrl("/common/pageView/uip")/>
<#if request.queryString?has_content>
<#assign dataurl=dataurl+'?'+request.queryString/>
</#if>
<div class="ajaxpanel" data-url="${dataurl}"></div>
</div>


<div class="row" style="padding-top:20px;">
<div class="span2 offset2">
<strong>${action.getText('usid')}</strong>
</div>
<div class="span4 offset4">
<form class="ajax view form-inline" data-replacement="usid_result">
<@s.hidden name="domain" id=""/>
<span>${action.getText('date')}${action.getText('range')}</span>
<@s.textfield label="%{getText('from')}" theme="simple" id="" name="from" cssClass="date"  size="10" maxlength="10"/>
<i class="icon-arrow-right"></i>
<@s.textfield label="%{getText('to')}" theme="simple" id="" name="to" cssClass="date"  size="10" maxlength="10"/>
<@s.submit value="%{getText('query')}" theme="simple"/>
</form>
</div>
</div>
<div id="usid_result">
<#assign dataurl=getUrl("/common/pageView/usid")/>
<#if request.queryString?has_content>
<#assign dataurl=dataurl+'?'+request.queryString/>
</#if>
<div class="ajaxpanel" data-url="${dataurl}"></div>
</div>

<div class="row" style="padding-top:20px;">
<div class="span2 offset2">
<strong>${action.getText('uu')}</strong>
</div>
<div class="span4 offset4">
<form class="ajax view form-inline" data-replacement="uu_result">
<@s.hidden name="domain" id=""/>
<span>${action.getText('date')}${action.getText('range')}</span>
<@s.textfield label="%{getText('from')}" theme="simple" id="" name="from" cssClass="date"  size="10" maxlength="10"/>
<i class="icon-arrow-right"></i>
<@s.textfield label="%{getText('to')}" theme="simple" id="" name="to" cssClass="date"  size="10" maxlength="10"/>
<@s.submit value="%{getText('query')}" theme="simple"/>
</form>
</div>
</div>
<div id="uu_result">
<#assign dataurl=getUrl("/common/pageView/uu")/>
<#if request.queryString?has_content>
<#assign dataurl=dataurl+'?'+request.queryString/>
</#if>
<div class="ajaxpanel" data-url="${dataurl}"></div>
</div>


<div class="row" style="padding-top:20px;">
<div class="span2 offset2">
<strong>${action.getText('url')}</strong>
</div>
<div class="span4">
<form class="ajax view form-inline" data-replacement="url_result">
<@s.hidden name="domain" id=""/>
<span>${action.getText('date')}</span>
<@s.textfield label="%{getText('date')}" theme="simple" id="" name="date" cssClass="date" size="10" maxlength="10"/>
<@s.submit value="%{getText('query')}" theme="simple"/>
</form>
</div>
<div class="span4">
<form class="ajax view form-inline" data-replacement="url_result">
<@s.hidden name="domain" id=""/>
<input type="hidden" name="date" value=""/>
<@s.submit value="%{getText('total')}" theme="simple"/>
</form>
</div>
</div>
<div id="url_result">
<#assign dataurl=getUrl("/common/pageView/url")/>
<#if request.queryString?has_content>
<#assign dataurl=dataurl+'?'+request.queryString/>
</#if>
<div class="ajaxpanel" data-url="${dataurl}"></div>
</div>

<div class="row" style="padding-top:20px;">
<div class="span2 offset2">
<strong>${action.getText('province')}</strong>
</div>
<div class="span4">
<form class="ajax view form-inline" data-replacement="pr_result">
<@s.hidden name="domain" id=""/>
<span>${action.getText('date')}</span>
<@s.textfield label="%{getText('date')}" theme="simple" id="" name="date" cssClass="date" size="10" maxlength="10"/>
<@s.submit value="%{getText('query')}" theme="simple"/>
</form>
</div>
<div class="span4">
<form class="ajax view form-inline" data-replacement="pr_result">
<@s.hidden name="domain" id=""/>
<input type="hidden" name="date" value=""/>
<@s.submit value="%{getText('total')}" theme="simple"/>
</form>
</div>
</div>
<div id="pr_result">
<#assign dataurl=getUrl("/common/pageView/pr")/>
<#if request.queryString?has_content>
<#assign dataurl=dataurl+'?'+request.queryString/>
</#if>
<div class="ajaxpanel" data-url="${dataurl}"></div>
</div>


<div class="row" style="padding-top:20px;">
<div class="span2 offset2">
<strong>${action.getText('city')}</strong>
</div>
<div class="span4">
<form class="ajax view form-inline" data-replacement="ct_result">
<@s.hidden name="domain" id=""/>
<span>${action.getText('date')}</span>
<@s.textfield label="%{getText('date')}" theme="simple" id="" name="date" cssClass="date" size="10" maxlength="10"/>
<@s.submit value="%{getText('query')}" theme="simple"/>
</form>
</div>
<div class="span4">
<form class="ajax view form-inline" data-replacement="ct_result">
<@s.hidden name="domain" id=""/>
<input type="hidden" name="date" value=""/>
<@s.submit value="%{getText('total')}" theme="simple"/>
</form>
</div>
</div>
<div id="ct_result">
<#assign dataurl=getUrl("/common/pageView/ct")/>
<#if request.queryString?has_content>
<#assign dataurl=dataurl+'?'+request.queryString/>
</#if>
<div class="ajaxpanel" data-url="${dataurl}"></div>
</div>

<div class="row" style="padding-top:20px;">
<div class="span2 offset2">
<strong>${action.getText('keyword')}</strong>
</div>
<div class="span4">
<form class="ajax view form-inline" data-replacement="kw_result">
<@s.hidden name="domain" id=""/>
<span>${action.getText('date')}</span>
<@s.textfield label="%{getText('date')}" theme="simple" id="" name="date" cssClass="date" size="10" maxlength="10"/>
<@s.submit value="%{getText('query')}" theme="simple"/>
</form>
</div>
<div class="span4">
<form class="ajax view form-inline" data-replacement="kw_result">
<@s.hidden name="domain" id=""/>
<input type="hidden" name="date" value=""/>
<@s.submit value="%{getText('total')}" theme="simple"/>
</form>
</div>
</div>
<div id="kw_result">
<#assign dataurl=getUrl("/common/pageView/kw")/>
<#if request.queryString?has_content>
<#assign dataurl=dataurl+'?'+request.queryString/>
</#if>
<div class="ajaxpanel" data-url="${dataurl}"></div>
</div>

<div class="row" style="padding-top:20px;">
<div class="span2 offset2">
<strong>${action.getText('searchengine')}</strong>
</div>
<div class="span4">
<form class="ajax view form-inline" data-replacement="se_result">
<@s.hidden name="domain" id=""/>
<span>${action.getText('date')}</span>
<@s.textfield label="%{getText('date')}" theme="simple" id="" name="date" cssClass="date" size="10" maxlength="10"/>
<@s.submit value="%{getText('query')}" theme="simple"/>
</form>
</div>
<div class="span4">
<form class="ajax view form-inline" data-replacement="se_result">
<@s.hidden name="domain" id=""/>
<input type="hidden" name="date" value=""/>
<@s.submit value="%{getText('total')}" theme="simple"/>
</form>
</div>
</div>
<div id="se_result">
<#assign dataurl=getUrl("/common/pageView/se")/>
<#if request.queryString?has_content>
<#assign dataurl=dataurl+'?'+request.queryString/>
</#if>
<div class="ajaxpanel" data-url="${dataurl}"></div>
</div>

</body>
</html></#escape>
