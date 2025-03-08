package cn.slienceme.gulimall.member.vo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class MemberLoginVo {

    private String loginacct;
    private String password;
}
