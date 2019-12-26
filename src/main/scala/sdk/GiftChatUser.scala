package sdk

import java.net.URI

import common.Cache
import common.Tool._
import db.{LiveRoom, User}
import org.java_websocket.client._
import org.java_websocket.drafts._
import org.java_websocket.handshake.ServerHandshake

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * Created by admin on 4/9/2017.
  */
class GiftChatUser(roomId:String) extends WebSocketClient( new URI(s"ws://${SDKMain.chatroomHost}/api/chatroom/socket/${roomId}?token="+SDKMain.giftUid.encrypt()+"&time="+ (System.currentTimeMillis()/1000).toString.encrypt()),  new Draft_17(),null,15000) {
  connect()
  var lastMessageTime=0l
  val userMap=new mutable.HashMap[String,ListBuffer[List[String]]]()
  val lenMap=new mutable.HashMap[String,Int]()
  var lastInTime=0l
  lazy val roomUserName= new User().queryById(roomId).map(_.name).getOrElse("")

  @Override
  def onOpen(serverHandshake: ServerHandshake): Unit = {
    println("gift open:"+roomId)
  }

  @Override
  def onMessage(s: String): Unit = {
    if (s != "heartbag" || s != "heartbeat") {
      try {
        lastMessageTime=System.currentTimeMillis()/1000
        val js = s.jsonToMap
        val actionType = js("actionType")
        if ("user_send_message" == actionType) { //发送文本消息
        }
        else if ("user_send_gift" == actionType) { //发送了礼物
          val jsBody = js("body").asInstanceOf[Map[String,Any]]
          val userName = jsBody("userName")
          val giftName = jsBody("giftName").toString
          val price = jsBody("price").toString.toInt
          if(price>=SDKMain.giftMessagePrice){
              SDKMain.sendGiftMessage(s"""{"actionType":"user_send_message","body":{"text":"${userName }给主播${roomUserName}赠送礼物${giftName}","msgType":"1","color":"#7CFC00","images":[]}}""")
          }
        }
        else if ("user_exit_room" == actionType) { // ---有人退出直播间
        }
        else if ("user_into_room" == actionType) { //进入直播室
          val curr=System.currentTimeMillis()/1000
          if(lastInTime < curr){
            val jsBody = js("body").asInstanceOf[Map[String,Any]]
            val userCount=jsBody("userCount").toString
            Cache.setCache("LIVE_ROOM_HOSTESS_USER_COUNT"+roomId,userCount,3600)
            lastInTime=System.currentTimeMillis()/1000 + 10
          }
        }
        else if ("user_kick_room" == actionType) { //被T了
        }
        else if ("room_close" == actionType) { //主播闪人了。
          close()
        }
      } catch {
        case e: Exception =>
      }
    }
  }
  @Override
  def onClose(i: Int, s: String, b: Boolean): Unit = {
    println("gift colse:"+roomId)
    SDKMain.spamMap.remove(roomId)
  }
  @Override
  def onError(e: Exception): Unit = {
    e.printStackTrace()
  }

  def isClose={
    if(System.currentTimeMillis() /1000 - lastMessageTime >60){
      close()
      true
    } else getConnection.isClosed
  }


  override def send(text: String) {
    try {
      super.send(text)
    }catch{
      case e:Exception=> close()
    }
  }
}
