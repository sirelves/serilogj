package serilogj.core.enrichers;

import serilogj.core.ILogEventEnricher;
import serilogj.core.ILogEventPropertyFactory;
import serilogj.events.LogEvent;
import serilogj.events.LogEventProperty;
import serilogj.events.ScalarValue;

public class ThreadNameEnricher implements ILogEventEnricher {
	public static final String ThreadNamePropertyName = "ThreadName";

	@Override
	public void enrich(LogEvent logEvent, ILogEventPropertyFactory propertyFactory) {
		String name = Thread.currentThread().getName();
		if (name != null) {
			logEvent.addPropertyIfAbsent(
					new LogEventProperty(ThreadNamePropertyName, new ScalarValue(name)));
		}
	}
}
