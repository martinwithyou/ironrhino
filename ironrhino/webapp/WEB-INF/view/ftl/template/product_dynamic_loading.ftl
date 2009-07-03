<div id="dynamic">
<div id="score"><span id="score_average" class="loading">loading....</span>/10(<span id="score_count" class="loading">loading...</span> votes) 
<#assign x=10>
<#list 1..x as i>
  <a href="${base}/product/score/${product.code}?score=${i}"
		class="ajax" onsuccess="updateScore()" onerror="login()">${i}</a>
</#list> 
</div>

<div id="related_products"  class="loading">
<#list product.relatedProducts as relatedProduct>
	<div><a href="${base}/product/${relatedProduct.code}.html"><img
		width="133" height="100" style=""
		src="${base}/pic/${relatedProduct.code}.small.jpg" />${relatedProduct.name}</a></div>
</#list>
</div>

<div id="comments"  class="loading">
loading......
</div>

<div id="comment_result"></div>			
<form id="comment" name="comment" onsubmit="return true;" action="${base}/product/comment" method="post" class="ajax reset">
<table class="ajax"><input type="hidden" name="id" value="${product.code}" id="comment_code"/>
<tr>
    <td class="tdLabel"><label for="comment.displayName" class="label">comment.displayName:</label></td>

    <td
><input type="text" name="comment.displayName" value="" id="comment.displayName"/>
</td>
</tr>
<tr>
    <td class="tdLabel"><label for="comment.email" class="label">comment.email:</label></td>
    <td
><input type="text" name="comment.email" value="" id="comment.email"/>
</td>
</tr>
<tr>
    <td class="tdLabel"><label for="comment.content" class="label">comment.content:</label></td>
    <td
><textarea name="comment.content" cols="50" rows="4" id="comment.content"></textarea>

</td>
</tr>
<tr>
    <td class="tdLabel"><label for="comment_captcha" class="label">captcha<span class="required">*</span>:</label></td>
    <td
><input type="text" name="captcha" size="6" value="" id="comment_captcha" class="autocomplete_off"/>
</td>
</tr>
<tr>
    <td colspan="2"><div align="right"><input type="submit" id="comment_submit" value="submit"/>
</div></td>
</tr>

</table></form>




<div><img id="captcha" src="${base}/captcha.jpg"/><a
	id="refreshCaptcha" href="#">refresh</a></div>
<div id="send_result"></div>
			
<form id="send" name="send" onsubmit="return true;" action="${base}/product/send" method="post" class="ajax reset">
<table class="ajax"><input type="hidden" name="id" value="${product.code}" id="send_code"/>
<tr>
    <td class="tdLabel"><label for="send_send_name" class="label">send.name:</label></td>
    <td
><input type="text" name="send.name" value="" id="send_send_name"/>

</td>
</tr>
<tr>
    <td class="tdLabel"><label for="send_send_email" class="label">send.email:</label></td>
    <td
><input type="text" name="send.email" value="" id="send_send_email"/>
</td>
</tr>
<tr>
    <td class="tdLabel"><label for="send_send_destination" class="label">send.destination:</label></td>
    <td
><input type="text" name="send.destination" value="" id="send_send_destination"/>
</td>

</tr>
<tr>
    <td class="tdLabel"><label for="send_send_message" class="label">send.message:</label></td>
    <td
><input type="text" name="send.message" size="50" value="" id="send_send_message"/>
</td>
</tr>
<tr>
    <td class="tdLabel"><label for="send_captcha" class="label">captcha<span class="required">*</span>:</label></td>
    <td
><input type="text" name="captcha" size="6" value="" id="send_captcha" class="autocomplete_off"/>

</td>
</tr>
<tr>
    <td colspan="2"><div align="right"><input type="submit" id="send_submit" value="submit"/>
</div></td>
</tr>
</table></form>
</div>
