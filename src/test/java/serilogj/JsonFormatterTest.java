package serilogj;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import serilogj.events.*;
import serilogj.formatting.json.JsonFormatter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;

public class JsonFormatterTest {

	private final JsonFormatter formatter = new JsonFormatter(false, "\n", false, null);

	private LogEvent createEvent(String message) {
		MessageTemplate template = new MessageTemplate(message, new ArrayList<>());
		return new LogEvent(new Date(), LogEventLevel.Information, null, template, new ArrayList<>());
	}

	@Test
	void formatsBasicEvent() throws IOException {
		LogEvent event = createEvent("Test message");
		StringWriter writer = new StringWriter();
		formatter.format(event, writer);
		String json = writer.toString();

		assertTrue(json.startsWith("{"));
		assertTrue(json.contains("\"Timestamp\""));
		assertTrue(json.contains("\"Level\""));
		assertTrue(json.contains("\"MessageTemplate\""));
	}

	@Test
	void includesLevelInOutput() throws IOException {
		LogEvent event = createEvent("Test");
		StringWriter writer = new StringWriter();
		formatter.format(event, writer);
		String json = writer.toString();

		assertTrue(json.contains("Information"));
	}

	@Test
	void escapesSpecialCharactersInStrings() {
		assertEquals("hello\\\"world", JsonFormatter.escape("hello\"world"));
		assertEquals("hello\\\\world", JsonFormatter.escape("hello\\world"));
		assertEquals("hello\\nworld", JsonFormatter.escape("hello\nworld"));
		assertEquals("hello\\tworld", JsonFormatter.escape("hello\tworld"));
	}

	@Test
	void handlesNullEscape() {
		assertNull(JsonFormatter.escape(null));
	}

	@Test
	void includesExceptionWhenPresent() throws IOException {
		MessageTemplate template = new MessageTemplate("Error", new ArrayList<>());
		LogEvent event = new LogEvent(new Date(), LogEventLevel.Error,
				new RuntimeException("test"), template, new ArrayList<>());
		StringWriter writer = new StringWriter();
		formatter.format(event, writer);

		assertTrue(writer.toString().contains("\"Exception\""));
	}

	@Test
	void rendersMessageWhenConfigured() throws IOException {
		JsonFormatter renderFormatter = new JsonFormatter(false, "\n", true, null);
		LogEvent event = createEvent("Hello");
		StringWriter writer = new StringWriter();
		renderFormatter.format(event, writer);

		assertTrue(writer.toString().contains("\"Message\""));
	}
}
