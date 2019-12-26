import com.typesafe.sbt.packager.docker._
import NativePackagerHelper._

name := "ai"

version := "1.7.8"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.14",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4",
  "org.apache.httpcomponents" % "httpmime" % "4.3.5",
  "org.apache.httpcomponents" % "httpclient" % "4.3.5",
  "mysql" % "mysql-connector-java" % "5.1.39",
  "commons-dbutils" % "commons-dbutils" % "1.6",
  "commons-collections" % "commons-collections" % "3.2.2",
  "org.javassist" % "javassist" % "3.20.0-GA",
  "com.alibaba" % "druid" % "1.0.24",
  "javax.mail" % "mail" % "1.4.7",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.6.3",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.3",
  "com.fasterxml.jackson.core" % "jackson-annotations" % "2.6.3",
  "net.jpountz.lz4" % "lz4" % "1.3.0",
  "com.typesafe" % "config" % "1.3.0",
  "io.swagger" % "swagger-annotations" % "1.5.6",
  "com.aliyun.openservices" % "tablestore" % "4.1.0",
  "org.ansj" % "ansj_seg" % "5.1.1",
  "com.aliyun" % "aliyun-java-sdk-cdn" % "2.0.1",
  "com.aliyun" % "aliyun-java-sdk-core" % "3.0.7",
  "org.java-websocket" % "Java-WebSocket" % "1.3.0",
  "com.aliyun" % "aliyun-java-sdk-green" % "2.6.0"
)

val root = (project in file(".")).enablePlugins(DockerPlugin).enablePlugins(JavaAppPackaging)


doc in Compile <<= target.map(_ / "none")


javaOptions in Universal ++= Seq(
  " -Dfile.encoding=utf-8"
)


mainClass in Compile := Some("sdk.SDKMain")

dockerCommands :=Seq(
  Cmd("FROM","livehl/java8"),
  Cmd("WORKDIR","/opt/docker"),
  ExecCmd("copy","opt/docker/", "/opt/docker/"),
  ExecCmd("CMD","bin/"+name.value)
)

packageName in Docker := packageName.value

dockerUpdateLatest  in Docker := true

dockerRepository :=Some("registry.cn-hangzhou.aliyuncs.com/cdhub")
