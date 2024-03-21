package DSP228

import chisel3._
import chisel3.experimental.FixedPoint
import chisel3.util._

// Description: DSPPipeline Module. Top-level module hosting all of the DSP Pipeline stages 
// inside. Contains the Forward FFT, Filter and Inverse FFT modules. Establishes necessary connections 
// between these modules. Decoupled (ready-valid-bits) input/output interface provided.
// For an N-point input signal, N cycles taken to stream in input and N cycles to stream output out.
// Takes in original digital signal, computes FFT, filters it, computes inverse FFT to reconstruct 
// signal and outputs the filtered signal.
// Can parameterize number of points in the FFT but MUST BE POWER OF 2!! 
// Can also parameterize bit width of input signal.

class DSPIO(points: Int, width: Int) extends Bundle {
    val in = Flipped(Decoupled(Vec(2, FixedPoint(width.W, (width/2).BP))))
    val out = Decoupled(Vec(2, FixedPoint(width.W, (width/2).BP)))
}

class DSPPipeline(points: Int, width: Int) extends Module {
    val io = IO(new DSPIO(points,width))
    
    val forwardFFT = Module(new FFT(points, width))
    val filter = Module(new Filter(points, width))
    val inverseFFT = Module(new IFFT(points,width))
    io.in <> forwardFFT.io.in
    filter.io.in <> forwardFFT.io.out
    inverseFFT.io.in <> filter.io.out
    io.out <> inverseFFT.io.out
}