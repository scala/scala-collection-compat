


package fix

import scala.concurrent.{Future, ExecutionContext}
import java.lang.Throwable

class FutureSrc(fs: Future[Int])(implicit ec: ExecutionContext){
  class E1 extends Throwable
  case class E2(v: Int) extends Throwable
  case object E3 extends Throwable

  fs.onComplete {
    case scala.util.Failure(_: E1) => println("type pattern")
    case scala.util.Failure(E2(_)) => println("constructor pattern")
    case scala.util.Failure(E3)    => println("singleton pattern")
    case _ => ()
  }

  fs.onComplete {
    case scala.util.Success(x) if x > 0 => println("x > 0")
    case scala.util.Success(x) if x < 0 => println("x < 0")
    case _ => ()
  }(ec)

  fs.map(identity).onComplete {
    case scala.util.Failure(x) => x
    case _ => ()
  }
  fs.map(identity).onComplete { case scala.util.Success(x) => x
                               case _ => () }

  val f = Future { 1 }
}
