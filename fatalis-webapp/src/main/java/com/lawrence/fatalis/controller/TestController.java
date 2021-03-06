package com.lawrence.fatalis.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lawrence.fatalis.base.BaseController;
import com.lawrence.fatalis.base.SpringContext;
import com.lawrence.fatalis.config.FatalisProperties;
import com.lawrence.fatalis.constant.ReloadConstant;
import com.lawrence.fatalis.model.Commission;
import com.lawrence.fatalis.rabbitmq.TopicSender1;
import com.lawrence.fatalis.rabbitmq.TopicSender2;
import com.lawrence.fatalis.redis.ClusterOperator;
import com.lawrence.fatalis.redis.RedisOperator;
import com.lawrence.fatalis.service.CommissionService;
import com.lawrence.fatalis.test.TestObj;
import com.lawrence.fatalis.util.*;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

@RestController
@RequestMapping(value = "/test")
public class TestController extends BaseController {

    @Resource
    private CommissionService testService;

    /**
     * 测试修改配置自动加载
     */
    @ApiOperation(value = "测试修改配置自动加载", notes = "测试修改配置自动加载")
    @RequestMapping(value = "/config", method = {RequestMethod.GET, RequestMethod.POST})
    public void config(HttpServletRequest request, HttpServletResponse response) {

        long l1 = System.nanoTime();

        String conf = "";
        String conf1 = "";
        String conf2 = "";
        String conf3 = "";
        try {
            conf = ReloadConstant.getAutoProper().getString("contextUrl");
            conf1 = ReloadConstant.getAutoOriProper().getString("spring.jndi.ignore");
            conf2 = ReloadConstant.getFixedProper().getString("contextUrl");
            conf3 = ReloadConstant.getFixedOriProper().getString("spring.jndi.ignore");
        } catch (Exception e) {
            e.printStackTrace();
        }

        long l2 = System.nanoTime();
        System.out.println("div: " + (l2 - l1));

        JSONObject json = new JSONObject();
        json.put("config", conf);
        json.put("config1", conf1);
        json.put("config2", conf2);
        json.put("config3", conf3);

        LogUtil.info(getClass(), json.toString());

        responseWrite(response, json.toString(), null);
    }

