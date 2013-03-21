<ul class="nav">
  <li><a href="<@url value="/"/>">${action.getText('index')}</a></li>
  <@authorize ifAnyGranted="ROLE_ADMINISTRATOR">
  <li><a href="<@url value="/user"/>">${action.getText('user')}</a></li>
  <li><a href="<@url value="/common/region"/>">${action.getText('region')}</a></li>
  <li><a href="<@url value="/common/treeNode"/>">${action.getText('treeNode')}</a></li>
  <li><a href="<@url value="/common/setting"/>">${action.getText('setting')}</a></li>
  <li><a href="<@url value="/common/dictionary"/>">${action.getText('dictionary')}</a></li>
  <li><a href="<@url value="/common/schema"/>">${action.getText('schema')}</a></li>
  <li><a href="<@url value="/common/page"/>">${action.getText('page')}</a></li>
  <li><a href="<@url value="/common/upload"/>">${action.getText('upload')}</a></li>
  <li><a href="<@url value="/common/console"/>">${action.getText('console')}</a></li>
  </@authorize>
</ul>