package com.scalakata
package evaluation

import java.nio.file.Path

trait EvaluatorSetup {
  import scala.concurrent.duration._
  import java.nio.file.Paths
  import build.BuildInfo._

  private val prelude =
    """|import com.scalakata._
       |@instrument class Playground {
       |  """.stripMargin

  private val preludeNoInstr =
    """|class Playground {
       |  """.stripMargin

  private def wrap(code: String) = s"$prelude$code}"

  private def wrapNoInstr(code: String) = s"$preludeNoInstr$code}"

  private def shiftRequest(pos: Int) = {
    val posO = pos + prelude.length
    RangePosition(posO, posO, posO)
  }
  def evalUnwrapped(code: String) = {
    evaluator(EvalRequest(code))
  }
  def evalNoInstr(code: String) = {
    evaluator(EvalRequest(wrapNoInstr(code)))
  }
  def eval(code: String) = {
    evaluator(EvalRequest(wrap(code)))
  }
  def eval2(before: String, code: String) = {
    evaluator(EvalRequest(before + System.lineSeparator + wrap(code)))
  }
  def autocomplete(code: String, pos: Int) = {
    presentationCompiler.autocomplete(CompletionRequest(wrap(code), shiftRequest(pos)))
  }
  def typeAt(code: String, pos: Int) = {
    presentationCompiler.typeAt(TypeAtRequest(wrap(code), shiftRequest(pos)))
  }

  private val artifacts = (annotationClasspath ++ modelClasspath).distinct.map(v ⇒ Paths.get(v.toURI))

  private val scalacOptions = build.BuildInfo.scalacOptions.to[Seq]
  private def evaluator = new Evaluator(
    Seq.empty[Path], //artifacts, // This is what causes the ss
    scalacOptions, // Seq("-deprecation", "-unchecked", "-encoding", "utf8"),
    security = false,
    timeout = 30.seconds
  )
  private def presentationCompiler = new PresentationCompiler(artifacts, scalacOptions)
}
