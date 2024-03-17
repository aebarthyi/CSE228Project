package DSP228

import chisel3._
import chisel3.util._
import chiseltest._
import chisel3.experimental.FixedPoint
import chiseltest.ChiselScalatestTester
import org.antlr.v4.runtime.misc.Pair
import org.scalatest.flatspec.AnyFlatSpec

import scala.math._
import scala.collection.mutable.ArrayBuffer

class DSPPipelineTester extends AnyFlatSpec with ChiselScalatestTester {
    behavior of "DSPPipeline"
    it should "correctly go thru stages" in {
        test(new DSPPipeline(8,32)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
            dut.clock.setTimeout(0)
            dut.io.in.valid.poke(false.B)
            for(i <- 0 until 2){
                dut.io.in.bits(i).poke(0.F(32.W, 16.BP))
            }
            dut.io.in.ready.expect(true.B)
            dut.io.out.valid.expect(false.B)
        }
    }
}