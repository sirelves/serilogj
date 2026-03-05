package serilogj.core.enrichers;

import java.lang.management.ManagementFactory;

import serilogj.core.ILogEventEnricher;
import serilogj.core.ILogEventPropertyFactory;
import serilogj.events.LogEvent;
import serilogj.events.LogEventProperty;
import serilogj.events.ScalarValue;

public class ProcessIdEnricher implements ILogEventEnricher {
	public static final String ProcessIdPropertyName = "ProcessId";

	private volatile LogEventProperty cachedProperty;

	@Override
	public void enrich(LogEvent logEvent, ILogEventPropertyFactory propertyFactory) {
		LogEventProperty property = cachedProperty;
		if (property == null) {
			property = new LogEventProperty(ProcessIdPropertyName, new ScalarValue(getProcessId()));
			cachedProperty = property;
		}
		logEvent.addPropertyIfAbsent(property);
	}

	private static String getProcessId() {
		try {
			String runtimeName = ManagementFactory.getRuntimeMXBean().getName();
			int atIndex = runtimeName.indexOf('@');
			if (atIndex > 0) {
				return runtimeName.substring(0, atIndex);
			}
			return runtimeName;
		} catch (Exception e) {
			return "unknown";
		}
	}
}
