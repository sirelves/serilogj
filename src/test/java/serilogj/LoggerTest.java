package serilogj;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import serilogj.events.LogEventLevel;
import serilogj.testing.InMemorySink;

public class LoggerTest {

	@Test
	void loggerWritesEventToSink() {
		InMemorySink sink = new InMemorySink();
		ILogger logger = new LoggerConfiguration()
				.setMinimumLevel(LogEventLevel.Verbose)
				.writeTo(sink)
				.createLogger();

		logger.information("Hello {Name}", "World");

		assertEquals(1, sink.getEvents().size());
		assertEquals(LogEventLevel.Information, sink.getEvents().get(0).getLevel());
	}

	@Test
	void loggerRespectsMinimumLevel() {
		InMemorySink sink = new InMemorySink();
		ILogger logger = new LoggerConfiguration()
				.setMinimumLevel(LogEventLevel.Warning)
				.writeTo(sink)
				.createLogger();

		logger.information("Should be filtered");
		logger.warning("Should be logged");

		assertEquals(1, sink.getEvents().size());
		assertEquals(LogEventLevel.Warning, sink.getEvents().get(0).getLevel());
	}

	@Test
	void loggerCapturesProperties() {
		InMemorySink sink = new InMemorySink();
		ILogger logger = new LoggerConfiguration()
				.setMinimumLevel(LogEventLevel.Verbose)
				.writeTo(sink)
				.createLogger();

		logger.information("Hello {Name}", "World");

		assertTrue(sink.getEvents().get(0).getProperties().containsKey("Name"));
	}

	@Test
	void loggerCapturesException() {
		InMemorySink sink = new InMemorySink();
		ILogger logger = new LoggerConfiguration()
				.setMinimumLevel(LogEventLevel.Verbose)
				.writeTo(sink)
				.createLogger();

		Exception ex = new RuntimeException("test error");
		logger.error(ex, "An error occurred");

		assertNotNull(sink.getEvents().get(0).getException());
	}

	@Test
	void forContextAddsSourceContext() {
		InMemorySink sink = new InMemorySink();
		ILogger logger = new LoggerConfiguration()
				.setMinimumLevel(LogEventLevel.Verbose)
				.writeTo(sink)
				.createLogger();

		ILogger contextLogger = logger.forContext(LoggerTest.class);
		contextLogger.information("Test");

		assertTrue(sink.getEvents().get(0).getProperties().containsKey("SourceContext"));
	}

	@Test
	void multipleEventsAreLogged() {
		InMemorySink sink = new InMemorySink();
		ILogger logger = new LoggerConfiguration()
				.setMinimumLevel(LogEventLevel.Verbose)
				.writeTo(sink)
				.createLogger();

		logger.verbose("One");
		logger.debug("Two");
		logger.information("Three");

		assertEquals(3, sink.getEvents().size());
	}
}
