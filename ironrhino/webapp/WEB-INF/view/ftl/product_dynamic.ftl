<div id="dynamic">

<div id="score">
<div>当前平均分:<span id="score_average">${scoreResult.average}</span>(<span id="score_count">${scoreResult.count}</span>次打分)
</div>
<ul class="unit-rating">
<#assign x=10>
<li class="current-rating" style="width: 300px;"></li>
<#list 1..x as i>
<li><a href="${base}/product/score/${product.code}?score=${i}" title="${i}"
		class="ajax r${i}-unit" onsuccess="updateScore()" onerror="login()">${i}</a></li>
</#list>
</ul>
</div>

<div class="blankline"  style="height: 40px;"></div>

<div id="related_products">
<#list product.relatedProducts as relatedProduct>
	<div><a href="${base}/product/${relatedProduct.code}.html"><img
		width="133" height="100" style=""
		src="${base}/pic/${relatedProduct.code}.small.jpg" />${relatedProduct.name}</a></div>
</#list>
</div>

<div id="comments">
<#list resultPage.result as comment>
	<div>${comment.displayName} says:${comment.content}</div>
</#list> 
<#if (resultPage.totalPage>1)>
	<div align="left">total records:${resultPage.totalRecord}| <#if !resultPage.first>
		<a
			href="${request.requestURI+resultPage.renderUrl(1)}"
			class="ajax_view" replacement="comments">First</a>|
	<a
			href="${request.requestURI+resultPage.renderUrl(resultPage.previousPage)}"
			class="ajax_view" replacement="comments">Previous</a>
	<#else>First|Previous</#if>
	| <#if !resultPage.last>
		<a
			href="${request.requestURI+resultPage.renderUrl(resultPage.nextPage)}"
			class="ajax_view" replacement="comments">Next</a>|
	<a
			href="${request.requestURI+resultPage.renderUrl(resultPage.totalPage)}"
			class="ajax_view" replacement="comments">Last</a>
	<#else>Next|Last</#if>
	 |${resultPage.pageNo} / ${resultPage.totalPage}</div>
</#if>
</div>

<div id="comment_result"></div>
<@s.form id="comment" name="comment" action="product!comment"
	method="post" cssClass="ajax reset">
	<@s.hidden name="id" value="${product.code}" />
	<@s.textfield id="comment.displayName"
		label="${action.getText('displayName')}" name="comment.displayName" />
	<@s.textfield id="comment.email" label="${action.getText('email')}"
		name="comment.email" />
	<@s.textarea id="comment.content" label="${action.getText('content')}"
		name="comment.content" cols="50" rows="4" />
	<@s.textfield label="${action.getText('captcha')}" name="captcha" size="6" cssClass="autocomplete_off required" />
	<@s.submit id="comment_submit" value="submit" />
</@s.form>

<div><img id="captcha" src="${base}/captcha.jpg" class="captcha"/></div>
<div id="send_result"></div>
<@s.form id="send" name="send" action="product!send" method="post"
	cssClass="ajax reset">
	<@s.hidden name="id" value="${product.code}" />
	<@s.textfield label="${action.getText('name')}" name="send.name" />
	<@s.textfield label="${action.getText('email')}" name="send.email" />
	<@s.textfield label="${action.getText('destination')}"
		name="send.destination" />
	<@s.textfield label="${action.getText('message')}" name="send.message"
		size="50" />
	<@s.textfield label="${action.getText('captcha')}" name="captcha" size="6" cssClass="autocomplete_off required"  />
	<@s.submit id="send_submit" value="submit" />
</@s.form>
</div>