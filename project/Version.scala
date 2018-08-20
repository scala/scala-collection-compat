case class Version(major: Int, minor: Int, patch: Int, isPre: Boolean = false) {
  def binary: String = {
    val suffix = if (isPre) "-pre" else ""
    s"${major}${minor}${suffix}"
  }
  override def toString: String = s"${major}.${minor}.${patch}"
}

object Version {
  private val versionRegex0 = "v?([0-9]+)\\.([0-9]+)\\.([0-9]+)".r
  private val versionRegex1 = "v?([0-9]+)\\.([0-9]+)\\.([0-9]+)-M([0-9]+)".r
  private val versionRegex2 = "v?([0-9]+)\\.([0-9]+)\\.([0-9]+)-pre".r
  private val versionRegex3 = "([0-9]+)\\.([0-9]+)".r
  def parse(raw: String): Option[Version] = {
    raw match {
      case versionRegex0(major, minor, patch) =>
        Some(Version(major.toInt, minor.toInt, patch.toInt))
      case versionRegex1(major, minor, patch, _) =>
        Some(Version(major.toInt, minor.toInt, patch.toInt))
      case versionRegex2(major, minor, patch) =>
        Some(Version(major.toInt, minor.toInt, patch.toInt, isPre = true))
      case versionRegex3(major, minor) =>
        Some(Version(major.toInt, minor.toInt, 0))
      case _ =>
        None
    }
  }
}
