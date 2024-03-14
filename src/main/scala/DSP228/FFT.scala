package DSP228

import chisel3._
import chisel3.experimental.FixedPoint
import chisel3.util._

object FFTState extends ChiselEnum {
  val idle, loadBuffer, loadRam, calculateStage, writeStage, out = Value
}
class FFTIO(points: Int, width: Int) extends Bundle{
  val in = Flipped(Decoupled(Vec(2, FixedPoint(width.W, (width/2).BP))))
  val out = Decoupled(Vec(2, FixedPoint(width.W, (width/2).BP)))
}

class FFT(points: Int, width: Int) extends Module {
  val io = IO(new FFTIO(points, width))
  val fftState = RegInit(FFTState.idle)
  val fftMem = Module(new RAM(points, width))
  val inputBuffer = RegInit(VecInit(Seq.fill(points)(0.F(width.W, (width/2).BP))))
  val outputBufferReal = RegInit(VecInit(Seq.fill(points)(0.F(width.W, (width/2).BP))))
  val outputBufferImag = RegInit(VecInit(Seq.fill(points)(0.F(width.W, (width/2).BP))))
  val butterfly = Module(new ButterflyUnit(width))
  val agu = Module(new AddressGenerationUnit(points))
  val twiddleRom = Module(new TwiddleFactor(points, width))
  val startCounter = Wire(Bool())
  val startOutput = Wire(Bool())
  val startTiming = Wire(Bool())
  val startWrite = Wire(Bool())
  val startRead = Wire(Bool())
  val startWait = Wire(Bool())
  val bubble = Reg(FixedPoint(width.W, (width/2).BP))
  val bubble2 = Reg(FixedPoint(width.W, (width / 2).BP))

  val (bitReversedCounter, wrap) = Counter(0 until points by 2, startCounter)
  val (bitReversedCounter2, wrap2) = Counter(1 until points by 2, startCounter)
  val (writeClock, writeWrap) = Counter(0 until 2, startWrite)
  val (readClock, readWrap) = Counter(0 until 2, startRead)
  val (readWait, waited) = Counter(0 until 4, startWait)
  val (timing, timeUp) = Counter(0 until (points*points)+1, startTiming)
  val (outputFinalCounter, finished) = Counter(0 until (points+1), startOutput)

  startWait := false.B
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

  switch(fftState){
    is(FFTState.idle){
      startTiming := false.B
      io.in.ready := true.B
      fftMem.io.read := true.B

      when(io.in.valid){
        startTiming := true.B
        startCounter := true.B
        inputBuffer(bitReversedCounter) := io.in.bits(0)
        inputBuffer(bitReversedCounter2) := io.in.bits(1)
        fftState := FFTState.loadBuffer
      }
    }

    is(FFTState.loadBuffer){
      startTiming := true.B
      startCounter := true.B
      inputBuffer(bitReversedCounter) := io.in.bits(0)
      inputBuffer(bitReversedCounter2) := io.in.bits(1)
      when(wrap){
        fftState := FFTState.loadRam
      }
    }

    is(FFTState.loadRam){
      startTiming := true.B
      fftMem.io.enable := true.B
      fftMem.io.read := false.B
      startCounter := true.B
      fftMem.io.realIn1 := inputBuffer(Reverse(bitReversedCounter))
      fftMem.io.imagIn1 := 0.F(width.W, (width/2).BP)
      fftMem.io.realIn2 := inputBuffer(Reverse(bitReversedCounter2))
      fftMem.io.imagIn2 := 0.F(width.W, (width/2).BP)
      fftMem.io.addr1 := bitReversedCounter
      fftMem.io.addr2 := bitReversedCounter2
      when(wrap){
        fftState := FFTState.calculateStage
      }
    }

    is(FFTState.calculateStage) {
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
        fftState := FFTState.writeStage
      }
    }

    is(FFTState.writeStage){
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
        fftState := FFTState.out

      }.elsewhen(writeWrap){
        agu.io.advance := true.B
        fftState := FFTState.calculateStage

      }
    }

    is(FFTState.out){
      startTiming := true.B
      startWait := false.B
      fftMem.io.enable := true.B
      fftMem.io.read := true.B
      when(outputFinalCounter > 0.U){
        io.out.valid := true.B
      }
      startOutput := true.B
      fftMem.io.addr1 := outputFinalCounter
      io.out.bits(0) := fftMem.io.realOut1
      io.out.bits(1) := fftMem.io.imagOut1
      printf(cf"OUTPUT: \n${io.out.bits(0).asSInt}|${io.out.bits(1).asSInt}\n")
      when(finished){
        printf(cf"CLOCKS: ${timing.asUInt}\n")
        fftState := FFTState.idle
      }
    }

  }


}
