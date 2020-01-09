package com.eastsoft.auth;

import com.alibaba.fastjson.JSONObject;
import com.zmy.sys_common.entity.Result;
import com.zmy.sys_common.feign.LuckyService;
import com.zmy.sys_common.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: zmy
 * Date: 2020/1/8
 * Time: 17:21
 * Description:
 */
@RestController
@RequestMapping("/test")
public class TestController {
    @Autowired
    LuckyService luckyService;
    @PostMapping("/login")
    public JSONObject login(@RequestBody Map<String, String> loginMap ){
        return luckyService.login(loginMap);
    }
    @PostMapping("/profile")
    public JSONObject profile(){
        return luckyService.profile();
    }

    @GetMapping("/info")
    public Result getInfo(){
        return Result.success("成功");
    }
}
