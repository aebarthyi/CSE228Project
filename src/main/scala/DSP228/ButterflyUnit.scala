package DSP228

import chisel3._
import chisel3.experimental.FixedPoint

class ButterFlyUnitIO(width: Int) extends Bundle{
  val aReal = Input(FixedPoint(width.W, (width/2).BP))
  val aImg = Input(FixedPoint(width.W, (width/2).BP))
  val bReal = Input(FixedPoint(width.W, (width/2).BP))
  val bImg = Input(FixedPoint(width.W, (width/2).BP))
  val twiddleReal = Input(FixedPoint(width.W, (width/2).BP))
  val twiddleImg = Input(FixedPoint(width.W, (width/2).BP))
  val coutReal = Output(FixedPoint(width.W, (width/2).BP))
  val coutImg = Output(FixedPoint(width.W, (width/2).BP))
  val doutReal = Output(FixedPoint(width.W, (width/2).BP))
  val doutImg = Output(FixedPoint(width.W, (width/2).BP))
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
