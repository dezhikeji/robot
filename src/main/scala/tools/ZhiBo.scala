package tools

import java.util
import java.util.{ArrayList, Date}

import com.aliyuncs.{AcsResponse, DefaultAcsClient, RpcAcsRequest}
import com.aliyuncs.cdn.model.v20141111.DescribeLiveStreamsControlHistoryResponse.LiveStreamControlInfo
import com.aliyuncs.cdn.model.v20141111.DescribeLiveStreamsOnlineListResponse.LiveStreamOnlineInfo
import com.aliyuncs.cdn.model.v20141111.DescribeLiveStreamsPublishListResponse.LiveStreamPublishInfo
import com.aliyuncs.cdn.model.v20141111.{DescribeLiveStreamsPublishListResponse, _}
import com.aliyuncs.cdn.transform.v20141111.DescribeLiveStreamsPublishListResponseUnmarshaller
import com.aliyuncs.http.HttpResponse
import com.aliyuncs.profile.DefaultProfile
import com.aliyuncs.transform.UnmarshallerContext
import tools.DescribeLiveStreamSnapshotInfoListResponse.LiveStreamSnapshotInfo

import scala.collection.convert.WrapAsJava._
import scala.collection.convert.WrapAsScala._
import scala.collection.mutable.ListBuffer


class ZhiBo(val aid:String, val akey:String, val endpoint:String, val host:String){

  val client ={
    val profile = DefaultProfile.getProfile(endpoint,aid,akey)
    new DefaultAcsClient(profile)
  }
  def getOnline(): List[LiveStreamOnlineInfo]={
    try{
      val describeCdnServiceRequest = new DescribeLiveStreamsOnlineListRequest()
      describeCdnServiceRequest.setDomainName(host)
      val httpResponse = client.getAcsResponse(describeCdnServiceRequest)
      httpResponse.getOnlineInfo.toList
    }catch {
      case e:Throwable=> e.printStackTrace()
        Nil
    }
  }

  def getHistory(start:String,end:String): List[LiveStreamPublishInfo]={
    try{
      val describeCdnServiceRequest = new DescribeLiveStreamsPublishListRequest()
      describeCdnServiceRequest.setDomainName(host)
      describeCdnServiceRequest.setStartTime(start)
      describeCdnServiceRequest.setEndTime(end)
      val httpResponse = client.getAcsResponse(describeCdnServiceRequest)
      httpResponse.getPublishInfo.toList
    }catch {
      case e:Throwable=> e.printStackTrace()
        Nil
    }
  }
  def getSnapshot(stream:String,app:String,start:String,end:String,limit:Int=10): List[LiveStreamSnapshotInfo]={
    try{
      val describeLiveStreamSnapshotRequest = new DescribeLiveStreamSnapshotInfoListRequest()
      describeLiveStreamSnapshotRequest.setDomainName(host)
      describeLiveStreamSnapshotRequest.setStartTime(start)
      describeLiveStreamSnapshotRequest.setEndTime(end)
      describeLiveStreamSnapshotRequest.setAppName(app)
      describeLiveStreamSnapshotRequest.setStreamName(stream)
      describeLiveStreamSnapshotRequest.setLimit(limit)
      val httpResponse = client.getAcsResponse(describeLiveStreamSnapshotRequest)
      val ret=httpResponse.getLiveStreamSnapshotInfo
      if(null!=ret)ret.toList else Nil
    }catch {
      case e:Throwable=> e.printStackTrace()
        Nil
    }
  }

}

class DescribeLiveStreamSnapshotInfoListRequest() extends RpcAcsRequest[DescribeLiveStreamSnapshotInfoListResponse]("Cdn", "2014-11-11", "DescribeLiveStreamSnapshotInfo") {
  private var ownerId = 0L
  private var securityToken:String = null
  private var domainName:String = null
  private var startTime:String = null
  private var appName:String = null
  private var endTime:String = null
  private var streamName:String = null
  private var limit = 0

  def getOwnerId: Long = this.ownerId

  def setOwnerId(ownerId: Long): Unit = {
    this.ownerId = ownerId
    putQueryParameter("OwnerId", ownerId)
  }

  override def getSecurityToken: String = this.securityToken

  override def setSecurityToken(securityToken: String): Unit = {
    this.securityToken = securityToken
    putQueryParameter("SecurityToken", securityToken)
  }

  def getDomainName: String = this.domainName

  def setDomainName(domainName: String): Unit = {
    this.domainName = domainName
    putQueryParameter("DomainName", domainName)
  }

  def getStartTime: String = this.startTime

  def setStartTime(startTime: String): Unit = {
    this.startTime = startTime
    putQueryParameter("StartTime", startTime)
  }

