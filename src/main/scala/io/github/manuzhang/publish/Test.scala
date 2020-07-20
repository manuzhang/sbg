package io.github.manuzhang.publish

object Test extends App {
//  val regex = "^(\\d{4})\\-(0[1-9]|1[012])\\-(0[1-9]|[12][0-9]|3[01])\\-(.*)[.]md".r
//
//  os.list(os.home / "git" / "manuzhang.github.io" / "_posts").foreach { post =>
//    val fileName = post.last
//    fileName match {
//      case regex(_, _, _, title) => {
//        println(title)
//      }
//      case _ => // ignore
//    }
//  }

  val generator = new HtmlGenerator
  generator.genHtml()
}
