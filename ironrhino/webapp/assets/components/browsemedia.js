var inputfiles = '<input id="files" type="file" multiple="true" onchange="upload(this.files)" style="width:130px;position:relative;"/>';
$(function(){
	var pageid = window.parent.pageid();
	if(!pageid)return;
	var html = '<li id="browse_tab"><span><a href="javascript:mcTabs.displayTab(\'browse_tab\',\'browse_panel\');" onmousedown="return browse();">浏览</a></span></li>';
	$('#general_tab').after(html);
	html = '<div id="browse_panel" class="panel">加载中...</div>';
	$('#general_panel').after(html);
	var baseurl = window.parent.location.href;
	baseurl = baseurl.substring(0,baseurl.indexOf('/common/'));
	html = inputfiles + '<a href="'+baseurl+'/common/upload?folder=/page/'+pageid+'" style="margin-left:35px;text-decoration:none;font-weight:bold;" target="_blank">管理文件</a>';
	$('#insert').after(html);
	$(document.body).bind('dragover',function(e){return false;})[0].ondrop = function(e){
	var id = e.dataTransfer.getData('Text');
	var target = $(e.target);
	if(!id||target.is('#browse_panel') || target.parents('#browse_panel').length)return true;
	var i = id.lastIndexOf('/');
	if(i>0)id = id.substring(i+1);
	if(e.preventDefault)e.preventDefault();
	if (e.stopPropagation) e.stopPropagation();
	if (confirm('确定删除?')) {
		$.post(baseurl+'/common/upload/delete',{
			folder:'/page/'+pageid,
			id:id
		},browse);
	}
	}
});

function upload(files){
	var pageid = window.parent.pageid();
	var baseurl = window.parent.location.href;
	baseurl = baseurl.substring(0,baseurl.indexOf('/common/'));
	$.ajaxupload(files,{
				url:baseurl+'/common/upload?folder=/page/'+pageid,
				success:browse
			});
}

function browse() {
	var pageid = window.parent.pageid();
	var baseurl = window.parent.location.href;
	baseurl = baseurl.substring(0,baseurl.indexOf('/common/'));
	var panel = $('#browse_panel');
	panel.bind('dragover',function(e){$(this).css('border','2px dashed #333');return false;})
		.bind('dragleave',function(e){$(this).css('border','0');return false;})
		.get(0).ondrop = function(e){
			e.preventDefault();
			$(this).css('border','0');
			upload(e.dataTransfer.files);
			return true;
		};
	$.getJSON(baseurl+'/common/page/files/'+pageid+'?suffix=swf,mpg,fla,wmv,mov,avi', function(data) {
		var html = '';
		 $.each(data, function(key, val) {
   	 	html += '<span onclick="select(this)" style="display:block;float:left;margin:0 5px;width:120px;height:30px;cursor:pointer;text-align:center;" src="'+val+'">'+key+'</span>';
  		});
		panel.html(html);
		$('span',panel).attr('draggable',true).each(function(){
			var t = $(this);
			this.ondragstart = function(e){
			 e.dataTransfer.effectAllowed = 'copy';
      		 e.dataTransfer.setData('Text', t.text());
		};
		});
		$('#files').replaceWith($(inputfiles));
		mcTabs.displayTab('browse_tab', 'browse_panel');
	});
	return false;
}

function select(span) {
	$('#src').val($(span).attr('src'));
	Media.preview();
	mcTabs.displayTab('general_tab', 'general_panel');
}