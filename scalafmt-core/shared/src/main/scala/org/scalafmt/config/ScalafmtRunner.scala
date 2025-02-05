package org.scalafmt.config

import metaconfig._
import scala.meta.Tree
import scala.meta.parsers.Parsed

/** A FormatRunner configures how formatting should behave.
  *
  * @param debug
  *   Should we collect debugging statistics?
  * @param eventCallback
  *   Listen to events that happens while formatting
  * @param parser
  *   Are we formatting a scala.meta.{Source,Stat,Case,...}? For more details,
  *   see members of [[scala.meta.parsers]].
  */
case class ScalafmtRunner(
    debug: Boolean = false,
    private val eventCallback: FormatEvent => Unit = null,
    parser: ScalafmtParser = ScalafmtParser.Source,
    optimizer: ScalafmtOptimizer = ScalafmtOptimizer.default,
    maxStateVisits: Int = 1000000,
    dialect: NamedDialect = NamedDialect.default,
    ignoreWarnings: Boolean = false,
    fatalWarnings: Boolean = false
) {
  @inline def getDialect = dialect.dialect

  @inline def topLevelDialect = dialect.copy(
    dialect = getDialect
      .withAllowToplevelTerms(true)
      .withToplevelSeparator("")
  )

  def forSbt: ScalafmtRunner = copy(dialect = topLevelDialect)

  def event(evt: => FormatEvent): Unit =
    if (null != eventCallback) eventCallback(evt)

  def events(evts: => Iterator[FormatEvent]): Unit =
    if (null != eventCallback) evts.foreach(eventCallback)

  def parse(input: meta.inputs.Input): Parsed[_ <: Tree] =
    parser.parse(input, getDialect)

  @inline def isDefaultDialect = dialect.name == NamedDialect.defaultName

}

object ScalafmtRunner {
  implicit lazy val surface: generic.Surface[ScalafmtRunner] =
    generic.deriveSurface
  implicit lazy val formatEventEncoder: ConfEncoder[FormatEvent => Unit] =
    ConfEncoder.StringEncoder.contramap(_ => "<FormatEvent => Unit>")

  /** The default runner formats a compilation unit and listens to no events.
    */
  val default = ScalafmtRunner(
    debug = false,
    parser = ScalafmtParser.Source,
    optimizer = ScalafmtOptimizer.default,
    maxStateVisits = 1000000
  )

  val sbt = default.forSbt

  implicit val codec: ConfCodecEx[ScalafmtRunner] =
    generic.deriveCodecEx(default).noTypos

}
