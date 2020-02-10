package entity;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;

/**
 * @Description: Feign执行之前进行拦截
 * @Author:      Zeki
 * @CreateDate:  2020/1/15 0015 下午 8:32
 */
public class FeignInterceptor implements RequestInterceptor {

    /**
     * 获取用户令牌，并将令牌再封装到头文件中
     */
    @Override
    public void apply(RequestTemplate requestTemplate) {
        // 记录了当前用户请求的所有数据，包含请求头和请求参数等
        // 用户当前请求时对应线程的数据，如果开启了Feign的熔断，默认是线程池隔离，会开启新的线程，需要将熔断策略换成信号量隔离，此时不会开启新的线程
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        Enumeration<String> headerNames = requestAttributes.getRequest().getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headKey = headerNames.nextElement();
            String headerValue = requestAttributes.getRequest().getHeader(headKey);
            // 将请求头信息封装到头中，使用Feign调用的时候，会传递给下一个微服务
            requestTemplate.header(headKey, headerValue);
        }
    }
}
