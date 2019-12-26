package sdk

import java.net.URI
import java.util.Date

import common.TimeTool
import common.Tool._
import db.{LiveRoom, UserActionLog}
import org.java_websocket.client._
import org.java_websocket.drafts._
import org.java_websocket.handshake.ServerHandshake
import tools.NetTool

/**
  * Created by admin on 4/9/2017.
  */
class ChatUser(roomId: String,uid:String) extends WebSocketClient(new URI(s"ws://room.qingquzhibo.com/api/chatroom/socket/${roomId}?token=" + uid.encrypt()), new Draft_17(), null, 15000) {
  var count = 0
  connect()

  @Override
  def onOpen(serverHandshake: ServerHandshake): Unit = {
    println("open")
  }

  @Override
  def onMessage(s: String): Unit = {
    println(s)
  }

  @Override
  def onClose(i: Int, s: String, b: Boolean): Unit = {
    println("colse")
  }

  @Override
  def onError(e: Exception): Unit = {
    e.printStackTrace()
  }
}
