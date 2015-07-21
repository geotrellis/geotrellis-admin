package geotrellis.admin.translate

import java.io.File

import com.quantifind.sumac.{ArgMain, FieldArgs}

trait TranslateArgs extends FieldArgs {

  /** Files and folders arguments to be translated */
  var files: Seq[File] = Seq.empty

  /** Ignored extensions in files. */
  var ignored: Seq[String] = Seq.empty

}

/** Command that translates from one format into another. */
trait TranslateCommand[A <: TranslateArgs] extends ArgMain[A] {

  val help: String

  /** Run the command on given arguments, see help for instructions. */
  def main(args: A): Unit = {
    if (args.files.isEmpty) println(help)
    else {
      for (file <- flattenFiles(args.files, args.ignored)) {
        try translate(file, args)
        catch {
          case exception: Throwable =>
            println(s"Error: ${exception.getMessage}.")
        }
      }
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
