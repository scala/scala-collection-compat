package scala.util.control.compat

import org.junit.Test

class ControlThrowableTest {
  @Test
  def doesNotSuppress(): Unit = {
    val t = new ControlThrowable {}
    t.addSuppressed(new Exception)
    assert(t.getSuppressed.isEmpty)
  }

  @Test
  def doesNotHaveStackTrace(): Unit = {
    assert(new ControlThrowable {}.getStackTrace.isEmpty)
  }
}
