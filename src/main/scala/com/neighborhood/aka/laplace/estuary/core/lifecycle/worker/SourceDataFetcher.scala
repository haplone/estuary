package com.neighborhood.aka.laplace.estuary.core.lifecycle.worker

import com.neighborhood.aka.laplace.estuary.core.lifecycle.WorkerType

/**
  * Created by john_liu on 2018/2/6.
  */
trait SourceDataFetcher extends worker{
  implicit val workerType = WorkerType.Fetcher
}
