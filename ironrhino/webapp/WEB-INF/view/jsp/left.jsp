<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="http://code.google.com/p/ironrhino" prefix="ir"%>
<c:if
	test="${(pageContext.request.scheme!='https')&&fn:indexOf(pageContext.request.servletPath,'cart')<0}">
	<s:set name="current" value="uid" />
	<s:iterator value="%{categoryTreeControl.categoryTree.children}">
		<div class="category_top round_corner">
		<div class="category_top_title"><a
			href="<s:url value="%{'/product/list/'+code}"/>"
			class="ajax view category<s:if test="%{code==#current}"> selected</s:if>"><s:property
			value="name" /></a></div>
		<s:if test="%{children.size()>0}">
			<div class="category_second round_corner"><s:iterator
				value="%{children}">
				<div class="category_third"><span
					class="category_second_title"><a
					href="<s:url value="%{'/product/list/'+code}"/>"
					class="ajax view category<s:if test="%{code==#current}"> selected</s:if>"><s:property
					value="name" /></a></span><s:if test="%{children.size()>0}">:<s:iterator
						value="%{children}" status="status">
						<span><a href="<s:url value="%{'/product/list/'+code}"/>"
							class="ajax view category<s:if test="%{code==#current}"> selected</s:if>"><s:property
							value="name" /></a></span>
						<s:if test="%{!status.last}">|</s:if>
					</s:iterator>
				</s:if></div>
			</s:iterator></div>
		</s:if></div>
		<div class="blankline"></div>
	</s:iterator>
	<div class="category_top round_corner">
	<div class="category_top_title"><a
		href="<s:url value="/product/list/null"/>"
		class="ajax view category<s:if test="%{'null'==#current}"> selected</s:if>">未分类产品</a></div>
	</div>
	<div class="blankline"></div>
	<div class="category_top round_corner">
	<div class="category_top_title"><a
		href="<s:url value="/product/list/history"/>"
		class="ajax view category<s:if test="%{'history'==#current}"> selected</s:if>">最近浏览过的产品</a></div>
	</div>
</c:if>