package com.emc.poc;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;

import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;


import com.alibaba.fastjson.JSONObject;


public class HttpTool {

    public static String send(String url, Map<String, Object> map, String encoding) {
        RestTemplate restTemplate = new RestTemplate();
        //3.1.X以上版本使用
        //restTemplate.getMessageConverters().add(0, StringHttpMessageConverter.DEFAULT_CHARSET);

        List<HttpMessageConverter<?>> converterList = restTemplate.getMessageConverters();
        HttpMessageConverter<?> converter = new StringHttpMessageConverter();
        converterList.add(0, converter);
        restTemplate.setMessageConverters(converterList);


        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
        headers.add("Accept-Encoding", "gzip,deflate");
        headers.add("Authorization", "Basic cm9vdDplYXN5b3Bz");

        //map 转换为json对象
        JSONObject jsonObj = new JSONObject(map);

        HttpEntity<String> formEntity = new HttpEntity<String>(jsonObj.toString(), headers);

        String result = restTemplate.postForObject(url, formEntity, String.class);
        return result;
    }
}
