package DSP228

import chisel3.util
import chiseltest.ChiselScalatestTester
import org.scalatest.flatspec.AnyFlatSpec

class BitReversalTester extends AnyFlatSpec with ChiselScalatestTester {
  def bitReversal(bits: Int): Int = {
    var result: Int = 0
    var n: Int = bits
    while (n != 0)
    {
      if (result != 0)
      {
        result = result << 1
      }
      if ((n % 2) == 1)
      {
        result = result ^ 1
      }
      n = n / 2
    }
    println("\nNumber : " + bits)
    println("Output : " + result)
    result
  }
  // TODO: IMPLEMENT CHISEL BITREVERSE AND TEST HERE:
//  def testBitReverse(bits: Int) = {
//    test(new ComplexMul(32)) { dut =>
//
//    }
//  }

  behavior of "Bitreversal"
  it should "correctly reverse the bits in number 679 (1010100111)" in {
    bitReversal(679)
  }

  it should "correctly calculate output for (3 + 2i) * (4 - 5i)" in {

}

  it should "correctly calculate output for (3 - 2i) * (4 - 5i)" in {

  }
}
