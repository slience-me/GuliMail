package cn.slienceme.gulimall.auth.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

public class UserRegistVo {

    @NotEmpty(message = "用户名必须提交")
    @Length(min = 6, max = 18, message = "用户名长度必须在6-18之间(后端tip)")
    private String userName;

    @NotEmpty(message = "密码必须提交")
    @Length(min = 6, max = 18, message = "密码长度必须在6-18之间(后端tip)")
    private String password;

    @Pattern(regexp = "^[1]([3-9])[0-9]{9}$", message = "手机号格式不正确(后端tip)")
    @NotEmpty(message = "手机号必须提交(后端tip)")
    private String phone;

    @NotEmpty(message = "验证码必须提交(后端tip)")
    private String code;


    public UserRegistVo() {

    }

    public UserRegistVo(String userName, String password, String phone, String code) {
        this.userName = userName;
        this.password = password;
        this.phone = phone;
        this.code = code;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "UserRegistVo{" +
                "userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", phone='" + phone + '\'' +
                ", code='" + code + '\'' +
                '}';
    }
}
