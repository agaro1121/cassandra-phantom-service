import java.util.Date

import com.outworkers.phantom.connectors.ContactPoint
import com.outworkers.phantom.dsl._
import java.util.{UUID => jUUID}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.outworkers.phantom.builder.query.RootSelectBlock
import com.outworkers.phantom.builder.query.SelectQuery.Default
import com.outworkers.phantom.connectors

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.language.postfixOps

object Tester extends App {

  //connector
  val connector = ContactPoint(9042).noHeartbeat().keySpace("hierro")

  // call site
  private val database = new UsersDatabase(connector)
  private val users = database.users

  private val all = users.selectAll

  private val result = Await.result(all, 10 seconds)

  println(result)

  println("Using Streams...")

  import com.outworkers.phantom.streams._
  implicit val actorSystem = ActorSystem("Streams")
  implicit val mat = ActorMaterializer()

  import database.session
  import database.space

  Source
    .fromPublisher(users.publisher())
    .to(Sink.foreach(println))
    .run()

}

//Scala Representation
case class User(
  email: String,
  portfolios: Set[jUUID],
  tickerUpdates: Map[String, Date],
  tickers: Set[String],
  topTickers: List[String]
)

/*
  Cassandra Representation
  Column names have to match DB
 */
sealed class Users extends CassandraTable[ConcreteUsers, User] {
  object email extends StringColumn(this) with PartitionKey
  object portfolios extends SetColumn[UUID](this)
  object tickers extends SetColumn[String](this)
  object ticker_updates extends MapColumn[String, Date](this)
  object top_tickers extends ListColumn[String](this)

  override def fromRow(row: Row): User =
    User(email(row), portfolios(row), ticker_updates(row), tickers(row), top_tickers(row))
}

//TODO: CRUD goes here
abstract class ConcreteUsers extends Users with RootConnector {
  def selectAll: Future[List[User]] = select.all().fetch()
}

class UsersDatabase(override val connector: KeySpaceDef) extends Database[UsersDatabase](connector) {
  object users extends ConcreteUsers with connector.Connector
}
