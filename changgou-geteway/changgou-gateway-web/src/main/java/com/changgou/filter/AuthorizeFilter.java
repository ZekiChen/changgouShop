package com.changgou.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @Description: 全局过滤器：实现用户权限校验（鉴权）
 * @Author:      Zeki
 * @CreateDate:  2020/1/12 0012 下午 5:51
 */
@Component
public class AuthorizeFilter implements GlobalFilter, Ordered {

    //令牌头名字
    private static final String AUTHORIZE_TOKEN = "Authorization";

    /**
     * 全局拦截
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // 如果用户是登录或者是一些不需要做权限认证的请求，则直接放行
        String uri = request.getURI().toString();
        if (!URLFilter.hasAuthorize(uri)) {
            return chain.filter(exchange);
        }

        // 获取用户令牌信息（有可能在请求头 or 请求参数 or Cookie中）
        String token = request.getHeaders().getFirst(AUTHORIZE_TOKEN);

        // 用于判断令牌是否在头文件中，如果不在->将令牌封装到头文件中，再传递给其他微服务（用于Oauth2更细粒度的鉴权）
        boolean hastToken = true;  // 令牌在头文件中

        if (StringUtils.isEmpty(token)) {
            token = request.getQueryParams().getFirst(AUTHORIZE_TOKEN);
            hastToken = false;
        }

        if (StringUtils.isEmpty(token)) {
            HttpCookie httpCookie = request.getCookies().getFirst(AUTHORIZE_TOKEN);
            if (httpCookie != null) {
                token = httpCookie.getValue();
            }
        }

        if (StringUtils.isEmpty(token)) {  // 如果没有令牌，则拦截
            response.setStatusCode(HttpStatus.UNAUTHORIZED);  // 401，没有权限的状态码
            return response.setComplete();  // 响应空数据
        } else {  // 有令牌，判断当前令牌是否有前缀bearer
            if (!hastToken) {  // 请求头中没有
                if (!token.startsWith("bearer ") && !token.startsWith("Bearer ")) {
                    token = "bearer " + token;
                }
                request.mutate().header(AUTHORIZE_TOKEN, token);  // 将令牌封装到头文件中
            }
        }

        // 如果有令牌，校验令牌是否有效
        // try {
        //     JwtUtil.parseJWT(token);  // 之前的校验，不使用，没有用到数字证书的公钥私钥
        // } catch (Exception e) {  // 无效则拦截
        //     response.setStatusCode(HttpStatus.UNAUTHORIZED);  // 401，没有权限的状态码
        //     return response.setComplete();  // 响应空数据
        // }

        return chain.filter(exchange);  // 有效，放行
    }

    /**
     * 排序
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
