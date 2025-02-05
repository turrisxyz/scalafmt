package org.scalafmt.dynamic

import java.nio.file.Path

import scala.util.control.NoStackTrace

sealed abstract class ScalafmtDynamicError(
    msg: String,
    cause: Throwable = null
) extends Error(msg, cause)

object ScalafmtDynamicError {
  sealed abstract class ConfigError(
      val configPath: Path,
      msg: String,
      cause: Throwable = null
  ) extends ScalafmtDynamicError(msg, cause)

  class ConfigDoesNotExist(configPath: Path)
      extends ConfigError(configPath, "Missing config")

  class ConfigMissingVersion(configPath: Path)
      extends ConfigError(configPath, "Missing version")

  class ConfigParseError(configPath: Path, why: String)
      extends ConfigError(configPath, s"Invalid config: $why")

  class CannotDownload(
      configPath: Path,
      version: ScalafmtVersion,
      cause: Throwable = null
  ) extends ConfigError(configPath, s"[v$version] failed to download", cause)

  class CorruptedClassPath(
      configPath: Path,
      version: ScalafmtVersion,
      urls: Seq[String],
      cause: Throwable
  ) extends ConfigError(
        configPath,
        urls.mkString(s"[v$version] corrupted class path: [", ",", "]"),
        cause
      )

  class ConfigInvalidVersion(configPath: Path, version: String)
      extends ConfigError(configPath, s"Invalid version: $version")
      with NoStackTrace

  class UnknownConfigError(configPath: Path, cause: Throwable)
      extends ConfigError(configPath, "unknown error", cause)

  case class UnknownError(cause: Throwable)
      extends ScalafmtDynamicError("unknown error", cause)

  class ScalafmtInterfaceMethodDeprecated(method: String)
      extends Error(s"Method Scalafmt.$method is deprecated")

}
