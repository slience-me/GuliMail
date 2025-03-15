package cn.slienceme.gulimall.gateway.config;

import cn.slienceme.common.exception.BizCodeEnume;
import cn.slienceme.common.utils.R;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.fastjson.JSON;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
public class SentinelGatewayConfig {

    public SentinelGatewayConfig() {
        GatewayCallbackManager.setBlockHandler(new BlockRequestHandler() {
            // 网关限流了请求 就会调用这个回调
            @Override
            public Mono<ServerResponse> handleRequest(ServerWebExchange serverWebExchange, Throwable throwable) {
                R error = R.error(BizCodeEnume.TOO_MANY_REQUEST_EXCEPTION.getCode(), BizCodeEnume.TOO_MANY_REQUEST_EXCEPTION.getMsg());
                String errJSon = JSON.toJSONString(error);
                // Mono<String> just = Mono.just("");
                Mono<ServerResponse> body = ServerResponse.ok().body(Mono.just(errJSon), String.class);
                return body;
            }
        });
    }
}
