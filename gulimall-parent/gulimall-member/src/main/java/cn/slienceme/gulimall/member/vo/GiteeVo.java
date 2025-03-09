package cn.slienceme.gulimall.member.vo;
import lombok.Data;

@Data
public class GiteeVo extends GiteeUserVo {

    private String social_access_token;
    private String social_token_type;
    private long social_expires_in;
    private String social_refresh_token;
    private String social_scope;
    private long social_created_at;
}
