package com.krypton.cloud.config

import io.netty.handler.codec.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.*
import reactor.core.publisher.Mono

@Service
class CorsFilter : WebFilter {

    override fun filter(ctx: ServerWebExchange?, chain: WebFilterChain?): Mono<Void> {
        return if (ctx != null) {
            ctx.response.headers.add("Access-Control-Allow-Origin", "*")

            ctx.response.headers.add("Access-Control-Allow-Methods", "GET, PUT, POST, DELETE, OPTIONS")

            ctx.response.headers.add("Access-Control-Allow-Headers", "DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Content-Range,Range")

            if (ctx.request.method == HttpMethod.OPTIONS) {
                ctx.response.headers.add("Access-Control-Max-Age", "1728000")

                ctx.response.statusCode = HttpStatus.NO_CONTENT

                Mono.empty()
            } else {
                ctx.response.headers.add("Access-Control-Expose-Headers", "DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Content-Range,Range")

                chain?.filter(ctx) ?: Mono.empty()
            }
        } else {
            chain?.filter(ctx) ?: Mono.empty()
        }
    }
}