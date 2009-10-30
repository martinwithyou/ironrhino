// http://ejohn.org/blog/javascript-micro-templating/
(function($) {
	var cache = {};
	$.fn.tmpl = function(data) {
		var fn = cache[this];
		if (!fn) {
			fn = new Function("obj",
					"var p=[],print=function(){p.push.apply(p,arguments);};" +

							// Introduce the data as local variables using
							// with(){}
							"with(obj){p.push('" +

							// Convert the template into pure JavaScript
							this.html().replace(/[\r\t\n]/g, " ").split("<%")
									.join("\t").replace(/((^|%>)[^\t]*)'/g,
											"$1\r").replace(/\t=(.*?)%>/g,
											"',$1,'").split("\t").join("');")
									.split("%>").join("p.push('").split("\r")
									.join("\\'") + "');}return p.join('');");
			cache[this] = fn;
		}
		return data ? fn(data) : fn;
	}
})(jQuery);