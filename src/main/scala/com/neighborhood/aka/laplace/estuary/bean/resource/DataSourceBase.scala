package com.neighborhood.aka.laplace.estuary.bean.resource

/**
  * Created by john_liu on 2018/2/7.
  * @tparam S 限定DataSource的类型,使Bean和类型参数绑定
  * @author neighborhood.aka.lapalce
  */
trait DataSourceBase[S] {

  //  var dataSourceType : DataSourceType
  def dataSourceType: String

}
