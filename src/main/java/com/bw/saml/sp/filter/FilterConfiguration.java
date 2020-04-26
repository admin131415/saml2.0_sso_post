package com.bw.saml.sp.filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author Xiaosy
 * @date 2017-11-06 10:16
 */
@Component
public class FilterConfiguration {

    @Bean
    public FilterRegistrationBean filterRegistrationBean(){
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(new AccessFilter());
      //  registrationBean.addUrlPatterns("/index.html");
        System.out.println("index过滤器");
        registrationBean.addUrlPatterns("/index");
        return registrationBean;
    }
}
