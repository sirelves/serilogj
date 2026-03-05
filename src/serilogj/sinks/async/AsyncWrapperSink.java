package serilogj.sinks.async;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import serilogj.core.ILogEventSink;
import serilogj.debugging.SelfLog;
import serilogj.events.LogEvent;

public class AsyncWrapperSink implements ILogEventSink, Closeable {
	private final ILogEventSink wrappedSink;
	private final BlockingQueue<LogEvent> queue;
	private final Thread worker;
	private volatile boolean disposed;

	public AsyncWrapperSink(ILogEventSink wrappedSink, int bufferSize) {
		if (wrappedSink == null) {
			throw new IllegalArgumentException("wrappedSink");
		}
		if (bufferSize <= 0) {
			throw new IllegalArgumentException("bufferSize must be positive");
		}
		this.wrappedSink = wrappedSink;
		this.queue = new LinkedBlockingQueue<>(bufferSize);
		this.worker = new Thread(this::processQueue, "serilogj-async-sink");
		this.worker.setDaemon(true);
		this.worker.start();
	}

	public AsyncWrapperSink(ILogEventSink wrappedSink) {
		this(wrappedSink, 10000);
	}

	@Override
	public void emit(LogEvent logEvent) {
		if (disposed) {
			return;
		}
		if (!queue.offer(logEvent)) {
			SelfLog.writeLine("Async sink buffer is full, dropping event");
		}
	}

	private void processQueue() {
		while (!disposed || !queue.isEmpty()) {
			try {
				LogEvent event = queue.poll(500, TimeUnit.MILLISECONDS);
				if (event != null) {
					try {
						wrappedSink.emit(event);
					} catch (Exception e) {
						SelfLog.writeLine("Exception in async sink worker: %s", e.getMessage());
					}
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
		}

		// Drain remaining events
		LogEvent event;
		while ((event = queue.poll()) != null) {
			try {
				wrappedSink.emit(event);
			} catch (Exception e) {
				SelfLog.writeLine("Exception draining async sink: %s", e.getMessage());
			}
		}
	}

	@Override
	public void close() throws IOException {
		disposed = true;
		try {
			worker.join(5000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		if (wrappedSink instanceof Closeable) {
			((Closeable) wrappedSink).close();
		}
	}
}
