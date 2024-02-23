package DSP228

import chisel3._
class ComplexMul(width: Int) extends Module{
  val io = IO(new Bundle {
    val aReal = Input(SInt(width.W))
    val aImg = Input(SInt(width.W))
    val bReal = Input(SInt(width.W))
    val bImg = Input(SInt(width.W))
    val realOut = Output(SInt(width.W))
    val imgOut = Output(SInt(width.W))
  })
  io.realOut := (io.aReal * io.bReal) - (io.aImg * io.bImg)
  io.imgOut := (io.aReal * io.bImg) + (io.aImg * io.bReal)
}
