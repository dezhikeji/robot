package sdk


import java.util.Date

import com.typesafe.config.ConfigFactory
import common.{Cache, Tool}
import common.Tool._
import db.{DBEntity, Setting, SpamKey, User}
import tools.{PornDetection, ZhiBo}

import scala.collection.mutable

/**
  * Created by admin on 2016/9/9.
  */
object SDKMain {
  lazy val conf = ConfigFactory.load()

  lazy val setting=new Setting().queryAll().map(s=> s.name -> s.value).toMap
  lazy val chatroomHost=setting("chatroomHost")
  lazy val porn = new PornDetection(setting("aliAccessId"),setting("aliAccessKey"), "cn-hangzhou")
  lazy val zb = new ZhiBo(setting("aliAccessId"),setting("aliAccessKey"),setting("zhiboEndpoint"), setting("zhiboHost"))
  lazy val spamUid = setting("spamUid")
  lazy val pornUid =setting("pornUid")
  lazy val giftUid = setting("giftUid")
  val spamMap = new mutable.HashMap[String, SpamChatUser]()
  val pornMap = new mutable.HashMap[String, PornChatUser]()
  val giftMap = new mutable.HashMap[String, GiftChatUser]()
  val spamCache = new mutable.ListBuffer[String]()
  val spamUserMap = new mutable.HashMap[String,mutable.HashSet[String]]()
  val spamCount = new mutable.HashMap[String,Int]()
  lazy val giftMessagePrice=setting("giftPrice").toInt
  lazy val hasPorn=setting("usePorn").toBoolean

  def main(args: Array[String]) {
    println("db url: "+conf.getString("mysql.url"))
    println("start ai "+new Date().sdatetime)
    val spamList = new SpamKey().queryAll.map(_.value.split("&")).flatten
    spamCache.appendAll(spamList)
    Cache.setCache("a", "1", 300)
    run {
      sendHeartbeat
    }
    if(hasPorn)(run(checkPorn))
    run {
      restart
    }
//    run{
//      cleanDb
//    }
    while (true) {
      try {
        zb.getOnline().filter(! _.getStreamName.contains("_")).map { live =>
          val room = live.getStreamName
          if (!spamMap.contains(room) || spamMap(room).isClose) {
            spamMap.put(room, new SpamChatUser(room))
          }
          if(hasPorn){
            if (!pornMap.contains(room)|| pornMap(room).isClose) {
              pornMap.put(room, new PornChatUser(room))
            }
          }
          if (!giftMap.contains(room)|| giftMap(room).isClose) {
            giftMap.put(room, new GiftChatUser(room))
          }
        }
        if((System.currentTimeMillis()/10000) % 6 ==0) {
          DBEntity.sql(s"update ${new User().tableName} set status=0 where id=?", pornUid)
          DBEntity.sql(s"update ${new User().tableName} set status=0 where id=?", spamUid)
          DBEntity.sql(s"update ${new User().tableName} set status=0 where id=?", giftUid)
        }
      } catch {
        case e: Throwable =>
          e.printStackTrace()
      }
      Thread.sleep(10 * 1000)
    }


    //查询开播信息
    //连接直播室
    //读取用户文本
    //鉴定广告


    //查询开播信息
    //鉴定黄色信息
    //发送告警信息

  }

  //定时清理用户行为数据
  def cleanDb{
    def del {
      val day="-2d".dateExp.sdate
      DBEntity.sql(s"delete from UserActionLog where id in (select id from  UserActionLog where createTime < '${day}' limit 10000)")
    }
    while (true) {
      Thread.sleep(500)
      try {
        del
      } catch {
        case e: Throwable =>
          e.printStackTrace()
      }
    }
  }

  def sendHeartbeat: Unit = {
    while (true) {
      Thread.sleep(5000)
      try {
        spamMap.values.map(_.send("heartbeat"))
        pornMap.values.map(_.send("heartbeat"))
      } catch {
        case e: Throwable =>
          e.printStackTrace()
      }
    }
  }

  def restart: Unit = {
    Thread.sleep(1000*3600*24)
    spamMap.values.map(_.close())
    pornMap.values.map(_.close())
    System.exit(0)
  }

  def checkPorn: Unit = {
    while (true) {
      try {
        pornMap.values.map(v=> run(v.checkImage()))
      } catch {
        case e:Throwable =>
          e.printStackTrace()
      }
      Thread.sleep(5000)
    }
  }

  def sendGiftMessage(str:String)={
    try {
      giftMap.values.foreach(_.send(str))
    } catch {
      case e:Throwable =>
        e.printStackTrace()
    }
  }

}
