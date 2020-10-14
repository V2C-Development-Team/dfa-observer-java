/*
 * Copyright (c) 2020 Caleb L. Power. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.calebpower.demo.dfaparser.ui;

import com.calebpower.demo.dfaparser.DFAParser;

import edu.uco.cs.v2c.dispatcher.api.listener.CommandListener;
import edu.uco.cs.v2c.dispatcher.api.payload.incoming.RouteCommandPayload;

/**
 * Process input into distinct tokens.
 * 
 * @author Caleb L. Power
 */
public abstract class TokenProcessor implements InputListener, CommandListener {
  
  /**
   * {@inheritDoc}
   */
  @Override public void onInput(String input) {
    try {
      String[] tokens = input.split("\\s+");
      for(String token : tokens)
        pushToken(token);
    } catch(Exception e) {
      System.err.println("Tokens could not be processed.");
    }
  }
  
  /**
   * {@inheritDoc}
   */
  @Override public void onIncomingCommand(RouteCommandPayload payload) {
    if(payload.getRecipient().equalsIgnoreCase(DFAParser.DISPATCHER_APP_ID))
      onInput(payload.getCommand());
  }
  
  /**
   * Pushes a single token to some machine.
   * 
   * @param token the token
   * @throws Exception if the token could not be processed
   */
  public abstract void pushToken(String token) throws Exception;
  
}
