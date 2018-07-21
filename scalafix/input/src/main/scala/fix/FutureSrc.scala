/*
rule = "scala:fix.CrossCompat"
 */
package fix

import scala.concurrent.{Future, ExecutionContext}
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
}
