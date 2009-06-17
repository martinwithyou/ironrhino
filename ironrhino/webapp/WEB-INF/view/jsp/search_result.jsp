<%@ page contentType="text/html; charset=utf-8" language="java"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN" lang="zh-CN">
<head>
<title>search</title>
</head>
<body>
<div id="search_result"><s:if test="%{searchResults!=null}">
 Search took <s:property value="%{searchResults.searchTime}" />ms
 <s:if test="%{searchResults.hits!=null}">
		<s:iterator value="searchResults.hits">
			<p><a href="<s:url value="%{'/product/'+data.code+'.html'}"/>"><s:property
				value="data.name" /></a> (<s:property value="%{formatScore(score)}" />)<br />
			<s:property value="data.shortDescription" /></p>
		</s:iterator>
		<s:if test="%{searchResults.pages!=null}">
			<p><s:iterator value="searchResults.pages" status="status">
				<s:if test="%{selected}">
					<s:property value="%{from}" />-<s:property value="%{to}" />
				</s:if>
				<s:else>
					<s:form action="search" method="get">
						<s:hidden name="q" />
						<s:hidden name="ps" />
						<s:hidden name="pn" value="%{#status.index+1}" />
						<s:submit value="%{from+'-'+to}" cssClass="link"/>
					</s:form>
				</s:else>
			</s:iterator></p>
		</s:if>
	</s:if>
	<s:else>
	No matched result
	</s:else>
</s:if></div>
</body>
</html>
