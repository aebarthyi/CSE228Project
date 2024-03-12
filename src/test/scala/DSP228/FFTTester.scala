package DSP228

import chisel3._
import chiseltest._
import chiseltest.ChiselScalatestTester
import org.scalatest.flatspec.AnyFlatSpec

class FFTTester extends AnyFlatSpec with ChiselScalatestTester{
  val sineWave440hz = Seq(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32)
  def testFFT(points: Int, width: Int)= {
    test(new FFT(points, width)).withAnnotations(Seq(WriteVcdAnnotation)){ dut =>
      for(i <- 0 until points) {
        dut.io.in.valid.poke(true.B)
        dut.io.in.bits.poke(sineWave440hz(i))
        dut.clock.step()
      }
      dut.clock.step(points*3)
      for(i <- 0 until points){
        println(dut.io.out.bits.peek()))
        dut.clock.step()
      }
    }
  }

  behavior of "FFT"
  it should "correctly calculate addresses for 8 points, 3 stages" in {
    testFFT(8, 24)
  }

  it should "correctly calculate addresses for 32 points, 5 stages" in {

  }

  it should "correctly calculate addresses for 1024 points, 10 stages" in {
  }
}
