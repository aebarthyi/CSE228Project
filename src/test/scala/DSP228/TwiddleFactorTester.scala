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
        test(new TwiddleFactor(16, 32)) { dut =>
            for (j <- 0 until 16) {
                dut.io.m.poke(j.U)
                val twiddleTuple = twiddleFactorModel(j,16)
                println(dut.io.twiddleFactorReal.peek() + "|" + dut.io.twiddleFactorImag.peek())
                dut.io.twiddleFactorReal.expect(twiddleTuple._1.F(32.W, 16.BP))
                dut.io.twiddleFactorImag.expect(twiddleTuple._2.F(32.W, 16.BP))
            }
        }
    }
}