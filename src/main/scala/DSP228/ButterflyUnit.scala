package DSP228

import chisel3._

class ButterFlyUnitIO(width: Int) extends Bundle{
  val aReal = Input(SInt(width.W))
  val aImg = Input(SInt(width.W))
  val bReal = Input(SInt(width.W))
  val bImg = Input(SInt(width.W))
  val twiddleReal = Input(SInt(width.W))
  val twiddleImg = Input(SInt(width.W))
  val coutReal = Output(SInt(width.W))
  val coutImg = Output(SInt(width.W))
  val doutReal = Output(SInt(width.W))
  val doutImg = Output(SInt(width.W))
}
class ButterflyUnit(width: Int) extends Module {
  val mul = Module(new ComplexMul(width))
  val io = IO(new ButterFlyUnitIO(width))
  //x1 * twiddle
  mul.io.aReal := io.bReal
  mul.io.aImg := io.bImg
  mul.io.bReal := io.twiddleReal
  mul.io.bImg := io.twiddleImg
  //Y0 = x0 + x1(twiddle)
  io.coutReal := io.aReal + mul.io.realOut
  io.coutImg := io.aImg + mul.io.imgOut
  //Y1 = x0 - x1(twiddle)
  io.doutReal := io.aReal - mul.io.realOut
  io.doutImg := io.aImg - mul.io.imgOut
}
