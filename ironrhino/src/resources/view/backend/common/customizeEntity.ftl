<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>Customize Entities</title>
<script>
Observation.obs = function(){
	$('#customized button.add').each(function(){$(this).click(function(){
	var table=$(this).parents('table')[0];
	var index=$('tr.add',table).length;
	html='<tr class="row add"><td></td><td><input type="text" name="changes['+index+'].name"/></td><td><select name="changes['+index+'].type"><@s.iterator value="@org.ironrhino.core.ext.hibernate.PropertyType@values()"><option value ="<@s.property value="name" />"><@s.property value="displayName" /></option></@s.iterator></select></td><tr>';
	$(html).appendTo(table.tBodies[0]);
	})});
	
	$('#discard').click(function(){
	if($('#changes')[0].tBodies[0].rows.length==0){
	alert('no changes');
	return;
	}
	ApplicationContextConsole.call('customizableEntityChanger.discardChanges()',null,function(){
		ajax({url:document.location.href});
	})});
	$('#apply').click(function(){
	if($('#changes')[0].tBodies[0].rows.length==0){
	alert('no changes');
	return;
	}
	ApplicationContextConsole.call('customizableEntityChanger.applyChanges()',null,function(){
		ajax({url:document.location.href});
	})});
}

</script>
</head>
<body>
<div id="list" style="width: 100%">
<table id="customized" class="sortable">
	<thead>
		<tr>
			<th>entity class</th>
			<th class="nosort">customized properties</th>
		</tr>
	</thead>
	<tbody>
		<@s.iterator value="customizableEntityChanger.customizableEntities">
			<tr class="row">
				<td><@s.property value="key" /></td>
				<td>
				<form method="post" class="ajax view">
				<input type="hidden" name="entityClassName" value="<@s.property value="key" />"/>
				<table class="sortable">
					<thead>
						<tr>
							<th class="nosort"><input type="checkbox" />remove</th>
							<th>property name</th>
							<th>property type</th>
						</tr>
					</thead>
					<tbody>
						<@s.iterator value="value">
							<tr class="row">
								<td><input type="checkbox" name="id"
									value="<@s.property value="key" />" /></td>
								<td><@s.property value="key" /></td>
								<td><@s.property value="value.displayName" /></td>
							</tr>
						</@s.iterator>
					</tbody>
					<tfoot>
						<tr>
							<td align="center" colspan="3">
							<button class="add" type="button">add</button>
							<button>save</button>
							</td>
						</tr>
					</tfoot>
				</table>
				</form>
				</td>
			</tr>
		</@s.iterator>
	</tbody>
</table>

<table id="changes" class="sortable">
	<thead>
		<tr>
			<th>entity class</th>
			<th class="nosort">customized properties</th>
		</tr>
	</thead>
	<tbody>
		<@s.iterator value="customizableEntityChanger.changes">
			<tr class="row">
				<td><@s.property value="key" /></td>
				<td>
				<table class="sortable">
					<thead>
						<tr>
							<th>property name</th>
							<th>property type</th>
							<th>add/remove</th>
						</tr>
					</thead>
					<tbody>
						<@s.iterator value="value">
							<tr class="row">
								<td><@s.property value="name" /></td>
								<td><@s.property value="type.displayName" /></td>
								<td><@s.if test="%{remove}">remove</@s.if><@s.else>add</@s.else></td>
							</tr>
						</@s.iterator>
					</tbody>
				</table>
				</td>
			</tr>
		</@s.iterator>
	</tbody>
	<tfoot>
		<tr>
			<td align="center" colspan="2">
			<button id="discard" class="btn"><span><span>${action.getText('discard')}</span></span></button>
			<button id="apply" class="btn"><span><span>${action.getText('apply')}</span></span></button>
			</td>
		</tr>
	</tfoot>
</table>

</div>

</body>
</html>
