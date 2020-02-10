package com.changgou.filter;

/**
 * @Description: 不需要认证就能访问的路径校验
 * @Author:      Zeki
 * @CreateDate:  2020/1/15 0015 下午 9:59
 */
public class URLFilter {

    private static final String ALL_URL = "/user/login,/api/user/add";  // 不需要拦截的url

    /**
     * 校验当前路径是否需要验证权限，需要true，不需要false
     */
    public static boolean hasAuthorize(String url) {
        String[] urls = ALL_URL.split(",");
        for (String uri : urls) {
            if (url.equalsIgnoreCase(uri)) {
                return false;
            }
        }
        return true;
    }
}
