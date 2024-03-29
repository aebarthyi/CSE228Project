package DSP228

import chisel3._
import chisel3.experimental.FixedPoint
import chisel3.util._

object FilterState extends ChiselEnum {
    val idle, streaming = Value
}

class FilterIO(width: Int) extends Bundle {
    val in = Flipped(Decoupled(Vec(2, FixedPoint(width.W, (width/2).BP))))
    val out = Decoupled(Vec(2, FixedPoint(width.W, (width/2).BP)))
}

// Description: Filter Module. Filters out undesired frequencies from the FFT output
// Works by creating a frequency domain response for a desired filter, using a complex multiplier
// to compute the product of this response with the frequency response of the input signal 
// (output of the FFT). The product is streamed out from the filter module and the input signal 
// is streamed in. N point filtering takes N cycles.
// Currently supported hardcoded filters: Low-pass, high-pass, band-pass and band-stop
// Instructions on how to use various filter modes shown below.
// Can parameterize number of points and bit width according to the original signal.

class Filter(points: Int, width: Int) extends Module {
    //fftModel from Rosetta code: https://rosettacode.org/wiki/Fast_Fourier_transform#Scala
    import scala.math.{ Pi, cos, sin, cosh, sinh, abs }
    case class Complex(re: Double, im: Double) {
        def +(x: Complex): Complex = Complex(re + x.re, im + x.im)
        def -(x: Complex): Complex = Complex(re - x.re, im - x.im)
        def *(x: Double):  Complex = Complex(re * x, im * x)
        def *(x: Complex): Complex = Complex(re * x.re - im * x.im, re * x.im + im * x.re)
        def /(x: Double):  Complex = Complex(re / x, im / x)
        override def toString(): String = {
        val a = "%1.3f" format re
        val b = "%1.3f" format abs(im)
        (a,b) match {
            case (_, "0.000") => a + " + 0.000i\n"
            case ("0.000", _) => b + "i\n"
            case (_, _) if im > 0 => a + " + " + b + "i\n"
            case (_, _) => a + " - " + b + "i\n"
        }
        }
    }
    def exp(c: Complex) : Complex = {
        val r = (cosh(c.re) + sinh(c.re))
        Complex(cos(c.im), sin(c.im)) * r
    }
    def _fft(cSeq: Seq[Complex], direction: Complex, scalar: Int): Seq[Complex] = {
        if (cSeq.length == 1) {
        return cSeq
        }
        val n = cSeq.length
        assume(n % 2 == 0, "The Cooley-Tukey FFT algorithm only works when the length of the input is even.")

        val evenOddPairs = cSeq.grouped(2).toSeq
        val evens = _fft(evenOddPairs map (_(0)), direction, scalar)
        val odds  = _fft(evenOddPairs map (_(1)), direction, scalar)

        def leftRightPair(k: Int): Tuple2[Complex, Complex] = {
        val base = evens(k) / scalar
        val offset = exp(direction * (Pi * k / n)) * odds(k) / scalar
        (base + offset, base - offset)
        }

        val pairs = (0 until n/2) map leftRightPair
        val left  = pairs map (_._1)
        val right = pairs map (_._2)
        left ++ right
    }
    def  fft(cSeq: Seq[Complex]): Seq[Complex] = _fft(cSeq, Complex(0,  2), 1)

    val io = IO(new FilterIO(width))

    val state_r = RegInit(FilterState.idle)
    val counter = new Counter(points)


    // select one of the bottom example filters as desired
    val filter_fft_in = Seq.tabulate(points)(i => if(i < points/2) {Complex(1, 0)} else {Complex(0, 0)}) // example low-pass filter (hard stop after n/2 points)
    // val filter_fft_in = Seq.tabulate(points)(i => if(i < points/2) {Complex(0, 0)} else {Complex(1, 0)}) // example high-pass filter (hard stop before n/2 points)
    // val filter_fft_in = Seq.tabulate(points)(i => if((i < points/4) || (i > (3*points/4))) {Complex(0, 0)} else {Complex(1, 0)}) // example band-pass filter (points n/4 to 3n/4 pass through)
    // val filter_fft_in = Seq.tabulate(points)(i => if((i < points/4) || (i > (3*points/4))) {Complex(1, 0)} else {Complex(0, 0)}) // example band-stop filter (points n/4 to 3n/4 are hard-stopped through)
    
    val filter_fft_out_real = Seq.tabulate(points)(i => (fft(filter_fft_in)(i).re).F(width.W, (width/2).BP))
    val filter_fft_out_imag = Seq.tabulate(points)(i => (fft(filter_fft_in)(i).im).F(width.W, (width/2).BP))
    
    val filter_weights_real : Vec[FixedPoint] = VecInit(filter_fft_out_real)
    val filter_weights_imag : Vec[FixedPoint] = VecInit(filter_fft_out_imag)
    val complexMult = Module(new ComplexMul(width))
    io.in.ready := false.B
    io.out.valid := false.B

    complexMult.io.aReal := io.in.bits(0)
    complexMult.io.aImg := io.in.bits(1)
    complexMult.io.bReal := filter_weights_real(counter.value)
    complexMult.io.bImg := filter_weights_imag(counter.value)
    io.out.bits(0) := complexMult.io.realOut
    io.out.bits(1) := complexMult.io.imgOut

    switch(state_r) {
        is(FilterState.idle) {
            io.in.ready := true.B
            when (io.in.fire) {
                state_r := FilterState.streaming

                io.out.valid := true.B
                printf("OUTPUT: \n")
                printf(cf"${io.out.bits(0).asSInt} + ${io.out.bits(1).asSInt}i\n")
                counter.inc()
            }
        }

        is(FilterState.streaming) {
            io.in.ready := false.B
            io.out.valid := true.B
            printf(cf"${io.out.bits(0).asSInt} + ${io.out.bits(1).asSInt}i\n")
            when(counter.inc()) {
                state_r := FilterState.idle
            }
        }
    }
}