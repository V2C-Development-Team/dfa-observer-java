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
package com.calebpower.demo.dfaparser.state;

/**
 * Navigates through the states with the help of provided tokens.
 * 
 * @author Caleb L. Power
 */
public class Machine {
  
  private State currentState = null;
  
  /**
   * Loads the first state into the machine.
   * 
   * @param state the first state
   */
  public void loadState(State state) {
    this.currentState = state;
  }
  
  /**
   * Pushes a token into the machine and transitions to the appropriate state.
   * 
   * @param token the token
   * @throws Exception if the provided token caused the machine to enter an
   *         illegal state.
   */
  public void pushToken(String token) throws Exception {
    if(currentState == null)
      throw new Exception("Illegal machine state.");
    
    currentState = currentState.getNextState(token);
  }
  
  /**
   * Retrieves the current state.
   * 
   * @return the current state
   */
  public State getCurrentState() {
    return currentState;
  }
  
}
