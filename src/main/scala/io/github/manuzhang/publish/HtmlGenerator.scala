package io.github.manuzhang.publish

import java.util.Collection

import com.vladsch.flexmark.ext.yaml.front.matter.{YamlFrontMatterBlock, YamlFrontMatterExtension, YamlFrontMatterNode}
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet
import com.vladsch.flexmark.util.misc.Extension
import os.{Path, RelPath}
import scalatags.Text.TypedTag
import scalatags.Text.all._

import scala.jdk.CollectionConverters._

class HtmlGenerator {

  def genHtml(): Unit = {
    val index = html(
      header(cssPath = "themes"),
      body(
        grid(
          sidebar(),
          posts(os.pwd / "posts"),
          footer()
        )
      )
    )

    os.write.over(os.pwd / "index.html", index.render)
  }

  def genPost(content: String, path: Path): Unit = {
    val placeholder = "placeholder"
    val index = html(
      header(cssPath = "../../themes"),
      body(
        grid(
          sidebar(),
          div(cls := "content pure-u-1 pure-u-md-3-4", placeholder),
          footer()
        )
      )
    )

    os.write.over(path, index.render.replace(placeholder, content), createFolders = true)
  }

  def grid(tags: Tag*): Tag = {
    div(
      id := "layout",
      cls := "pure-g"
    )(frag(tags))
  }

  def header(cssPath: String): Tag = {
    head(
      meta(charset := "utf-8"),
      meta(name := "viewport", content := "width=device-width, initial-scale=1.0"),
      meta(name := "description", content := "A layout example that shows off a blog page with a list of posts."),
      // title("Blog – Layout Examples – Pure"),
      link(rel := "stylesheet", href := "https://unpkg.com/purecss@2.0.3/build/pure-min.css"),
      link(rel := "stylesheet", href := "https://unpkg.com/purecss@2.0.3/build/grids-responsive-min.css"),
      link(rel := "stylesheet", href := s"$cssPath/styles.css"),
      link(rel := "stylesheet", href := s"$cssPath/prism.css"),
      script(src := s"$cssPath/prism.js")
    )
  }

  def sidebar(): Tag = {
    div(id := "layout", cls := "pure-g",
      div(cls := "sidebar pure-u-1 pure-u-md-1-4",
        div(cls := "header",
          h1(cls := "brand-title", "My Blog"),
          h2(cls := "brand-tagline", "Static Blog generated in Scala"),
          tag("nav")(cls := "nav",
            ul(cls := "nav-list",
              li(cls := "nav-item",
                a(cls := "pure-button", href := "https://github.com/manuzhang", "GitHub")
              ),
              li(cls := "nav-item",
                a(cls := "pure-button", href := "https://twitter.com/manuzhang", "Twitter")
              )
            )
          )
        )
      )
    )
  }

  def posts(path: Path): Tag = {
    val regex = "^(\\d{4})\\-(0[1-9]|1[012])\\-(0[1-9]|[12][0-9]|3[01])\\-(.*)[.]md".r
    val parserOptions = new MutableDataSet()
      .set(Parser.EXTENSIONS,
        List(YamlFrontMatterExtension.create()).asJava.asInstanceOf[Collection[Extension]])
      .toImmutable()
    val parser = Parser.builder(parserOptions).build()
    val rendererOptions = new MutableDataSet()
      .set(HtmlRenderer.FENCED_CODE_NO_LANGUAGE_CLASS, "language-text")
      .toImmutable
    val renderer = HtmlRenderer.builder(rendererOptions).build()

    val toc = os.list(path).flatMap { post =>
      post.last match {
        case regex(year, month, day, shortTitle) => {
          val input = os.read(post)
          val node = parser.parse(input)
          val dateStr = s"$year-$month-$day"
          val fileName = s"$dateStr-$shortTitle"
          val output = s"output/$fileName/index.html"
          val content = renderer.render(node)
            .replace("<code>", "<code class='language-text'>")

          node.getChildren.asScala.headOption match {
            case Some(f: YamlFrontMatterBlock) =>
              f.getChildren.iterator().asScala.flatMap {
                case child: YamlFrontMatterNode if child.getKey == "title" =>
                  child.getValues.asScala.headOption.map(Post(_, content, output, dateStr))
                case _ => None
              }
            case _ => None
          }

        }
        case _ => None
      }
    }.sortBy(_.date).reverse.map { post =>
      genPost(s"<h1>${post.title}</h1>${post.content}", os.pwd / RelPath(post.path))
      li(cls := "pure-menu-item", p(span(cls := "post-meta", s"${post.date} >> "), post.link))
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

  case class Post(title: String, content: String, path: String, date: String) {

    def link: TypedTag[String] = a(href := path, title)
  }
}
