package org.ironrhino.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import org.ironrhino.common.util.AnnotationUtils;
import org.ironrhino.core.annotation.NaturalId;
import org.ironrhino.core.annotation.NotInCopy;
import org.ironrhino.core.annotation.NotInJson;
import org.ironrhino.core.model.Model;
import org.junit.Test;

public class AnnotationUtilsTest {

	@Test
	public void testGetAnnotatedPropertyNames() {
		Set<String> names = AnnotationUtils.getAnnotatedPropertyNames(
				Model.class, NaturalId.class);
		assertEquals(2, names.size());
		assertTrue(names.contains("name"));
		assertTrue(names.contains("value"));
		names = AnnotationUtils.getAnnotatedPropertyNames(Model.class,
				NotInCopy.class);
		assertEquals(1, names.size());
		assertTrue(names.contains("relations"));
		names = AnnotationUtils.getAnnotatedPropertyNames(Model.class,
				NotInJson.class);
		assertEquals(2, names.size());
		assertTrue(names.contains("password"));
		assertTrue(names.contains("relations"));

	}

	@Test
	public void testGetAnnotatedPropertyNameAndValues() {
		Model model = new Model();
		model.setId("id");
		model.setName("name");
		model.setValue("value");
		Map<String, Object> map = AnnotationUtils
				.getAnnotatedPropertyNameAndValues(model, NaturalId.class);
		assertEquals(2, map.size());
		assertTrue(map.keySet().contains("name"));
		assertTrue(map.keySet().contains("value"));
		assertTrue(map.values().contains("name"));
		assertTrue(map.values().contains("value"));
	}

	@Test
	public void testEquals() {
		Model m1 = new Model();
		m1.setId("id1");
		m1.setName("name1");
		Model m2 = new Model();
		m2.setId("id1");
		m2.setName("name1");
		assertEquals(m1, m2);
	}

}
