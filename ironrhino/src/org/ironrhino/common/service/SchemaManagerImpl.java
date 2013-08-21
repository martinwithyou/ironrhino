package org.ironrhino.common.service;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ironrhino.common.model.Schema;
import org.ironrhino.core.service.BaseManagerImpl;

@Singleton
@Named
public class SchemaManagerImpl extends BaseManagerImpl<Schema> implements
		SchemaManager {

}
