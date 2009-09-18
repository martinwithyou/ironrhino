package org.ironrhino.pms.service;

import org.ironrhino.core.service.BaseManagerImpl;
import org.ironrhino.pms.model.Category;
import org.springframework.stereotype.Component;

@Component("categoryManager")
public class CategoryManagerImpl extends BaseManagerImpl<Category> implements
		CategoryManager {

}
