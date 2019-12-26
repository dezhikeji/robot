package sdk

import java.net.URI
import java.util.Date

import common.{Cache, TimeTool}
import common.Tool._
import db.{LiveRoom, PornInfo, UserActionLog}
import org.java_websocket.client._
import org.java_websocket.drafts._
import org.java_websocket.handshake.ServerHandshake
import tools.NetTool
/**
  * Created by admin on 4/9/2017.
  */
class PornChatUser(roomId: String) extends WebSocketClient(new URI(s"ws://${SDKMain.chatroomHost}/api/chatroom/socket/${roomId}?token=" + SDKMain.pornUid.encrypt()+"&time="+ (System.currentTimeMillis()/1000).toString.encrypt()), new Draft_17(), null, 15000) {
  var checkCount=0
  var maxPoint=0f
  connect()
  var lastMessageTime=0l
  var lastInTime=0l
  var first=true

  @Override
  def onOpen(serverHandshake: ServerHandshake): Unit = {
    println("porn open:"+roomId)
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
    println("porn colse:"+roomId)
    SDKMain.pornMap.remove(roomId)
  }

  @Override
  def onError(e: Exception): Unit = {
    e.printStackTrace()
  }

  override def send(text: String){
    try {
      super.send(text)
    }catch{
      case e:Exception=> close()
    }
  }

  def isClose={
    if(System.currentTimeMillis() /1000 - lastMessageTime >20){
      close()
      true
    } else getConnection.isClosed
  }

  def checkImage(): Unit = {
    this.synchronized {
      checkCount = checkCount + 1
      if(!first) {
        if (maxPoint < 30 && checkCount < 10) return
        if (maxPoint < 50 && checkCount % 4 != 0 && checkCount < 10) return
        if (maxPoint < 70 && checkCount % 2 == 0 && checkCount < 10) return
      }
      if (checkCount < 10) {
        val images = SDKMain.zb.getSnapshot(roomId, "zb", TimeTool.getUTCTimeStr("-9s".dateExp), TimeTool.getUTCTimeStr(new Date()), 1).map { snap =>
          "http://" + snap.getOssBucket + "." + snap.getOssEndpoint + "/" + snap.getOssObject
        }
        val point = if (images.size == 0) -1 else SDKMain.porn.checkImage(images).sum
        if (point > maxPoint) {
          maxPoint = point
        }
        if (point > 99f) {
          new PornInfo(uuid, roomId, new Date().sdate, point * 100 toInt, images.head, new Date()).insertWithId()
        }
        first=false
      } else {
        //    send(s"""{"actionType":"user_send_message","body":{"text":"${time.getMinutes+":"+time.getSeconds}正在执行人工智能鉴黄操作","color":"#dc0606","images":[]}}""")
        println(roomId + ":" + maxPoint)
        val msg = if (maxPoint > 99f) s"检测到非法内容，即将强制关闭直播间" else if (maxPoint > 90) "性感过头！儿童不宜!" else if (maxPoint > 70) "过于诱惑" else if (maxPoint > 30) "似乎有点性感" else if (maxPoint >= 0) "非常健康" else "检测失败"
        println(roomId + ":" + msg)
        send(s"""{"actionType":"user_send_message","body":{"text":"${msg}","msgType":"1","color":"#7CFC00","images":[]}}""")
        if (maxPoint >= 99.2f) {
          NetTool.HttpPost(s"http://${SDKMain.chatroomHost}/api/chatroom/control", null, Map("action" -> "close","type"->"3","token" -> "chatroom".encrypt(), "uid" -> roomId, "room" -> roomId))
          new UserActionLog(uuid, roomId, "DELETE", "/api/hostess/close", "127.0.0.1", new Date()).insertWithId()
          new LiveRoom(uid = roomId, status = 3).update("uid", "status")
        }
        maxPoint = 0f
        checkCount = 0
        first=true
      }
    }
  }
}
