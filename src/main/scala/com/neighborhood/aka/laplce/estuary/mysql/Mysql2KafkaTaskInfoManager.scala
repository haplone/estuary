package com.neighborhood.aka.laplce.estuary.mysql

import java.net.InetSocketAddress
import java.nio.charset.Charset
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

import com.alibaba.otter.canal.common.zookeeper.ZkClientx
import com.alibaba.otter.canal.filter.aviater.AviaterRegexFilter
import com.alibaba.otter.canal.parse.inbound.mysql.MysqlConnection
import com.alibaba.otter.canal.parse.inbound.mysql.MysqlConnection.{BinlogFormat, BinlogImage}
import com.alibaba.otter.canal.parse.inbound.mysql.dbsync.TableMetaCache
import com.alibaba.otter.canal.parse.index.ZooKeeperLogPositionManager
import com.alibaba.otter.canal.protocol.position.EntryPosition
import com.neighborhood.aka.laplce.estuary.bean.key.BinlogKey
import com.neighborhood.aka.laplce.estuary.bean.task.Mysql2KafkaTaskInfoBean
import com.neighborhood.aka.laplce.estuary.core.lifecycle.Status
import com.neighborhood.aka.laplce.estuary.core.lifecycle.Status.Status
import com.neighborhood.aka.laplce.estuary.core.sink.KafkaSinkFunc
import com.neighborhood.aka.laplce.estuary.core.task.{RecourceManager, TaskManager}
import com.typesafe.config.Config
import org.apache.commons.lang.StringUtils

import scala.util.parsing.json.JSONObject

/**
  * Created by john_liu on 2018/2/7.
  */
class Mysql2KafkaTaskInfoManager(taskInfoBean: Mysql2KafkaTaskInfoBean) extends TaskManager with RecourceManager[String, MysqlConnection, KafkaSinkFunc[BinlogKey, String]] {

  /**
    * 同步任务控制器的ActorRef
    */
  val syncController: AnyRef = null
  /**
    * 传入的任务配置bean
    */
  val taskInfo = taskInfoBean
  /**
    * 支持的binlogFormat
    */
  lazy val supportBinlogFormats = Option(taskInfo.binlogFormat)
    .map {
      formatsStr =>
        formatsStr
          .split(",")
          .map {
            formatStr =>
              formatsStr match {
                case "ROW" => BinlogFormat.ROW
                case "STATEMENT" => BinlogFormat.STATEMENT
                case "MIXED" => BinlogFormat.MIXED
              }
          }
    }
  /**
    * 支持的binlogImage
    */
  lazy val supportBinlogImages = Option(taskInfo.binlogImages)
    .map {
      binlogImagesStr =>
        binlogImagesStr.split(",")
          .map {
            binlogImageStr =>
              binlogImageStr match {
                case "FULL" => BinlogImage.FULL
                case "MINIMAL" => BinlogImage.MINIMAL
                case "NOBLOB" => BinlogImage.NOBLOB
              }
          }
    }
  /**
    * 利用canal模拟的mysql从库的slaveId
    */
  val slaveId = taskInfoBean.slaveId
  /**
    * 同步任务开始entry
    */
  var startPosition: EntryPosition = if (StringUtils.isEmpty(this.taskInfo.journalName)) new EntryPosition("mysql-bin.000013", 4L) else new EntryPosition(this.taskInfo.journalName, this.taskInfo.position)
  /**
    * canal的mysqlConnection
    */
  val mysqlConnection = buildSource
  /**
    * kafka客户端
    */
  val kafkaSink = buildSink
  /**
    * MysqlBinlogParser
    */
  lazy val binlogParser: MysqlBinlogParser = buildParser
  /**
    * logPosition处理器
    */
  lazy val logPositionHandler: LogPositionHandler = buildEntryPositionHandler



  /**
    * 实现@trait ResourceManager
    *
    * @return canal的mysqlConnection
    */
  override def buildSource: MysqlConnection = buildMysqlConnection

  /**
    * 实现@trait ResourceManager
    *
    * @return KafkaSinkFunc
    */
  override def buildSink: KafkaSinkFunc[BinlogKey, String] = {
    new KafkaSinkFunc[BinlogKey, String](this.taskInfo)
  }

