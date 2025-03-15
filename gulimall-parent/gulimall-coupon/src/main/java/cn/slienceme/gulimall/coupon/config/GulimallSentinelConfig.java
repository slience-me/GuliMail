package cn.slienceme.gulimall.coupon.config;

import cn.slienceme.common.exception.BizCodeEnume;
import cn.slienceme.common.utils.R;
import com.alibaba.csp.sentinel.adapter.servlet.callback.UrlBlockHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class GulimallSentinelConfig implements UrlBlockHandler{
    @Override
    public void blocked(HttpServletRequest request, HttpServletResponse response, BlockException ex) throws IOException {
        R r = R.error(BizCodeEnume.TOO_MANY_REQUEST_EXCEPTION.getCode(),BizCodeEnume.TOO_MANY_REQUEST_EXCEPTION.getMsg());
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(JSON.toJSONString(r));
    }
}
