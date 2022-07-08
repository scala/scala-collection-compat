case class Version(major: Int, minor: Int) {
  override def toString = s"${major}${minor}"
}

object Version {
  // `(#.+)?` allows republishing for a new Scala version
  // `|x` allows the sbt 1.7 style ".x" versions
  private val versionRegex0 = "v?([0-9]+)\\.([0-9]+)\\.([0-9]+|x)(?:#.+)?".r
  private val versionRegex1 = "v?([0-9]+)\\.([0-9]+)\\.([0-9]+|x)-(.+)(?:#.+)?".r
  private val versionRegex2 = "([0-9]+)\\.([0-9]+)(?:#.+)?".r
  private val versionRegex3 = "([0-9]+)(?:#.+)?".r
  def parse(raw: String): Option[Version] = {
    raw match {
      case versionRegex0(major, minor, _) =>
        Some(Version(major.toInt, minor.toInt))
      case versionRegex1(major, minor, _, _) =>
        Some(Version(major.toInt, minor.toInt))
      case versionRegex2(major, minor) =>
        Some(Version(major.toInt, minor.toInt))
      case versionRegex3(major) =>
        Some(Version(major.toInt, 0))
      case _ =>
        None
    }
  }
}
