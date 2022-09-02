package com.example

import io.ktor.server.application.*
import com.example.plugins.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.hooks.*
import io.ktor.server.auth.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.get
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import io.ktor.util.reflect.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import java.util.concurrent.atomic.AtomicInteger
import javax.management.openmbean.SimpleType
import kotlin.reflect.KType

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
//    configureSecurity()
    install(SimplePlugin)
    install(DataTransformationBenchmarkPlugin)
    configureSerialization()
    configureTemplating()
    //目前不知道做
//    install(AutoHeadResponse)
    //PartialContent使服务器能够响应带有Range标头的请求并仅发送部分内容。
    //AutoHeadResponse提供了自动响应HEAD每个已GET定义路由的请求的能力。Content-Length这允许客户端应用程序通过读取标头值来确定文件大小。
    //如果您需要发送原始正文负载，请使用call.respondBytes函数。
    install(Resources)
//    configureRouting()
//    install(Authentication) {
//        basic("auth-basic") {
//            realm = "Access to the '/' path"
//            validate { credentials ->
//                if (credentials.name == "jetbrains" && credentials.password == "foobar") {
//                    UserIdPrincipal(credentials.name)
//                } else {
//                    null
//                }
//            }
//        }
//    }
    val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    install(MicrometerMetrics) {
        registry = appMicrometerRegistry
    }
    routing {

        get("/metrics") {
            call.respond(appMicrometerRegistry.scrape())
        }
    }
    routing {
//        authenticate("auth-jwt") {
            get("/") {
                println("init get")
                call.respondText("Hello, ${call.principal<UserIdPrincipal>()?.name}!")

//            }

        }
    }
    install(RequestValidation) {
        validate<String> { bodyText ->
            if (!bodyText.startsWith("Hello"))
                ValidationResult.Invalid("Body text should start with 'Hello'")
            else ValidationResult.Valid
        }
    }
}


val SimplePlugin = createApplicationPlugin(
    "SimplePlugin",
    createConfiguration = ::SimplePluginConfig
) {
    println("SimplePlugin")
    val value = pluginConfig.value1;
    println(value)
    val key = AttributeKey<Long>("time");
    print(value)
    onCall { a->

    }

    onCallReceive { call ->
//        call.application.log.info(call.application.attributes[key].toString())
        println("onCallReceive")
    }
}
val DataTransformationBenchmarkPlugin = createApplicationPlugin(name = "DataTransformationBenchmarkPlugin") {
    val onCallTimeKey = AttributeKey<Long>("onCallTimeKey")
    onCall { call ->
        val onCallTime = System.currentTimeMillis()
        call.attributes.put(onCallTimeKey, onCallTime)
    }

    onCallReceive { call ->
        val onCallTime = call.attributes[onCallTimeKey]
        val onCallReceiveTime = System.currentTimeMillis()
        println("Read body delay (ms): ${onCallReceiveTime - onCallTime}")
    }
}

public class SimplePluginConfig {
    var value1: String = "aaaaa";
    var value2: String = "";
}
