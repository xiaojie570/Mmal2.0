package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.RedisPool;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisPoolUtil;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Created by lenovo on 2018/10/8.
 */

@Controller
@RequestMapping("/user/")
public class UserController {

    @Autowired
    private IUserService iUserService;
    /**
     * 用户登录
     * @param username
     * @param password
     * @param session
     * @return
     */
    @RequestMapping(value="login.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username,
                                      String password,
                                      HttpSession session,
                                      HttpServletRequest httpServletRequest,
                                      HttpServletResponse httpServletResponse) {
        ServerResponse<User> response = iUserService.login(username,password);
        // 如果返回值是正确的，则将用户信息存储在session中
        if(response.isSuccess()) {
            CookieUtil.writeLoginToken(httpServletResponse,session.getId());
            CookieUtil.readLoginToken(httpServletRequest);
            //session.setAttribute(Const.CURRENT_USER,response.getData());
            // Cookie: JSESSIONID=AF3E1E5DF03A8D1F629750BDAC5CA288
            // A0CEAD9BE1AF0C363C9FC0AC504FCC01
            // A0CEAD9BE1AF0C363C9FC0AC504FCC01
            // Cookie: JSESSIONID=A0CEAD9BE1AF0C363C9FC0AC504FCC01
            RedisPoolUtil.setEx(session.getId(), JsonUtil.obj2String(response.getData()), Const.RedisCacheExtime.REDIS_SESSION_EXTIME);

        }
        // 将response返回
        return response;
    }

    @RequestMapping(value="logout.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> login(HttpServletRequest request, HttpServletResponse response) {
        String loginToken = CookieUtil.readLoginToken(request);
        CookieUtil.delLoginToken(request,response);
        RedisPoolUtil.del(loginToken);
        return ServerResponse.createBySuccess();
    }

    @RequestMapping(value="register.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(User user) {

        return  iUserService.register(user);
    }

    @RequestMapping(value="check_valid.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> checkValid(String str,String type) {
        return iUserService.checkValid(str,type);
    }

    /**
     * 获取用户信息
     * @return
     */
    @RequestMapping(value="get_user_info.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpServletRequest request) {
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken))
            return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");

        String userJsonStr = RedisPoolUtil.get(loginToken);

        User user = JsonUtil.String2Obj(userJsonStr,User.class);

        if(user != null)
            return ServerResponse.createBySuccess(user);

        return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
    }

    @RequestMapping(value="forget_get_question.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetGetQuestion(String username) {
        return iUserService.selectQuestion(username);
    }

    // 判断忘记密码的问题是否正确
    @RequestMapping(value="forget_check_answer.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetCheckAnswer(String username,String question,String answer) {
        return iUserService.checkAnswer(username,question,answer);
    }


    // 忘记密码中的重置密码
    @RequestMapping(value="forget_reset_password.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetRestPassword(String username,String passwordNew,String forgetToken) {
        System.out.println(forgetToken);
        return iUserService.forgetRestPassword(username,passwordNew,forgetToken);
    }

    // 登录状态下，重置密码

    /**
     * 因为是在登录状态，所以可以从session中获取用户信息
     * @param passwordOld
     * @param passwordNew
     * @return
     */
    @RequestMapping(value="reset_password.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> resetPassword(HttpServletRequest request, String passwordOld, String passwordNew) {
        // 从sesssion中获取user的信息
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken))
            return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");

        String userJsonStr = RedisPoolUtil.get(loginToken);

        User user = JsonUtil.String2Obj(userJsonStr,User.class);

        // 横向越权判断
        if(user == null)
            return ServerResponse.createByErrorMessage("用户未登录");

        return iUserService.resetPassword(passwordOld,passwordNew,user);
    }

    // 在登录状态下，更新个人信息

    /**
     * 1. 从session中拿到当前用户的信息
     * 2. 将当前用户的id等信息放入到要更新个人信息的user中
     *
     * @param user 新的用户信息
     * @return 返回的是User，将更新的user放到session中，并且要返回给前台
     */
    @RequestMapping(value="update_information.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> update_information(HttpServletRequest request,User user) {
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken))
            return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");

        String userJsonStr = RedisPoolUtil.get(loginToken);

        User currentUser = JsonUtil.String2Obj(userJsonStr,User.class);

        if(currentUser == null)
            return ServerResponse.createByErrorMessage("用户未登录");
        // 防止id变化，通过从session中获取
        user.setId(currentUser.getId());
        user.setAnswer(currentUser.getUsername());

        //
        ServerResponse<User> response = iUserService.updateInformation(user);
        // 如果更新成功，需要将新的用户信息存储到session中
        if(response.isSuccess()) {
            response.getData().setUsername(currentUser.getUsername());
            RedisPoolUtil.setEx(loginToken,JsonUtil.obj2String(response.getData()),Const.RedisCacheExtime.REDIS_SESSION_EXTIME);
        }
        return response;
    }

    // 获取个人的详细信息

    /**
     * 1. 需要从session进行登录的判断，如果调用该接口，但是没有进行登录，需要进行强制的登录
     *
     * @return
     */
    @RequestMapping(value="get_information.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> get_infromation(HttpServletRequest request) {
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken))
            return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");

        String userJsonStr = RedisPoolUtil.get(loginToken);

        User currentUser = JsonUtil.String2Obj(userJsonStr,User.class);
        if(currentUser == null)
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，需要强制登录");
        return iUserService.getInformation(currentUser.getId());
    }

}
