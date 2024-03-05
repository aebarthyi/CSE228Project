package DSP228

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec



class TwiddleFactorTester extends AnyFlatSpec with ChiselScalatestTester {
    def twiddleFactorModel(m: Int, N: Int): (Double, Double) = {
        val twiddleReal = (math.cos(2 * math.Pi * m/N))
        val twiddleImag = (math.sin(2 * math.Pi * m/N))
        (twiddleReal, twiddleImag)
    }

    behavior of "TwiddleFactor"
    it should "correctly calculate Twiddle Factor" in {
        test(new TwiddleFactor(8)) { dut =>
            for (j <- 0 until 8) {
                dut.io.m.poke(j.U)
                val twiddleTuple = twiddleFactorModel(j,8) 
                dut.io.twiddleFactorReal.expect(twiddleTuple._1.F(32.W, 8.BP))
                dut.io.twiddleFactorImag.expect(twiddleTuple._2.F(32.W, 8.BP))
            }
        }
    }
}