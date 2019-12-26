package tools

import java.util.UUID

import com.aliyuncs.DefaultAcsClient
import com.aliyuncs.green.model.v20161216.{TextAntispamDetectionRequest, TextAntispamDetectionResponse}
import com.aliyuncs.profile.DefaultProfile
import common.Tool._

import scala.collection.convert.WrapAsScala._


class AntiSpam(val aid: String, val akey: String, val endpoint: String) {

  val client = {
    val profile = DefaultProfile.getProfile(endpoint, aid, akey)
    new DefaultAcsClient(profile)
  }

  /**
    * 反垃圾检查
    * @param text 内容
    * @param uid  用户
    * @param time 发帖时间
    * @return
    */
  def checkSpam(text: String, uid: String, time: Long): Boolean = {
    val textAntispamDetectionRequest: TextAntispamDetectionRequest = new TextAntispamDetectionRequest
    //为防中文乱码，务必设置编码类型为utf-8
    textAntispamDetectionRequest.setEncoding("UTF-8")
    textAntispamDetectionRequest.setConnectTimeout(3000)
    textAntispamDetectionRequest.setReadTimeout(4000)
    //设置待检测文本
    val dataItems = List(Map("dataId" -> UUID.randomUUID(), "content" -> text, "postId" -> uid, "postTime" -> time))
    textAntispamDetectionRequest.setDataItems(dataItems.toJson());
    //超时设置
    textAntispamDetectionRequest.setConnectTimeout(4000)
    textAntispamDetectionRequest.setReadTimeout(4000)

    try {
      val textAntispamDetectionResponse = client.getAcsResponse(textAntispamDetectionRequest)
      if ("Success" == textAntispamDetectionResponse.getCode) {
        textAntispamDetectionResponse.getTextAntispamResults.filter(v => null != v.getIsSpam && v.getIsSpam).size > 0
      } else false
    } catch {
      case e: Exception =>
        e.printStackTrace()
        false
    }
  }

}
