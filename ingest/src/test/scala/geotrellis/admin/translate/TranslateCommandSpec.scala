package geotrellis.admin.translate

import java.io.File

import org.scalatest.{Matchers, FlatSpec}

class TranslateCommandSpec extends FlatSpec with Matchers {

  val mockPath = "/test/command.json/all-set."
  val mockCommand = new TranslateCommand[ARGToGeoTiffConfig] {
    val name = ""
    val config = ARGToGeoTiffConfig()
    val description = ""
    def translate(file: File, args: ARGToGeoTiffConfig): Unit = ()
  }

  def testFile(name: String = ""): Seq[File] =
    Seq(new File(getClass.getResource(s"/translate/files/$name").getFile))

  "changeExtension" should "change correctly it" in  {
    val argPath = mockPath + "arg"
    val jsonPath = mockPath + "json"
    mockCommand.changeExtension(jsonPath, "json", "arg") should be (argPath)
    mockCommand.changeExtension(jsonPath, "json", "json") should be (jsonPath)
    mockCommand.changeExtension(argPath, "arg", "json") should be (jsonPath)
  }

  it should "throw IllegalStateException if no corresponding extension is found" in {
    a [IllegalStateException] should be thrownBy {
      mockCommand.changeExtension(mockPath, ".mock", "")
    }
  }

  "flattenFiles" should "list all files in directory" in {
    val ignoredExt = Seq.empty[String]
    val file = testFile("fileA.txt")
    val folder = testFile()
    println(mockCommand.flattenFiles(file, ignoredExt))
    mockCommand.flattenFiles(file, ignoredExt).length should be (1)
    mockCommand.flattenFiles(folder, ignoredExt).length should be (4)
  }

  it should "ignore specified file" in {
    val ignoredExt = Seq("json")
    val folder = testFile()
    mockCommand.flattenFiles(folder, ignoredExt).length should be (3)
  }

}