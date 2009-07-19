package org.ironrhino.core.jbpm;

import java.util.List;

import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.ProcessInstance;

public interface ProcessInformationService {

	List<ProcessDefinition> getAllProcessDefinitionKeys();

	public List<ProcessInstance> getAllOpenExecutions();

}