package org.ironrhino.core.jbpm;

import java.util.List;

import org.jbpm.api.ExecutionService;
import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

public class ProcessInformationServiceImpl implements ProcessInformationService {

	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private ExecutionService executionService;

	public List<ProcessDefinition> getAllProcessDefinitionKeys() {
		return repositoryService.createProcessDefinitionQuery().list();
	}

	public List<ProcessInstance> getAllOpenExecutions() {
		return executionService.createProcessInstanceQuery().list();
	}

}