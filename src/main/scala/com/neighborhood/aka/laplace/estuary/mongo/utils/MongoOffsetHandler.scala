package com.neighborhood.aka.laplace.estuary.mongo.utils

import com.neighborhood.aka.laplace.estuary.core.source.DataSourceConnection
import com.neighborhood.aka.laplace.estuary.core.task.PositionHandler
import com.neighborhood.aka.laplace.estuary.core.util.ZooKeeperLogPositionManager
import com.neighborhood.aka.laplace.estuary.mongo.source.MongoOffset
import org.slf4j.LoggerFactory

/**
  * Created by john_liu on 2018/4/25.
  */
class MongoOffsetHandler(
                          val manager: ZooKeeperLogPositionManager[MongoOffset],
                          private val startMongoOffset: Option[MongoOffset] = None,
                          val destination: String
                        ) extends PositionHandler[MongoOffset] {
  val logger = LoggerFactory.getLogger(classOf[MongoOffsetHandler])

  override def persistLogPosition(destination: String = this.destination, logPosition: MongoOffset): Unit = {
    manager.persistLogPosition(destination, logPosition)
  }

  override def getlatestIndexBy(destination: String= this.destination): MongoOffset = {
    manager.getLatestIndexBy(destination)
  }

  override def findStartPosition(conn: DataSourceConnection): MongoOffset
  = ???


}
