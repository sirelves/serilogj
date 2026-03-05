package serilogj.configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import serilogj.core.ILogEventSink;

public class SinkRegistry {
	private static final Map<String, Function<Map<String, String>, ILogEventSink>> factories = new ConcurrentHashMap<>();

	public static void register(String name, Function<Map<String, String>, ILogEventSink> factory) {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("name");
		}
		if (factory == null) {
			throw new IllegalArgumentException("factory");
		}
		factories.put(name.toLowerCase(), factory);
	}

	public static ILogEventSink create(String name, Map<String, String> properties) {
		Function<Map<String, String>, ILogEventSink> factory = factories.get(name.toLowerCase());
		if (factory == null) {
			throw new IllegalArgumentException("Unknown sink: " + name);
		}
		return factory.apply(properties);
	}

	public static boolean isRegistered(String name) {
		return factories.containsKey(name.toLowerCase());
	}
}
