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

import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * The terminal UI.
 * 
 * TODO
 * - create a top panel that shows the messages
 * - create a small bottom bar that lets the user send from the server
 * 
 * @author Caleb L. Power
 */
public class TerminalUI implements Runnable {
  
  private List<InputListener> inputListeners = new LinkedList<>();
  private Thread thread = null;
  
  /**
   * Builds the terminal UI.
   * 
   * @return an instance of the TerminalUI
   */
  public static TerminalUI build() {
    TerminalUI terminalUI = new TerminalUI();
    terminalUI.thread = new Thread(terminalUI);
    terminalUI.thread.setDaemon(false);
    terminalUI.thread.start();
    return terminalUI;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override public void run() {
    try(Scanner keyboard = new Scanner(System.in)) { 
      while(!thread.isInterrupted()) {
        String line = keyboard.nextLine().strip();
        for(InputListener listener : inputListeners)
          listener.onInput(line);
      }
    } catch(Exception e) { }
  }
  
  /**
   * Adds an input listener.
   * 
   * @param listener the input listener
   */
  public void registerInputListener(InputListener listener) {
    inputListeners.add(listener);
  }
  
  /**
   * Stops the thread.
   */
  public void stop() {
    if(thread != null) thread.interrupt();
    inputListeners.clear();
  }
}
