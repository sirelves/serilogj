package serilogj.sinks.console;

import java.io.IOException;
import java.io.Writer;
import java.util.Locale;

import serilogj.core.ILogEventSink;
import serilogj.debugging.SelfLog;
import serilogj.events.LogEvent;
import serilogj.formatting.ITextFormatter;
import serilogj.formatting.display.MessageTemplateTextFormatter;

public class ConsoleSink implements ILogEventSink {
	private final ITextFormatter formatter;
	private final Writer output;

	public ConsoleSink(String outputTemplate, Locale locale) {
		if (outputTemplate == null) {
			throw new IllegalArgumentException("outputTemplate");
		}
		this.formatter = new MessageTemplateTextFormatter(outputTemplate, locale);
		this.output = new java.io.OutputStreamWriter(System.out);
	}

	@Override
	public synchronized void emit(LogEvent logEvent) {
		if (logEvent == null) {
			throw new IllegalArgumentException("logEvent");
		}
		try {
			formatter.format(logEvent, output);
			output.flush();
		} catch (IOException e) {
			SelfLog.writeLine("Failed to write to console, exception %s", e.getMessage());
		}
	}
}
