package com.lee.wxpaytest.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.lee.wxpaytest.Users;
import com.lee.wxpaytest.utils.CommonUtils;
import com.lee.wxpaytest.utils.HttpUtils;
import com.lee.wxpaytest.utils.IpUtils;
import com.lee.wxpaytest.utils.WXPayUtil;
import com.sun.org.apache.regexp.internal.RE;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@RestController
public class controller {
    @Value("${wxpay.appid}")
    private String appid;
    @Value("${wxpay.appsecret}")
    private String appsecret;
    @Value("${wxpay.mer_id}")
    private String mer_id;
    @Value("${wxpay.key}")
    private String key;
    @Value("${wxpay.backUrl}")
    private String backUrl;
    //下单地址
    private String url="http://www.xdclass.net:8081/pay/unifiedorder";

    @RequestMapping("/save")
    public void add(@RequestParam(value = "video_id",required = true) int video_id,
                      HttpServletRequest request,HttpServletResponse response) throws Exception {
        String ip = IpUtils.getIpAddr(request);
        //保存信息
        Users.objecMap.put("ip","192.168.23.1");
        Users.objecMap.put("userId",1);
        Users.objecMap.put("video_id",video_id);
        Users.objecMap.put("deal",false);
        //统一下单
        SortedMap<String, String> map = new TreeMap<String, String>();
        map.put("appid",appid);
        map.put("mch_id",mer_id);
        map.put("nonce_str",CommonUtils.generateUUID());
        map.put("body","title");
        map.put("out_trade_no",CommonUtils.generateUUID());
        map.put("total_fee","6");
        map.put("spbill_create_ip",ip);
        map.put("notify_url",backUrl);
        map.put("trade_type","NATIVE");
        //sign签名
        String sign = WXPayUtil.createSign(map, key);
        System.out.println(sign);
        map.put("sign",sign);
        String payXml = WXPayUtil.mapToXml(map);
        System.out.println(payXml);
        String back = HttpUtils.doPost(url, payXml, 2000);
        back = new String(back.getBytes("ISO-8859-1"), "UTF-8");
        if(back==null){
          throw new NullPointerException();
        }
        Map<String, String> orderMap = WXPayUtil.xmlToMap(back);
        String code_url=orderMap.get("code_url");
        //生成二维码
        Map<EncodeHintType,Object> hints=new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION,ErrorCorrectionLevel.L);
        hints.put(EncodeHintType.CHARACTER_SET,"UTF-8");
        BitMatrix bitMatrix = new MultiFormatWriter().encode(code_url,BarcodeFormat.QR_CODE,400,400,hints);
        ServletOutputStream outputStream = response.getOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix,"PNG",outputStream);

    }
    @RequestMapping("/back1")
    public Object back(HttpServletRequest request,HttpServletResponse response) throws Exception {
        ServletInputStream in = request.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(in ,"UTF-8"));
        StringBuffer sb=new StringBuffer();
        String temp=null;
        while ((temp=br.readLine())!=null){
            sb.append(temp);
        }
        br.close();
        in.close();
        Map<String,String> result=WXPayUtil.xmlToMap(sb.toString());
        System.out.println(result.toString());
        //获取sortedMap
        SortedMap<String, String> map = WXPayUtil.getSortedMap(result);
        //判断签名是否正确
        boolean flag = WXPayUtil.isCorrectSign(map, key);
        if(flag){
            System.out.println("ok");
            //业务场景
            if(map.get("result_code").equals("SUCCESS")){
               if((boolean)Users.objecMap.get("deal")==false){
                   Users.objecMap.put("deal",true);
               }
            }
        }
        //更改订单状态
        //影响行数 row==1或者row==0 相应微信成功或者失败
        return null;
    }
}
