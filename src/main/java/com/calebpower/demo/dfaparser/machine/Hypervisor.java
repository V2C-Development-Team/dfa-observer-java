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
    machine.loadState(bootloader.getInitialState());
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
    System.out.printf("\n--- Reached new state: %1$d\n", state.getID());
    for(Entry<String, String> entry : machine.getRegister().entrySet())
      System.out.printf("Entry: %1$s -> %2$s\n", entry.getKey(), entry.getValue());
  }
  
  /**
   * {@inheritDoc}
   */
  @Override public void onTerminalState(State state) {
    System.out.print("\n--- Hit terminal state!\nValues:");
    for(int i = 0; i < values.length; i++)
      System.out.printf(" [ %1$s ]", values[i]);
    System.out.printf("\n\n--- Rebooting...");
    machine.loadState(bootloader.getInitialState());
  }
  
}
