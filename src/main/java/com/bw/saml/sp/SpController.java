package com.bw.saml.sp;

import com.bw.saml.cc.saml.SAMLSignature;
import com.bw.saml.constants.Constants;
import org.apache.commons.codec.binary.Base64;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;

/**
 * @author Xiaosy
 * @date 2017-12-21 10:54
 */
@Controller
@RequestMapping("/sp")
public class SpController {

    @RequestMapping("/consumer")
    public String consumer(@RequestParam("SAMLResponse") String SAMLResponse, HttpServletRequest request, HttpServletResponse response) throws Exception {
        System.out.println("SAMLResponse=" + SAMLResponse);
        //将SAMLResponse从字符串转化成xml 形式
        byte[]byteResponse = new Base64().decode(SAMLResponse.getBytes("utf-8"));
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteResponse);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(byteArrayInputStream);
        System.out.println("document========="+document);
        Element element = document.getDocumentElement();
        System.out.println("element========="+element);
        SAMLSignature samlSignature = new SAMLSignature();
        //当SP从IDP收到消息时,它使用IDP元数据中定义的签名证书,以验证消息是否是由IDP创建的,
        // 并且在传输过程中未被篡改.
        //数字证书来绑定公钥和公钥所属人。
        if(samlSignature.verifySAMLSignature(element)){
            //签名验证成功
            UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
            Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
            XMLObject responseXmlObj = unmarshaller.unmarshall(element);

            Response responseObj = (Response) responseXmlObj;
            Assertion assertion = responseObj.getAssertions().get(0);
            String subject = assertion.getSubject().getNameID().getValue();
            String issuer = assertion.getIssuer().getValue();
            String audience = assertion.getConditions().getAudienceRestrictions().get(0).getAudiences().get(0).getAudienceURI();
            String statusCode = responseObj.getStatus().getStatusCode().getValue();

            System.out.println("subject=" + subject);
            Cookie cookie = new Cookie(Constants.SP_COOKIE_KEY,Constants.SP_COOKIE_VALUE);
            cookie.setPath("/");
            response.addCookie(cookie);

            System.out.println("到達index");
            // 设置location响应头
      //      response.setHeader("loc", "../index.html");
            // 注意：一次重定向，向服务器发送两次请求
       //     System.out.println(response.getHeader("loc"));

           return "index";
           // response.sendRedirect("/index.html");
        }
        return null;
    }
}
