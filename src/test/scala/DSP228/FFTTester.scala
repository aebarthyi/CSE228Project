package DSP228

import chisel3._
import chisel3.util._
import chiseltest._
import chiseltest.ChiselScalatestTester
import org.scalatest.flatspec.AnyFlatSpec

class FFTTester extends AnyFlatSpec with ChiselScalatestTester{
  val sineWave440hz = Seq(1,2,3,4,5,6,7,8)
  def testFFT(points: Int, width: Int)= {
    test(new FFT(points, width)).withAnnotations(Seq(WriteVcdAnnotation)){ dut =>
      dut.io.in.valid.poke(false.B)
      for(i <- 0 until points/2) {
        dut.io.in.valid.poke(true.B)
        dut.io.in.bits(0).poke(sineWave440hz(2*i))
        dut.io.in.bits(1).poke(sineWave440hz((2*i)+1))
        dut.clock.step()
      }
      dut.io.in.valid.poke(false.B)
      dut.clock.step(points*9)
      dut.clock.step(8)
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
