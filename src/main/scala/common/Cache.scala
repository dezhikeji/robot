package common

import java.io.Serializable
import java.util.Collections

import com.alicloud.openservices.tablestore._
import com.alicloud.openservices.tablestore.model._
import com.typesafe.config.ConfigFactory
import sdk.SDKMain.setting
import tools.ReflectTool
import tools.Z4ZTool._

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by isaac on 16/2/15.
  */
object Cache {
  lazy val conf = ConfigFactory.load()
  //固定缓存,非常常用又不改变的
  private val staticCache = Collections.synchronizedMap[String, AnyRef](new mutable.HashMap[String, AnyRef]())
  //使用字符串压缩
  private val useZ4z = conf.getBoolean("cache.z4z")

  //分别存储到一级缓存\二级缓存
  private def setCacheValue(key: String, v: AnyRef, time: Int) = {
    val saveTime = if (time == Int.MaxValue) time else System.currentTimeMillis() / 1000 + time
    val cacheData = getCacheData(v, saveTime, useZ4z)
    OtsCache.setCache(key, cacheData)
  }

  private def getCacheValue(key: String) = {
    OtsCache.getCache(key).map(_.getValue)
  }

  /**
    *
    * @param key
    * @param v
    * @param time
    * @return
    */
  def setCache(key: String, v: AnyRef, time: Int = Int.MaxValue) = {
    setCacheValue(key, v, time)
  }

  /**
    * 增加已有的值
    *
    * @param key  key
    * @param v    增加数
    * @param time 时间
    * @return 新值
    */
  def addCache(key: String, v: Int, time: Int = Int.MaxValue) = {
    val oldValue = getCache(key).getOrElse("0").toString.toInt
    if (v != 0) {
      setCache(key, (oldValue + v).toString, time)
    }
    oldValue + v
  }

  def getCache(key: String) = {
    getCacheValue(key)
  }

  def delCache(key: String) = {
    staticCache.remove(key)
    Future {
      OtsCache.delCache(key)
    }
  }

  def cleanSecondCache {
    OtsCache.cleanCache()
  }

  /**
    * 静态缓存,不清理只过期
    *
    * @param key
    * @param v
    * @param time
    * @return
    */
  def setStaticCache(key: String, v: AnyRef, time: Int = -1) = {
    val saveTime = if (time == Int.MaxValue) time else System.currentTimeMillis() / 1000 + time
    staticCache.put(key, (saveTime, v, false))
  }

  def getStaticCache(key: String) = {
    val cv = staticCache.get(key).asInstanceOf[Tuple3[Long, AnyRef, Boolean]]
    val now = System.currentTimeMillis() / 1000
    if (cv != null)
      if (cv._1 <= now) {
        delCache(key)
        None
      } else
        Some(if (cv._3) cv._2.asInstanceOf[Array[Byte]].unz4zStr else cv._2)
    else None
  }

  def getCacheData(value: AnyRef, time: Long, z4z: Boolean) = {
    val data = if (z4z) ReflectTool.getBytesByObject(value).z4z else ReflectTool.getBytesByObject(value)
    new CacheData(data, time, z4z)
  }

}

//用于缓存的数据结构
class CacheData(data: Array[Byte], val time: Long, val z4z: Boolean) extends Serializable {
  def getValue = {
    if (z4z) ReflectTool.getObjectByBytes(data.unz4z)
    else ReflectTool.getObjectByBytes(data)
  }

  def getBinryData = data
}

object OtsCache {
  lazy val conf = ConfigFactory.load()
  private lazy val client = new SyncClient(setting("otsUrl"),setting("aliAccessId"), setting("aliAccessKey"), setting("otsInstanceName"))
  private val cacheTableName = setting("otsTable")
  checkTable


  private def checkTable = {
    client.listTable()
    if (!client.listTable().getTableNames.contains(cacheTableName)) {
      client.listTable().getTableNames.toArray.foreach(println)
      val tableMeta = new TableMeta(cacheTableName)
      tableMeta.addPrimaryKeyColumn("key", PrimaryKeyType.STRING)
      val ttl = if (conf.hasPath("cache.ots.ttl")) conf.getInt("cache.ots.ttl") else 3600 * 24 * 3650
      val request = new CreateTableRequest(tableMeta, new TableOptions(ttl, 1))
      request.setReservedThroughput(new ReservedThroughput(0, 0))
      client.createTable(request)
    }
  }

