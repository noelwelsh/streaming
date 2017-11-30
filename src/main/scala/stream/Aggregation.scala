package stream

import scala.concurrent.duration._

object Aggregation {
  val input: Stream[Event] = ...

  val events: Stream[Event] = input.filter(_ == key)
  val latest: Stream[Timestamp] =
    input.scan(Time.zero)((latest, event) =>
      latest.max(event.timestamp))

  val window1: Stream[Seq[Event]] =
    events.slidingWindow(latest)((latest, event) =>
      (latest - event.timestamp) < 1.minutes)

  val window2: Stream[Seq[Event]] =
    events.slidingWindow(latest)((latest, event) =>
      (latest - event.timestamp) < 5.minutes)

  (window1 zip window2).foldLeft(???)((accum, windows) => ???)


  def merge(left: Stream[A], right: Stream[B]): Stream[Either[A,B]] = ???

  def slidingWindow(events: Stream[Event], latest: Stream[Timestamp], width: Duration): Stream[Seq[Event]] = {
    val joined: Stream[(Event, Timestamp)] = events.join(latest)
    joined.scanLeft(Seq.empty){ (window, joined) =>
      val (event: Event, ts: Timestamp) = joined
      // Todo: remove duplicates
      (event +: joined).filter(event => (ts - event.timestamp) < width)
    }

  }
}
