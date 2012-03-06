package org.ironrhino.core.coordination.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ironrhino.core.coordination.Membership;
import org.ironrhino.core.metadata.DefaultProfile;
import org.ironrhino.core.util.AppInfo;

@Singleton
@Named("membership")
@DefaultProfile
public class StandaloneMembership implements Membership {

	private Map<String, List<String>> groups = new HashMap<String, List<String>>();

	public void join(String group) {
		List<String> members = groups.get(group);
		if (members == null) {
			members = new ArrayList<String>();
			groups.put(group, members);
		}
		String instanceId = AppInfo.getInstanceId();
		if (!members.contains(instanceId))
			members.add(instanceId);
	}

	public void leave(String group) {
		List<String> members = groups.get(group);
		if (members != null) {
			String instanceId = AppInfo.getInstanceId();
			members.remove(instanceId);
		}
	}

	public boolean isLeader(String group) {
		List<String> members = getMembers(group);
		if (members == null || members.isEmpty())
			return false;
		return members.get(0).equals(AppInfo.getInstanceId());
	}

	public List<String> getMembers(String group) {
		return groups.get(group);
	}

}
