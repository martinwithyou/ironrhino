package org.ironrhino.core.jbpm;

import org.jbpm.api.Execution;
import org.jbpm.api.ExecutionService;
import org.jbpm.api.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

public class ProcessServiceImpl implements ProcessService {

	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private ExecutionService executionService;

	public boolean isProcessDeployed(String key) {
		return repositoryService.createProcessDefinitionQuery()
				.processDefinitionKey(key).list().size() > 0;
	}

	public void startProcess(String key) {
		executionService.startProcessInstanceByKey(key);
	}

	public void signal(Execution execution) {
		if (execution.getState().equals(Execution.STATE_INACTIVE_SCOPE))
			execution = execution.getExecutions().iterator().next();
		executionService.signalExecutionById(execution.getId(), "transition");
	}

}