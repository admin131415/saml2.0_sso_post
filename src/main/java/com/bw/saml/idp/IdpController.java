package com.bw.saml.idp;

import com.bw.saml.cc.pojo.AuthnRequestField;
import com.bw.saml.cc.service.AuthnRequestHandler;
import com.bw.saml.cc.service.SamlResponseGenerator;
import com.bw.saml.constants.Constants;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * @author Xiaosy
 * @date 2017-11-14 14:59
 */
@Controller
@RequestMapping("/idp")
public class IdpController {

    @Autowired
    private AuthnRequestHandler authnRequestHandler;
    @Autowired
    private SamlResponseGenerator samlResponseGenerator;
    @Autowired
    private SamlRequestCache samlRequestCache;
    @GetMapping("/sso")
    public void sso(String SAMLRequest, HttpServletRequest request,HttpServletResponse response) throws Exception {
        System.out.println("samlRequest ============ " + SAMLRequest);
        /**
         * 是否在idp端已登录
         */
        Cookie[]cookies = request.getCookies();
        String cookie_value = null;
        if(cookies != null){
            System.out.println("在idp端已登录");
            for(Cookie cookie:cookies){
                if(Constants.IDP_COOKIE_KEY.equalsIgnoreCase(cookie.getName())){
                    cookie_value = cookie.getValue();
                }
            }
        }
        if(cookie_value != null && Constants.IDP_COOKIE_VALUE.equalsIgnoreCase(cookie_value)){
            //已登录，解析SAMLRequest对象,查找出用户信息
            String email = "test@qq.com";
            AuthnRequestField authnRequestField = authnRequestHandler.handleAuthnRequest(SAMLRequest);
            String result = samlResponseGenerator.generateSamlResponse(email,authnRequestField);
            response.reset();
            PrintWriter printWriter = response.getWriter();
            printWriter.write( samlResponseGenerator.getForm(authnRequestField.getAssertionConsumerServiceUrl(), new Base64().encodeAsString(result.getBytes("utf-8"))));
            printWriter.flush();
            printWriter.close();
            return;
        }else {
        	System.out.println("//重定向到登陆页面");
            //重定向到登陆页面 ?SAMLRequest=" + SAMLRequest
            samlRequestCache.setSAMLRequest(SAMLRequest);
             // response.sendRedirect("/login.html");
            // 设置302状态码
            response.setStatus(302);
            // 设置location响应头
            response.setHeader("location", "../login.html?SAMLRequest=" + SAMLRequest);
            // 注意：一次重定向，向服务器发送两次请求
            System.out.println(response.getHeader("location"));
            return;
        }

    }

    @PostMapping("/auth")
    public LoginResponse login(String username, String password, HttpServletRequest req, HttpServletResponse res) throws Exception {
        LoginResponse loginResponse = new LoginResponse();
        System.out.println("认证密码");
        if ("admin".equals(username) && "admin".equals(password)) {
            String email = "test@qq.com";
            //鉴权通过
            System.out.println("auth pass...鉴权通过");
            AuthnRequestField authnRequestField = authnRequestHandler.handleAuthnRequest(samlRequestCache.getSAMLRequest());
            System.out.println("authnRequestField==============="+authnRequestField);
            String result = samlResponseGenerator.generateSamlResponse(email, authnRequestField);
            res.reset();
            Cookie cookie = new Cookie(Constants.IDP_COOKIE_KEY,Constants.IDP_COOKIE_VALUE);

            cookie.setPath("/");
            res.addCookie(cookie);
            System.out.println("res========================="+res);

            PrintWriter printWriter = res.getWriter();
            //响应转化成string型
            printWriter.write(samlResponseGenerator.getForm(authnRequestField.getAssertionConsumerServiceUrl(), new Base64().encodeAsString(result.getBytes("utf-8"))));
            printWriter.flush();
            printWriter.close();

           // return null;

            return null;
        }
        loginResponse.setCode(1);
        System.out.println("loginResponse============="+loginResponse);
        return loginResponse;
    }
}
