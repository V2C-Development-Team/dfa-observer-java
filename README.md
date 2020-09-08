# Deterministic Finite-State Automata Parser Demonstration

*Copyright (c) 2020 Caleb L. Power. All rights reserved.*

The purpose of this program is to demonstrate a deterministic finite state
automaton and show how it could be used in rudimentary forms of natural voice
processing.

## Build

You need Java 11. This project can be tested and compiled with the following command.

`gradle clean test shadowJar`

## Execute

To run it, just do `java -jar build\libs\dfa-parser.jar`.

You'll need to enter a string of text in accordance with the grammar below.

![Image of the parser's underlying DFA](docs/parse_fsa.png?raw=true)

To quit the program, you can just hit `CTRL + C`.

## Configuration

If you're going to use the Bootloader in a different program, keep the
following in mind:

* The schema has to be a single JSON object.
* Each top-level object in the main JSON object needs to have an integer key, even though it's represented as a string.
* The top-level object with the lowest key will be the starting point. There can only be one starting point.
* Each top-level object may or may not contain a "transitions" object
* The splat is a reserved keyword in the "transitions" object; it denotes a default state on any token.
* Explicitly-defined transitions take precedence over implicitly-defined splat transitions.
* Top-level objects can contain a "state" variable-- either "restart" or "done".
* Top-level objects can contain a "set" object, which must have both a "key" and a "value".
* Top-level objects can contain a "save" object, which will save the transition to the specified key.
* The "set" key and the "save" key cannot be the same for one state.

**This code repository has been released under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).**