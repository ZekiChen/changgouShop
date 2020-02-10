package com.changgou.oauth.config;

import com.changgou.oauth.util.UserJwt;
import com.changgou.user.feign.UserFeign;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/*****
 * 自定义授权认证类
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    ClientDetailsService clientDetailsService;

    @Autowired
    private UserFeign userFeign;

    /****
     * 自定义授权认证
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // ================================客户端信息认证 start======================================
        //取出身份，如果身份为空说明没有认证
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //没有认证统一采用httpbasic认证，httpbasic中存储了client_id和client_secret，开始认证client_id和client_secret
        if(authentication==null){
            // 查询数据库 oauth_client_details
            ClientDetails clientDetails = clientDetailsService.loadClientByClientId(username);
            if(clientDetails!=null){
                //秘钥
                String clientSecret = clientDetails.getClientSecret();
                //静态方式
                // return new User(
                //         username,
                //         new BCryptPasswordEncoder().encode(clientSecret),
                //         AuthorityUtils.commaSeparatedStringToAuthorityList(""));  // 权限
                //数据库查找方式
                return new User(username,  // 客户端ID
                        clientSecret,      // 客户端秘钥->加密
                        AuthorityUtils.commaSeparatedStringToAuthorityList(""));
            }
        }
        // ================================客户端信息认证 end======================================

        // ================================用户账号密码信息认证 start======================================
        if (StringUtils.isEmpty(username)) {
            return null;
        }

        Result<com.changgou.user.pojo.User> userResult = userFeign.findById(username);
        if (userResult == null || userResult.getData() == null) {
            return null;
        }

        //根据用户名查询用户信息
        // String pwd = new BCryptPasswordEncoder().encode("szitheima");
        String pwd = userResult.getData().getPassword();

        //创建User对象
        String permissions = "user,vip";  // 指定用户的角色权限

        UserJwt userDetails = new UserJwt(username,pwd,AuthorityUtils.commaSeparatedStringToAuthorityList(permissions));
        // ================================用户账号密码信息认证 end======================================

        return userDetails;
    }
}
