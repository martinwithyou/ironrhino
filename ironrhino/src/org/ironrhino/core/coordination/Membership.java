package org.ironrhino.core.coordination;

import java.util.List;

public interface Membership {

	public void join(String group);

	public void leave(String group);

	public boolean isLeader(String group);

	public String getLeader(String group);

	public List<String> getMembers(String group);

}