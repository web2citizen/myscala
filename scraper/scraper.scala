import java.io.StringReader
import scala.io.Source
import scala.xml.Text
import scala.xml.Node
import scala.xml.parsing.NoBindingFactoryAdapter
import nu.validator.htmlparser.sax.HtmlParser
import nu.validator.htmlparser.common.XmlViolationPolicy
import org.xml.sax.InputSource

// htmlをパースしてxmlノードとして返す
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
val table = node \\ "table" filter (_ \ "@class" contains Text("yfnc_datamodoutline1"))
val row = table \ "tbody" \ "tr" \ "td" \ "table" \ "tbody" \ "tr"

row.foreach({r =>
  val td = r \ "td" filter (_ \ "@class" contains Text("yfnc_tabledata1"))
  
  if(td.size == 7){
    td.foreach(d => print(d.text.replace(",", "")+" "))
    println("")
  }
})



