package com.calebpower.demo.dfaparser.machine;

import java.util.List;
import java.util.Map.Entry;

import com.calebpower.demo.dfaparser.DFAParser;
import com.calebpower.demo.dfaparser.state.State;
import com.calebpower.demo.dfaparser.state.StateListener;
import com.calebpower.demo.dfaparser.ui.Logger;

/**
 * The faux hypervisor implementation to manage the machine and the values
 * that we're ultimately trying to manage.
 * 
 * @author Caleb L. Power
 */
public class Hypervisor implements StateListener {
  
  private static final String LOG_LABEL = "HYPERVISOR";
  
  private Bootloader bootloader = null;
  private Machine machine = null;
  private String[] values = new String[5];
  
  /**
   * Instantiates the hypervisor.
   * @throws Exception if the bootloader couldn't load the file
   */
  public Hypervisor() throws Exception {
    for(int i = 0; i < values.length; i++)
      values[i] = "";
    
    String rawSchema = DFAParser.readResource("/state_schema.json");
    bootloader = new Bootloader(rawSchema);
  }
  
  /**
   * Boots the machine.
   * (This can be construed as a hypervisor booting a virtual machine.)
   * 
   * @param machine the machine
   */
  public void boot(Machine machine) {
    this.machine = machine;
    machine.loadState(bootloader.getInitialState());
  }
  
  /**
   * {@inheritDoc}
   */
  @Override public void onState(State state) {
    System.out.printf("- Reached new state: %1$d via token %2$s\n", state.getID(), state.getIncomingTransition());
    for(Entry<String, List<String>> entry : machine.getRegister().entrySet()) {
      Logger.onDebug(LOG_LABEL, String.format("--- Key: %1$s\n", entry.getKey()));
      for(String value : entry.getValue())
        Logger.onDebug(LOG_LABEL, String.format("----- Value: %1$s\n", value));
    }
  }
  
  /**
   * {@inheritDoc}
   */
  @Override public void onTerminalState(State state) {
    Logger.onDebug(LOG_LABEL, "--- Hit terminal state!");
    
    if(!machine.getRegister().containsKey("action"))
      Logger.onDebug(LOG_LABEL, "Oracle could not pick out the action.");
    else if(!machine.getRegister().containsKey("position"))
      Logger.onDebug(LOG_LABEL, "Oracle could not pick out the position.");
    else {
      String position = machine.getRegister().get("position").get(0);
      int index = -1;
      
      switch(position.toLowerCase()) {
      case "1":
      case "one":
        index = 0;
        break;
        
      case "2":
      case "two":
        index = 1;
        break;
        
      case "3":
      case "three":
        index = 2;
        break;
        
      case "4":
      case "four":
        index = 3;
        break;
        
      case "5":
      case "five":
        index = 4;
      }
      
      if(index == -1)
        Logger.onError(LOG_LABEL, "Oracle picked up an invalid position.");
      else {
        String action = machine.getRegister().get("action").get(0);
        if(action.equals("place")) {
          if(!machine.getRegister().containsKey("word"))
            Logger.onError(LOG_LABEL, "Oracle could not pick out the word.");
          else {
            String word = machine.getRegister().get("word").get(0);
            Logger.onInfo(LOG_LABEL, String.format("Modifying position %1$d: \"%2$s\" -> \"%3$s\"", index, values[index], word));
            values[index] = word;
          }
        } else if(action.equals("delete")) {
          Logger.onInfo(LOG_LABEL, String.format("Modifying position %1$d: \"%2$s\" -> \" \"", index, values[index]));
          values[index] = "";
        } else Logger.onError(LOG_LABEL, "Oracle picked up an invalid action.");
      }
    }
    
    StringBuilder sb = new StringBuilder();
    for(int i = 0; i < values.length; i++)
      sb.append(String.format("[ %1$s ] ", values[i]));
    
    Logger.onInfo(LOG_LABEL, sb.toString());
    Logger.onDebug(LOG_LABEL, "--- Rebooting...");
    machine.loadState(bootloader.getInitialState());
  }
  
}
