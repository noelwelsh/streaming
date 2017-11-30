package stream
package exec

sealed trait Stream[A] {
  def next: Result[A]
}
object Stream {
  final case class Map[A,B](source: Stream[A], f: A => B) extends Stream[B]
  final case class Zip[A,B](left: Stream[A], right: Stream[B]) extends Stream[(A,B)] {

    var initialized: Boolean = false
    var emitted: Boolean = false
    var lastLeft: Result[A] = _
    var lastRight: Result[A] = _

    def initialize(): Unit = {
      if(!initialized) {
        lastLeft = left.next
        lastRight = right.next
      }
      initialized = true
    }

    def next: Result[(A,B)] = {
      initialize()

      if(emitted) {
        lastLeft = left.next
        lastRight = right.next
      } else {
        lastLeft match {
          case Emit(_) => ()
          case Await => lastLeft = left.next
          case Completed => ()
        }
        lastRight match {
          case Emit(_) => ()
          case Await => lastRight = right.next
          case Completed => ()
        }
      }

      lastLeft match {
        case Emit(v1) =>
          lastRight match {
            case Emit(v2) =>
              emitted = true
              Result.emit((v1, v2))
            case Await =>
              emitted = false
              Result.await
            case Completed =>
              emitted = false
              Result.completed
          }

        case Await =>
          lastRight match {
            case Emit(v2) =>
              emitted = false
              Result.await
            case Await =>
              emitted = false
              Result.await
            case Completed =>
              emitted = false
              Result.completed
          }

        case Completed =>
          emitted = false
          Result.completed
      }
    }
  }
  final case class FromIterator[A](source: Iterator[A]) extends Stream[A]
}
