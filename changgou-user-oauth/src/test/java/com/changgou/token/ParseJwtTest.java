package com.changgou.token;

import org.junit.Test;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;

/*****
 * @Author: www.itheima
 * @Date: 2019/7/7 13:48
 * @Description: com.changgou.token
 *  使用公钥解密令牌数据
 ****/
public class ParseJwtTest {

    /***
     * 校验令牌
     */
    @Test
    public void testParseToken(){
        //令牌
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzY29wZSI6WyJhcHAiXSwibmFtZSI6bnVsbCwiaWQiOm51bGwsImV4cCI6MjAxMTA3ODcwMiwiYXV0aG9yaXRpZXMiOlsidmlwIiwidXNlciJdLCJqdGkiOiJjOGYzMTdiZi0yYzQwLTQyOTYtOGI3Yy1mYTdlNWU0OGJhZmIiLCJjbGllbnRfaWQiOiJjaGFuZ2dvdSIsInVzZXJuYW1lIjoic3ppdGhlaW1hIn0.BqqD1VhUFJfYH6TZkazeHGQcK7erXsItnaCxZY2wf1STncB0Jy9yH2_3wriqUnty-Z4rYJLcBJP24uXjNJ_xzoJwGjzG1Vrt1nORMvpH9LvSUpbWEatZU0rHiJFSkAvmHwko6rL07tai0f7Uz_XQYD4v0AN_j13BBhJ2yAtD9AH0tODX12GfiVtw_aKniIPcvLJPnna69bolu74jWD4t54yA6WxS9rkU5tqMiL4Ij5xEQ-DGE_enkyGSk4D05JTCnG3fRMW8rJ21MfNodbPUpP6_T3mLlS62_8gaH_JeHe1gpexsijNkBxtfHkjMsWmGfQOieHjwk81PCiXB-16xkA";

        //公钥
        String publickey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiVv13I0/nDyzSbXEI8C0xCfGG2OB+MC0yzeMpaMO5Gt21uJ+PwnKS6Ue+m7yGcQz0TCT9kdUCXjEVpQ5DVRigN+LeRA6jsqcqNYj0Wc/z25rTMtW5x+iLAyL7kcC8D5c0J8mEX0v9NjoVNCEPqzKo16bZscdhiz7TqMwHMk1MMrnk9ugKH8Nd73K3miFmXX2ry+AGbMvb3CJkdLL8SHOwcP4KEUpmAAV3YFuOI7ikuZllNx3aoj2lOLNfbOPQrOjeQBBz56T5Ov/3TJREY2ccezW4kPc1ecADLop2whn5vwJv0Bx7izAuCjCdUuhINDqT5qYVSS8INziRHqNdiTZLwIDAQAB-----END PUBLIC KEY-----";

        //校验Jwt
        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier(publickey));

        //获取Jwt原始内容 载荷
        String claims = jwt.getClaims();
        System.out.println(claims);
        //jwt令牌
        String encoded = jwt.getEncoded();
        System.out.println(encoded);
    }
}
