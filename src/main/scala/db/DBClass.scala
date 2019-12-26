package db

import java.text.SimpleDateFormat
import java.util.{Date, UUID}

import common._
import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import io.swagger.annotations.{ApiModel, ApiModelProperty}

import scala.annotation.meta.field
import scala.collection.mutable.HashMap
import common.TimeTool._


/**
  * Created by 林 on 14-3-26.
  */

class BaseDBEntity[+Self <: BaseDBEntity[Self]](tableName: String) extends DBEntity(tableName) {

  def uuid = UUID.randomUUID().toString.replace("-", "")

  def toJson: String = {
    BaseDBEntity.map.writeValueAsString(this)
  }

  def toMap: Map[String, Any] = {
    BaseDBEntity.map.readValue(toJson, Map[String, Any]().getClass).asInstanceOf[Map[String, Any]]
  }

  def toHashMap: HashMap[String, Any] = {
    BaseDBEntity.map.readValue(toJson, HashMap[String, Any]().getClass).asInstanceOf[HashMap[String, Any]]
  }

  def fromJson(json: String): Self = {
    BaseDBEntity.map.readValue(json, this.getClass).asInstanceOf[Self]
  }

  //将对应的更新类转为实体类
  def changeUpdateBean(): Self = {
    fromJson(toJson)
  }

  override def queryById(id: String): Option[Self] = {
    super.queryById(id) map (_.asInstanceOf[Self])
  }

  override def queryByIds(ids: List[String]): List[Self] = {
    super.queryByIds(ids) map (_.asInstanceOf[Self])
  }


  override def queryOne(sql: String, param: String*): Option[Self] = {
    super.queryOne(s"select * from $tableName where " + sql, param: _*) map (_.asInstanceOf[Self])
  }

  override def queryAll(): List[Self] = {
    super.queryAll map (_.asInstanceOf[Self])
  }

  override def query(where: String, param: String*): List[Self] = {
    super.query(where, param: _*) map (_.asInstanceOf[Self])
  }

  //这个接口需要传条件、排序
  override def queryPage(where: String, pageNum: Int, pageSize: Int, order: String, param: String*): (Int, List[Self]) = {
    val (count, list) = super.queryPage(where, pageNum, pageSize, order, param: _*)
    (count, list map (_.asInstanceOf[Self]))
  }

}

object BaseDBEntity {
  protected val map = new ObjectMapper() with ScalaObjectMapper
  map.registerModule(DefaultScalaModule)
  map.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
  map.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm"))


  def toJson(data: AnyRef) = {
    map.writeValueAsString(data)
  }

  def toHashMap(dbe: DBEntity): HashMap[String, Any] = BaseDBEntity.map.readValue(map.writeValueAsString(dbe), HashMap[String, Any]().getClass).asInstanceOf[HashMap[String, Any]]

  //自动检查数据的查询结果是否存在
  implicit class DBOptionAdd[T <: DBEntity](o: Option[T]) {
    def dbCheck: T = if (o.isEmpty) throw new DataNoFindExcepiton else o.get
  }

  def uuid = UUID.randomUUID().toString.replace("-", "")

}

import BaseDBEntity.uuid


/**
  * 注释说明   表级别
  * {"method":"get,post,put,delete"(管理后台中的方法),"ref":"quick" or "cache"(获取关联表的方式),"map":["lat","lng"](地图属性的经纬度)}
  *
  */


/**
  * 系统设置表，系统中重要设置全部存储于此表中
  *
  */
//系统参数
class Setting(val id: String = "", val name: String = "", val value: String = "", val remark: String = "") extends BaseDBEntity[Setting]("Setting")

//操作日志
@ApiModel(value = "UserActionLog", description = "用户操作日志")
class UserActionLog(val id: String = uuid,
                    @(ApiModelProperty@field)(value = "用户", required = true)
                    val uid: String = "",
                    @(ApiModelProperty@field)(value = "方法", required = true)
                    val method: String = "",
                    @(ApiModelProperty@field)(value = "路径", required = true)
                    val path: String = "",
                    @(ApiModelProperty@field)(value = "ip", required = true)
                    val ip: String = "",
                    @(ApiModelProperty@field)(value = "创建时间")
                    val createTime: Date = new Date) extends BaseDBEntity[UserActionLog]("UserActionLog")


@ApiModel(value = "LiveRoom", description = """直播室""")
class LiveRoom(val id: String = uuid,
               @(ApiModelProperty@field)(value = "用户", required = true, reference = "User")
               val uid: String = "",
               @(ApiModelProperty@field)(value = "名称", required = true)
               val name: String = "",
               @(ApiModelProperty@field)(value = "状态(0=未开播  1=开播中 2=开播异常状态 3=已封禁)", required = true)
               val status: Int = 0
              ) extends BaseDBEntity[LiveRoom]("LiveRoom")


@ApiModel(value = "User", description = "用户")
class User (val id: String = uuid,
            @(ApiModelProperty@field)(value = "名称", required = true)
            val name: String = "",
            @(ApiModelProperty@field)(value = "状态(0=未认证,1=认证中,2=已认证,3=已封禁)", required = true)
            val status:Int=0,
            @(ApiModelProperty@field)(value = "vip类型(0=一般用户,1-6 vip等级)", required = true)
            val levelType: Int = 0
           ) extends BaseDBEntity[User]("Users")

@ApiModel(value = "UserGold", description = """用户金币""")
class UserGold(val id: String = uuid,
               @(ApiModelProperty@field)(value = "可用余额")
               val amount: Long = 0l,
               @(ApiModelProperty@field)(value = "购买总额")
               val totalBuy: Long = 0l,
               @(ApiModelProperty@field)(value = "消费总额")
               val totalUse: Long = 0l,
               @(ApiModelProperty@field)(value = "创建时间(前端请忽略)", hidden = true)
               val createTime: Date = new Date) extends BaseDBEntity[UserGold]("UserGold")


//分享记录
@ApiModel(value = "PornInfo", description = "鉴黄记录")
class PornInfo(val id: String = uuid,
               @(ApiModelProperty@field)(value = "主播", required = true)
               val uid: String = "",
               @(ApiModelProperty@field)(value = "日期", required = true)
               val date: String = "",
               @(ApiModelProperty@field)(value = "分数", required = true)
               val point: Int = 0,
               @(ApiModelProperty@field)(value = "照片", required = true)
               val image: String = "",
               @(ApiModelProperty@field)(value = "创建时间")
               val createTime: Date = new Date) extends BaseDBEntity[PornInfo]("PornInfo")

@ApiModel(value = "SpamKey", description = "反垃圾敏感词")
class SpamKey(val id: String = uuid,
              @(ApiModelProperty@field)(value = "敏感词", required = true)
              val value: String = "",
              @(ApiModelProperty@field)(value = "创建时间", required = false, hidden = true)
              val createTime: Date = new Date()) extends BaseDBEntity[SpamKey]("SpamKey")
