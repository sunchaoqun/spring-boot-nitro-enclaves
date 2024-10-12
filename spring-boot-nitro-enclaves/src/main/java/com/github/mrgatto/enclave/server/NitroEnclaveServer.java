package com.github.mrgatto.enclave.server;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.github.mrgatto.enclave.server.network.Listener;
import com.github.mrgatto.enclave.server.network.ListenerConnection;
import com.github.mrgatto.enclave.server.network.ListenerConsumer;

public class NitroEnclaveServer {

	private static final Logger LOG = LoggerFactory.getLogger(NitroEnclaveServer.class);

	private final Listener clientListener;

	private final ListenerConsumer listenerConsumer;

	private volatile boolean running = true;

	private final ExecutorService executorService;
	private final int maxConcurrentConnections;

	public NitroEnclaveServer(Listener clientListener, ListenerConsumer listenerConsumer) {
		this.clientListener = clientListener;
		this.listenerConsumer = listenerConsumer;
		this.maxConcurrentConnections = 5000;
		this.executorService = Executors.newFixedThreadPool(maxConcurrentConnections);
	}

	@PostConstruct
	private void init() {
		LOG.info("Configured Socket Listener: {}", this.clientListener.getClass());
	}

	public void run() {
		Assert.notNull(this.clientListener, "Listener must not be null");

		LOG.info("Starting {} listener", ClassUtils.getSimpleName(this.clientListener.getClass()));
		this.clientListener.start();

		try {
			while (running) {
				ListenerConnection conn = this.clientListener.accept();
				if (conn != null) {
					executorService.submit(() -> processConnection(conn));
				}
			}
		} catch (Exception e) {
			LOG.error("Unexpected error in server loop", e);
		} finally {
			shutdown();
		}
	}

	private void processConnection(ListenerConnection conn) {
		try {
			LOG.info("SCQ1-> Accepted connection {}", conn);
			this.listenerConsumer.process(conn);
			LOG.info("SCQ1-> End of connection {}", conn);
		} catch (IOException e) {
			LOG.error("Error dealing with connection", e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (IOException e) {
					LOG.warn("Error closing connection", e);
				}
			}
		}
	}

	public void shutdown() {
		running = false;
		executorService.shutdown();
		try {
			if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
				executorService.shutdownNow();
			}
		} catch (InterruptedException e) {
			executorService.shutdownNow();
		}
		IOUtils.closeQuietly(this.clientListener);
	}

}
