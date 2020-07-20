package io.github.manuzhang.publish

import java.text.SimpleDateFormat
import java.util.Collection

import com.vladsch.flexmark.ext.yaml.front.matter.{YamlFrontMatterBlock, YamlFrontMatterExtension, YamlFrontMatterNode}
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet
import com.vladsch.flexmark.util.misc.Extension
import os.{Path, RelPath}
import scalatags.Text.all._

import scala.jdk.CollectionConverters._

class HtmlGenerator {

  def genHtml(): Unit = {
    val index = html(
      header(),
      body(
        grid(
          sidebar(),
          posts(os.home / "git" / "manuzhang.github.io" / "_posts"),
          footer()
        )
      )
    )

    os.write.over(os.pwd / "index.html", index.render)
  }

  def genPost(content: String, path: Path): Unit = {
    val index = html(
      header(),
      body(
        grid(
          sidebar(),
          div(cls := "content pure-u-1 pure-u-md-3-4", content),
          footer()
        )
      )
    )

    os.write.over(path, index.render)
  }

  def grid(tags: Tag*): Tag = {
    div(
      id := "layout",
      cls := "pure-g"
    )(frag(tags))
  }

  def header(): Tag = {
    head(
      meta(charset := "utf-8"),
      meta(name := "viewport", content := "width=device-width, initial-scale=1.0"),
      meta(name := "description", content := "A layout example that shows off a blog page with a list of posts."),
      // title("Blog – Layout Examples – Pure"),
      link(rel := "stylesheet", href := "https://unpkg.com/purecss@2.0.3/build/pure-min.css"),
      link(rel := "stylesheet", href := "https://unpkg.com/purecss@2.0.3/build/grids-responsive-min.css"),
      link(rel := "stylesheet", href := "style.css")
    )
  }

  def sidebar(): Tag = {
    div(id := "layout", cls := "pure-g",
      div(cls := "sidebar pure-u-1 pure-u-md-1-4",
        div(cls := "header",
          h1(cls := "brand-title","A Sample Blog"),
          h2(cls := "brand-tagline","Creating a blog layout using Pure"),
          tag("nav")(cls := "nav",
            ul(cls := "nav-list",
              li(cls := "nav-item",
                a(cls := "pure-button", href := "http://purecss.io","Pure")
              ),
              li(cls := "nav-item",
                a(cls := "pure-button", href := "http://yuilibrary.com","YUI Library")
              )
            )
          )
        )
      )
    )
  }

  def posts(path: Path): Tag = {
    val regex = "^(\\d{4})\\-(0[1-9]|1[012])\\-(0[1-9]|[12][0-9]|3[01])\\-(.*)[.]md".r
    val options = new MutableDataSet()
      .set(Parser.EXTENSIONS,
        List(YamlFrontMatterExtension.create()).asJava.asInstanceOf[Collection[Extension]])
      .toImmutable()
    val parser = Parser.builder(options).build()
    val renderer = HtmlRenderer.builder().build()

    val posts = os.pwd / "posts"
    if (!os.exists(posts)) {
      os.makeDir(posts)
    }

    val toc = os.list(path).flatMap { post =>
      post.last match {
        case regex(year, month, day, shortTitle) => {
          val node = parser.parse(os.read(post))
          val dateStr = s"$year-$month-$day"
          val dateFormat = new SimpleDateFormat("yyyy-mm-dd")
          val date = dateFormat.parse(dateStr)
          val fileName = s"$dateStr-$shortTitle"
          val subDir = posts / fileName
          if (!os.exists(subDir)) {
            os.makeDir(subDir)
          }

          val postFile = s"posts/$fileName/index.html"
          val content = renderer.render(node)
          genPost(content, os.pwd / RelPath(postFile))

          node.getChildren.asScala.headOption match {
            case Some(f: YamlFrontMatterBlock) =>
              f.getChildren.iterator().asScala.flatMap {
                case child: YamlFrontMatterNode if child.getKey == "title" =>
                  child.getValues.asScala.headOption.map(
                    title => date -> a(href := postFile, title))
                case _ => None
              }
            case _ => None
          }
        }
        case _ => None
      }
    }.sortBy(_._1).reverse.map { case (date, post) =>
      li(cls := "pure-menu-item", p(s"$date >> ", post))
    }
    div(cls := "content pure-u-1 pure-u-md-3-4",
      ul(
        cls := "pure-menu-list",
        toc
      )
    )
  }

  def footer(): Tag = {
    div()
  }
}
