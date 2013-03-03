/*
 * Copyright (c) 2013 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing.server.jetty;

import net.jadler.stubbing.RequestRecorder;
import net.jadler.stubbing.StubResponseProvider;
import net.jadler.stubbing.server.StubHttpServer;
import org.apache.commons.lang.Validate;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Default stub http server implementation using <a href="http://jetty.codehaus.org/jetty/" target="_blank">Jetty</a>
 * as an http server.
 */
public class JettyStubHttpServer implements StubHttpServer {

    private static final Logger logger = LoggerFactory.getLogger(JettyStubHttpServer.class);
    private final Server server;
    private final Connector selectChannelConnector;
    private StubResponseProvider ruleProvider;
    private RequestRecorder requestRecorder;

    public JettyStubHttpServer() {
        this(0);
    }

    public JettyStubHttpServer(final int port) {
        this.server = new Server();
        this.selectChannelConnector = new SelectChannelConnector();
        selectChannelConnector.setPort(port);
        server.addConnector(selectChannelConnector);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerResponseProvider(final StubResponseProvider ruleProvider) {
        Validate.notNull(ruleProvider, "ruleProvider cannot be null");
        this.ruleProvider = ruleProvider;
        updateHandlers();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerRequestRecorder(RequestRecorder requestRecorder) {
        Validate.notNull(requestRecorder, "requestRecorder cannot be null");
        this.requestRecorder = requestRecorder;
        updateHandlers();
    }

    private void updateHandlers() {
        final HandlerList handlers = new HandlerList();
        if (requestRecorder!=null) {
            handlers.addHandler(new RequestRecordingHandler(requestRecorder));
        }
        if (ruleProvider!=null) {
            handlers.addHandler(new StubHandler(ruleProvider));
        }
        handlers.addHandler(new DefaultHandler());
        server.setHandler(handlers);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() throws Exception {
        logger.debug("starting jetty");
        server.start();
        logger.debug("jetty started");
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() throws Exception {
        logger.debug("stopping jetty");
        server.stop();
        logger.debug("jetty stopped");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPort() {
        return selectChannelConnector.getLocalPort();
    }
}