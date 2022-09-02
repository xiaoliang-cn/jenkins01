package com.example.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.http.content.*
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.routing.get
import kotlinx.serialization.Serializable
import java.util.*

fun Application.configureRouting() {
    install(StatusPages) {
        exception<AuthenticationException> { call, cause ->
            call.respond(HttpStatusCode.Unauthorized)
        }
        exception<AuthorizationException> { call, cause ->
            call.respond(HttpStatusCode.Forbidden)
        }
        exception<RequestValidationException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, cause.reasons.joinToString())
        }
    }


    routing {
//        get("/") {

//            val token = JWT.create()
//                .withAudience(audience)
//                .withIssuer(issuer)
//                .withClaim("username", user.username)
//                .withExpiresAt(Date(System.currentTimeMillis() + 60000))
//                .sign(Algorithm.HMAC256(secret))
//            call.respond(hashMapOf("token" to token))
//            call.respondText(token)
//        }
        post("/text") {
            val body = call.receive<String>()
            call.respond(body)
        }
        get<Articles.New> {
            // Show a page with fields for creating a new article ...
            call.respondText("Create a new article")
        }
        get<Articles.Id> { article ->
            // Show an article with id ${article.id} ...
            call.respondText("An article with id ${article.id}", status = HttpStatusCode.OK)
        }
        get<Articles.Id.Edit> { article ->
            // Show a page with fields for editing an article ...
            call.respondText("Edit an article with id ${article.parent.id}", status = HttpStatusCode.OK)
        }
        // Static plugin. Try to access `/static/index.html`
        static("/static") {
            resources("static")
        }
    }
}

@Serializable
@Resource("/articles")
class Articles(val sort: String? = "new") {
    @Serializable
    @Resource("new")
    class New(val parent: Articles = Articles())

    @Serializable
    @Resource("{id}")
    class Id(val parent: Articles = Articles(), val id: Long) {
        @Serializable
        @Resource("edit")
        class Edit(val parent: Id)
    }
}

class AuthenticationException : RuntimeException()
class AuthorizationException : RuntimeException()
