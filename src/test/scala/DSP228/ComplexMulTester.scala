package DSP228

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class ComplexMulModelTester extends AnyFlatSpec with ChiselScalatestTester {
  def complexMulModel(aReal: Int, aImg: Int, bReal: Int, bImg: Int): (Int, Int) ={
    val real = (aReal * bReal) - (aImg * bImg)
    val imag = (aReal * bImg) + (aImg * bReal)
    (real, imag)
  }
  def testComplexMul(aReal: Int, aImg: Int, bReal: Int, bImg: Int) = {
    test(new ComplexMul(32)) { dut =>
      dut.io.aReal.poke(aReal)
      dut.io.bReal.poke(bReal)
      dut.io.aImg.poke(aImg)
      dut.io.bImg.poke(bImg)
      dut.clock.step()
      println("Model | real: "+ complexMulModel(aReal,aImg,bReal,bImg)._1 + " imag: "+ complexMulModel(aReal,aImg,bReal,bImg)._2)
      println("Chisel | real: "+ dut.io.realOut.peek()  + " imag: "+ dut.io.imgOut.peek())
      dut.io.realOut.expect(complexMulModel(aReal,aImg,bReal,bImg)._1)
      dut.io.imgOut.expect(complexMulModel(aReal,aImg,bReal,bImg)._2)
    }
  }

  behavior of "ComplexMul"
  it should "correctly calculate output for (3 + 2i) * (4 + 5i)" in {
    testComplexMul(3, 2, 4, 5)
  }

  it should "correctly calculate output for (3 + 2i) * (4 - 5i)" in {
    testComplexMul(3, 2, 4, -5)
  }

  it should "correctly calculate output for (3 - 2i) * (4 - 5i)" in {
    testComplexMul(3, -2, 4, -5)
  }
}


