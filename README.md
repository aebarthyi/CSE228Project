# CSE228Project
Andrew and Omkar's CSE228 Final Project
## Description

## Prerequisites
Make sure you have sbt installed on the machine you are planning to use.\
If you haven't installed it before, follow the directions from this [link](https://www.scala-sbt.org/1.x/docs/Setup.html)
## Getting Started
Clone the repo to your machine, and run the following commands in order:
1. `cd CSE228Project`
2. `sbt`
3. `compile`

After following these steps you will have compiled the project

## Things that work:
1. Complex Multiplier and Tester
2. Butterfly Unit and Tester
3. Twiddle Factor ROM and Tester
4. Bit Reverse model (for use to test the AGU)
5. Memory and Tester

## Work in progress:
1. Address Generation Unit
2. Filters
3. Forward and Inverse FFT blocks (or singular butterfly units, depending on multipliers specified)

## How to test
Once the sbt kernel is opened up (Done by step 2 of Getting Started), you can test all of the created tests by running the `test` command.\
If you want to test individual components you can do so by running the `testOnly *testClass` command in the sbt kernel instead.\
The test classes that are currently supported are:
1. `BitReversalTester`
2. `ButterflyUnitTester`
3. `ComplexMulModelTester`
4. `TwiddleFactorTester`
5. `RAMTester`
