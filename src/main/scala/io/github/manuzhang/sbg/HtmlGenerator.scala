package io.github.manuzhang.sbg

import java.time.format.DateTimeFormatter
import java.util.Collection

import com.typesafe.scalalogging.Logger
import com.vladsch.flexmark.ext.yaml.front.matter.{YamlFrontMatterBlock, YamlFrontMatterExtension, YamlFrontMatterNode}
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet
import com.vladsch.flexmark.util.misc.Extension
import io.github.manuzhang.sbg.HtmlGenerator.logger
import os.{Path, RelPath}
import scalatags.Text.TypedTag
import scalatags.Text.all._

import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters._

object HtmlGenerator {
  val logger = Logger(classOf[HtmlGenerator])
}

class HtmlGenerator {
  val title = "ManuZhang's Blog"
  val description = "Static Blog generated in Scala"
  val siteUrl = "https://manuzhang.github.io"
  val rssFile = "rss/index.xml"

  def genHtml(input: String, output: String): Unit = {
    val outputPath = Path(output)
    val postsPath = "posts"
    val postList = genPosts(Path(input), postsPath)

    postList.foreach { post =>
      val header = div(h1(post.title), p(post.date, a(cls := "home", href := "../../", "Home")))
      logger.info(s"Generating ${post.path}")
      genPost(s"${header}${post.content}", outputPath / postsPath / RelPath(post.path))
    }

    val toc = ul(
      postList.map { post =>
        li(
          cls := "post-item",
          p(span(cls := "post-meta", s"${post.date} >> "), post.link))
      }.toList
    )

    os.write.over(Path(output) / RelPath(rssFile), rssXml(postList), createFolders = true)

    val themePath = "themes"
    os.write.over(Path(output) / "index.html",
      page(themePath, toc))

    os.copy(os.pwd / themePath, outputPath / themePath, replaceExisting = true)
  }

  def page(themePath: String, content: Tag): String = {
    html(
      header(themePath),
      body(
        div(id := "layout", cls := "pure-g",
          div(cls := "sidebar pure-u-1 pure-u-md-1-4", sidebar()),
          div(cls := "content pure-u-1 pure-u-md-3-4",
            content,
            div(cls := "footer pure-u-1", footer())
          )
        )
      )
    ).render
  }

  def genPost(content: String, path: Path): Unit = {
    val placeholder = "placeholder"

    os.write.over(path,
      page("../../themes", div(placeholder)).replace(placeholder, content),
      createFolders = true)
  }

  def header(cssPath: String): Tag = {
    head(
      meta(charset := "utf-8"),
      meta(name := "viewport", content := "width=device-width, initial-scale=1.0"),
      meta(name := "description", content := description),
      tag("title")(title),
      link(rel := "shortcut icon", href := "favicon.png"),
      link(rel := "stylesheet", href := "https://unpkg.com/purecss@2.0.3/build/pure-min.css"),
      link(rel := "stylesheet", href := "https://unpkg.com/purecss@2.0.3/build/grids-responsive-min.css"),
      link(rel := "stylesheet", href := s"$cssPath/styles.css"),
      link(rel := "stylesheet", href := s"$cssPath/prism.css"),
      script(src := s"$cssPath/prism.js")
    )
  }

  def sidebar(): Tag = {
    div(cls := "header",
      h1(cls := "brand-title", title),
      h2(cls := "brand-tagline", description),
      tag("nav")(cls := "nav",
        ul(cls := "nav-list",
          li(cls := "nav-item",
            a(cls := "pure-button", href := "https://github.com/manuzhang", "GitHub")
          ),
          li(cls := "nav-item",
            a(cls := "pure-button", href := "https://twitter.com/manuzhang", "Twitter")
          ),
          li(cls := "nav-item",
            a(cls := "pure-button", href := rssFile, "RSS")
          )
        )
      )
    )
  }

  def genPosts(input: Path, output: String): List[Post] = {
    val parserOptions = new MutableDataSet()
      .set(Parser.EXTENSIONS,
        List(YamlFrontMatterExtension.create()).asJava.asInstanceOf[Collection[Extension]])
      .toImmutable()
    val parser = Parser.builder(parserOptions).build()
    val rendererOptions = new MutableDataSet()
      .set(HtmlRenderer.FENCED_CODE_NO_LANGUAGE_CLASS, "language-text")
      .toImmutable
    val renderer = HtmlRenderer.builder(rendererOptions).build()

    val postBuffer = ArrayBuffer.empty[Post]
    val regex = "^(\\d{4})\\-(0[1-9]|1[012])\\-(0[1-9]|[12][0-9]|3[01])\\-(.*)[.]md".r

    def fillPostList(p: Path): Unit = {
      os.list(p).foreach { file =>
        if (file.toIO.isDirectory) {
          fillPostList(file)
        } else {
          file.last match {
            case regex(year, month, day, shortTitle) => {
              val input = os.read(file)
              val node = parser.parse(input)
              val dateStr = s"$year-$month-$day"
              val fileName = s"$dateStr-$shortTitle"
              val outputFile = s"$output/$fileName/index.html"
              val content = renderer.render(node)
                .replace("<code>", "<code class='language-text'>")

              var title = shortTitle
              node.getChildren.asScala.headOption.foreach {
                case f: YamlFrontMatterBlock =>
                  f.getChildren.iterator().asScala.foreach {
                    case child: YamlFrontMatterNode if child.getKey == "title" =>
                      child.getValues.asScala.headOption.foreach { t =>
                        title = t
                      }
                    case _ =>
                  }
                case _ =>
              }
              postBuffer += Post(title, content, outputFile, dateStr)
            }

            case _ =>
              logger.warn(s"${file.last} is not a valid file name" )
          }
        }
      }
    }

    fillPostList(input)
    postBuffer.toList.sortBy(_.date).reverse
  }

  def footer(): Tag = {
    span(
      "Generated with ", a(href := "https://github.com/manuzhang/sbg", "SBG."),
      " Written in Scala"
    )
  }

  def rssXml(posts: List[Post]): String = {
    val snippet = tag("rss")(attr("version") := "2.0")(
      tag("channel")(
        tag("title")(title),
        tag("link")(siteUrl),
        tag("description")(description),

        for(p <- posts) yield tag("item")(
          tag("title")(p.title),
          tag("link")(s"$siteUrl/${p.path}"),
          tag("description")(p.content),
          tag("pubDate")(p.rssPubDate),
        )

      )
    )
    """<?xml version="1.0"?>""" + snippet.render
  }

  case class Post(title: String, content: String, path: String, date: String) {

    def link: TypedTag[String] = a(href := path, title)

    def rssPubDate: String = {
      DateTimeFormatter.RFC_1123_DATE_TIME.format(
        DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(s"${date}T00:00:00+00:00"))
    }
  }
}
