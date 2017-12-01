package stream
package syntax

import java.time.{Duration, Instant}

/**
  * Add sliding window functionality to [[stream.Stream]]. Use by importing
  * `stream.syntax.window._`
  *
  * This serves more as an example of defining an extension method, and typical
  * packaging of them, than good design. It would be just as easy to add these
  * directly to Stream, and probably less confusing to the end user.
  */
object window {
  implicit class WindowOps[A](stream: Stream[A]){
    val epochStart = Instant.ofEpochMilli(0L)

    def slidingWindow(timestamp: A => Instant)(
      withinWindow: Duration => Boolean): Stream[Seq[A]] = {
      stream.
        scanLeft((epochStart, Seq.empty[A])){ (acc, a) =>
          val (latest, window) = acc
          val ts = timestamp(a)
          val now = if(ts.isAfter(latest)) ts else latest
          val updatedWindow =
            (a +: window).filter{ a =>
              val duration = Duration.between(timestamp(a), now)
              withinWindow(duration)
            }

          (now, updatedWindow)
        }.
        map{ case (_, window) => window }
    }
  }
}
