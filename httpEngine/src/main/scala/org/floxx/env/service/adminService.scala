package org.floxx.env.service

import org.floxx.domain
import org.floxx.domain.Mapping.UserSlot
import org.floxx.domain.{Planning, PlanningDayItem}
import org.floxx.env.api.adminApi.Mapping
import org.floxx.env.repository.cfpRepository.SlotRepo
import org.floxx.env.repository.userRepository.UserRepo
import org.floxx.model.jsonModel.Slot
import org.floxx.model.{SimpleUser, SlotId}
import zio._

object adminService {

  trait AdminService {
    def updateEnv(days: Map[String, String]): Task[Int]
    def insertUserSlotMapping(mapping: Mapping): Task[Int]
    def loadUsers: Task[Seq[SimpleUser]]
    def mappingUserSlot: Task[Seq[UserSlot]]
    def planning: Task[Seq[Planning]]
  }

  case class AdminServiceImpl(slotRepo: SlotRepo, userRepo: UserRepo) extends AdminService {
    override def updateEnv(days: Map[String, String]): Task[Int] =
      for {
        slots <- slotRepo.allSlots
        updatedSlots <- updateSlots(slots, days)
        _ <- slotRepo.drop
        r <- slotRepo.addSlots(updatedSlots.toList)
      } yield r

    override def insertUserSlotMapping(mapping: Mapping): Task[Int] = slotRepo.addMapping(mapping)

    override def loadUsers: Task[Seq[SimpleUser]] =
      userRepo.allUsers

    private def updateSlots(slots: Seq[domain.Slot], env: Map[String, String]): Task[Seq[domain.Slot]] = {

      def updateId(id: domain.Slot.Id, oldDay: String, newDay: String): domain.Slot.Id =
        domain.Slot.Id(id.value.replace(oldDay, newDay))

      Task(slots.map(s => {
        val oldDay = s.day
        val newDay = env(oldDay.value)
        s.copy(
          slotId = updateId(s.slotId, oldDay.value, newDay),
          day    = domain.Slot.Day(newDay)
        )
      }))
    }

    override def planning: Task[Seq[Planning]] =
      slotRepo.mappingUserSlot >>= (m => groupByAndOrder(m))

    override def mappingUserSlot: Task[Seq[UserSlot]] = slotRepo.mappingUserSlot
  }
  private def groupByAndOrder(us: Seq[UserSlot]): Task[Seq[Planning]] =
    Task(
      us.groupBy(_.slot.day)
        .map {
          case (d, slot) =>
            Planning(
              d,
              slot.groupBy(_.slot.roomId).toSeq.sortBy(_._1).map(f => PlanningDayItem(f._1, f._2.sortBy(identity[UserSlot])))
            )

        }
        .toSeq
    )

  val layer: RLayer[Has[SlotRepo] with Has[UserRepo], Has[AdminService]] = (AdminServiceImpl(_, _)).toLayer

  def updateEnv(days: Map[String, String]): RIO[Has[AdminService], Int] =
    ZIO.serviceWith[AdminService](_.updateEnv(days))

  def insertUserSlotMapping(mapping: Mapping): RIO[Has[AdminService], Int] =
    ZIO.serviceWith[AdminService](_.insertUserSlotMapping(mapping))

  def loadUsers: RIO[Has[AdminService], Seq[SimpleUser]] =
    ZIO.serviceWith[AdminService](_.loadUsers)

  def mappingUserSlot: RIO[Has[AdminService], Seq[UserSlot]] =
    ZIO.serviceWith[AdminService](_.mappingUserSlot)

  def planning: RIO[Has[AdminService], Seq[Planning]] =
    ZIO.serviceWith[AdminService](_.planning)
}
