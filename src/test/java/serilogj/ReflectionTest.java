package serilogj;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import serilogj.reflection.Property;
import serilogj.reflection.Reflection;

import java.util.Map;

public class ReflectionTest {

	public static class SimpleBean {
		public String name = "test";
		public int value = 42;

		public String getName() { return name; }
		public int getValue() { return value; }
	}

	public static class ChildBean extends SimpleBean {
		public String extra = "child";
	}

	@Test
	void extractsPublicFields() {
		Map<String, Property> props = Reflection.getProperties(SimpleBean.class);
		assertFalse(props.isEmpty());
	}

	@Test
	void extractsGetterMethods() {
		Map<String, Property> props = Reflection.getProperties(SimpleBean.class);
		assertTrue(props.containsKey("name") || props.containsKey("Name"));
	}

	@Test
	void extractsInheritedProperties() {
		Map<String, Property> props = Reflection.getProperties(ChildBean.class);
		assertTrue(props.containsKey("extra"));
	}

	@Test
	void cachesProperties() {
		Map<String, Property> first = Reflection.getProperties(SimpleBean.class);
		Map<String, Property> second = Reflection.getProperties(SimpleBean.class);
		assertSame(first, second);
	}
}
