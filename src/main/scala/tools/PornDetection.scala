package tools

import com.aliyuncs.DefaultAcsClient
import com.aliyuncs.green.model.v20161216.ImageDetectionRequest
import com.aliyuncs.profile.DefaultProfile
import sdk.SDKMain

import scala.collection.convert.WrapAsJava._
import scala.collection.convert.WrapAsScala._


class PornDetection(val aid: String, val akey: String, val endpoint: String) {

  val client = {
    val profile = DefaultProfile.getProfile(endpoint, aid, akey)
    new DefaultAcsClient(profile)
  }

  /**
    * 鉴黄操作
    * @param images 图片链接
    * @return
    */
  def checkImage(images:List[String]): List[Float] = {
    val imageDetectionRequest = new ImageDetectionRequest()
    //为防中文乱码，务必设置编码类型为utf-8
    imageDetectionRequest.setAsync(false)
    imageDetectionRequest.setScenes(List("porn"))
    imageDetectionRequest.setConnectTimeout(4000)
    imageDetectionRequest.setReadTimeout(4000)
    imageDetectionRequest.setImageUrls(images)
    SDKMain.synchronized {
      try {
        val imageDetectionResponse = client.getAcsResponse(imageDetectionRequest)
        if ("Success" == imageDetectionResponse.getCode) {
          imageDetectionResponse.getImageResults.map(_.getPornResult.getRate.floatValue()).toList
        } else images.map(v => 0f)
      } catch {
        case e: Exception =>
          e.printStackTrace()
          images.map(v => 0f)
      }
    }
  }

}
