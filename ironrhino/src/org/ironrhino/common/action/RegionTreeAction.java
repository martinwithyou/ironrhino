package org.ironrhino.common.action;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import org.apache.struts2.ServletActionContext;
import org.ironrhino.common.model.Region;
import org.ironrhino.common.support.RegionTreeControl;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.JsonConfig;
import org.ironrhino.core.struts.BaseAction;
import org.ironrhino.core.util.HtmlUtils;

@AutoConfig(namespace = "/", actionName = "region")
public class RegionTreeAction extends BaseAction {

	private static final long serialVersionUID = -1333891551369466096L;

	private Collection<Region> children;

	private boolean async = true;

	private long root;

	@Autowired
	private transient RegionTreeControl regionTreeControl;

	public long getRoot() {
		return root;
	}

	public void setRoot(long root) {
		this.root = root;
	}

	public boolean isAsync() {
		return async;
	}

	public void setAsync(boolean async) {
		this.async = async;
	}

	public Collection<Region> getChildren() {
		return children;
	}

	@JsonConfig(root = "children")
	public String children() {
		Region region;
		if (root < 1)
			region = regionTreeControl.getRegionTree();
		else
			region = regionTreeControl.getRegionTree().getDescendantOrSelfById(
					root);
		if (region != null)
			children = region.getChildren();
		ServletActionContext.getResponse().setHeader("Cache-Control",
				"max-age=86400");
		return JSON;
	}

	@Override
	public String execute() {
		if (!async) {
			Region region;
			if (root < 1)
				region = regionTreeControl.getRegionTree();
			else
				region = regionTreeControl.getRegionTree()
						.getDescendantOrSelfById(root);
			children = region.getChildren();
		}
		return SUCCESS;
	}

	public String table() {
		return "table";
	}

	public Region getRegionTree() {
		return regionTreeControl.getRegionTree();
	}

	public String getTreeViewHtml() {
		return HtmlUtils.getTreeViewHtml(children, async);
	}

}
