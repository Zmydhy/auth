# 鉴权模块
## 说明
### abc（Role-Based Access Control）权限系统说明  
 ![avatar](C:\Users\Administrator\Pictures\relation.png)   
共维护了十张表，表间关系如图。  
实现了以下基础功能     
![avatar](C:\Users\Administrator\Pictures\relation.png)   

本项目支持类似多租户，多项目接入和鉴权    
一个项目拥有多个用户  
一个项目拥有多个角色  
一个用户可以同时赋予多个角色  
一个角色可以同时赋予多个权限  
平台管理员具有项目，用户，角色，权限的所有权限  
当一个项目接入鉴权系统时，由平台管理员创建项目（同时自动创建了项目管理员账户，并且赋予管理员权限）   
项目管理员具有创建项目用户，创建项目角色，赋予用户角色，赋予项目角色权限，添加项目权限的权限              
项目普通用户操作业务模块的权限   
项目访客具有查看业务列表的权限    

权限目前分为三种：  
菜单  title 排序号 路由地址 是否可见 code= menu:title   
按钮  title  icon  是否可见  code = point:title   
api	title  标识   code =api:title    


使用redis 存储jwt生成的token，key为username  value为token   
Jwt的续签，默认 token的有限期为10分钟，在后五分钟内使用token访问系统，会自动刷新token，返回到response的header中名为reToken，并且更新到redis数据库中，旧token失效。

## 引入  
### 第一步 pom    
```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
    <version>2.1.1.RELEASE</version>
</dependency>
<dependency>
    <groupId>com.zmy</groupId>
    <artifactId>log</artifactId>
    <version>0.0.3</version>
</dependency>
<dependency>
    <groupId>com.zmy</groupId>
    <artifactId>sys-common</artifactId>
    <version>0.1.1</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt</artifactId>
    <version>0.6.0</version>
</dependency>
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.8</version>
    <optional>true</optional>
</dependency>
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>RELEASE</version>
</dependency>
<dependency>
    <groupId>org.mongodb</groupId>
    <artifactId>mongo-java-driver</artifactId>
    <version>3.4.2</version>
</dependency>
```
### 第二步  yml
```
spring:
  redis:
    host: 129.1.19.28
    port: 6379
    database: 8
    timeout: 30000
sys:
  jwt:
    key: lucky
    ttl: 600000
  redis:
    exp: 900
·
map:
  url: http://129.1.244.244:12999
```
### 第三步  
```
@EnableFeignClients(basePackages = "com.zmy")
@SpringBootApplication(exclude = MongoAutoConfiguration.class,scanBasePackages = {"com.zmy","com.eastsoft"})
@AutoConfigurationPackage
@ComponentScan(basePackages = {"com.zmy","com.eastsoft"})
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }

}
```
### 第四步  添加SystemConfig.class
```
@Configuration
public class SystemConfig extends WebMvcConfigurationSupport {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    /**
     * 添加拦截器的配置
     */
    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        //1.添加自定义拦截器
        registry.addInterceptor(jwtInterceptor).
                addPathPatterns("/**").//2.指定拦截器的url地址
                excludePathPatterns("/test/login");//3.指定不拦截的url地址
    }

    /**
     * 修改自定义消息转换器
     * @param converters 消息转换器列表
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        //调用父类的配置
        super.configureMessageConverters(converters);
        //创建fastJson消息转换器
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();

        //升级最新版本需加=============================================================
        List<MediaType> supportedMediaTypes = new ArrayList<>();
        supportedMediaTypes.add(MediaType.APPLICATION_JSON);
        supportedMediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
        supportedMediaTypes.add(MediaType.APPLICATION_ATOM_XML);
        supportedMediaTypes.add(MediaType.APPLICATION_FORM_URLENCODED);
        supportedMediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);
        supportedMediaTypes.add(MediaType.APPLICATION_PDF);
        supportedMediaTypes.add(MediaType.APPLICATION_RSS_XML);
        supportedMediaTypes.add(MediaType.APPLICATION_XHTML_XML);
        supportedMediaTypes.add(MediaType.APPLICATION_XML);
        supportedMediaTypes.add(MediaType.IMAGE_GIF);
        supportedMediaTypes.add(MediaType.IMAGE_JPEG);
        supportedMediaTypes.add(MediaType.IMAGE_PNG);
        supportedMediaTypes.add(MediaType.TEXT_EVENT_STREAM);
        supportedMediaTypes.add(MediaType.TEXT_HTML);
        supportedMediaTypes.add(MediaType.TEXT_MARKDOWN);
        supportedMediaTypes.add(MediaType.TEXT_PLAIN);
        supportedMediaTypes.add(MediaType.TEXT_XML);
        fastConverter.setSupportedMediaTypes(supportedMediaTypes);

        //创建配置类
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        //修改配置返回内容的过滤
        //WriteNullListAsEmpty  ：List字段如果为null,输出为[],而非null
        //WriteNullStringAsEmpty ： 字符类型字段如果为null,输出为"",而非null
        //DisableCircularReferenceDetect ：消除对同一对象循环引用的问题，默认为false（如果不配置有可能会进入死循环）
        //WriteNullBooleanAsFalse：Boolean字段如果为null,输出为false,而非null
        //WriteDateUseDateFormat：格式化date 时区+8
        //WriteMapNullValue：是否输出值为null的字段,默认为false
        fastJsonConfig.setSerializerFeatures(
                SerializerFeature.DisableCircularReferenceDetect,
                SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteDateUseDateFormat
        );
        fastConverter.setFastJsonConfig(fastJsonConfig);
        //将fastjson添加到视图消息转换器列表内
        converters.add(fastConverter);
    }
}
```
### 第六步 使用
```
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
```
