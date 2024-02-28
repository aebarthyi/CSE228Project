package DSP228
import chisel3._
import chisel3.util._
import chisel3.experimental.FixedPoint

class TwiddleFactor(N: Int) extends Module {
    val io = IO(new Bundle {
        val m = Input(UInt(log2Ceil(N).W))
        val twiddleFactorReal = Output(FixedPoint(32.W, 8.BP))
        val twiddleFactorImag = Output(FixedPoint(32.W, 8.BP))
    })

    val realData = Seq.tabulate(N)(i => (math.cos(2 * math.Pi * i/N)).F(32.W, 8.BP))
    val imagData = Seq.tabulate(N)(i => (math.sin(2 * math.Pi * i/N)).F(32.W, 8.BP))
    
    val twiddleRealLUT: Vec[FixedPoint] = VecInit(realData)
    val twiddleImagLUT: Vec[FixedPoint] = VecInit(imagData)

    io.twiddleFactorReal := twiddleRealLUT(io.m)
    io.twiddleFactorImag := twiddleImagLUT(io.m)
}