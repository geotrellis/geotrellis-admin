package geotrellis.admin.translate

import java.io.File

import scopt.OptionParser

/** Translation arguments. */
class TranslateConfig(var files: Seq[File] = Seq.empty,
                      var ignoredExtensions: Seq[String] = Seq.empty,
                      var verbose: Boolean = false)

/** Command that translates from one format into another. */
trait TranslateCommand[A <: TranslateConfig] {

  val name: String
  val config: A
  val description: String

  lazy val parser = new OptionParser[Unit](name) {

    head(name)
    note(description)

    help("help") abbr "h" text "prints this usage text"

    opt[Unit]('v', "verbose") foreach { _ =>
      config.verbose = true
    } text "activate verbose mode"

    opt[Seq[String]]("ignored-extensions") abbr "ie" valueName "<extension(s)>" foreach { x =>
      config.ignoredExtensions = x
    } text "ignored file extensions separated by a comma"

    arg[File]("<file>...") minOccurs 1 unbounded() foreach { x =>
      config.files = config.files :+ x
    } text "files and folders to be translated" validate { x =>
      if (x.exists()) success
      else failure(s"file $x does not exist")
    }
  }

  /** Parse arguments and start the command. */
  def main(args: Array[String]): Unit =
    if (parser.parse(args)) run(config)

  /** Run translation on all files, see description for instructions. */
  def run(args: A): Unit =
    for (file <- flattenFiles(args.files, args.ignoredExtensions)) {
      try translate(file, args)
      catch {
        case e: Exception =>
          println(s"Error: ${e.getMessage}")
          if (args.verbose) e.printStackTrace()
      }
    }

  /**
   * Filter given path and return all files including first children.
   *
   * @param files files and/or folders
   * @param ignoredExtensions file extensions to be ignored
   * @return all matching files
   */
  def flattenFiles(files: Seq[File], ignoredExtensions: Seq[String]): Seq[File] =
    files.flatMap { file =>
      if (file.isDirectory)
        file.listFiles().map(subFile => new File(subFile.getAbsolutePath))
      else
        Seq(file)
    } filter { file =>
      val path = file.getPath
      ignoredExtensions.forall(! path.endsWith(_))
    }

  /**
   * Change path extension.
   *
   * @param path path to update
   * @param from old extension (e.g. arg)
   * @param to new extension (e.g. json)
   * @return updated path
   */
  def changeExtension(path: String, from: String, to: String): String = {
    val pos = path.lastIndexOf(from)
    if (pos < 0 || path.length != pos + from.length)
      throw new IllegalStateException(s"extension doesn't correspond to $from in $path")
    path.patch(pos, to, from.length)
  }

  /**
   * Run the conversion on given file.
   *
   * @param file file to translate
   */
  def translate(file: File, args: A): Unit

}