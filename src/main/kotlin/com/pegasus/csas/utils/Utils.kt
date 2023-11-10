import java.math.BigDecimal

import com.pegasus.csas.server.model.ServiceProvider
import com.pegasus.csas.server.serviceProvidersDao
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*

suspend fun PipelineContext<Unit, ApplicationCall>.getServiceProvider(): ServiceProvider? {
    val apiKey = call.request.header("X-API-KEY")
    return if (apiKey == null) {
        call.respond(HttpStatusCode.Unauthorized)
        null
    } else {
        val provider = serviceProvidersDao.getByApiKey(apiKey)
        if (provider != null) {
            provider
        } else {
            call.respond(HttpStatusCode.Unauthorized)
            null
        }
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.getAdmin(): String? {
    val apiKey = call.request.header("X-API-KEY")
    return if (apiKey == null) {
        call.respond(HttpStatusCode.Unauthorized)
        null
    } else {
        if (apiKey == "iamgod") {
            "admin_user"
        } else {
            call.respond(HttpStatusCode.Unauthorized)
            null
        }
    }
}

fun Any?.somethingToLong(): Long {
    if (this == null) {
        return 0L
    }
    return when (this) {
        is Long -> this
        is Int -> this.toLong()
        is Double -> this.toLong()
        is String -> this.toLong()
        is BigDecimal -> this.toLong()
        else -> 0L.also { println("Unknown type to convert: $this ${this.javaClass.name}") }
    }
}