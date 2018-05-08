package com.neighborhood.aka.laplace.estuary.mysql.lifecycle.inorder

import akka.actor.{Actor, ActorLogging}
import com.neighborhood.aka.laplace.estuary.core.lifecycle.{BatcherMessage, FetcherMessage, PowerAdapter, SinkerMessage}
import com.neighborhood.aka.laplace.estuary.core.task.TaskManager

/**
  * Created by john_liu on 2018/5/5.
  */
class MysqlBinlogInOrderPowerAdapter(
                                      taskManager: TaskManager
                                    ) extends Actor with ActorLogging with PowerAdapter {
  override def receive: Receive = {
    case "control" => control
    case "cost" => computeCost
    case FetcherMessage(x) => {
      x match {
        case timeCost: Long => updateFetchTimeByTimeCost(timeCost)
      }
    }
    case BatcherMessage(x) => {
      x match {
        case timeCost: Long => updateBatchTimeByTimeCost(timeCost)
      }
    }
    case SinkerMessage(x) => {
      x match {
        case timeCost: Long => updateSinkTimeByTimeCost(timeCost)
      }
    }

  }

  override def computeCost: Unit = {

  }

  override def control: Unit = ???
}
