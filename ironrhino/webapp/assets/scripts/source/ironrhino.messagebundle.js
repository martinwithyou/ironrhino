MessageBundle = {
	'en' : {
		'ajax.loading' : 'loading...',
		'ajax.error' : 'network error,please try later',
		'required' : 'please input value',
		'selection.required' : 'please select',
		'email' : 'must be a valid email',
		'integer' : 'must be a integer',
		'integer.positive' : 'must be a positive integer',
		'double' : 'must be a decimal',
		'double.positive' : 'must be a positive decimal',
		'confirm.delete' : 'sure to delete?',
		'save.and.create' : 'save and add',
		'no.selection' : 'no selection',
		'no.modification' : 'no modification',
		'select' : 'please select'
	},
	'zh-cn' : {
		'ajax.loading' : '正在加载...',
		'ajax.error' : '网络故障,请稍后再试',
		'required' : '请填写',
		'selection.required' : '请选择',
		'email' : 'email不合法',
		'integer' : '请填写整数',
		'integer.positive' : '请填写正整数',
		'double' : '请填写数字',
		'double.positive' : '请填写大于零的数字',
		'confirm.delete' : '确定要删除?',
		'save.and.create' : '保存并新建',
		'no.selection' : '没有选中',
		'no.modification' : '没有更改',
		'add' : '添加',
		'remove' : '删除',
		'browse' : '浏览文件',
		'select' : '请选择',
		'save' : '保存',
		'restore' : '还原',
		'confirm' : '确定',
		'cancel' : '取消',
		'error' : '错误',
		'success' : '操作成功'
	},
	get : function() {
		var key = arguments[0];
		var lang = (navigator.language || navigator.browserLanguage || '')
				.toLowerCase();
		if (!MessageBundle[lang])
			lang = 'en';
		var msg = MessageBundle[lang][key];
		if (typeof(msg) == 'undefined')
			msg = key;
		for (var i = 1; i < arguments.length; i++)
			msg = msg.replace('{' + i + '}', arguments[i]);
		return msg;
	}
};