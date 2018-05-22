package com.neighborhood.aka.laplace.estuary.mongo.task

import com.neighborhood.aka.laplace.estuary.bean.credential.MongoCredentialBean
import com.neighborhood.aka.laplace.estuary.bean.datasink.KafkaBean
import com.neighborhood.aka.laplace.estuary.bean.identity.{BaseExtractBean, SyncDataType}
import com.neighborhood.aka.laplace.estuary.bean.resource.{MongoBean, SourceDataType}
import com.neighborhood.aka.laplace.estuary.mongo.SettingConstant

/**
  * Created by john_liu on 2018/4/25.
  */
final class Mongo2KafkaTaskInfoBean(

                                     override val hosts: List[String],
                                     override val port: Int,

                                     /**
                                       * 是否计数，默认不计数
                                       */
                                     val isCounting: Boolean = false,

                                     /**
                                       * 是否计算每条数据的时间，默认不计时
                                       */
                                     val isCosting: Boolean = false,

                                     /**
                                       * 是否保留最新binlog位置
                                       */
                                     val isProfiling: Boolean = false,

                                     val batcherNum: Int = SettingConstant.BATCH_NUM,
                                     override val ignoredNs: Array[String] = Array.empty,
                                     override val concernedNs: Array[String] = Array.empty,
                                     override val mongoCredentials: Option[List[MongoCredentialBean]] = None

                                   ) extends MongoBean with KafkaBean with BaseExtractBean {
  /**
    * 数据同步形式
    */
  override var dataSyncType: String = SyncDataType.NORMAL.toString

}
