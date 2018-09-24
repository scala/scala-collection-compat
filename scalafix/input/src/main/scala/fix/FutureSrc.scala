/*
rule = "Collection213CrossCompat"
 */
package fix

import scala.concurrent.{Future, ExecutionContext, future}
import java.lang.Throwable

class FutureSrc(fs: Future[Int])(implicit ec: ExecutionContext){
  class E1 extends Throwable
  case class E2(v: Int) extends Throwable
  case object E3 extends Throwable

  fs.onFailure {
    case _: E1 => println("type pattern")
    case E2(_) => println("constructor pattern")
    case E3    => println("singleton pattern")
  }

  fs.onSuccess {
    case x if x > 0 => println("x > 0")
    case x if x < 0 => println("x < 0")
  }(ec)

  fs.map(identity).onFailure {
    case x => x
  }
  fs.map(identity).onSuccess { case x => x }

  val f = future { 1 }
}
