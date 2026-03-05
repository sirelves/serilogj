package serilogj;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import serilogj.events.*;
import serilogj.formatting.display.MessageTemplateTextFormatter;
import serilogj.sinks.rollingfile.RollingFileSink;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;

public class RollingFileSinkTest {

	@Test
	void createsLogFile(@TempDir Path tempDir) throws IOException {
		String pathFormat = tempDir.resolve("log-{Date}.txt").toString();
		MessageTemplateTextFormatter formatter = new MessageTemplateTextFormatter("{Message}", null);

		RollingFileSink sink = new RollingFileSink(pathFormat, null, null, false, formatter);

		MessageTemplate template = new MessageTemplate("Test message", new ArrayList<>());
		LogEvent event = new LogEvent(new Date(), LogEventLevel.Information, null, template, new ArrayList<>());

		sink.emit(event);
		sink.close();

		File[] files = tempDir.toFile().listFiles();
		assertNotNull(files);
		assertTrue(files.length > 0);
	}

	@Test
	void respectsRetentionPolicy(@TempDir Path tempDir) throws IOException {
		String pathFormat = tempDir.resolve("log-{Date}.txt").toString();
		MessageTemplateTextFormatter formatter = new MessageTemplateTextFormatter("{Message}", null);

		RollingFileSink sink = new RollingFileSink(pathFormat, null, 1, false, formatter);

		MessageTemplate template = new MessageTemplate("Test", new ArrayList<>());
		LogEvent event = new LogEvent(new Date(), LogEventLevel.Information, null, template, new ArrayList<>());

		sink.emit(event);
		sink.close();

		File[] files = tempDir.toFile().listFiles();
		assertNotNull(files);
		assertTrue(files.length <= 2);
	}
}
