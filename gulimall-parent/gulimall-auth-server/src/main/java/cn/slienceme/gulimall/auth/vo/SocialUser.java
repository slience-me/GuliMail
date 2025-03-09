package cn.slienceme.gulimall.auth.vo;

import lombok.Data;


/**
 * 用以封装社交登录认证后换回的令牌等信息
 */
@Data
public class SocialUser {

    /*{
        "access_token": "6589d7ab780af280bbc2896442efeb2d",
        "token_type": "bearer",
        "expires_in": 86400,
        "refresh_token": "0657e413992e553baf01543eb6dc96e96dee9b1e6a21c9d616cbf970518b0bae",
        "scope": "user_info emails",
        "created_at": 1741491134
    }*/
    private String access_token;
    private String token_type;
    private long expires_in;
    private String refresh_token;
    private String scope;
    private long created_at;

}
