package org.floxx.repository

import java.util.UUID

object Keys {

  sealed trait RedisKey {

    val _root_key: String
    val description: String

    def next: String = s"${_root_key}:${UUID.randomUUID}"

  }

  case class SchedulesCfp() extends RedisKey {

    override val _root_key: String = "shedulecfp"
    override val description: String = "contains all schedules for 3 days (Json format)"
  }

}
