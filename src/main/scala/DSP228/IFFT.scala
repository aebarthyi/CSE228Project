package DSP228

import chisel3._
import chisel3.experimental.FixedPoint
import chisel3.util._

object IFFTState extends ChiselEnum {
  val idle, load, calculateStage, writeStage, out = Value
}
class IFFTIO(points: Int, width: Int) extends Bundle{
  val in = Flipped(Decoupled(Vec(2, FixedPoint(width.W, (width/2).BP))))
  val out = Decoupled(Vec(2, FixedPoint(width.W, (width/2).BP)))
}

class IFFT(points: Int, width: Int) extends Module {
  val io = IO(new IFFTIO(points, width))
  val ifftState = RegInit(IFFTState.idle)
  val fftMem = Module(new RAM(points, width))
  val butterfly = Module(new ButterflyUnit(width))
  val agu = Module(new AddressGenerationUnit(points))
  val twiddleRom = Module(new TwiddleFactor(points, width))
  val startCounter = Wire(Bool())
  val startOutput = Wire(Bool())
  val startTiming = Wire(Bool())
  val startWrite = Wire(Bool())
  val startRead = Wire(Bool())

  val (bitReversedCounter, wrap) = Counter(0 until points, startCounter)
  val (writeClock, writeWrap) = Counter(0 until 2, startWrite)
  val (readClock, readWrap) = Counter(0 until 2, startRead)
  val (timing, timeUp) = Counter(0 until (points*points)+1, startTiming)
  val (outputFinalCounter, finished) = Counter(0 until (points+1), startOutput)

  startCounter := false.B
  startOutput := false.B
  startWrite := false.B
  startRead := false.B
  startTiming := false.B

  twiddleRom.io.m := 0.U
  butterfly.io.aReal := 0.F(width.W, (width/2).BP)
  butterfly.io.bReal := 0.F(width.W, (width/2).BP)
  butterfly.io.aImg := 0.F(width.W, (width/2).BP)
  butterfly.io.bImg := 0.F(width.W, (width/2).BP)
  butterfly.io.twiddleReal := 0.F(width.W, (width/2).BP)
  butterfly.io.twiddleImg := 0.F(width.W, (width/2).BP)

  agu.io.advance := false.B
  io.in.ready := true.B
  io.out.bits(0) := 0.F(width.W, (width/2).BP)
  io.out.bits(1) := 0.F(width.W, (width/2).BP)
  io.out.valid := false.B
  fftMem.io.addr1 := 0.U
  fftMem.io.addr2 := 0.U
  fftMem.io.realIn1 := 0.F(width.W, (width/2).BP)
  fftMem.io.realIn2 := 0.F(width.W, (width/2).BP)
  fftMem.io.imagIn1 := 0.F(width.W, (width/2).BP)
  fftMem.io.imagIn2 := 0.F(width.W, (width/2).BP)
  fftMem.io.enable := false.B
  fftMem.io.read := true.B

  switch(ifftState){
    is(IFFTState.idle){
      startTiming := false.B
      io.in.ready := true.B
      fftMem.io.read := true.B

      when(io.in.valid){
        startTiming := true.B
        startCounter := true.B
        fftMem.io.read := false.B
        fftMem.io.addr1 := Reverse(bitReversedCounter)
        fftMem.io.realIn1 := io.in.bits(1)
        fftMem.io.imagIn1 := io.in.bits(0)
        ifftState := IFFTState.load
      }
    }

    is(IFFTState.load){
      startTiming := true.B
      fftMem.io.enable := true.B
      fftMem.io.read := false.B
      startCounter := true.B
      fftMem.io.addr1 := Reverse(bitReversedCounter)
      fftMem.io.realIn1 := io.in.bits(1)
      fftMem.io.imagIn1 := io.in.bits(0)
      when(wrap){
        ifftState := IFFTState.calculateStage
      }
    }

    is(IFFTState.calculateStage) {
      startTiming := true.B
      startWrite := false.B
      startRead := true.B
      fftMem.io.read := true.B
      agu.io.advance := false.B
      fftMem.io.enable := true.B
      startCounter := false.B

      fftMem.io.addr1 := agu.io.addressA
      fftMem.io.addr2 := agu.io.addressB

      twiddleRom.io.m := agu.io.twiddleAddress

      butterfly.io.aReal := fftMem.io.realOut1
      butterfly.io.bReal := fftMem.io.realOut2

      butterfly.io.aImg := fftMem.io.imagOut1
      butterfly.io.bImg := fftMem.io.imagOut2

      butterfly.io.twiddleReal := twiddleRom.io.twiddleFactorReal
      butterfly.io.twiddleImg := twiddleRom.io.twiddleFactorImag

      fftMem.io.realIn1 := RegNext(butterfly.io.coutReal)
      fftMem.io.realIn2 := RegNext(butterfly.io.doutReal)

      fftMem.io.imagIn1 := RegNext(butterfly.io.coutImg)
      fftMem.io.imagIn2 := RegNext(butterfly.io.doutImg)

      when(readWrap) {
        ifftState := IFFTState.writeStage
      }
    }

    is(IFFTState.writeStage){
      startTiming := true.B
      startWrite := true.B
      startRead := false.B
      fftMem.io.enable := true.B
      fftMem.io.read := false.B

      fftMem.io.addr1 := agu.io.addressA
      fftMem.io.addr2 := agu.io.addressB
      twiddleRom.io.m := agu.io.twiddleAddress

      butterfly.io.aReal := fftMem.io.realOut1
      butterfly.io.bReal := fftMem.io.realOut2

      butterfly.io.aImg := fftMem.io.imagOut1
      butterfly.io.bImg := fftMem.io.imagOut2

      butterfly.io.twiddleReal := twiddleRom.io.twiddleFactorReal
      butterfly.io.twiddleImg := twiddleRom.io.twiddleFactorImag

      fftMem.io.realIn1 := RegNext(butterfly.io.coutReal)
      fftMem.io.realIn2 := RegNext(butterfly.io.doutReal)

      fftMem.io.imagIn1 := RegNext(butterfly.io.coutImg)
      fftMem.io.imagIn2 := RegNext(butterfly.io.doutImg)

      when(agu.io.done && writeWrap){
        ifftState := IFFTState.out
        printf(cf"OUTPUT:\n")

      }.elsewhen(writeWrap){
        agu.io.advance := true.B
        ifftState := IFFTState.calculateStage
      }
    }

    is(IFFTState.out){
      startTiming := true.B
      fftMem.io.enable := true.B
      fftMem.io.read := true.B
      when(outputFinalCounter > 0.U){
        io.out.valid := true.B
        printf(cf"${io.out.bits(0).asSInt} + ${io.out.bits(1).asSInt}i\n")
      }
      startOutput := true.B
      fftMem.io.addr1 := outputFinalCounter
      io.out.bits(1) := fftMem.io.realOut1 >> log2Ceil(points)
      io.out.bits(0) := fftMem.io.imagOut1 >> log2Ceil(points)
      when(finished){
        printf(cf"CLOCKS: ${timing}\n")
        ifftState := IFFTState.idle
      }
    }

  }
}
