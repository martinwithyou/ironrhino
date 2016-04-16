# 对采用ironrhino有顾虑? #
ironrhino是基于spring,struts2,hibernate等的一个集成框架,还有对它们有一些扩展,ironrhino非侵入式的,对开发者来说编程模型并没有改变,因此不要担心需要重新熟悉一个框架,对性能安全可测试性等担心跟ironrhino也没有多大关系.



# 做了哪些扩展? #

struts2:
  * 自定义了ActionMapper用来构造更美观的url,还有增加了一些新的通用的result类型.
  * 提供了一个ironrhino-default的包和一个BaseAction用来集成,主要是对ajax的支持,此功能跟浏览器端的基于jquery的js是耦合的,但是对于开发者来说是透明的,开发者写的action不需要做特殊处理,页面也只需要加上一些class就可以获得ajax支持,当然也可以不加.
  * 对struts2的配置自动化,不需要xml配置文件,只需要为Action或者实体类增加一个annotation,跟struts2的codebehind相比简单了很多,因为ironrhino考虑了约定俗成而不需要像codebehind那样把xml的配置用annotaion重新实现一遍.

hibernate:
  * 提供了一些接口和基础类给实体类继承实现,不需要自己覆盖hashCode equals这些方法,提供了树形实体的支持,提供了基础的BaseManager供继承或直接使用,此BaseManager具有公用的查询功能比如分页,分页的时候一般在struts action里面自动注入了参数,不需要对分页做特殊处理.
  * 内置了两个spring aop aspect可以记录和发布实体的改变.
  * 可以运行期动态增加或者删除实体类的动态属性,注意不是动态增加删除实体类,只是修改实体类里面的动态属性,ironrhino提供了界面可以配置动态属性,此功能是简单的修改了hibernate的源码(源码里面有注释,就是简单的增加了几个方法)而不是扩展hibernate(hibernate的扩展性太差),因此需要保证这几个类优先hibernate的类被加载,最好是放在classes里面不要打入jar包.

spring security:
  * 使用spring aop可以运行期动态修改配置.
  * rememberme可以选择记住多久时间.
  * 更安全的PasswordEncoder.
  * 可以配置参数名,增加ANY\_CHANNEL等功能,这些已加入官方代码,可以参考http://jira.springframework.org/secure/IssueNavigator.jspa?reset=true&reporterSelect=specificuser&reporter=quaff

其它:
  * 用dwr提供了一个通用的接口可以在页面上直接调用spring里面bean的方法供后台管理使用,因为安全原因限制了只能管理员角色使用,管理员角色请参考spring security.
  * 发送邮件提供了使用activemq异步发送功能,如果没有配置activemq则使用同步发送,代码不需要改变.
  * 提供了struts2的fckeditor的connector.
  * 为ehcache提供了一个类似oscache的jsp tag,方便对jsp页面局部做缓存
  * 用ecside作为列表展示组件,对ecside的js做了扩展支持配合ironrhino的ajax提交等功能.服务器端action代码可以完全不依赖ecside只是从request里面取参数和设置属性,在服务器端freemarker做了模板因此在服务器端可以不依赖ecside,请freemarker做的模板不是完全取代jsp tag,此模板依赖ironrhino,并且只提供ironrhino需要的功能,鉴于ecside处于不活跃状态并且需要更多的浏览器端功能支持,将来可能采用extjs取代ecside.

小功能:
  * 提供了ip查询,从lumaqq里面挖出来的,使用纯真的ip数据库.
  * 提供了把中国各个省市区县的功能,数据来源于06年的维基百科,有些地名可能需要更新.
  * 提供了openid的登录.