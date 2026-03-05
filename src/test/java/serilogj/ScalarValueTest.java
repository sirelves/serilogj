package serilogj;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import serilogj.events.ScalarValue;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Date;

public class ScalarValueTest {

	@Test
	void rendersNullValue() throws IOException {
		ScalarValue sv = new ScalarValue(null);
		StringWriter writer = new StringWriter();
		sv.render(writer, null, null);
		assertEquals("null", writer.toString());
	}

	@Test
	void rendersStringWithQuotes() throws IOException {
		ScalarValue sv = new ScalarValue("hello");
		StringWriter writer = new StringWriter();
		sv.render(writer, null, null);
		assertEquals("\"hello\"", writer.toString());
	}

	@Test
	void rendersStringLiteralWithFormatL() throws IOException {
		ScalarValue sv = new ScalarValue("hello");
		StringWriter writer = new StringWriter();
		sv.render(writer, "l", null);
		assertEquals("hello", writer.toString());
	}

	@Test
	void rendersDateWithFormat() throws IOException {
		Date date = new Date(0);
		ScalarValue sv = new ScalarValue(date);
		StringWriter writer = new StringWriter();
		sv.render(writer, "yyyy", null);
		assertTrue(writer.toString().matches("\\d{4}"));
	}

	@Test
	void rendersTemporalAccessorWithFormat() throws IOException {
		LocalDateTime ldt = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
		ScalarValue sv = new ScalarValue(ldt);
		StringWriter writer = new StringWriter();
		sv.render(writer, "yyyy-MM-dd", null);
		assertEquals("2024-01-15", writer.toString());
	}

	@Test
	void rendersTemporalAccessorWithoutFormat() throws IOException {
		LocalDateTime ldt = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
		ScalarValue sv = new ScalarValue(ldt);
		StringWriter writer = new StringWriter();
		sv.render(writer, null, null);
		assertTrue(writer.toString().contains("2024"));
	}

	@Test
	void rendersIntegerValue() throws IOException {
		ScalarValue sv = new ScalarValue(42);
		StringWriter writer = new StringWriter();
		sv.render(writer, null, null);
		assertEquals("42", writer.toString());
	}
}
