package DSP228

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
class ButterflyUnitTester extends AnyFlatSpec with ChiselScalatestTester{
  def complexMulModel(aReal: Double, aImg: Double, bReal: Double, bImg: Double): (Double, Double) ={
    val real = (aReal * bReal) - (aImg * bImg)
    val imag = (aReal * bImg) + (aImg * bReal)
    (real, imag)
  }
  def ButterflyUnit(aReal: Double, aImg: Double, bReal: Double, bImg: Double, twiddleReal: Double, twiddleImg: Double): ((Double, Double),(Double, Double)) = {
    val aOutReal = aReal + complexMulModel(bReal, bImg, twiddleReal, twiddleImg)._1 //x0 + x1(twiddle)
    val aOutImg = aImg + complexMulModel(bReal, bImg, twiddleReal, twiddleImg)._2 //x0 + x1(twiddle)
    val bOutReal = aReal - complexMulModel(bReal, bImg, twiddleReal, twiddleImg)._1 //x0 - x1(twiddle)
    val bOutImg = aImg - complexMulModel(bReal, bImg, twiddleReal, twiddleImg)._2 //x0 - x1(twiddle)
    ((aOutReal, aOutImg), (bOutReal,bOutImg))
  }

  def testButterflyUnit(x0: (Double, Double), x1: (Double, Double), twiddle: (Double, Double)): Unit = {
    test(new ButterflyUnit(16)) {dut =>
      dut.io.aReal.poke(x0._1)
      dut.io.aImg.poke(x0._2)
      dut.io.bReal.poke(x1._1)
      dut.io.bImg.poke(x1._2)
      dut.io.twiddleReal.poke(twiddle._1)
      dut.io.twiddleImg.poke(twiddle._2)
      dut.io.coutReal.expect(ButterflyUnit(x0._1, x0._2, x1._1, x1._2, twiddle._1, twiddle._2)._1._1)
      dut.io.coutImg.expect(ButterflyUnit(x0._1, x0._2, x1._1, x1._2, twiddle._1, twiddle._2)._1._2)
      dut.io.doutReal.expect(ButterflyUnit(x0._1, x0._2, x1._1, x1._2, twiddle._1, twiddle._2)._2._1)
      dut.io.doutImg.expect(ButterflyUnit(x0._1, x0._2, x1._1, x1._2, twiddle._1, twiddle._2)._2._2)
      println(ButterflyUnit(x0._1, x0._2, x1._1, x1._2, twiddle._1, twiddle._2)._1._1 + " " +
        ButterflyUnit(x0._1, x0._2, x1._1, x1._2, twiddle._1, twiddle._2)._1._2 + "\n" +
        ButterflyUnit(x0._1, x0._2, x1._1, x1._2, twiddle._1, twiddle._2)._2._1 + " " +
        ButterflyUnit(x0._1, x0._2, x1._1, x1._2, twiddle._1, twiddle._2)._2._2 + "\n"
      )
    }
  }

  behavior of "ButterflyUnit"
  it should "MODEL: correctly calculate butterfly of x0(3 + 2j) and x1(2 + 5j) with twiddle of w(4 - 3j)" in {
    println("Y0: " + ButterflyUnit(3, 2, 2, 5, 4,-3)._1)
    println("Y1: " + ButterflyUnit(3, 2, 2, 5, 4,-3)._2)
  }

  it should "CHISEL: correctly calculate butterfly of x0(3 + 2j) and x1(2 + 5j) with twiddle of w(4 - 3j)" in {
    val twiddle = (0.0, 1.0) //4 - 3j
    val x0 = (-2.0,0.0) //3 + 2j
    val x1 = (-2.0,0.0) //2 + 5j
    testButterflyUnit(x0, x1, twiddle)
  }
}
