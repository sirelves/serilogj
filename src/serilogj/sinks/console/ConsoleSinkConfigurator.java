package serilogj.sinks.console;

import java.util.Locale;

import serilogj.core.ILogEventSink;

public class ConsoleSinkConfigurator {
	private static final String DefaultConsoleOutputTemplate = "{Timestamp:yyyy-MM-dd HH:mm:ss} [{Level}] {Message}{NewLine}{Exception}";

	public static ILogEventSink console() {
		return console(DefaultConsoleOutputTemplate, null);
	}

	public static ILogEventSink console(String outputTemplate) {
		return console(outputTemplate, null);
	}

	public static ILogEventSink console(Locale locale) {
		return console(DefaultConsoleOutputTemplate, locale);
	}

	public static ILogEventSink console(String outputTemplate, Locale locale) {
		return new ConsoleSink(outputTemplate, locale);
	}
}