    /**
     * 测试controller层json返回页面
     */
    @ApiOperation(value = "测试controller层json返回页面", notes = "测试controller层json返回页面")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "id", dataType = "String", required = false, value = "对象id", defaultValue = "test")
    })
    @ApiResponses({
            @ApiResponse(code = 404, message = "请求地址错误")
    })
    @RequestMapping(value = "/json", method = {RequestMethod.GET, RequestMethod.POST})
    public JSONObject json(HttpServletRequest request, String id) {
        TestObj to = new TestObj();
        to.setId(id);
        to.setArray(new JSONArray());
        to.setJson(new JSONObject());

        LogUtil.info(getClass(), JSON.toJSONString(to));

        return pubResponseJson(true, "成功", to);
    }

    /**
     * 测试StringUtil和DateUtil
     */
    @ApiOperation(value = "测试StringUtil和DateUtil", notes = "测试StringUtil和DateUtil")
    @RequestMapping(value = "/dateStr", method = {RequestMethod.GET, RequestMethod.POST})
    public JSONObject dateStr(HttpServletRequest request, HttpServletResponse response) {
        JSONObject json = new JSONObject();
        Date date = new Date();
        json.put("年: ", DateUtil.getYear(date));
        json.put("月: ", DateUtil.getMonth(date));
        json.put("日: ", DateUtil.getDay(date));
        json.put("年龄: ", DateUtil.getAge("19880902", "yyyyMMdd"));
        json.put("迟月: ", DateUtil.getMonthLaterDateString("19880902", "yyyyMMdd", 2));
        json.put("null空: ", StringUtil.isNull(null));
        json.put("''空: ", StringUtil.isNull(" "));
        json.put("'null'空: ", StringUtil.isNull("null"));

        return json;
    }

    @Resource
    private FatalisProperties fatalisProperties;

    /**
     * 测试redis和集群cluster
     *
     * @param request
     * @return JSONObject
     */
    @RequestMapping(value = "/redis", method = {RequestMethod.GET, RequestMethod.POST})
    @ApiIgnore
    public JSONObject redis(HttpServletRequest request) {
        TestObj to = new TestObj();

        HttpSession session = request.getSession();
        String id = (String) session.getAttribute("id");
        if (StringUtil.isNull(id)) {
            id = StringUtil.getUUIDStr();

            session.setAttribute("id", id);
        }
        to.setId(id);

        if (fatalisProperties.getRedisClusterOpen()) {
            ClusterOperator clusterOperator = (ClusterOperator) SpringContext.getBean("clusterOperator");
            clusterOperator.setObject("test", to);
            to = clusterOperator.getObject("test", TestObj.class);
        } else {
            RedisOperator redisOperator = (RedisOperator) SpringContext.getBean("redisOperator");
            redisOperator.setObject("test", to);
            to = redisOperator.getObject("test");
        }

        JSONObject json = new JSONObject();
        json.put("test", to);

        return json;
    }

    @Resource
    private CommissionService commissionService;

    /**
     * 测试多数据源读写分离, 主库写
     *
     * @param request
     * @return JSONObject
     */
    @RequestMapping(value = "/add", method = {RequestMethod.GET, RequestMethod.POST})
    @ApiIgnore
    public JSONObject add(HttpServletRequest request) {
        String sid = request.getParameter("sid");
        String config = request.getParameter("config");

        Commission commission = new Commission();
        if (StringUtil.isNull(sid)) {
            sid = StringUtil.getRandomNum(10);
        }
        if (StringUtil.isNull(config)) {
            config = StringUtil.get64RandomStr();
        }
        commission.setSid(sid);
        commission.setConfig(config);
        commissionService.addCommission(commission);


        JSONObject json = new JSONObject();
        json.put("commission", commission);

        return json;
    }

    /**
     * 测试多数据源读写分离, 从库读
     *
     * @param request
     * @return JSONObject
     */
    @RequestMapping(value = "/query", method = {RequestMethod.GET, RequestMethod.POST})
    @ApiIgnore
    public JSONObject query(HttpServletRequest request) {
        String sid = request.getParameter("sid");
        String config = request.getParameter("config");

        List<Commission> list = commissionService.queryCommission();

        JSONObject json = new JSONObject();
        json.put("size", list.size());
        json.put("list", list);

        return json;
    }

    /**
     * 测试多数据源读写分离, 主库读
     *
     * @param request
     * @return JSONObject
     */
    @RequestMapping(value = "/get", method = {RequestMethod.GET, RequestMethod.POST})
    @ApiIgnore
    public JSONObject get(HttpServletRequest request) {
        String sid = request.getParameter("sid");

        long l1 = System.nanoTime();

        Commission com = commissionService.getCommission(sid);

        JSONObject json = new JSONObject();
        json.put("com", com);

        long l2 = System.nanoTime();

        System.out.println(l2 - l1);

        return json;
    }

    /**
     * 测试session共享
     *
     * @param request
     * @return JSONObject
     */
    @RequestMapping(value = "/session", method = {RequestMethod.GET, RequestMethod.POST})
    @ApiIgnore
    public JSONObject session(HttpServletRequest request) {
        HttpSession session = request.getSession();

        String uid = (String) session.getAttribute("uid");
        if (StringUtil.isNull(uid)) {
            uid = StringUtil.getUUIDStr();
        }

        session.setAttribute("uid", uid);

        JSONObject json = new JSONObject();
        json.put("uid", uid);

        return json;
    }

    /**
     * 测试rabbitmq的topic模式
     *
     * @param request
     * @return JSONObject
     */
    @RequestMapping(value = "/mqtopic", method = {RequestMethod.GET, RequestMethod.POST})
    @ApiIgnore
    public JSONObject mqtopic(HttpServletRequest request, String type) {
        TopicSender1 sender1 = (TopicSender1) SpringContext.getBean("topicSender1");
        TopicSender2 sender2 = (TopicSender2) SpringContext.getBean("topicSender2");

        if (StringUtil.isNull(type)) {
            type = "1";
        }

        TestObj to = new TestObj();
        switch (type) {
            case "1":
                to.setId("testid");
                sender1.sendMessage1(to);
                sender2.sendMessage1(to);
                break;
            case "2":
                to.setArray(new JSONArray());
                sender1.sendMessage2(to);
                break;
            case "3":
                Map<String, Object> map = new HashMap<>();
                map.put("mq", "mqmap");
                to.setMap(map);
                sender1.sendMessage3(to);
                break;
            default:
                break;
        }

        return pubResponseJson(true, "Mq消息发送成功", to);
    }

    /**
     * 测试restful请求工具类
     *
     * @param request
     * @return JSONObject
     */
    @RequestMapping(value = "/http/{veriable}", method = {RequestMethod.GET, RequestMethod.POST})
    @ApiIgnore
    public JSONObject test(HttpServletRequest request, @PathVariable String veriable, @RequestBody JSONObject input) {
        String header = request.getHeader("header");
        String uriparam = request.getParameter("uriparam");
        String param = request.getParameter("param");

        JSONObject json = new JSONObject();
        json.put("header", header);
        json.put("param", param);
        json.put("veriable", veriable);
        json.put("urip", uriparam);
        json.put("str", input);

        return json;
    }

    /**
     * 测试cxf调用quartz项目中的webservice服务接口
     *
     * @param request
     * @return JSONObject
     */
    @RequestMapping(value = "/cxf", method = {RequestMethod.GET, RequestMethod.POST})
    @ApiIgnore
    public JSONObject test(HttpServletRequest request) {
        long l1 = System.nanoTime();

        String res = WebServiceUtil.cxfWebService("http://localhost:90/fatalis-quartz/services/QuartzService?wsdl",
                "getConfig", null, "webserviceParam", "enableUpdate");

        long l2 = System.nanoTime();
        System.out.println(l2 - l1);

        JSONObject json = new JSONObject();
        json.put("res", res);

        return json;
    }

    public static void main(String[] args) {
        /*try {
            String cron = "0/10 * * * * ?";
            System.out.println(cron = AESCoder.encrypt(cron, AESCoder.URLPARAM_KEY));
            System.out.println(AESCoder.decrypt(cron, AESCoder.URLPARAM_KEY));
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        Map<String, String> ver = new HashMap<>();
        ver.put("veriable", "veriableParam");
        Map<String, String> headers = new HashMap<>();
        headers.put("header", "requestHeader");
        JSONObject param = new JSONObject();
        param.put("name", "lawrence");
        String res = RestHttpUtil.INSTANCE.restPost("http://localhost/fatalis-webapp/test/http/{veriable}?uriparam=uriParam",
                ver, headers, param, "application/json;charset=UTF-8");
        System.out.println(res);

    }

}
