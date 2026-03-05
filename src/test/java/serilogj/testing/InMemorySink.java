package serilogj.testing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import serilogj.core.ILogEventSink;
import serilogj.events.LogEvent;

public class InMemorySink implements ILogEventSink {
	private final List<LogEvent> events = Collections.synchronizedList(new ArrayList<>());

	@Override
	public void emit(LogEvent logEvent) {
		if (logEvent == null) {
			throw new IllegalArgumentException("logEvent");
		}
		events.add(logEvent);
	}

	public List<LogEvent> getEvents() {
		return Collections.unmodifiableList(new ArrayList<>(events));
	}

	public void clear() {
		events.clear();
	}
}
