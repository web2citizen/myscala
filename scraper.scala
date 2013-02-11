import java.io.StringReader
import scala.io.Source
import scala.xml.Text
import scala.xml.Node
import scala.xml.parsing.NoBindingFactoryAdapter
import nu.validator.htmlparser.sax.HtmlParser
import nu.validator.htmlparser.common.XmlViolationPolicy
import org.xml.sax.InputSource

def toNode(str: String): Node = {
  val hp = new HtmlParser
  hp.setNamePolicy(XmlViolationPolicy.ALLOW)

  val saxer = new NoBindingFactoryAdapter
  hp.setContentHandler(saxer)
  hp.parse(new InputSource(new StringReader(str)))

  saxer.rootElem
}

val body = Source.fromURL("http://finance.yahoo.com/q/hp?s=%5EDJI+Historical+Prices").mkString
val node = toNode(body)
val data = node \\ "td" filter (_ \ "@class" contains Text("yfnc_tabledata1"))

println(data)
