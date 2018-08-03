import org.jsoup.Jsoup
import com.github.nscala_time.time.Imports._

import java.nio.file._
import java.net.URL

// Fetch the latest Scala 2.13 by scraping the artifactory web directory
// We just search the latest version by date
// run the command: latest-213 in sbt
// NB. maven-metadata.xml does not point to the latest version
object LatestScala {

  def printLatestScala213(): Unit = {
    val url =
      "https://scala-ci.typesafe.com/artifactory/scala-integration/org/scala-lang/scala-library/"
    val index = new URL(url).openStream()
    val html  = scala.io.Source.fromInputStream(index).mkString
    index.close

    val doc                = Jsoup.parse(html, url)
    val pre                = doc.select("pre").get(1).text
    val versionsAndDateRaw = pre.split("\n").drop(1).dropRight(1)
    val dateFormat         = DateTimeFormat.forPattern("dd-MMM-yyyy HH:mm")
    val versionsAndDate =
      versionsAndDateRaw.map { line =>
        val Array(version, dateRaw) = line.split("/")
        val dateClean               = dateRaw.dropRight(1).trim
        val date                    = DateTime.parse(dateClean, dateFormat)
        (version, date)
      }

    def Descending[T: Ordering] = implicitly[Ordering[T]].reverse

    val (version, date) = versionsAndDate.sortBy(_._2)(Descending).head
    val latestVersion   = version
    val lastestDate     = dateFormat.print(date)

    println()
    println(latestVersion)
    println()
    println(lastestDate)
    println()
  }
}
