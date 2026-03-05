package serilogj.core.enrichers;

import serilogj.core.ILogEventEnricher;
import serilogj.core.ILogEventPropertyFactory;
import serilogj.events.LogEvent;
import serilogj.events.LogEventProperty;
import serilogj.events.ScalarValue;

public class ThreadIdEnricher implements ILogEventEnricher {
	public static final String ThreadIdPropertyName = "ThreadId";

	@Override
	public void enrich(LogEvent logEvent, ILogEventPropertyFactory propertyFactory) {
		logEvent.addPropertyIfAbsent(
				new LogEventProperty(ThreadIdPropertyName, new ScalarValue(Thread.currentThread().getId())));
	}
}
