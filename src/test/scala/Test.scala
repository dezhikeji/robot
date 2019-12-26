import java.io.{File, FileInputStream}
import java.util.Date

import akka.actor.{Actor, ActorSystem, Props}
import common.{OtsCache, TimeTool}
import common.Tool._
import db._
import org.apache.http.entity.ByteArrayEntity
import sdk.{ChatUser, SDKMain}
import tools.NetTool

import scala.collection.mutable
import scala.io.Source
import scala.util.Random
import scala.collection.JavaConversions._

/**
  * Created by isaac on 16/4/15.
  */
object Test {



  def main(array: Array[String]): Unit = {
    val users=new User().query("icon != '' and icon is not null")
    println(users.size)
    users.mutile(5)foreach{u=>
      safe {
//        val code = SDKMain.porn.checkImage(u.icon :: Nil).head
//        if (code > 99) {
//          new User(u.id, icon = "").update("id", "icon")
//          println(code+":"+u.id + ":" + u.icon )
//        }
      }
    }
    System.exit(0)

//    println(List(0,1).get(1))

    import org.ansj.splitWord.analysis.BaseAnalysis
    import org.ansj.splitWord.analysis.DicAnalysis
    import org.ansj.splitWord.analysis.IndexAnalysis
    import org.ansj.splitWord.analysis.NlpAnalysis
    import org.ansj.splitWord.analysis.ToAnalysis
//    val str = "洁面仪配合洁面深层清洁毛孔 清洁鼻孔面膜碎觉使劲挤才能出一点点皱纹 脸颊毛孔修复的看不见啦 草莓鼻历史遗留问题没辙 脸和脖子差不多颜色的皮肤才是健康的 长期使用安全健康的比同龄人显小五到十岁 28岁的妹子看看你们的鱼尾纹"
//
//    System.out.println(BaseAnalysis.parse(str))
//    System.out.println(ToAnalysis.parse(str))
//    System.out.println(DicAnalysis.parse(str))
//    System.out.println(IndexAnalysis.parse(str))
//    val time=TimeTool.getTimeValue
//    val result=NlpAnalysis.parse(str).getTerms
//    TimeTool.printTimeValue(time,"first",1)
//    0 to 5 foreach{i=>
//      NlpAnalysis.parse(str).getTerms
//    }
//    TimeTool.printTimeValue(time,"two",1)

//    System.out.println(result.map(_.getName))

//    runTasks
//    val sys=ActorSystem.create("sys")
//    val ta=sys.actorOf(Props(new TActor()), name = "afds")
//    ta ! "dafasf"
//    ta! "exit"

//    val cu=new ChatUser("5c2b07c0fc874c9082770f23de39d212","be08d8cbb4154e0a99214408f0c9ca71")


//    val time=new Date()
//    //    send(s"""{"actionType":"user_send_message","body":{"text":"${time.getMinutes+":"+time.getSeconds}正在执行人工智能鉴黄操作","color":"#dc0606","images":[]}}""")
//    val images = SDKMain.zb.getSnapshot("4cda6938b25b4889a7d5608469b96787", "zb", TimeTool.getUTCTimeStr("-1m".dateExp), TimeTool.getUTCTimeStr(new Date()), 1).map { snap =>
//      "http://" + snap.getOssBucket + "." + snap.getOssEndpoint + "/" + snap.getOssObject
//    }
//    println(images)
//    Thread.sleep(2000)
//    val images=List("http://zblz.oss-cn-hangzhou.aliyuncs.com/screenshots/zb/91fa6c23c72443899b4a7ad33f793367/2008.jpg")
//    val point = if(images.size ==0) -1 else SDKMain.porn.checkImage(images.toList).sum
//    println(point)

  }
}

//class TActor  extends Actor {
//  def receive = {
//    case "exit"=>println("退出")
//      context.stop(self)
//    case a:Any=>
//    println("收到消息:"+a)
//  }
//  override def postStop() = {
//    println("stop")
//  }
//}
