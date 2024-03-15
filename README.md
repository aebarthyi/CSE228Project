# CSE228Project
Andrew and Omkar's CSE228 Final Project
## Description
In this project, we aim to make a chisel generator for a hardware digital signal processing audio filter that allows for audio frequency spectrum analysis. Our entire signal processing pipeline will consist of hardware implementations of a fast fourier transform (to convert a analog signal to digital), weighted audio filters(to apply weights/filters to the binned analog signals) and an inverse fast fourier transform (to convert the edited digital signal back to a digital form). We will be using the Cooley-Tukey algorithm, more specifically the radix-2 method to give us a fast performing implementation of an FFT. At the very least, we aim to provide CD quality audio using our audio filter, i.e. 44.1 kHZ/16-bit audio resolution with a 1024 point FFT. Furthermore, we also aim to provide parameterizable FFT block lengths to resize the transformation resolution as well as to provide parameterizable bit depth for the input audio.
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
4. Bit Reverse model (for use to test the AGU) and Tester
5. Memory and Tester
6. Address Generation Unit and Tester
7. Forward FFT blocks and Tester

## Work in progress:
1. Filters
2. Inverse FFT blocks (or singular butterfly units, depending on multipliers specified)

## How to test
Once the sbt kernel is opened up (Done by step 2 of Getting Started), you can test all of the created tests by running the `test` command.\
If you want to test individual components you can do so by running the `testOnly *testClass` command in the sbt kernel instead.\
The test classes that are currently supported are:
1. `BitReversalTester`
2. `ButterflyUnitTester`
3. `ComplexMulModelTester`
4. `TwiddleFactorTester`
5. `RAMTester`
6. `AddressGenerationTester`
7. `FFTTester`
