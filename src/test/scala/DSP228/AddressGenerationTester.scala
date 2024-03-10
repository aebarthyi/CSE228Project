package DSP228

import chisel3._
import chiseltest._
import chiseltest.ChiselScalatestTester
import org.scalatest.flatspec.AnyFlatSpec

import scala.collection.mutable.ListBuffer

class AddressGenerationTester extends AnyFlatSpec with ChiselScalatestTester {
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
  def addressGenerationUnit(points: Int, stage: Int, width: Int, addrAin: Int, addrBin: Int) : (Int, Int)={
    val addrA = (( addrAin << stage) | (addrAin >> (width - stage ))) & (points-1)
    val addrB = (( addrBin << stage) | (addrBin >> (width - stage ))) & (points-1)
    (addrA, addrB)
  }
  def testAGU(points: Int, stages: Int) = {
    test(new AddressGenerationUnit(points, stages)) { dut =>
      dut.io.en.poke(true.B)
      var addrA = 0
      var addrB = 0
      val indexes = new ListBuffer[Int]()
      for (i <- 0 until points) {
        indexes += bitReversal(5, i)
      }

      for (j <- 0 until stages) {
        dut.io.stage.poke(j.U)
        println()
        println("stage " + j)
        println()
        for (i <- 0 until (points / 2)) {
          addrA = 2 * i
          addrB = addrA + 1
          indexes(addrA) = addressGenerationUnit(points, j, stages, addrA, addrB)._1
          dut.io.addressA.expect(addressGenerationUnit(points, j, stages, addrA, addrB)._1)
          indexes(addrB) = addressGenerationUnit(points, j, stages, addrA, addrB)._2
          dut.io.addressB.expect(addressGenerationUnit(points, j, stages, addrA, addrB)._2)
          println("{" + dut.io.twiddleAddress.peek() + "}")
          println("{" + addressGenerationUnit(points, j, stages, addrA, addrB)._1 + "}{" + addressGenerationUnit(points, j, stages, addrA, addrB)._2 + "}")
          dut.clock.step()
        }
      }

      println(indexes)
    }

  }

  behavior of "AddressGenerationUnit"
  it should "correctly calculate addresses for 8 points, 3 stages" in {
    testAGU(8,3)
  }

  it should "correctly calculate addresses for 32 points, 5 stages" in {
    testAGU(32,5)
  }

  it should "correctly calculate addresses for 1024 points, 10 stages" in {
    testAGU(1024,10)
  }
}
