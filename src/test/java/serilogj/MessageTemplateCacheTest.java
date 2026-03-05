package serilogj;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import serilogj.core.pipeline.MessageTemplateCache;
import serilogj.events.MessageTemplate;
import serilogj.parsing.MessageTemplateParser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class MessageTemplateCacheTest {

	@Test
	void cachesTemplates() {
		MessageTemplateCache cache = new MessageTemplateCache(new MessageTemplateParser());
		MessageTemplate first = cache.parse("Hello {Name}");
		MessageTemplate second = cache.parse("Hello {Name}");
		assertSame(first, second);
	}

	@Test
	void parsesDifferentTemplates() {
		MessageTemplateCache cache = new MessageTemplateCache(new MessageTemplateParser());
		MessageTemplate a = cache.parse("A {X}");
		MessageTemplate b = cache.parse("B {Y}");
		assertNotSame(a, b);
	}

	@Test
	void handlesLongTemplatesWithoutCaching() {
		MessageTemplateCache cache = new MessageTemplateCache(new MessageTemplateParser());
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 200; i++) {
			sb.append("abcdefgh");
		}
		String longTemplate = sb.toString();
		MessageTemplate first = cache.parse(longTemplate);
		MessageTemplate second = cache.parse(longTemplate);
		assertNotSame(first, second);
	}

	@Test
	void handlesConcurrentAccess() throws Exception {
		MessageTemplateCache cache = new MessageTemplateCache(new MessageTemplateParser());
		int threadCount = 8;
		int iterations = 100;
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);

		List<Future<?>> futures = new ArrayList<>();
		for (int t = 0; t < threadCount; t++) {
			final int thread = t;
			futures.add(executor.submit(() -> {
				for (int i = 0; i < iterations; i++) {
					String template = "Thread " + thread + " iteration " + i + " {Value}";
					assertNotNull(cache.parse(template));
				}
			}));
		}

		for (Future<?> future : futures) {
			future.get(10, TimeUnit.SECONDS);
		}

		executor.shutdown();
	}
}
