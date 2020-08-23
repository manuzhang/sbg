package io.github.manuzhang.publish

import org.rogach.scallop.ScallopConf

class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
  val input = opt[String](required = true)
  verify()
}

object Main extends App {

  val conf = new Conf(args)
  val generator = new HtmlGenerator
  generator.genHtml(conf.input())
}
