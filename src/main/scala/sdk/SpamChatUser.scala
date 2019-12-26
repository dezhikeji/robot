package sdk

import java.net.URI

import common.Cache
import org.java_websocket.client._
import org.java_websocket.drafts._
import common.Tool._
import db._
import org.ansj.splitWord.analysis.NlpAnalysis
import org.java_websocket.handshake.ServerHandshake
import tools.NetTool
import scala.collection.JavaConversions._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * Created by admin on 4/9/2017.
  */
class SpamChatUser(roomId:String) extends WebSocketClient( new URI(s"ws://${SDKMain.chatroomHost}/api/chatroom/socket/${roomId}?token="+SDKMain.spamUid.encrypt()+"&time="+ (System.currentTimeMillis()/1000).toString.encrypt()),  new Draft_17(),null,15000) {
  connect()
  var lastMessageTime=0l
  val userMap=new mutable.HashMap[String,ListBuffer[List[String]]]()
  val lenMap=new mutable.HashMap[String,Int]()
  var lastInTime=0l

  @Override
  def onOpen(serverHandshake: ServerHandshake): Unit = {
    println("spam open:"+roomId)
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
//          val userName = jsBody("userName")
//          val userId = jsBody("userId").toString
//          val level = jsBody("level").toString.toInt +1
//          Cache.setCache("gift_user"+userId,"1",60 * level )
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
          // 被3个以上的人T 10次封禁
          val jsBody = js("body").asInstanceOf[Map[String, Any]]
          val userId = jsBody("userId").toString
          val actUid = jsBody("actUid").toString
          val user = new User().queryById(userId).dbCheck
          val userGold = new UserGold().queryById(userId).dbCheck
          if (SDKMain.spamCache.filter(v => user.name.contains(v)).size > 0 && user.levelType == 0 && userGold.totalBuy ==0) {
            new User(userId, status = 3).update("id", "status")
            send(s"""{"actionType":"user_send_message","body":{"text":"${user.name.take(2) + "..."}恶意扰乱直播间，已送往西天","msgType":"1","color":"#7CFC00","images":[]}}""")
          } else {
            if (!SDKMain.spamUserMap.contains(userId)) SDKMain.spamUserMap(userId) = new mutable.HashSet[String]()
            SDKMain.spamUserMap(userId).add(actUid)
            SDKMain.spamCount(userId) = SDKMain.spamCount.getOrElse(userId, 0) + 1
            if (SDKMain.spamUserMap(userId).size >= 3 && SDKMain.spamCount(userId) >= 10) {
              if (user.levelType == 0 && userGold.totalBuy ==0) {
                new User(userId, status = 3).update("id", "status")
                send(s"""{"actionType":"user_send_message","body":{"text":"${user.name.take(2) + "..."}恶意扰乱直播间，已送往西天","msgType":"1","color":"#7CFC00","images":[]}}""")
              } else if (user.levelType != 7 && SDKMain.spamCount(userId) >= (10 * user.levelType + 10 + userGold.totalBuy/100)) {
                new User(userId, status = 3).update("id", "status")
                send(s"""{"actionType":"user_send_message","body":{"text":"${user.name.take(2) + "..."}恶意扰乱直播间，已送往西天","msgType":"1","color":"#7CFC00","images":[]}}""")
              }
            }
          }
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
    println("spam colse:"+roomId)
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
