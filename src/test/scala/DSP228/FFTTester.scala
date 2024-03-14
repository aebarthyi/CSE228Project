package DSP228

import chisel3._
import chisel3.util._
import chiseltest._
import chisel3.experimental.FixedPoint
import chiseltest.ChiselScalatestTester
import org.scalatest.flatspec.AnyFlatSpec

class FFTTester extends AnyFlatSpec with ChiselScalatestTester{
  def testFFT(points: Int, width: Int,input: Seq[Int])= {
    test(new FFT(points, width)).withAnnotations(Seq(WriteVcdAnnotation)){ dut =>
      dut.clock.setTimeout(0)
      dut.io.in.valid.poke(false.B)
      for(i <- 0 until points/2) {
        dut.io.in.valid.poke(true.B)
        dut.io.in.bits(0).poke(input(2*i).F(width.W, (width/2).BP))
        dut.io.in.bits(1).poke(input((2*i)+1).F(width.W, (width/2).BP))
        dut.clock.step()
      }
      dut.io.in.valid.poke(false.B)
      dut.clock.step(points*18)
    }
  }

  behavior of "FFT"
  it should "correctly calculate addresses for 4 points, 2 stages" in {
    val fourPoints= Seq(1,2,3,4)
    testFFT(4, 24, fourPoints)
  }

  it should "correctly calculate addresses for 8 points, 3 stages" in {
    val eightPoints= Seq(1,2,3,4,5,6,7,8)
    testFFT(8, 24, eightPoints)
  }

  it should "correctly calculate addresses for 32 points, 5 stages" in {
    val thirty2Points= Seq(0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31)
    testFFT(32, 24, thirty2Points)
  }

  it should "correctly calculate addresses for 64 points, 6 stages" in {
    val sixty4points = Seq.tabulate(64)(i => i)
    testFFT(64, 24, sixty4points)
  }

  it should "correctly calculate addresses for 128 points, 7 stages" in {
    val one28points = Seq.tabulate(128)(i => i)
    testFFT(128, 32, one28points)
  }

  it should "correctly calculate addresses for 256 points, 8 stages" in {
    val two56points = Seq.tabulate(256)(i => i)
    testFFT(256, 32, two56points)
  }

}
