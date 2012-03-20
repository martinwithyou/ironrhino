1.unzip tiny_mce.zip
2.cp jquery.ajaxupload.js tiny_mce
  cp tiny_mce_popup.js tiny_mce
  cp browseimage.js tiny_mce/plugins/advimage/js/browseimage.js
  cp browsemedia.js tiny_mce/plugins/media/js/browsemedia.js
3.add <script type="text/javascript" src="../../jquery.ajaxupload.js"></script><script type="text/javascript" src="js/browseimage.js"></script> to tiny_mce/plugins/advimage/image.htm
  add <script type="text/javascript" src="../../jquery.ajaxupload.js"></script><script type="text/javascript" src="js/browsemedia.js"></script> to tiny_mce/plugins/media/media.htm