/**
 * Copyright 2025 json.cn
 */
package cn.slienceme.gulimall.auth.vo;
import lombok.Data;

import java.util.Date;

/**
 * Auto-generated: 2025-03-09 13:18:3
 *
 * @author json.cn (i@json.cn)
 * @website http://www.json.cn/
 */
@Data
public class GiteeVo extends GiteeUserVo {

    private String social_access_token;
    private String social_token_type;
    private long social_expires_in;
    private String social_refresh_token;
    private String social_scope;
    private long social_created_at;
}
