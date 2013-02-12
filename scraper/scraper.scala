import scala.io.Source
import java.io.StringReader
import java.io.File
import java.util.Date
import scala.xml.XML
import scala.xml.Text
import scala.xml.Node
import scala.xml.parsing.NoBindingFactoryAdapter
import nu.validator.htmlparser.sax.HtmlParser
import nu.validator.htmlparser.common.XmlViolationPolicy
import org.xml.sax.InputSource
//import java.sql.{DriverManager, Connection, Statement, ResultSet,SQLException}

// htmlをパースしてxmlノードとして返す
def toNode(str: String): Node = {
  val hp = new HtmlParser
  hp.setNamePolicy(XmlViolationPolicy.ALLOW)

  val saxer = new NoBindingFactoryAdapter
  hp.setContentHandler(saxer)
  hp.parse(new InputSource(new StringReader(str)))

  saxer.rootElem
}

def getStockData(stock: String, position: Int) = {
  println("http://finance.yahoo.com/q/hp?s="+stock+"+Historical+Prices&z="+position+66+"&y="+position)
  val body = Source.fromURL("http://finance.yahoo.com/q/hp?s="+stock+"+Historical+Prices&z="+position+66+"&y="+position).mkString
  val node = toNode(body)
  val table = node \\ "table" filter (_ \ "@class" contains Text("yfnc_datamodoutline1"))
  val row = table \ "tbody" \ "tr" \ "td" \ "table" \ "tbody" \ "tr"
  row.foreach({r =>
    val td = r \ "td" filter (_ \ "@class" contains Text("yfnc_tabledata1"))
    
    if (td.size == 7){
      td.foreach({d => 
	    if (d == td[0])
		  //"%td %<tb, %<tY" format(new Date )
		else
	      print(d.text.replace(",", "")+" ")
		
      })
      println("")
    }
  })
  Thread.sleep(1000)  
}



val config = XML.loadFile(new File("scraper/config.xml"))
val stocks = config \ "universe" \ "stock"
val days = config \ "days" head

val limitDay = days.text.toInt
println(limitDay)

stocks.foreach({stock =>
  for (i <- 0 to limitDay)
  if (i % 66 == 0 ) getStockData(stock.text, i)
})






//Class.forName("com.mysql.jdbc.Driver").newInstance();
//var con = DriverManager.getConnection("jdbc:mysql://localhost/test?user=scott&password=tiger");
