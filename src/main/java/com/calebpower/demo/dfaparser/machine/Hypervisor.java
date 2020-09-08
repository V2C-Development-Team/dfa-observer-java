package com.calebpower.demo.dfaparser.machine;

import java.util.Map.Entry;

import com.calebpower.demo.dfaparser.DFAParser;
import com.calebpower.demo.dfaparser.state.State;
import com.calebpower.demo.dfaparser.state.StateListener;

/**
 * The faux hypervisor implementation to manage the machine and the values
 * that we're ultimately trying to manage.
 * 
 * @author Caleb L. Power
 */
public class Hypervisor implements StateListener {
  
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
    System.out.printf("--- Reached new state: %1$d via token %2$s\n", state.getID(), state.getIncomingTransition());
    for(Entry<String, String> entry : machine.getRegister().entrySet())
      System.out.printf("- %1$s = %2$s\n", entry.getKey(), entry.getValue());
  }
  
  /**
   * {@inheritDoc}
   */
  @Override public void onTerminalState(State state) {
    System.out.println("--- Hit terminal state!");
    
    if(!machine.getRegister().containsKey("action"))
      System.err.println("Oracle could not pick out the action.");
    else if(!machine.getRegister().containsKey("position"))
      System.err.println("Oracle could not pick out the position.");
    else {
      String position = machine.getRegister().get("position");
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
        System.err.println("Oracle picked up an invalid position.");
      else {
        String action = machine.getRegister().get("action");
        if(action.equals("place")) {
          if(!machine.getRegister().containsKey("word"))
            System.err.println("Oracle could not pick out the word.");
          else {
            String word = machine.getRegister().get("word");
            System.out.printf("Modifying position %1$d: \"%2$s\" -> \"%3$s\"\n", index, values[index], word);
            values[index] = word;
          }
        } else if(action.equals("delete")) {
          System.out.printf("Modifying position %1$d: \"%2$s\" -> \" \"\n", index, values[index]);
          values[index] = "";
        } else System.err.println("Oracle picked up an invalid action.");
      }
    }
    
    for(int i = 0; i < values.length; i++)
      System.out.printf(" [ %1$s ]", values[i]);
    System.out.printf("\n--- Rebooting...\n");
    machine.loadState(bootloader.getInitialState());
  }
  
}
