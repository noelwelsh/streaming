package stream

import java.time.{Duration, Instant}

object Aggregation {
  import stream.syntax.window._

  final case class Event(instant: Instant, key: String)

  // Events that occur slightly out of order. B is the most common event at the
  // start of the stream, A at the end.
  val events =
    Seq(
      Event(Instant.ofEpochMilli(100L), "A"),
      Event(Instant.ofEpochMilli(120L), "B"),
      Event(Instant.ofEpochMilli(130L), "A"),
      Event(Instant.ofEpochMilli(140L), "B"),
      Event(Instant.ofEpochMilli(120L), "B"),
      Event(Instant.ofEpochMilli(150L), "B"),
      Event(Instant.ofEpochMilli(160L), "A"),
      Event(Instant.ofEpochMilli(160L), "A"),
      Event(Instant.ofEpochMilli(180L), "A"),
      Event(Instant.ofEpochMilli(200L), "A"),
      Event(Instant.ofEpochMilli(200L), "B"),
      Event(Instant.ofEpochMilli(210L), "A"),
      Event(Instant.ofEpochMilli(220L), "A"),
      Event(Instant.ofEpochMilli(240L), "B"),
      Event(Instant.ofEpochMilli(280L), "A"),
      Event(Instant.ofEpochMilli(240L), "A"),
      Event(Instant.ofEpochMilli(260L), "A"),
      Event(Instant.ofEpochMilli(280L), "A"),
      Event(Instant.ofEpochMilli(290L), "B"),
      Event(Instant.ofEpochMilli(300L), "A")
    )

  val source = Stream.fromSeq(events)

  val windowSize = Duration.ofMillis(50L)
  val windowed = source.slidingWindow(windowSize)(_.instant)

  // Count occurrences and order from lowest to highest
  val occurrences = windowed.map{ window =>
    val keys = window.map(event => event.key).distinct
    val keysCount = keys.map(key => window.count(event => event.key == key))

    (keys.zip(keysCount)).sortBy{ case (key, count) => count }
  }
}
