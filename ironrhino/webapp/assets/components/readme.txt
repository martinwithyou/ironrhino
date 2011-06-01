unzip tiny_mce.zip
1.replace tiny_mce/tiny_mce_popup.js with tiny_mce_popup.js
2.cp browseimage.js tiny_mce/plugins/advimage/js/browseimage.js
add <script type="text/javascript" src="js/browseimage.js"></script> to tiny_mce/plugins/advimage/image.htm
3.cp browsemedia.js tiny_mce/plugins/media/js/browsemedia.js
add <script type="text/javascript" src="js/browsemedia.js"></script> to tiny_mce/plugins/media/media.htm