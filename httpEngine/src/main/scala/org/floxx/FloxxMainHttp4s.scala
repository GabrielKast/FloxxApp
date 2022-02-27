package org.floxx

import cats.syntax.all._
import org.http4s.implicits._
import org.floxx.Environment.{AppEnvironment, appEnvironnement}
import org.floxx.env.api._
import org.floxx.env.configuration.config.{GlobalConfig, getConf}
import org.http4s.StaticFile
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.{AuthMiddleware, Router}
import org.joda.time.DateTimeZone
import org.slf4j.{Logger, LoggerFactory}
import zio.{ExitCode, _}
import zio.interop.catz._
import org.http4s.server.staticcontent._

object FloxxMainHttp4s extends zio.App {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  val server: ZIO[AppEnvironment, Throwable, Unit] = {
    ZIO.runtime[AppEnvironment].flatMap { implicit r =>
      {
        for {
          conf <- getConf
          server <- BlazeServerBuilder[ApiTask]
            .bindHttp(conf.floxx.port, "0.0.0.0")
            .withHttpApp(floxxApp(conf))
            .serve
            .compile
            .drain
        } yield {
          server
        }
      }
    }
  }

  DateTimeZone.setDefault(DateTimeZone.forID("Europe/Paris"))

 def floxxServices(conf:GlobalConfig) = {
   val authMiddleware: AuthMiddleware[ApiTask, UserInfo] = AuthMiddleware(authUser(conf))


        securityApi.api <+>
   authMiddleware(trackApi.api) <+>
     authMiddleware(hitApi.api) <+>
     authMiddleware(technicalApi.api) <+>
     authMiddleware(statsApi.api)
}





def floxxApp(conf:GlobalConfig) = Router[ApiTask](
  "/api" -> floxxServices(conf),
  "/" -> {
    logger.info("load static part of app")
    StaticApi.api
  }
).orNotFound
  
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    logger.info("server starting..")
    server
      .provideLayer(appEnvironnement)
      .fold[ExitCode](ex =>
        {
          logger.error(s"failure ${ex.getMessage}",ex)
          ExitCode.failure},
        _ =>{
        logger.info("success")
        ExitCode.success})
  }
}
