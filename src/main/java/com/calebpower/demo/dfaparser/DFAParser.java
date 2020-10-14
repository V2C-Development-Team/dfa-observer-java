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
package com.calebpower.demo.dfaparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import com.calebpower.demo.dfaparser.machine.Hypervisor;
import com.calebpower.demo.dfaparser.machine.Machine;
import com.calebpower.demo.dfaparser.net.DispatcherHandler;
import com.calebpower.demo.dfaparser.ui.Logger;
import com.calebpower.demo.dfaparser.ui.TerminalUI;

/**
 * Deterministic Finite-State Automata Parser Demonstration
 * 
 * @author Caleb L. Power
 */
public class DFAParser {
  
  /**
   * Denotes the name of this application for the benefit of the dispatcher.
   */
  public static final String DISPATCHER_APP_ID = "concept";
  
  private static final String DEFAULT_DESTINATION = "ws://127.0.0.1:2585/v1/messages";
  private static final String DESTINATION_PARAM_LONG = "destination";
  private static final String DESTINATION_PARAM_SHORT = "d";
  private static final String LOG_LABEL = "CONTROLLER";
  
  /**
   * Entry-point.
   * 
   * @param args the arguments
   */
  public static void main(String[] args) {
    System.out.println("Hello, world!");
    
    try {
      Options options = new Options();
      options.addOption(DESTINATION_PARAM_SHORT, DESTINATION_PARAM_LONG, true,
          "Specifies the default dispatcher address. Default = " + DEFAULT_DESTINATION);
      CommandLineParser parser = new DefaultParser();
      CommandLine cmd = parser.parse(options, args);
      
      String destination = null;

      if (cmd.hasOption(DESTINATION_PARAM_LONG)) {
        Logger.onDebug(LOG_LABEL, "Picked up an explicitly specified destination.");
        destination = cmd.getOptionValue(DESTINATION_PARAM_LONG);
      } else {
        Logger.onDebug(LOG_LABEL, "No port specified, falling back to default.");
        destination = DEFAULT_DESTINATION;
      }
      
      Machine machine = new Machine();
      Hypervisor hypervisor = new Hypervisor();
      hypervisor.boot(machine);
      machine.registerStateListener(hypervisor);
      
      DispatcherHandler handler = DispatcherHandler.build(destination);
      handler.registerCommandListener(machine);
      
      TerminalUI terminalUI = TerminalUI.build();
      terminalUI.registerInputListener(machine);
      
      // catch SIGTERM
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override public void run() {
          try {
            System.out.println("Shutting down...");
            terminalUI.stop();
            Thread.sleep(1000);
          } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        }
      });
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Reads a resource, preferably plaintext. The resource can be in the
   * classpath, in the JAR (if compiled as such), or on the disk. <em>Reads the
   * entire file at once--so it's probably not wise to read huge files at one
   * time.</em> Eliminates line breaks in the process, so best for source files
   * i.e. HTML or SQL.
   * 
   * @param resource the file that needs to be read
   * @return String containing the file's contents
   */
  public static String readResource(String resource) {
    try {
      if(resource == null) return null;
      File file = new File(resource);
      InputStream inputStream = null;
      if(file.canRead())
        inputStream = new FileInputStream(file);
      else
        inputStream = DFAParser.class.getResourceAsStream(resource);
      if(inputStream == null) return null;
      InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
      BufferedReader reader = new BufferedReader(streamReader);
      StringBuilder stringBuilder = new StringBuilder();
      for(String line; (line = reader.readLine()) != null;)
        stringBuilder.append(line.trim());
      return stringBuilder.toString();
    } catch(IOException e) { }
    return null;
  }
  
}
