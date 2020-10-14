/*
 * Copyright (c) 2020 V2C Development Team. All rights reserved.
 * Licensed under the Version 0.0.1 of the V2C License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at <https://tinyurl.com/v2c-license>.
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions 
 * limitations under the License.
 */
package com.calebpower.demo.dfaparser.net;

import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.calebpower.demo.dfaparser.DFAParser;
import com.calebpower.demo.dfaparser.ui.Logger;

import edu.uco.cs.v2c.dispatcher.api.DispatcherConnection;
import edu.uco.cs.v2c.dispatcher.api.listener.CommandListener;
import edu.uco.cs.v2c.dispatcher.api.listener.ConfigUpdateListener;
import edu.uco.cs.v2c.dispatcher.api.listener.ConnectionCloseListener;
import edu.uco.cs.v2c.dispatcher.api.listener.ConnectionOpenListener;
import edu.uco.cs.v2c.dispatcher.api.listener.HeartbeatListener;
import edu.uco.cs.v2c.dispatcher.api.listener.MessageListener;
import edu.uco.cs.v2c.dispatcher.api.listener.WebSocketErrorListener;
import edu.uco.cs.v2c.dispatcher.api.payload.incoming.ErrorPayload;
import edu.uco.cs.v2c.dispatcher.api.payload.incoming.HeartbeatPayload;
import edu.uco.cs.v2c.dispatcher.api.payload.incoming.InboundConfigUpdatePayload;
import edu.uco.cs.v2c.dispatcher.api.payload.incoming.RouteCommandPayload;
import edu.uco.cs.v2c.dispatcher.api.payload.incoming.RouteMessagePayload;
import edu.uco.cs.v2c.dispatcher.api.payload.outgoing.DeregisterListenerPayload;
import edu.uco.cs.v2c.dispatcher.api.payload.outgoing.HeartbeatAckPayload;
import edu.uco.cs.v2c.dispatcher.api.payload.outgoing.RegisterListenerPayload;

/**
 * Handles interactions with the dispatcher.
 * 
 * @author Caleb L. Power
 */
public class DispatcherHandler implements CommandListener,
                                          ConfigUpdateListener,
                                          ConnectionCloseListener,
                                          ConnectionOpenListener,
                                          HeartbeatListener,
                                          MessageListener,
                                          WebSocketErrorListener,
                                          Runnable {
  
  private static final String LOG_LABEL = "HANDLER";
  
  private AtomicBoolean running = new AtomicBoolean(true);
  private AtomicReference<DispatcherConnection> connection = new AtomicReference<>();
  private String destination = null;
  private Thread thread = null;
  
  private DispatcherHandler(String destination) throws URISyntaxException {
    this.destination = destination;
  }
  
  /**
   * Builds the handler.
   * 
   * @param destination the destination
   * @return a new DispatcherHandler instance
   * @throws URISyntaxException if the destination was malformed
   */
  public static DispatcherHandler build(String destination) throws URISyntaxException {
    DispatcherHandler handler = new DispatcherHandler(destination);
    handler.thread = new Thread(handler);
    handler.thread.setDaemon(false);
    handler.thread.start();
    return handler;
  }

  /**
   * {@inheritDoc}
   */
  @Override public void onIncomingMessage(RouteMessagePayload payload) {
    Logger.onDebug(LOG_LABEL, String.format("Received message: %1$s", payload.toString()));
  }

  /**
   * {@inheritDoc}
   */
  @Override public void onConnect() {
    Logger.onDebug(LOG_LABEL, "Connected to dispatcher.");
    connection.get().send(new RegisterListenerPayload()
        .setApp(DFAParser.DISPATCHER_APP_ID));
  }

  /**
   * {@inheritDoc}
   */
  @Override public void onClose(int statusCode, String reason) {
    Logger.onDebug(LOG_LABEL, "WebSocket was disconnected.");
  }

  /**
   * {@inheritDoc}
   */
  @Override public void onConfigUpdate(InboundConfigUpdatePayload payload) {
    Logger.onDebug(LOG_LABEL, String.format("Received config update: %1$s", payload.toString()));
  }

  /**
   * {@inheritDoc}
   */
  @Override public void onIncomingCommand(RouteCommandPayload payload) {
    Logger.onDebug(LOG_LABEL, String.format("Received incoming command: %1$s", payload.toString()));
  }

  /**
   * {@inheritDoc}
   */
  @Override public void onRemoteError(ErrorPayload payload) {
    Logger.onDebug(LOG_LABEL, String.format("Received error message: %1$s", payload.toString()));
  }

  /**
   * {@inheritDoc}
   */
  @Override public void onLocalError(Exception throwable) {
    Logger.onDebug(LOG_LABEL, String.format("Detected local error: %1$s",
        throwable.getMessage() == null ? throwable.getClass().getName()
            : throwable.getMessage()));
    if(thread != null) thread.interrupt();
  }

  /**
   * {@inheritDoc}
   */
  @Override public void onHeartbeat(HeartbeatPayload payload) {
    Logger.onDebug(LOG_LABEL, String.format("Detected heartbeat: %1$s", payload.toString()));
    HeartbeatAckPayload ack = new HeartbeatAckPayload()
        .setApp(DFAParser.DISPATCHER_APP_ID)
        .setKey(payload.getKey())
        .setTimestamp(System.currentTimeMillis());
    connection.get().send(ack);
  }

  /**
   * {@inheritDoc}
   */
  @Override public void run() {
    try {
      connection.set(new DispatcherConnection(destination));
      connection.get().registerListener(this);
      connection.get().connect();
    } catch(Exception e) {
      e.printStackTrace();
    }
    
    while(running.get()) try {
      Thread.sleep(500L);
    } catch(InterruptedException e) { }
    
    try {
      connection.get().send(
          new DeregisterListenerPayload().setApp(DFAParser.DISPATCHER_APP_ID)); // TODO make this configurable
      Thread.sleep(1000L);
      if(connection != null) connection.get().closeBlocking();
    } catch(Exception e) { }
  }
  
  /**
   * Stops the handler thread.
   */
  public void kill() {
    running.set(false);
  }
  
  /**
   * Registers a listener.
   * 
   * @param listeners the listener
   */
  public void registerCommandListener(Object... listeners) {
    try {
      while(connection.get() == null) Thread.sleep(500L);
      connection.get().registerListener(listeners);
    } catch(InterruptedException e) { }
  }

}