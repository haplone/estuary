package com.neighborhood.aka.laplace.estuary.bean.exception.sink

import com.neighborhood.aka.laplace.estuary.bean.exception.EstuaryException

/**
  * Created by john_liu on 2018/5/28.
  */
abstract class SinkDataException(
                         message: => String,
                         cause: Throwable
                       ) extends EstuaryException(message, cause) {
  def this(message: => String) = this(message, null)

  def this(cause: Throwable) = this("", cause)
}
