package org.ironrhino.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.ironrhino.common.util.BeanUtils;
import org.ironrhino.common.util.EditAware;
import org.ironrhino.core.model.Model;
import org.ironrhino.core.model.Node;
import org.junit.Test;

public class BeanUtilsTest {

	@Test
	public void testCopyProperties() {
		Model source = new Model();
		source.setId("id");
		source.setName("name");
		source.setValue("value");
		source.setPassword("password");
		source.getRelations().add(new Model());
		Model target = new Model();
		BeanUtils.copyProperties(source, target, "password");
		assertEquals(source, target);
		assertEquals(1, source.getRelations().size());
		assertEquals(0, target.getRelations().size());
		assertNull(target.getPassword());
	}

	@Test
	public void testCopyPropertiesWithEditAware() {
		Model source = new Model();
		source.setId("id");
		source.setName("name");
		source.setValue("value");
		source.setPassword("password");
		source.getRelations().add(new Model());
		Model target = new Model();
		EditAware ea = BeanUtils.copyProperties(source, target, true,
				"password");
		assertEquals(source, target);
		assertEquals(1, source.getRelations().size());
		assertEquals(0, target.getRelations().size());
		assertNull(target.getPassword());
		assertTrue(ea.isEdited("id"));
		assertNull(ea.getOldValue("id"));
		assertFalse(ea.isEdited("password"));
		assertNull(ea.getOldValue("password"));
	}

	@Test
	public void testDeepClone() {
		Node n1 = new Node("n1");
		Node n2 = new Node("n2");
		Node n3 = new Node("n3");
		List<Node> children = new ArrayList<Node>();
		children.add(n2);
		children.add(n3);
		n1.setChildren(children);
		n2.setParent(n1);
		n3.setParent(n1);
		Node clone1 = BeanUtils.deepClone(n1);
		Node clone2 = ((List<Node>) clone1.getChildren()).get(0);
		Node clone3 = ((List<Node>) clone1.getChildren()).get(1);
		assertTrue(n1.equals(clone1));
		assertFalse(n1 == clone1);
		assertTrue(n2.equals(clone2));
		assertFalse(n2 == clone2);
		assertTrue(n3.equals(clone3));
		assertFalse(n3 == clone3);
		assertTrue(n1.equals(clone2.getParent()));
		assertFalse(n1 == clone2.getParent());
		assertTrue(n1.equals(clone3.getParent()));
		assertFalse(n1 == clone3.getParent());

	}
}
