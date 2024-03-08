package DSP228
import chisel3._
import chisel3.util._
import chisel3.experimental.FixedPoint

class RAM(N: Int) extends Module {
    val io = IO(new Bundle {
        val read = Input(Bool())
        val in_valid = Input(Bool())
        val addr1 = Input(UInt(log2Ceil(N).W))
        val addr2 = Input(UInt(log2Ceil(N).W))
        val out_valid = Output(Bool())
        val out1 = Output(FixedPoint(32.W, 8.BP))
        val out2 = Output(FixedPoint(32.W, 8.BP))
    })
    // TODO: Implement Behavior
    io.out_valid := 0.B 
    io.out1 := 0.F(32.W, 8.BP)
    io.out2 := 0.F(32.W, 8.BP)
}