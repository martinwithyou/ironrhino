( function($) {
	SearchHighlighter = {
		init : function() {
			rf = document.referrer;
			if (window.location.href.indexOf('/search') > 0)
				rf = window.location.href;
			keywords = null;
			if (rf && rf.indexOf('?') > 0) {
				name = 'q';
				value = '';
				if (rf.indexOf('yahoo.com') > 0)
					name = 'p';
				// else if(rf.indexOf('baidu.com')>0)
		// param='wd';
		arr = rf.substring(rf.indexOf('?') + 1).split("&");
		for (i = 0; i < arr.length; i++) {
			ar = arr[i].split('=');
			if (ar[0] == name) {
				value = ar[1] || '';
				break;
			}
		}
		keywords = decodeURIComponent(value.replace(/\+/g, ' '));
		if (keywords) {
			if ($('q'))
				$('q').value = keywords;
			keywords = keywords.replace(/<|>/, ' ');
			keywords = keywords.split(/\s+/);
			for (i = 0; i < keywords.length; i++)
				SearchHighlighter.highlight(document.body, keywords[i]);
		}
	}
},
highlight : function(node, word) {
	if (node.hasChildNodes) {
		for ( var i = 0; i < node.childNodes.length; i++)
			SearchHighlighter.highlight(node.childNodes[i], word);
	}
	if (node.nodeType == 3) {
		text = node.nodeValue.toLowerCase();
		word = word.toLowerCase();
		if (text.indexOf(word) != -1) {
			pn = node.parentNode;
			if (pn.className != "highlight") {
				nv = node.nodeValue;
				ni = text.indexOf(word);
				before = document.createTextNode(nv.substr(0, ni));
				docWordVal = nv.substr(ni, word.length);
				after = document.createTextNode(nv.substr(ni + word.length));
				hiwordtext = document.createTextNode(docWordVal);
				hiword = document.createElement("span");
				hiword.className = "highlight";
				hiword.appendChild(hiwordtext);
				pn.insertBefore(before, node);
				pn.insertBefore(hiword, node);
				pn.insertBefore(after, node);
				pn.removeChild(node);
			}
		}
	}
}
	}

	$( function() {
		SearchHighlighter.init()
	});
})(jQuery);