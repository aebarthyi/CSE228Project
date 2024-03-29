package DSP228
import chisel3._
import chisel3.util._
import chisel3.experimental.FixedPoint

// Description: RAM Module. Random Access Memory that stores inputs and outputs 
// of the butterfly modules, which allow for the near-memory FFT/IFFT computation.
// Internally uses two separate SyncReadMems, one for real-values, one for imaginary-values
// Can parameterize capacity of the the RAM as needed by the desired FFT computation depth.
// Can also parameterize bit width of the data being stored.

class RAMUnitIO(NumEntries: Int, width: Int) extends Bundle {
    // enable signal
    val enable = Input(Bool())

    // read or write signal
    val read = Input(Bool()) // if read = high then read, else write
    
    // writing inputs
    val realIn1 = Input(FixedPoint(width.W, (width/2).BP))
    val realIn2 = Input(FixedPoint(width.W, (width/2).BP))
    val imagIn1 = Input(FixedPoint(width.W, (width/2).BP))
    val imagIn2 = Input(FixedPoint(width.W, (width/2).BP))
    
    // addresses
    val addr1 = Input(UInt(log2Ceil(NumEntries).W))
    val addr2 = Input(UInt(log2Ceil(NumEntries).W))
    
    // output signals
    val realOut1 = Output(FixedPoint(width.W, (width/2).BP))
    val realOut2 = Output(FixedPoint(width.W, (width/2).BP))
    val imagOut1 = Output(FixedPoint(width.W, (width/2).BP))
    val imagOut2 = Output(FixedPoint(width.W, (width/2).BP))
}

class RAM(NumEntries: Int, width: Int) extends Module {
    val io = IO(new RAMUnitIO(NumEntries, width))
    // TODO: ???
    val realMem = SyncReadMem(NumEntries, FixedPoint(width.W, (width/2).BP))
    val imagMem = SyncReadMem(NumEntries, FixedPoint(width.W, (width/2).BP))
    
    io.realOut1 := DontCare
    io.realOut2 := DontCare
    io.imagOut1 := DontCare
    io.imagOut2 := DontCare
    
    when(io.enable) {
        val rdwr_real1 = realMem(io.addr1)
        val rdwr_real2 = realMem(io.addr2)
        val rdwr_imag1 = imagMem(io.addr1)
        val rdwr_imag2 = imagMem(io.addr2)
        
        when(io.read) {
            io.realOut1 := rdwr_real1
            io.realOut2 := rdwr_real2
            io.imagOut1 := rdwr_imag1
            io.imagOut2 := rdwr_imag2
        } .otherwise {
            rdwr_real1 := io.realIn1
            rdwr_real2 := io.realIn2
            rdwr_imag1 := io.imagIn1
            rdwr_imag2 := io.imagIn2
        }
    }
}