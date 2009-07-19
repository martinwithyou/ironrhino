package org.ironrhino.core.jbpm;

import org.jbpm.api.Execution;

public interface ProcessService {

	public boolean isProcessDeployed(String key);

	public void startProcess(String key);

	public void signal(Execution execution);

}