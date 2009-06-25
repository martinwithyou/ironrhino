<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>Product Attribute</title>
</head>
<body>
<form action="attribute" method="post" class="ajax view keepopen"><@s.hidden
	name="id" />
<table class="datagrid">
	<thead>
		<tr>
			<th name="attributes[#index].name">name</th>
			<th name="attributes[#index].value">value</th>
			<th>delete</th>
		</tr>
	</thead>
	<tbody>
		<@s.iterator value="attributes" status="stat">
			<tr>
				<td><@s.textfield name="%{'attributes['+#stat.index+'].name'}"
					value="%{name}" theme="simple" /></td>
				<td><@s.textfield name="%{'attributes['+#stat.index+'].value'}"
					value="%{value}" theme="simple" /></td>
				<td>
				<button type="button" class="delete_row btn"><span><span>删除</span></span></button>
				<button type="button" class="add_row btn"><span><span>添加</span></span></button>
				</td>
			</tr>
		</@s.iterator>
	</tbody>
	<tfoot>
		<tr>
			<td colspan="3">
			<button type="button" class="add_row btn"><span><span>添加</span></span></button>
			<button type="button" class="reset btn"><span><span>取消</span></span></button>
			<button type="submit" class="btn"><span><span>保存</span></span></button>
			</td>
		</tr>
	</tfoot>
</table>

</form>
</body>
</html>