  def setCache(key: String, data: CacheData) {
    val rowChange = new RowPutChange(cacheTableName)
    val primaryKey = PrimaryKeyBuilder.createPrimaryKeyBuilder().addPrimaryKeyColumn("key", PrimaryKeyValue.fromString(key)).build()
    rowChange.setPrimaryKey(primaryKey)
    rowChange.addColumn("value", ColumnValue.fromBinary(data.getBinryData))
    rowChange.addColumn("time", ColumnValue.fromLong(Long.box(data.time)))
    rowChange.addColumn("z4z", ColumnValue.fromBoolean(data.z4z))
    rowChange.setCondition(new Condition(RowExistenceExpectation.IGNORE))
    val request = new PutRowRequest()
    request.setRowChange(rowChange)
    Tool.reTry(3) {
      val result = client.putRow(request)
      result.getConsumedCapacity().getCapacityUnit().getWriteCapacityUnit()
    }
  }

  def getCache(key: String) = {
    val now = System.currentTimeMillis() / 1000
    val criteria = new SingleRowQueryCriteria(cacheTableName)
    val primaryKey = PrimaryKeyBuilder.createPrimaryKeyBuilder()
    primaryKey.addPrimaryKeyColumn("key", PrimaryKeyValue.fromString(key))
    criteria.setPrimaryKey(primaryKey.build())
    criteria.setMaxVersions(1)
    val request = new GetRowRequest()
    request.setRowQueryCriteria(criteria)
    var value: Option[CacheData] = None
    Tool.reTry(3) {
      val result = client.getRow(request)
      val row = result.getRow()
      if (result.getRow != null && !result.getRow.isEmpty) {
        val dataMap = ("value" :: "time" :: "z4z" :: Nil).map(k => k -> getColData(row.getColumn(k).head.getValue)).toMap
        value = Some(new CacheData(dataMap("value").asInstanceOf[Array[Byte]], dataMap("time").toString.toLong, dataMap.getOrElse("z4z", false).asInstanceOf[Boolean]))
      }
    }
    if (value.isDefined && value.get.time > now) {
      value
    } else {
      if (value.isDefined && value.get.time <= now) delCache(key)
      None
    }
  }

  def delCache(key: String) = {
    val rowChange = new RowDeleteChange(cacheTableName)
    val primaryKey = PrimaryKeyBuilder.createPrimaryKeyBuilder()
    primaryKey.addPrimaryKeyColumn("key", PrimaryKeyValue.fromString(key))
    rowChange.setPrimaryKey(primaryKey.build())
    val request = new DeleteRowRequest()
    request.setRowChange(rowChange)
    val result = client.deleteRow(request)
    result.getConsumedCapacity().getCapacityUnit().getWriteCapacityUnit()
  }

  /**
    * 批量查询
    *
    * @return
    */
  def getAll = {
    val criteria = new RangeRowQueryCriteria(cacheTableName)
    val inclusiveStartKey = PrimaryKeyBuilder.createPrimaryKeyBuilder()
    inclusiveStartKey.addPrimaryKeyColumn("key", PrimaryKeyValue.INF_MIN)
    val exclusiveEndKey = PrimaryKeyBuilder.createPrimaryKeyBuilder()
    exclusiveEndKey.addPrimaryKeyColumn("key", PrimaryKeyValue.INF_MAX)
    criteria.setInclusiveStartPrimaryKey(inclusiveStartKey.build())
    criteria.setExclusiveEndPrimaryKey(exclusiveEndKey.build())
    criteria.setMaxVersions(1)
    val request = new GetRangeRequest()
    request.setRangeRowQueryCriteria(criteria)
    client.getRange(request).getRows.toList
  }

  /**
    * 清理缓存里面的数据
    *
    * @return
    */
  def cleanCache(all: Boolean = false) = {
    val now = System.currentTimeMillis() / 1000
    //筛选数据
    val datas = getAll.map { v =>
      val dataMap = ("key" :: "value" :: "time" :: Nil).map(k => k -> getColData(v.getColumn(k).head.getValue)).toMap
      dataMap("key").asInstanceOf[String] -> new CacheData(dataMap("value").asInstanceOf[Array[Byte]], dataMap("time").toString.toLong, dataMap.getOrElse("z4z", false).asInstanceOf[Boolean])
    }.toMap
    //删除数据
    datas.filter(v => if (all) true else v._2.time <= now).foreach { data =>
      println(data._1)
      Cache.delCache(data._1)
    }
    (datas.size, datas.size, 0)
  }

  def getColData(v: ColumnValue): Object = {
    if (v == null) return null
    v.getType match {
      case ColumnType.BINARY => v.asBinary()
      case ColumnType.BOOLEAN => Boolean.box(v.asBoolean())
      case ColumnType.DOUBLE => Double.box(v.asDouble())
      case ColumnType.INTEGER => Long.box(v.asLong())
      case ColumnType.STRING => v.asString()
    }
  }

}
