import org.junit.{Assert, Test}

import build.BuildInfo._

import com.typesafe.tools.mima.lib.MiMaLib
import com.typesafe.tools.mima.core.Config

class BinaryCompaTest {
  @Test
  def compat(): Unit = {
    Config.setup("foo", Array(oldClasspath, newClasspath))
    val mima = new MiMaLib(Config.baseClassPath)
    val allProblems = mima.collectProblems(oldClasspath, newClasspath)
    Assert.assertTrue(allProblems.isEmpty)
  }
}