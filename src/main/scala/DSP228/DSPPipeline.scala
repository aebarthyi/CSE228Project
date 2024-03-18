package DSP228

import chisel3._
import chisel3.experimental.FixedPoint
import chisel3.util._

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