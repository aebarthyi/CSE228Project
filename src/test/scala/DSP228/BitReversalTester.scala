package DSP228

import chisel3.util
import chiseltest.ChiselScalatestTester
import org.scalatest.flatspec.AnyFlatSpec

class BitReversalTester extends AnyFlatSpec with ChiselScalatestTester {
  def bitReversal(bits: Int, n: Int): Int = { //n = sizeof(bits)
    var result: Int = 0
    var num: Int = bits
    for(i <- 0 until n){
      result <<= 1
      result |= (num & 1)
      num >>= 1
    }
    result
  }

  behavior of "Bitreversal"
  it should "correctly reverse the bits in number 679 (1010100111)" in {
    bitReversal(679, 32)
  }

  it should "correctly calculate the bit-reversed indices for 0-7" in {
    val input = Seq(0,1,2,3,4,5,6,7)
    val output = input.map{e => bitReversal(e,3)}
    println(output)
}

  it should "correctly calculate the bit-reversed indices for 0-1023" in {
    val input = Seq.range(0,1023)
    val output = input.map{e => bitReversal(e,10)}
    println(output)
  }
}
