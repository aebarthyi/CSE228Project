package DSP228
import chisel3._
import chisel3.util._
import chisel3.experimental.FixedPoint

// Description: TwiddleFactor module. Read-only-memory that contains the precomputed
// Twiddle Factor values needed for the Coole-Tukey Radix-2 FFT calculation.
// Uses chisel Vec to store data.
// Can parameterize size of the lookup table as well as the bit width.

class TwiddleFactor(N: Int, width: Int) extends Module {
    val io = IO(new Bundle {
        val m = Input(UInt(log2Ceil(N).W))
        val twiddleFactorReal = Output(FixedPoint(width.W, (width/2).BP))
        val twiddleFactorImag = Output(FixedPoint(width.W, (width/2).BP))
    })

    val realData = Seq.tabulate(N)(i => (math.cos(2 * math.Pi * i/N)).F(width.W, (width/2).BP))
    val imagData = Seq.tabulate(N)(i => (math.sin(2 * math.Pi * i/N)).F(width.W, (width/2).BP))
    
    val twiddleRealLUT: Vec[FixedPoint] = VecInit(realData)
    val twiddleImagLUT: Vec[FixedPoint] = VecInit(imagData)

    io.twiddleFactorReal := twiddleRealLUT(io.m)
    io.twiddleFactorImag := twiddleImagLUT(io.m)
}