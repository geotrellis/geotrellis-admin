package sandbox.circuit

import diode._
import diode.RootModelRW
import diode.data._
import diode.util._
import diode.react.ReactConnector

import utest._

// TODO write more tests
object DirectoryTreeHandlerTests extends TestSuite {
  def tests = TestSuite {
    // test data

    def build = new LayerHandler(new RootModelRW(LayerModel()))

    'InitLayers - {
      val handler = build
      val result = handler.handleAction(LayerModel(), InitLayers)
    }
  }
}
