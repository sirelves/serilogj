package serilogj.configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import serilogj.LoggerConfiguration;
import serilogj.core.ILogEventEnricher;
import serilogj.core.ILogEventSink;
import serilogj.core.enrichers.MachineNameEnricher;
import serilogj.core.enrichers.ProcessIdEnricher;
import serilogj.core.enrichers.ThreadIdEnricher;
import serilogj.core.enrichers.ThreadNameEnricher;
import serilogj.events.LogEventLevel;

public class PropertiesFileConfiguration {

	public static LoggerConfiguration configure(String filePath) throws IOException {
		Properties props = new Properties();
		try (InputStream input = new FileInputStream(filePath)) {
			props.load(input);
		}
		return configure(props);
	}

	public static LoggerConfiguration configure(Properties props) {
		LoggerConfiguration config = new LoggerConfiguration();

		String level = props.getProperty("serilogj.minimum-level");
		if (level != null) {
			config.setMinimumLevel(parseLevel(level));
		}

		for (String key : props.stringPropertyNames()) {
			if (key.startsWith("serilogj.sink.")) {
				String sinkName = key.substring("serilogj.sink.".length());
				int dotIndex = sinkName.indexOf('.');
				if (dotIndex == -1) {
					String sinkType = props.getProperty(key);
					Map<String, String> sinkProps = extractSinkProperties(props, key + ".");
					if (SinkRegistry.isRegistered(sinkType)) {
						ILogEventSink sink = SinkRegistry.create(sinkType, sinkProps);
						config.writeTo(sink);
					}
				}
			}

			if (key.startsWith("serilogj.enrich.")) {
				String enricherName = props.getProperty(key);
				ILogEventEnricher enricher = createEnricher(enricherName);
				if (enricher != null) {
					config.with(enricher);
				}
			}
		}

		return config;
	}

	private static Map<String, String> extractSinkProperties(Properties props, String prefix) {
		Map<String, String> result = new HashMap<>();
		for (String key : props.stringPropertyNames()) {
			if (key.startsWith(prefix)) {
				result.put(key.substring(prefix.length()), props.getProperty(key));
			}
		}
		return result;
	}

	private static LogEventLevel parseLevel(String level) {
		switch (level.trim().toLowerCase()) {
			case "verbose": return LogEventLevel.Verbose;
			case "debug": return LogEventLevel.Debug;
			case "information": return LogEventLevel.Information;
			case "warning": return LogEventLevel.Warning;
			case "error": return LogEventLevel.Error;
			case "fatal": return LogEventLevel.Fatal;
			default: return LogEventLevel.Information;
		}
	}

	private static ILogEventEnricher createEnricher(String name) {
		if (name == null) return null;
		switch (name.trim().toLowerCase()) {
			case "threadid": return new ThreadIdEnricher();
			case "threadname": return new ThreadNameEnricher();
			case "machinename": return new MachineNameEnricher();
			case "processid": return new ProcessIdEnricher();
			default: return null;
		}
	}
}
