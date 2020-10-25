package io.github.manuzhang.sbg

import org.rogach.scallop.ScallopConf

class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
  val input = opt[String](required = true)
  val output = opt[String](default = Some((os.pwd / "output").toString()), required = false)
  verify()
}

object Main extends App {

  val conf = new Conf(args)
  val generator = new HtmlGenerator
  generator.genHtml(conf.input(), conf.output())
}
