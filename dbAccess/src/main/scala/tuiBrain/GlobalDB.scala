package tuiBrain

import cats.effect.{IO, Resource}
import com.zaxxer.hikari.HikariConfig
import doobie.{Fragment, *}
import doobie.hikari.HikariTransactor
import cats.effect.*
import cats.*
import doobie.implicits.*
import doobie.util.fragment
import io.circe.{Decoder, Encoder, yaml}
import cats.syntax.traverse._
import doobie.WeakAsync._
import java.io.PrintWriter
import io.circe.yaml.Printer
import io.circe.*
import io.circe.syntax.*
import io.circe.generic.auto.*

import cats.effect.unsafe.implicits.global

import java.io.File
import java.nio.file.{Files, Paths}
import scala.io.Source
import scala.io.StdIn.readLine

object GlobalDB{
  private def readYaml[A](yamlPath: String, default: () => A)(using d: Decoder[A])(using e: Encoder[A]): A = {
    val content = {
      val myFile = File(yamlPath)
      myFile.createNewFile()
      val oo = Source.fromFile(myFile)
      val ret = oo.getLines().mkString("\n")
      oo.close()
      ret
    }

    val value = yaml.parser.parse(content).flatMap(_.as[A]).getOrElse(default())

    val printWriter = PrintWriter(yamlPath)
    printWriter.write(Printer().pretty(value.asJson))
    printWriter.close()
    value
  }

  case class mySettings(dbPath: String)

  private val yamlFilePath : String = "data.yaml"
  private val default = () => {
    println("No path to database was found.\nEnter path and database name to start: ")
    val str = readLine()
    val file = new File(str)
    if !file.exists() then Files.createFile(Paths.get(str))
    mySettings(str)
  }

//  private val databasePath = "/home/rev/testDB.db"
  val databasePath : mySettings = readYaml[mySettings](yamlFilePath, default)

  given Resource[IO, HikariTransactor[IO]] = for {
    hikariConfig <- Resource.pure {
      val config = new HikariConfig()
      config.setDriverClassName("org.sqlite.JDBC")
      config.setJdbcUrl(s"jdbc:sqlite:${databasePath.dbPath}")
      config.setUsername("")
      config.setPassword("")
      config
    }
    xa <- HikariTransactor.fromHikariConfig[IO](hikariConfig)
  } yield xa


  def runUpdate(sqlStr: Fragment)(using transactor: Resource[IO, HikariTransactor[IO]]): IO[Int] = transactor.use(sqlStr.update.run.transact(_))
  def runUpdate(sqlStr: List[String])(using transactor: Resource[IO, HikariTransactor[IO]]): IO[List[Int]] =
    transactor.use( xa => sqlStr.traverse(q => Update0(q, None).run.transact(xa)) )

  def tableINE(name : String) : Fragment = fr"CREATE TABLE IF NOT EXISTS" ++ Fragment.const(name) ++ fr"("
  def select(cols : String, table : String) : Fragment = fr"SELECT" ++ Fragment.const(cols) ++ fr"FROM" ++ Fragment.const(table)

  def insert(table : String, cols : String, values : String) : Fragment =
    fr"INSERT INTO" ++ Fragment.const(table) ++ fr"(" ++ Fragment.const(cols) ++ fr") VALUES (" ++ Fragment.const(values) ++ fr")"

  def update(table: String, values: String): Fragment =
    fr"UPDATE" ++ Fragment.const(table) ++ fr"SET" ++ Fragment.const(values)

  def delete(table: String): Fragment =
    fr"DELETE FROM " ++ Fragment.const(table)

  def drop(table: String): Fragment =
    fr"DROP TABLE" ++ Fragment.const(table)

  def query[A : Read](sqlStr: Fragment, droper : Int = 0, taker : Int = Int.MaxValue)(using transactor: Resource[IO, HikariTransactor[IO]]): IO[List[A]] =
    transactor.use { sqlStr.query[A].stream.transact(_).drop(droper).take(taker).compile.toList }

  extension (s : Fragment)
    def nl : Fragment = s ++ Fragment.const("\n")
    def nx : Fragment = s ++ Fragment.const(",\n")
    def notNull : Fragment = s ++ Fragment.const("NOT NULL")
    def unique : Fragment = s ++ Fragment.const("UNIQUE")
    def pk : Fragment = s ++ Fragment.const("PRIMARY KEY")
    def fin : Fragment = s ++ Fragment.const(")")
    def ai : Fragment = s ++ Fragment.const("AUTOINCREMENT")

    def text(colName : String) : Fragment = s ++ Fragment.const(colName) ++ fr"TEXT"
    def inter(colName : String) : Fragment = s ++ Fragment.const(colName) ++ fr"INTEGER"
    def real(colName : String) : Fragment = s ++ Fragment.const(colName) ++ fr"REAL"
    def dater(colName : String) : Fragment = s ++ Fragment.const(colName) ++ fr"DATE"

    def where(colName : String, value : String) : Fragment = s  ++ fr"WHERE" ++ Fragment.const(colName) ++ fr"=" ++ Fragment.const(value)
//    def whereT(colName : String, value : String) : Fragment = s  ++ fr"WHERE" ++ Fragment.const(colName) ++ fr"= '" ++ Fragment.const(value) ++ fr"'"
    def whereT(colName : String, value : String) : Fragment = s  ++ fr"WHERE" ++ Fragment.const(colName) ++ fr"=" ++ Fragment.const("\'" + value + "\'")
    def like(colName : String, value : String) : Fragment = s  ++ fr"WHERE" ++ Fragment.const(colName) ++ fr"LIKE" ++ Fragment.const("\'%" + value + "%\'")

    def order(colName : String) : Fragment = s  ++ fr"ORDER BY" ++ Fragment.const(colName)
    def desc(colName : String) : Fragment = s  ++ fr"ORDER BY" ++ Fragment.const(colName) ++ fr"DESC"
    def limit(num : Int) : Fragment = s  ++ fr"LIMIT" ++ Fragment.const(num.toString)

    def getSql : String = s.toString.substring(10).dropRight(2)

    def run: Int = runUpdate(s).unsafeRunSync()

}
