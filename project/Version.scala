object Version {
  // `(#.+)?` allows republishing for a new Scala version
  // `|x` allows the sbt 1.7 style ".x" versions
  private val versionRegex0 = "v?([0-9]+)\\.([0-9]+)\\.([0-9]+|x)(?:#.+)?".r
  private val versionRegex1 = "v?([0-9]+)\\.([0-9]+)\\.([0-9]+|x)-(.+)(?:#.+)?".r
  private val versionRegex2 = "([0-9]+)\\.([0-9]+|x)(?:#.+)?".r
  def parse(raw: String): Option[String] = {
    raw match {
      case versionRegex0(major, minor, _) =>
        Some(s"${major.toInt}${minor.toInt}")
      case versionRegex1(major, minor, _, _) =>
        Some(s"${major.toInt}${minor.toInt}")
      case versionRegex2(major, "x") =>
        Some(s"${major.toInt}")
      case versionRegex2(major, minor) =>
        Some(s"${major.toInt}${minor.toInt}")
      case _ =>
        None
    }
  }
}
