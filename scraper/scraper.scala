import scala.io.Source
import java.io.StringReader
import java.io.File
import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat
import scala.xml.XML
import scala.xml.Text
import scala.xml.Node
import scala.xml.parsing.NoBindingFactoryAdapter
import nu.validator.htmlparser.sax.HtmlParser
import nu.validator.htmlparser.common.XmlViolationPolicy
import org.xml.sax.InputSource
import java.sql.DriverManager

// htmlをパースしてxmlノードとして返す
def toNode(str: String): Node = {
  val hp = new HtmlParser
  hp.setNamePolicy(XmlViolationPolicy.ALLOW)

  val saxer = new NoBindingFactoryAdapter
  hp.setContentHandler(saxer)
  hp.parse(new InputSource(new StringReader(str)))

  saxer.rootElem
}

// 日付の差を取得
def diffDays(date1: Date, date2: Date) : Int = {
  val oneDateTime:Long = 1000 * 60 * 60 * 24
  return ((date1.getTime() - date2.getTime()) / oneDateTime).asInstanceOf[Int];
}

// yahooファイナンスのページからデータを取得
def getStockData(stock: String, position: Int) : String = {
  println("http://finance.yahoo.com/q/hp?s="+stock+"+Historical+Prices&z="+position+66+"&y="+position)
  val body = Source.fromURL("http://finance.yahoo.com/q/hp?s="+stock+"+Historical+Prices&z="+position+66+"&y="+position).mkString
  val node = toNode(body)
  val table = node \\ "table" filter (_ \ "@class" contains Text("yfnc_datamodoutline1"))
  val row = table \ "tbody" \ "tr" \ "td" \ "table" \ "tbody" \ "tr"
  
  var sql = "INSERT INTO stock_daily(code, date, open, high, low, close, volume, adj_close) VALUES "
  row.foreach({r =>
    val td = r \ "td" filter (_ \ "@class" contains Text("yfnc_tabledata1"))
    if (td.size == 7){
	  sql +="('" + stock + "',"
      td.foreach({d => 
	    if (d == td(0)) {
		  val date = new SimpleDateFormat("MMM d,yyyy", Locale.US).parse(d.text)
		  sql += "'" + "%tY-%<tm-%<td".format(date)+ "',"
		} else {
	      sql += d.text.replace(",", "")+","
		}
      })
	  sql = sql.substring(0, sql.length()-1)
      sql += "),"
    }
  })
  sql = sql.substring(0, sql.length()-1)
  return sql
}

// メインここから
val pageNum = 66 // yahooファイナンスの1ページあたりの日数
val config = XML.loadFile(new File("scraper/config.xml"))
val stocks = config \ "universe" \ "stock"
val startday = config \ "startday" head
val date2 = new SimpleDateFormat("yyyy-MM-dd").parse(startday.text)

// DB接続設定
val jdbc = config \ "db" \ "jdbc" head
val host = config \ "db" \ "host" head
val user = config \ "db" \ "user" head
val passwd = config \ "db" \ "passwd" head
val dbname = config \ "db" \ "dbname" head

try {
  Class.forName(jdbc.text).newInstance();
  val conString = "jdbc:mysql://"+host.text+"/"+dbname.text+ 
                  "?user="+user.text+"&password="+passwd.text;
  println(conString)
  var con = DriverManager.getConnection(conString);
  
  val limitDay = diffDays(new Date, date2)
  println("過去"+limitDay+"日分のデータを取得します。")

  // 事前データを削除
  val sql = "DELETE FROM stock_daily;"
  var stmt = con.createStatement();
  stmt.executeUpdate(sql)
  stmt.close()
  // 銘柄毎に指定日からのデータを取得
  stocks.foreach({stock =>
    for (i <- 0 to limitDay) {
      // ページ先頭の日になったら取得
      if (i % pageNum == 0 ) {
        val sql2 = getStockData(stock.text, i)
		//println(sql)
        stmt = con.createStatement();
        stmt.executeUpdate(sql2)
        stmt.close()
	    Thread.sleep(1000)
      }
	}
  })
  // 修正株価を適用
  val sql3 = "update stock_daily set adj_open=(truncate(open * adj_close /close + 0.005, 2)), adj_high=(truncate(high * adj_close /close + 0.005, 2)), adj_low=(truncate(low * adj_close /close + 0.005, 2));"
  stmt = con.createStatement();
  stmt.executeUpdate(sql3)
  stmt.close()
  con.close();
} catch {
  case e => e.printStackTrace()
}



