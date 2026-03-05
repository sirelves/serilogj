package serilogj.core.enrichers;

import java.net.InetAddress;

import serilogj.core.ILogEventEnricher;
import serilogj.core.ILogEventPropertyFactory;
import serilogj.events.LogEvent;
import serilogj.events.LogEventProperty;
import serilogj.events.ScalarValue;

public class MachineNameEnricher implements ILogEventEnricher {
	public static final String MachineNamePropertyName = "MachineName";

	private volatile LogEventProperty cachedProperty;

	@Override
	public void enrich(LogEvent logEvent, ILogEventPropertyFactory propertyFactory) {
		LogEventProperty property = cachedProperty;
		if (property == null) {
			property = new LogEventProperty(MachineNamePropertyName, new ScalarValue(getMachineName()));
			cachedProperty = property;
		}
		logEvent.addPropertyIfAbsent(property);
	}

	private static String getMachineName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (Exception e) {
			try {
				String hostname = System.getenv("HOSTNAME");
				if (hostname != null && !hostname.isEmpty()) {
					return hostname;
				}
				hostname = System.getenv("COMPUTERNAME");
				if (hostname != null && !hostname.isEmpty()) {
					return hostname;
				}
			} catch (Exception ignored) {
			}
			return "unknown";
		}
	}
}
