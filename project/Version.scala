case class Version(major: Int, minor: Int, patch: Int) {
  def binary: String            = s"${major}${minor}"
  override def toString: String = s"${major}.${minor}.${patch}"
}

object Version {
  private val versionRegex0 = "v?([0-9]+)\\.([0-9]+)\\.([0-9]+)".r
  private val versionRegex1 = "v?([0-9]+)\\.([0-9]+)\\.([0-9]+)-(.+)".r
  private val versionRegex2 = "([0-9]+)\\.([0-9]+)".r
  private val versionRegex3 = "([0-9]+)".r
  def parse(raw: String): Option[Version] = {
    raw match {
      case versionRegex0(major, minor, patch) =>
        Some(Version(major.toInt, minor.toInt, patch.toInt))
      case versionRegex1(major, minor, patch, _) =>
        Some(Version(major.toInt, minor.toInt, patch.toInt))
      case versionRegex2(major, minor) =>
        Some(Version(major.toInt, minor.toInt, 0))
      case versionRegex3(major) =>
        Some(Version(major.toInt, 0, 0))
      case _ =>
        None
    }
  }
}