  /**
    * @return canal的mysqlConnection
    */
  def buildMysqlConnection: MysqlConnection = {
    //charsetNumber
    val connectionCharsetNumber: Byte = taskInfo.connectionCharsetNumber
    //字符集
    val connectionCharset: Charset = taskInfo.connectionCharset
    val receiveBufferSize = taskInfo.receiveBufferSize
    val sendBufferSize = taskInfo.sendBufferSize
    val masterCredentialInfo = taskInfo.master
    val address = new InetSocketAddress(masterCredentialInfo.address, masterCredentialInfo.port)
    val username = masterCredentialInfo.username
    val password = masterCredentialInfo.password
    val database = masterCredentialInfo.defaultDatabase
    val conn = new MysqlConnection(address, username, password, connectionCharsetNumber, database)
    conn.setCharset(connectionCharset)
    conn.setSlaveId(slaveId)
    conn.getConnector.setSendBufferSize(sendBufferSize)
    conn.getConnector.setReceiveBufferSize(receiveBufferSize)
    conn
  }

  /**
    * @return 构建binlogParser
    */
  def buildParser: MysqlBinlogParser = {
    val convert = new MysqlBinlogParser(taskInfo.isProfiling)
    val eventFilter = if (!StringUtils.isEmpty(taskInfo.filterPattern)) new AviaterRegexFilter(taskInfo.filterPattern) else null
    val eventBlackFilter = if (!StringUtils.isEmpty(taskInfo.filterBlackPattern)) new AviaterRegexFilter(taskInfo.filterBlackPattern) else null
    if (eventFilter != null && eventFilter.isInstanceOf[AviaterRegexFilter]) convert.setNameFilter(eventFilter.asInstanceOf[AviaterRegexFilter])
    if (eventBlackFilter != null && eventBlackFilter.isInstanceOf[AviaterRegexFilter]) convert.setNameBlackFilter(eventBlackFilter.asInstanceOf[AviaterRegexFilter])

    convert.setCharset(taskInfo.connectionCharset)
    convert.setFilterQueryDcl(taskInfo.filterQueryDcl)
    convert.setFilterQueryDml(taskInfo.filterQueryDml)
    convert.setFilterQueryDdl(taskInfo.filterQueryDdl)
    convert.setFilterRows(taskInfo.filterRows)
    convert.setFilterTableError(taskInfo.filterTableError)

    convert
  }

  /**
    * @return logPosition处理器
    */
  def buildEntryPositionHandler: LogPositionHandler = {
    val servers = taskInfo.zookeeperServers
    val timeout = taskInfo.zookeeperTimeout
    val zkLogPositionManager = new ZooKeeperLogPositionManager
    zkLogPositionManager.setZkClientx(new ZkClientx(servers, timeout))
    new LogPositionHandler(binlogParser, zkLogPositionManager, slaveId = this.slaveId, destination = this.taskInfo.syncTaskId, address = new InetSocketAddress(taskInfo.master.address, taskInfo.master.port), master = Option(startPosition))

  }

  override def taskType: String = s"${taskInfo.dataSourceType}-${taskInfo.dataSyncType}-{${taskInfo.dataSinkType}}"
}

object Mysql2KafkaTaskInfoManager {
  lazy val zkClientx = null
  val taskStatusMap = new ConcurrentHashMap[String, Map[String, Status]]()

  /**
    * 任务管理器的构造的工厂方法
    */
  def buildManager(taskInfoBean: Mysql2KafkaTaskInfoBean): Mysql2KafkaTaskInfoManager = {
    new Mysql2KafkaTaskInfoManager(taskInfoBean)
  }

  /**
    *每当任务状态变化时，更新之
    */
  def onChangeStatus(mysql2KafkaTaskInfoManager: Mysql2KafkaTaskInfoManager): Unit = {
    val syncTaskId = mysql2KafkaTaskInfoManager.taskInfo.syncTaskId
    val syncControllerStatus = mysql2KafkaTaskInfoManager.syncControllerStatus
    val fetcherStatus = mysql2KafkaTaskInfoManager.fetcherStatus
    val sinkerStatus = mysql2KafkaTaskInfoManager.sinkerStatus
    val batcherStatus = mysql2KafkaTaskInfoManager.batcherStatus
    val listenerStatus = mysql2KafkaTaskInfoManager.heartBeatListenerStatus
    val map = Map("syncControllerStatus" -> syncControllerStatus, "fetcherStatus" -> fetcherStatus, "sinkerStatus" -> sinkerStatus, "batcherStatus" -> batcherStatus, "listenerStatus" -> listenerStatus)

    taskStatusMap.put(syncTaskId, map)
  }
}
