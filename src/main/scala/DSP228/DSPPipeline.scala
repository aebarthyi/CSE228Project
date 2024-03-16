package DSP228

import chisel3._
import chisel3.experimental.FixedPoint
import chisel3.util._

object PipelineState extends ChiselEnum {
    val idle, calculating, out = value
}

class DSPIO(points: Int, width: Int) extends Bundle {
    val in = Flipped(Decoupled(Vec(2, FixedPoint(width.W, (width/2).BP))))
    val out = Decoupled(Vec(2, FixedPoint(width.W, (width/2).BP)))
}

class DSPPipeline(points: Int, width: Int) extends Module {

}