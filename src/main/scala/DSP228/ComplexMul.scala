package DSP228

import chisel3._
import chisel3.experimental.FixedPoint
class ComplexMul(width: Int) extends Module{
  val io = IO(new Bundle {
    val aReal = Input(FixedPoint(width.W, (width/2).BP))
    val aImg = Input(FixedPoint(width.W, (width/2).BP))
    val bReal = Input(FixedPoint(width.W, (width/2).BP))
    val bImg = Input(FixedPoint(width.W, (width/2).BP))
    val realOut = Output(FixedPoint(width.W, (width/2).BP))
    val imgOut = Output(FixedPoint(width.W, (width/2).BP))
  })
  io.realOut := (io.aReal * io.bReal) - (io.aImg * io.bImg)
  io.imgOut := (io.aReal * io.bImg) + (io.aImg * io.bReal)
}
