package DSP228

import chisel3._
import chisel3.experimental.FixedPoint
import chisel3.util._

object FFTState extends ChiselEnum {
  val idle, loadBuffer, loadRam, calculateStage, writeStage, waitForRead, out = Value
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
  val twiddleRom = Module(new TwiddleFactor(points/2, width))
  val startCounter = Wire(Bool())
  val startOutput = Wire(Bool())
  val finalOutput = Wire(Bool())
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
  val (outputCounter, outFin) = Counter(0 until points by 2, startOutput)
  val (outputCounter2, outFin2) = Counter(1 until points by 2, startOutput)
  val (outputFinalCounter, finished) = Counter(0 until points, startOutput)
  startWait := false.B
  startCounter := false.B
  startOutput := false.B
  startWrite := false.B
  startRead := false.B
  finalOutput := false.B
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
      io.in.ready := true.B
      fftMem.io.read := true.B

      when(io.in.valid){
        startCounter := true.B
        inputBuffer(bitReversedCounter) := io.in.bits(0)
        inputBuffer(bitReversedCounter2) := io.in.bits(1)
        fftState := FFTState.loadBuffer
      }
    }

    is(FFTState.loadBuffer){
      startCounter := true.B
      inputBuffer(bitReversedCounter) := io.in.bits(0)
      inputBuffer(bitReversedCounter2) := io.in.bits(1)
      when(wrap){
        fftState := FFTState.loadRam
      }
    }

    is(FFTState.loadRam){
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
      fftMem.io.imagIn1 := RegNext(butterfly.io.doutImg)

      when(readWrap) {
        printf(cf"read stage\n")
        printf(cf"A: ${butterfly.io.aReal.asSInt}|${butterfly.io.aImg.asSInt}\n")
        printf(cf"B: ${butterfly.io.bReal.asSInt}|${butterfly.io.bImg.asSInt}\n")
        printf(cf"ButterflyOutput\n")
        printf(cf"A: ${butterfly.io.coutReal.asSInt}|${butterfly.io.coutImg.asSInt}\n")
        printf(cf"B: ${butterfly.io.doutReal.asSInt}|${butterfly.io.doutImg.asSInt}\n")
        fftState := FFTState.writeStage
      }
    }

    is(FFTState.writeStage){
      printf(cf"write stage\n")
      printf(cf"ButterflyOutput\n")
      printf(cf"A: ${butterfly.io.coutReal.asSInt}|${butterfly.io.coutImg.asSInt}\n")
      printf(cf"B: ${butterfly.io.doutReal.asSInt}|${butterfly.io.doutImg.asSInt}\n")
      printf(cf"Memory input\n")
      printf(cf"A: ${fftMem.io.realIn1.asSInt}|${fftMem.io.imagIn1.asSInt}\n")
      printf(cf"B: ${fftMem.io.realIn2.asSInt}|${fftMem.io.imagIn2.asSInt}\n")
      printf(cf"Twiddle: \n${butterfly.io.twiddleReal.asSInt}|${butterfly.io.twiddleImg.asSInt}\n")

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
      fftMem.io.imagIn1 := RegNext(butterfly.io.doutImg)

      when(agu.io.done){
        fftState := FFTState.out
        fftMem.io.read := false.B
        fftMem.io.addr1 := outputFinalCounter
      }.elsewhen(writeWrap){
        agu.io.advance := true.B
        fftState := FFTState.calculateStage
      }
    }

    is(FFTState.out){
      startWait := false.B
      fftMem.io.enable := true.B
      fftMem.io.read := true.B
      io.out.valid := true.B
      startOutput := true.B
      fftMem.io.addr1 := outputFinalCounter
      io.out.bits(0) := fftMem.io.realOut1
      io.out.bits(1) := fftMem.io.imagOut1
      printf(cf"OUTPUT: \n${io.out.bits(0).asSInt}|${io.out.bits(1).asSInt}\n")
      when(finished){
        fftState := FFTState.idle
      }
    }
  }


}