  def getAppName: String = this.appName

  def setAppName(appName: String): Unit = {
    this.appName = appName
    putQueryParameter("AppName", appName)
  }

  def getEndTime: String = this.endTime

  def setEndTime(endTime: String): Unit = {
    this.endTime = endTime
    putQueryParameter("EndTime", endTime)
  }
  def getStreamName: String = this.streamName

  def setStreamName(streamName: String): Unit = {
    this.streamName = streamName
    putQueryParameter("StreamName", streamName)
  }
  def getLimit: Int = this.limit

  def setLimit(limit: Int): Unit = {
    this.limit = limit
    putQueryParameter("Limit", limit)
  }

  override def getResponseClass: Class[DescribeLiveStreamSnapshotInfoListResponse] = classOf[DescribeLiveStreamSnapshotInfoListResponse]
}

object DescribeLiveStreamSnapshotInfoListResponse {

  class LiveStreamSnapshotInfo {
    private var ossEndpoint: String = null
    private var ossBucket: String = null
    private var ossObject: String = null
    private var createTime: String = null

    def getOssEndpoint: String = this.ossEndpoint

    def setOssEndpoint(ossEndpoint: String): Unit = {
      this.ossEndpoint = ossEndpoint
    }

    def getOssBucket: String = this.ossBucket

    def setOssBucket(ossBucket: String): Unit = {
      this.ossBucket = ossBucket
    }

    def getOssObject: String = this.ossObject

    def setOssObject(ossObject: String): Unit = {
      this.ossObject = ossObject
    }

    def getCreateTime: String = this.createTime

    def setCreateTime(createTime: String): Unit = {
      this.createTime = createTime
    }
  }

}

class DescribeLiveStreamSnapshotInfoListResponse extends AcsResponse {
  private var requestId: String = null
  private var streamSnapshotInfo: List[DescribeLiveStreamSnapshotInfoListResponse.LiveStreamSnapshotInfo] = Nil

  def getRequestId: String = this.requestId

  def setRequestId(requestId: String): Unit = {
    this.requestId = requestId
  }

  def getLiveStreamSnapshotInfo: util.List[DescribeLiveStreamSnapshotInfoListResponse.LiveStreamSnapshotInfo] = this.streamSnapshotInfo

  def setLiveStreamSnapshotInfo(streamSnapshotInfo:List[DescribeLiveStreamSnapshotInfoListResponse.LiveStreamSnapshotInfo]): Unit = {
    this.streamSnapshotInfo = streamSnapshotInfo
  }

  override def getInstance(context: UnmarshallerContext): DescribeLiveStreamSnapshotInfoListResponse = DescribeStreamSnapshotInfoListResponseUnmarshaller.unmarshall(this, context)
}

object DescribeStreamSnapshotInfoListResponseUnmarshaller {
  def unmarshall(describeStreamSnapshotInfoListResponse: DescribeLiveStreamSnapshotInfoListResponse, context: UnmarshallerContext): DescribeLiveStreamSnapshotInfoListResponse = {
    describeStreamSnapshotInfoListResponse.setRequestId(context.stringValue("DescribeLiveStreamSnapshotInfoListResponse.RequestId"))
    val streamSnapshotInfo =new ListBuffer[DescribeLiveStreamSnapshotInfoListResponse.LiveStreamSnapshotInfo]
    0 until context.lengthValue("DescribeLiveStreamSnapshotInfoListResponse.LiveStreamSnapshotInfoList.Length") map {i=>
      val liveStreamSnapshotInfo = new DescribeLiveStreamSnapshotInfoListResponse.LiveStreamSnapshotInfo
      liveStreamSnapshotInfo.setOssEndpoint(context.stringValue("DescribeLiveStreamSnapshotInfoListResponse.LiveStreamSnapshotInfoList[" + i + "].OssEndpoint"))
      liveStreamSnapshotInfo.setOssBucket(context.stringValue("DescribeLiveStreamSnapshotInfoListResponse.LiveStreamSnapshotInfoList[" + i + "].OssBucket"))
      liveStreamSnapshotInfo.setOssObject(context.stringValue("DescribeLiveStreamSnapshotInfoListResponse.LiveStreamSnapshotInfoList[" + i + "].OssObject"))
      liveStreamSnapshotInfo.setCreateTime(context.stringValue("DescribeLiveStreamSnapshotInfoListResponse.LiveStreamSnapshotInfoList[" + i + "].CreateTime"))
      streamSnapshotInfo.append(liveStreamSnapshotInfo)
    }
    describeStreamSnapshotInfoListResponse.setLiveStreamSnapshotInfo(streamSnapshotInfo.toList)
    describeStreamSnapshotInfoListResponse
  }
}