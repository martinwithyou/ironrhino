package com.ironrhino.pms.service;

import org.ironrhino.core.service.BaseManagerImpl;
import org.springframework.stereotype.Component;

import com.ironrhino.pms.model.Category;

@Component("categoryManager")
public class CategoryManagerImpl extends BaseManagerImpl<Category> implements
		CategoryManager {

}
