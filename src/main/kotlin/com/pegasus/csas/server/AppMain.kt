package com.pegasus.csas.server

import com.pegasus.csas.firebase.FirebaseDb
import com.pegasus.csas.persistence.ForgotPasswordStatusDao
import com.pegasus.csas.persistence.ServiceProviderDao
import com.pegasus.csas.persistence.VerificationStatusDao
import com.pegasus.csas.server.apis.*
import com.pegasus.csas.utils.serviceConfig
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import org.jetbrains.exposed.sql.Database


object HTTP {
    val client = HttpClient(Apache)
}

val db = Database.connect(
    serviceConfig.dbPath,
    driver = "org.postgresql.Driver",
    user = serviceConfig.dbUser,
    password = serviceConfig.dbPassword
)
val firebaseDb = FirebaseDb()
val serviceProvidersDao = ServiceProviderDao(firebaseDb)
val verificationStatusDao = VerificationStatusDao(firebaseDb)
val forgotPasswordStatusDao = ForgotPasswordStatusDao(firebaseDb)

fun Application.main() {

//    install(DropwizardMetrics) {
//        val reporter = Slf4jReporter.forRegistry(registry)
//                .outputTo(log)
//                .convertRatesTo(TimeUnit.SECONDS)
//                .convertDurationsTo(TimeUnit.MILLISECONDS)
//                .build()
//        reporter.start(10, TimeUnit.SECONDS)
//    }
    intercept(ApplicationCallPipeline.Call) {
        call.response.header("Access-Control-Allow-Origin", "*")
    }
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }
    install(Locations)
    install(Routing) {
        AuthApi()
        VerificationApi()
        StakeHistoryApi()
        InternalApi()
        AddressApi()
    }

    environment.monitor.subscribe(ApplicationStopping)
    {
        HTTP.client.close()
    }
}
