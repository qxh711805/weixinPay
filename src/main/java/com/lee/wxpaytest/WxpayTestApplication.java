package com.lee.wxpaytest;

import com.sun.org.apache.regexp.internal.RE;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class WxpayTestApplication {


    public static void main(String[] args) {
        SpringApplication.run(WxpayTestApplication.class, args);
    }
}
