package cn.slienceme.gulimall.auth.vo;


public class UserLoginVo {

    private String loginacct;
    private String password;

    @Override
    public String toString() {
        return "UserLoginVo{" +
                "loginacct='" + loginacct + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    public UserLoginVo() {
    }

    public UserLoginVo(String loginacct, String password) {
        this.loginacct = loginacct;
        this.password = password;
    }

    public String getLoginacct() {
        return loginacct;
    }

    public void setLoginacct(String loginacct) {
        this.loginacct = loginacct;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
