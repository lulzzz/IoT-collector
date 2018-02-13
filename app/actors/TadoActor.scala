package actors

import akka.actor.Actor
import com.google.inject.Inject
import play.api.Logger
import services.{InfluxDBService, TadoService}

import scala.concurrent.{ExecutionContext, Future}

class TadoActor @Inject()(tadoAPI: TadoService,
                          influxDBService: InfluxDBService)(implicit ec: ExecutionContext) extends Actor {

  def receive = {
    case Poll(id) =>
      Logger.info("--- Polling tado ---")
      tadoAPI.state(id).flatMap {
        case Left(error) => Future.successful(Logger.error(error))
        case Right(measurement) =>
          implicit val tags: Seq[(String, String)] = Seq(
            "device" -> "tado",
            "deviceId" -> id.toString
          )
          influxDBService.store("tado", Seq(measurement))
      }
  }
}
