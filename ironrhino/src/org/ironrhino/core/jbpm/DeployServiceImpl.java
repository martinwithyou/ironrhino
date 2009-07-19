package org.ironrhino.core.jbpm;

import org.jbpm.api.NewDeployment;
import org.jbpm.api.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

public class DeployServiceImpl implements DeployService {

	@Autowired
	private RepositoryService repositoryService;

	public void deploy(String processDefinition) {
		NewDeployment deployment = repositoryService.createDeployment();
		deployment.addResourceFromClasspath(processDefinition);
		deployment.deploy();
	}

}
