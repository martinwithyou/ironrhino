MessageBundle = {
	'en' : {
		'ajax.loading' : 'loading...',
		'ajax.error' : 'network error,please try later',
		'required' : 'please input value',
		'selection.required' : 'please select',
		'email' : 'this field must be a valid email',
		'integer' : 'this field must be integer',
		'double' : 'this field must be double',
		'confirm.delete' : 'are you sure to delete?',
		'save.and.create' : 'save and add',
		'add' : 'add',
		'remove' : 'remove'
	},
	'zh-cn' : {
		'ajax.loading' : '正在加载...',
		'ajax.error' : '网络故障,请稍后再试',
		'required' : '必填项,请填写',
		'selection.required' : '必填项,请选择',
		'email' : 'email不合法',
		'integer' : '请填写整数',
		'double' : '请填写数字',
		'confirm.delete' : '确定要删除?',
		'save.and.create' : '保存并新建',
		'add' : '添加',
		'remove' : '删除',
		'browse' : '浏览文件'
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