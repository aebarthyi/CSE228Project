package DSP228

import chisel3._
import chisel3.experimental.FixedPoint
import chisel3.util._

object PipelineState extends ChiselEnum {
    val idle, forward, filter, inverse, out = Value
}

class DSPIO(points: Int, width: Int) extends Bundle {
    val in = Flipped(Decoupled(Vec(2, FixedPoint(width.W, (width/2).BP))))
    val out = Decoupled(Vec(2, FixedPoint(width.W, (width/2).BP)))
}

class DSPPipeline(points: Int, width: Int) extends Module {
    val io = IO(new DSPIO(points,width))
    val state_r = RegInit(PipelineState.idle)
    
    io.in.ready := false.B
    io.out.valid := false.B
    for (i <- 0 until 2) {
        io.out.bits(i) := 0.F(width.W, (width/2).BP)
    }

    switch(state_r) {
        is(PipelineState.idle) {
            io.in.ready := true.B
            io.out.valid := false.B
            
        }
    }
}