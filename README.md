# miaosha

[TOC]

### 启动

1. 新建一个miaosha数据库，右键输入miaosha.sql。
2. 打开idea，导入项目（注意打开的是pom.xml），修改数据库的配置文件（config.src.main.resources.config.local中）,主要修改datasource-local.yml（数据库的配置）和redis-local.yml（redis的配置），按顺序先启动server，config，gateway，然后启动剩余微服务。
3. 打开前端HBuilderX，新建一个项目，覆盖前端文件夹中的src文件，打开内置终端输入npm run dev。

## 学习笔记

学习网站https://www.bilibili.com/video/BV1kv411y7aD

### 分布式秒杀系统的难点

1. 并发量大
2. 线程安全的问题（防止库存超卖）：如果单纯加锁会导致性能大幅度下滑，甚至导致系统奔溃。
3. 防止提前下单
4. 倒计时的实现（偏前端） 
5. 事务一致性的问题（分布式事务）
6. 分布式限流，请求削峰

### 架构图

![1620572932767](https://github.com/MouMangTai/miaosha/blob/main/img/1.png)

包括路由网关，注册中心，秒杀服务（单独分出来，哪怕扛不住高并发，也不影响其他服务），订单服务，商品服务

请求通过秒杀服务，在商品服务中判断库存（然后减库存，存在线程安全问题），在订单服务中生成订单，（订单服务和商品服务之间存在事务一致性问题)

数据库也要分库分表（单表查询数据比较慢）

消息中间件 - RabbitMQ 进行一些请求削峰的处理等

分布式缓存 - Redis

### 路由网关的作用（Zuul/GateWay）

请求路由，负载均衡，请求过滤（黑名单过滤，流量整形，请求限流）

#### 如果路由网关崩了怎么办

多台路由网关进行集群，然后前面加上负载均衡，但是该负载均衡是属于4层负载-LVS

像Gateway,nginx都是属于应用层的技术（七层技术），而对于七层负载，客户端发请求到服务器，先访问nginx时需要进行三次握手，然后nginx转发请求到集群中的某台服务器也需要三次握手，处理完请求后响应也需要四次挥手。

问题：多次的三次握手，四次挥手，响应数据必须经过负载均衡服务器。

nginx官方表示能抗住5W并发。

如果是LVS（传输层技术4层技术），发送请求无需建立三次握手，直接转给后面的服务器，也就是说每一次请求只有一次三次握手，并且数据直接传输到服务器当中。响应的数据可以通过网络直接传输给客户端，极大的提高效率（因为响应的数据量远远大于请求的数据量）。

LVS可以抗住几十万甚至上百万的并发。

#### 那为什么还要nginx

nginx可以根据请求的内容做定制化的负载，而LVS并不知道请求的内容，只能随意的负载。并且LVS的机器价格比较高。

系统往往是一个LVS负载后面跟着多个nginx负载

#### 如果LVS也崩了

则需要设置LVS备机，来抗住一小段时间的并发量

#### 主机和备机IP地址不同，如何在主机崩了情况下访问备机ip，并且备机如何知道主机崩了

通过KeepAlived：

1. 能够监听主机和备机，主机一崩，通知备机顶上
2. ip漂移技术

### 注册中心的作用（Eureka，Zookeeper，Nacos）

最本质的作用是注册服务

#### A服务如何调用B服务

都先注册在注册中心上，然后A服务在注册中心找到服务B，然后调用

#### 为什么不直接调用

解耦，方便统一管理

思想类似于IOC容器（类和类之间的解耦），而注册中心是为了服务与服务之间的解耦

1. 如果直接调用，需要知道对方的ip地址，而B服务有可能集群，当然可以用nginx进行负载均衡，但是当服务的数量增加，每个服务器都得需要准备一个负载均衡的服务器，负载均衡服务器也需要进行负载均衡，导致成本增加。
2. 假设不集群，A服务要存储多个服务器的ip地址，这多个微服务有需要进行负载均衡。
3. B服务的ip地址发生变化，A服务也需要进行相应的配置更改。
4. 当使用了注册中心，不管有多少微服务都注册到注册中心上，注册中心知道每个地址属于哪个服务，A服务去注册中心找B服务，可以返回B服务下的集群中的多个ip地址，就可以很方便的实现负载均衡，每个服务器不需要管理其他服务器的地址，只需要知道注册中心的地址。

### 数据库设计

```mysql
#创建秒杀数据库
CREATE DATABASE `miaosha` /*!40100 DEFAULT CHARACTER SET utf8 */;

#商品表
CREATE TABLE `goods` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL,
  `info` text,
  `price` decimal(10,2) DEFAULT NULL,
  `save` int(11) NOT NULL,
  `begin_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `end_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#订单表
CREATE TABLE `orders` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `oid` varchar(30) NOT NULL,
  `gid` int(11) NOT NULL,
  `uid` int(11) NOT NULL,
  `gnumber` tinyint(4) NOT NULL DEFAULT '1',
  `all_price` decimal(10,2) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` tinyint(4) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `oid` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#模拟数据
insert into goods values
(null,"小天鹅滚筒洗衣机","性价比高",999.98,1000,"2021-05-10 10:00:00","2021-05-10 12:00:00"),
(null,"格力电频空调","一点只用一度电",2999.98,1000,"2021-05-10 10:00:00","2021-05-10 12:00:00"),
(null,"苹果手机","性价比高",5999.98,1000,"2021-05-10 10:00:00","2021-05-10 12:00:00")
```

### 工程结构

![1620607544363](https://github.com/MouMangTai/miaosha/blob/main/img/2.png)

#### 为什么要单独创建一个微服务工程继承于父工程

父工程的作用是为了统一管理依赖和依赖的版本，对于某些微服务有着许多相同的依赖，就再提取一个微服务工程。

### 配置环境多环境部署

程序必须要经过本地环境，测试环境，生产环境，这些环境的ip地址都不同。

如果是单环境配置的话，改变环境需要改变很多地方的配置。

### 秒杀场次的获取

```java
public static String getSecKillTime(int i){
        //根据当前时间 计算第一场的时间
        Calendar calendar = Calendar.getInstance();
        //获得当前的小时
        int h = calendar.get(Calendar.HOUR_OF_DAY);
        if(h % 2 != 0){
            h = h - 1;
        }

        calendar.set(Calendar.HOUR_OF_DAY,h);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);

        //计算场次
        calendar.add(Calendar.HOUR_OF_DAY,i*2);

        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime());

    }
```

```java
@RequestMapping("/times")
    public ResultData<List<String>> getSecKillTime(){

        List<String> times = new ArrayList<>();

        //计算5个场次的时间
        for (int i = 0; i < 5; i++) {
            String time = DateUtil.getSecKillTime(i);
            times.add(time);
        }
        return new ResultData<List<String>>().setData(times);
    }
```

### 对秒杀场次进行缓存

#### 添加依赖

```xml
<dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-cache</artifactId>
        </dependency>
    </dependencies>
```

#### 配置redis

```yml
spring:
  redis:
    host: 127.0.0.1
    password: 12345
  cache:
    redis:
      #10分钟清空一次缓存
      time-to-live: 600000 
```

#### 添加注解

该注解表示执行下面方法时先从缓存中查找，如果没有则从数据库中查找，并且插入到redis中（名字通过cacheNames和key进行拼接）

```java
@Cacheable(cacheNames = "seckill",key = "'times'+#time")
public List<Goods> getSecGoodsListByTime(String time) {
    System.out.print("查询数据库了");
    QueryWrapper<Goods>queryWrapper = new QueryWrapper<Goods>().eq("begin_time",time);
    List<Goods> goods = goodsMapper.selectList(queryWrapper);
    return goods;
}
```

该注解表示每当执行方法都将缓存中的数据库删除，用于增加商品时使用：防止增加完商品后缓存没有变化，进而查商品总从缓存中查找而找不到新增加的商品。

```java
@CachePut(cacheNames="seckill",key="'XXXXXX'")
```

### 倒计时的实现

#### 第一版

```javascript
export default{
		name : "SecKillTime",
		props:['overTime'],
		data(){
			return {
				h:"00",
				m:"00",
				s:"00"
			}
		},
		methods:{
			djs(){
				var _this =this
				//什么时候结束
				var over = new Date(this.overTime);
				//当前时间
				var now = new Date();
				//转换成毫秒值
				var begin  = now.getTime();
				var end = over.getTime();
				//中间差距的时间，相差多少毫秒
				var times = end-begin;
				
				var hour = parseInt(times/1000/60/60);
				var min = parseInt(times/1000/60%60);
				var sec = parseInt(times/1000%60);
				
				this.h = this.formatTime(hour);
				this.m = this.formatTime(min);
				this.s = this.formatTime(sec);
				
				setTimeout(function(){
					// begin-=1000;
					_this.djs();
				},1000);
				
			},
			formatTime(s){
				return s<10?("0"+s):s;
			}
		},
		watch:{
			overTime(v){
				this.djs();
			}
		}
	}
```

#### 问题

1. 不能以客户端的时间为准：**应该以服务器的时间为准。**
2. 如果服务器集群之后，负载均衡会导致请求分发到不同的集群服务器上，服务器的时间不一致问题：**设置一个时间服务器，所有集群服务器写一个脚本，一段时间去获取时间服务器的时间使得误差尽量减少。**
3. 如果要获取服务器的时间，则前端每一秒都要请求一次服务器，服务器压力过大：**只在第一次获取服务器时间，后面通过nowtime+=1000人为的设置。**

#### 第二版

```javascript
	var now;
	var begin;
	export default {
		name: "SecKillTime",
		props: ['overTime'],
		data() {
			return {
				h: "00",
				m: "00",
				s: "00"
			}
		},
		methods: {
			djs() {
				var _this = this
				//什么时候结束
				var over = new Date(this.overTime);
				//当前时间
				// var now = new Date();
				//转换成毫秒值
				var end = over.getTime();
				//中间差距的时间，相差多少毫秒
				var times = end - begin;

				var hour = parseInt(times / 1000 / 60 / 60);
				var min = parseInt(times / 1000 / 60 % 60);
				var sec = parseInt(times / 1000 % 60);

				this.h = this.formatTime(hour);
				this.m = this.formatTime(min);
				this.s = this.formatTime(sec);

				setTimeout(function() {
					begin += 1000;
					_this.djs();
				}, 1000);

			},
			formatTime(s) {
				return s < 10 ? ("0" + s) : s;
			},
			getServerNow() {
				var _this = this;
				this.utils.ajax({
					url: this.utils.urls.now,
					success: function(data) {
						now = new Date(data);
						begin = now.getTime();
						_this.djs();
					}
				}, this);
			}
		},
		watch: {
			overTime(v) {
				this.getServerNow();
			}
		}
	}
```

#### 切换页面时定时器累加问题

```javascript
//定义一个全局timer
var timer;
//方法中调用setTimeout
timer = setTimeout(function() {
    begin += 1000;
    _this.djs();
}, 1000);
//通过$once来监听定时器，在beforeDestroy钩子可以被清除。
mounted() {
    this.$once('hook:beforeDestroy', () => {
        clearTimeout(timer) // 此处的timer即 上文const的 timer
    })
}
```

### 父子组件

#### 父组件

```javascript
//1. 传递参数和方法
<times :overTime="overTime" :overFunc="overFunc"/>

//2. 导入子组件
import times from "./SecKillTime.vue"
export default {
    name: "SecKillGoods",
    //3. 导入子组件
    components:{
        times
    },
    data() {
        return {
            //4. 传递的数据
            overTime:"",
        }
    },
    methods: {
        //5. 传递的方法（当子组件触发时执行）
        overFunc(){
            this.flag = true;
        }
    }
}
```

#### 子组件

```javascript
export default {
    name: "SecKillTime",
    //1. 通过props获取父组件传来的数据和方法
    props: {
        overTime :{
            type:String
        },
        overFunc:{
            type:Function
        }
    },
    methods: {
        // 省略其他
        djs() {
            if(times <= 0 ){
                //2. 调用父组件方法
                this.overFunc();
            }
        }
    }
}
```

### 抢购功能实现

#### 如何防止提前下单

1. 下单前，查询缓存，获取当前商品开始的秒杀时间，和当前时间做一个对比，如果当前时间再秒杀时间之后，则表示可以开启秒杀（会导致redis中的key急剧增加）
2. 利用redis的HashSet（HashSet的访问速度为O(1)）
   1. redis存放一个字符串类型的nowTime（yyMMddHH）。
   2. 并且存放一系列HashSet：key为secKill_{time}，value为当前time秒杀场次下的商品列表的id。如key:seckill_20210511,value:876，176，25。
   3. 当请求秒杀某个商品876时，先获取nowTime当前场次，然后和secKill_拼接为key，查看是否存在value为876的值，如果在则可以进行抢购。
   4. 还需要一个定时任务（使用Elastic-Job和Quartz）：每两个小时修改redis中的nowTime，并且删除之前场次的集合。

#### 如何防止重复提交

1. 页面生成时，会生成一个隐藏的UUID号，提交请求后，会带着这个UUID，服务器接收到请求，然后服务器将UUID存入redis，如果redis中已经存在，说明当前请求时重复请求，拒绝处理即可。（只能防止误触碰的重复提交，如果有人恶意的通过工具重复发送请求，没办法判断）
2. 提交必须输入验证码（好处：1.防止重复提交 2.防止恶意提交，秒杀器，脚本。3.拉长服务器的请求处理时间  坏处：1.普通验证码容易被破解）

### 秒杀请求限流

当单位时间内请求量很大的时候，拒绝一部分请求的手段。是解决高并发的重要手段之一。

在什么地方限流？-路由网关

#### 根据什么来限流

1. 如果根据请求数量来限流，因为请求是经过路由网关，那么秒杀服务会影响其他微服务。
2. 根据ip地址来限流（单位时间每个ip只能访问几次）或根据用户ID或根据**URL限流**（常用）

#### 限流的实现方案

1. 根据压测算出大概每秒能承受多少次的请求，设置一个maxRequest=最大请求数量和count=0，每当有一个请求过来count就+1，并且每一秒count都重新置为0，当count>maxRequest时就拒绝请求。

   （问题：1. 如果路由网关集群之后，maxRequest会叠加，每一次加一台机器都要重新计算maxRequest。2. 请求不均匀：前一秒内的1W请求都集中在一秒的后半段，后一秒内的1W请求都集中在一秒的前半段，则一秒有2W的服务进入。）

2. 漏桶算法:略

3. 令牌桶算法⭐：路由网关中有个令牌桶数据结构（类似于集合）（将令牌桶放在redis中可以避免路由网关集群后最大令牌数成倍增加导致通过的请求数成倍增加的问题），存有许多令牌，包含当前令牌数和最大的令牌数。有一个功能会按照一定的速率往令牌桶中放令牌。当请求经过路由网关时去令牌桶中申请令牌，获得令牌则通过，没获取则拒绝或等待。

   （好处：1. 是一种滑动窗口的限流。2. 每一个请求可以申请到不同数量的令牌，可以实现不同的业务。） 

   （问题：存在并发多线程的问题，线程安全问题：1. 加锁（影响性能），2. 利用redis单线程机制⭐，让redis查令牌和取令牌同时执行，利用lua脚本，在lua脚本中写上查令牌和取令牌，将lua脚本整个传给redis，可以达到无锁线程安全的目的）

   （根据Url进行限流，在redis中根据不同的url设置多个令牌桶）

![1620785959429](https://github.com/MouMangTai/miaosha/blob/main/img/3.png)

### redis + Lua脚本实现令牌桶算法

#### Lua脚本

因为redis的单线程特性（6.0之后io变成多线程，执行命令仍然是单线程），Lua脚本可以作为一个单元被redis执行，这个执行的过程不会被其他客户端的其他命令所打断。

Lua脚本对于redis来讲具有原子性，在实际开发过程中，往往可以借助Lua脚本原子性的特点，实现无锁化的线程安全。

#### 语法

```lua
eval "redis.call('set','name','xiaoming')" 0

eval "return redis.call('get','name')" 0

eval "redis.call('set','KEYS[1]','ARGV[1]')" 1 name xiaohong

eval "local number = tonumber(ARGV[1]) if number % 2 == 0 then return redis.call('get','name') else return redis.call('get','age') end" 0 18
```

#### 令牌桶的实现

key：Hash - 令牌桶   （key - 需要限流的关键属性）

Hash - 当前剩余令牌，令牌的最大数量，每秒产生多少令牌，下一次可以生产令牌的时间

#### 如何添加令牌（令牌的生成方式）

1. 通过额外的线程来按照一定的速率往令牌桶的添加令牌（redis中有许多令牌桶时需要许多的线程来生成）
2. 当请求申请令牌时附带当前时间，该时间和令牌桶中的更新时间进行对比，算出时间差，根据每秒产生的令牌数和时间差计算出需要产生多少令牌，然后加到当前剩余令牌，并将当前时间更新到令牌桶中。（相比较线程的消耗几乎可以忽略不计）⭐

#### 令牌的预支设置

在高并发的请求下，那些重量级的请求可以一直会得不到令牌

1. 当请求申请令牌大于当前剩余令牌时，进行预支时要根据预支的数量和每秒生产的令牌数计算所需的时间，然后加到下一次可以生产令牌的时间之上，并将令牌数设置为0。
2. 如果再来一个请求申请令牌数也大于当前剩余令牌，并且当前时间还不到下一次生产令牌的时间，然后计算两者差值，返回该差值告诉请求需要等待多少秒才能够预支，然后同样的要根据预支的数量和每秒生产的令牌数计算所需的时间，加到下一次可以生产令牌的时间，以供一下次请求去计算。
3. 当前请求的预支需要下一次请求去等待。

![1620801654436](https://github.com/MouMangTai/miaosha/blob/main/img/4.png)

#### 脚本代码

##### 初始化Lua脚本

```lua
--判断key是否存在，如果不存在就初始化令牌桶
--获得参数key并且用..进行拼接
local key = 'tongKey_'..KEYS[1]

--令牌桶的最大容量
local maxTokens = tonumber(ARGV[1])

--每秒产生的令牌数量
local secTokens = tonumber(ARGV[2])

--计算当前时间（微秒）
local nextTime = tonumber(ARGV[3])


--判断令牌桶是否存在
local result = redis.call('exists',key)
if result == 0 then	 	redis.call('hmset',key,'hasTokens',maxTokens,'maxTokens',maxTokens,'secTokens',secTokens,'nextTime',nextTime)
end
```

##### 令牌桶的领取

```lua
--当前领取的令牌桶的key
local key = 'tongKey_'..KEYS[1]

--获取当前需要领取令牌的数量
local getTokens = tonumber(ARGV[1])

--获取令牌桶中的参数
local hasTokens = tonumber(redis.call('hget',key,'hasTokens'))

--获得最大的令牌数
local maxTokens= tonumber(redis.call('hget',key,'maxTokens'))


--每秒生产的令牌的数量
local secTokens= tonumber(redis.call('hget',key,'secTokens'))

--下一次可以生产令牌的时间（微妙）
local nextTime = tonumber(redis.call('hget',key,'nextTime'))


--当前时间（微妙值）
local nowArray = redis.call('time')
local nowTime = nowArray[1]*1000000 + nowArray[2]

--单个令牌生成的耗时
local singTokenTime = 1000000/secTokens


--获得超时时间
local timeout = tonumber(ARGV[2] or -1)

--判断超时时间
if timeout ~= -1 then
    if timeout < nextTime - nowTime then
        return -1
    end
end



--重新计算令牌
if nowTime > nextTime then
    --计算上一次生成令牌到现在的差时
    local hasTime = nowTime - nextTime
    --可以产生的令牌数
    local createTokens = hasTime/singTokenTime
    --当前总的令牌数
    hasTokens = math.min(hasTokens+createTokens,maxTokens)
    --重新设置下一次可以生成令牌的时间
    nextTime = nowTime
end


--获取令牌

--计算当前能够拿走的令牌
local canGetTokens = math.min(hasTokens,getTokens)
--计算需要预支的令牌数量
local yuzhiTokens = getTokens - canGetTokens
--计算如果预支这些令牌，需要多少时间（微秒）
local yuzhiTime = yuzhiTokens * singTokenTime
--重新设置令牌桶中的值
hasTokens = hasTokens - canGetTokens


--更新令牌桶
redis.call('hmset',key,'hasTokens',hasTokens,'nextTime',nextTime+yuzhiTime)

--返回当前请求需要等待的时间
return nextTime -nowTime
```



### GateWay过滤器

TokenLimitFilter

```java
@Component
public class TokenLimitFilter implements GatewayFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //令牌桶限流 -URL
        //获取当前请求的URL
        ServerHttpRequest request = exchange.getRequest();
        String requestPath = request.getPath().value();

        System.out.println("当前请求的url路径"+requestPath);

        //请求放行
        return chain.filter(exchange);
    }
}
```

TokenLimitFilterFactory

```java
@Component
public class TokenLimitFilterFactory extends AbstractGatewayFilterFactory {
    @Autowired
    private TokenLimitFilter tokenLimitFilter;
    @Override
    public GatewayFilter apply(Object config) {
        return tokenLimitFilter;
    }
    @Override
    public String name(){
        return "TokenLimiter";
    }
}
```

### 请求下单数据一致性的问题（性能？安全/？）

1. 商品服务中商品减少的数量一定要等于订单服务中订单增加的数量。

2. 库存不能为负数即**不能超卖**

单机测试的结果：库存10000件，请求方式1秒1w5个请求

1. 直接下单的普通业务：发生了超卖，前后差距8秒

2. 添加synchronized关键字：没有发生超卖，前后耗时15秒

   问题一：只是本地加锁，分布式环境下不可用，实际开发应该采用分布式锁（redis/zookeeper）

   问题二：需要注意和事务注解（@Transactional）配合使用的问题

   ⭐不加注解为线程安全，加了注解则不为线程安全

   因为@Transactional这个事务管理注解是基于AOP技术实现的，即标识当前方法要用AOP进行代理，代理对象会在调用该方法之前先开启事务，然后加锁执行方法，然后解锁，最后提交/回滚事务。当某个线程执行方法后解了锁却还没提交事务就被打断，则会发生线程安全问题。根据事务的隔离性，当前事务是不能读到其他事务未提交的动作，方法中若将库存减去，其他事务不能看见，所以会导致超卖的情况。

   解决：先加锁在进行事务管理，对整个controller进行加锁，但是性能会很差。

3. 分布式锁：没有发生超卖，前后耗时1分47秒，因为并发比较高的情况下，大量的请求失败了。

4. 数据库锁（用到排他锁）：没有发生超卖，前后耗时11秒，并发比较高，大量的请求失败

   - 表锁（悲观锁）：事务会直接锁表，其他事务不能再对该表进行任何操作

   - 行锁（悲观锁）：事务会锁行，其他事务不能再操作该行，但是可以操作其他行
     	- 共享锁（读锁）：一个事务如果对某一行记录添加了读锁，则其他事务也只能对该行加读锁，不能添加排他锁。
     	- 排他锁（写锁）：一个事务如果对某一行记录添加了写锁，则其他事务不能对该行加读锁和写锁。

   **insert，update，delete语句自带排他锁（锁行），select语句没有任何锁。**

   **如果要给select语句添加共享锁，需要在后面设置lock in share mode**

   **如果要给select语句添加排他锁，需要在后面设置for update**

   注意：

   1. 如果select，insert，update ，delete 的where条件中，没有携带主键或者一个唯一性索引的字段，那么就会自动升级成为表锁。
   2. 如果sql语句中，where条件的id是一个范围，则会锁住范围内的所有行，哪怕这行记录不存在，这种情况称之为间隙锁。

5. ⭐数据库的乐观锁：直接修改时判断库存（无锁），没有发生超卖，前后的耗时6秒，但实际开发中还存在一些问题。

6. ⭐redis的lua脚本：无锁化的操作，没有发生超卖，前后耗时4秒

   用lua脚本去扣减redis，数据库不动，生成的订单也放在redis中，当redis中的库存为0时通过异步的方式线程读取redis中数据到数据库。

#### 如果在同步数据库之前redis崩了怎么办

当请求来了之后，先看redis中库存够不够，如果够先扣减库存，然后返回信息告诉说够，如果秒杀服务接收到该消息，则会将当前秒杀的信息放入MQ消息队列中，然后订单服务和商品服务都来消费MQ中的信息。当秒杀服务将消息放入MQ中之后，就告诉前端抢购完成，这个过程相当于一个异步的操作。

MQ的作用：

1. 分布式事务的保证 - 追求**最终一致性**：保证MQ中的消息（即秒杀服务放进MQ的消息）消费端一定能够收到，基于消息确认机制 + 重试机制 + 补偿机制，不用担心redis崩溃掉。
2. 请求削峰：假设有许多请求发给上游服务（即秒杀服务）则问题不大，因为业务逻辑不是很复杂，但是当这些请求分发给下游（即订单服务和商品服务）则压力会更大，甚至可能会导致崩溃。也可以理解为一种限流，消费端可以限制消费的数量，什么时候消费完才从MQ中拿取信息。会拉长处理时间，以时间换性能。

### 秒杀下单的lua脚本

```lua
--获取下单的商品id
local gid = KEYS[1]
--获取下单数量
local gnumber = tonumber(ARGV[1])

--进行库存的判定
--获取当前商品的库存
local gsave = tonumber(redis.call('get','goods'..gid) or 0)

--判断库存
if gsave < gnumber then
   --库存不足
   return -1;
end

--库存充足，进行库存扣减
local result = redis.call('decrby','goods'..gid,gnumber)

--返回结果，抢购成功
if result > 0 then
   --抢购成功，但是还有库存可以继续
   return 1
   else
   --抢购成功，并且已经没有库存
   return 0
end
```

### MQ有可能引起重复发送

本来一个生成消息，一个消费消息

但是如果引入了消息确认机制+重试+补偿，则可能生成一个消息，消费者会收到多个消息。

可以将消费者设置成一个幂等接口（相同的消息不管调用多少次结果都是一样）进行一个最终一致性的判断。

每个消息带一个唯一标识，消费者接收到消息后从redis中检查是否有该标识，如果有则表示已经消费过了，否则就存入。



### 重点

限流，缓存，MQ，请求削峰，分布式事务

### 后期改进

验证码，排队，积分，物流，评论，收藏，会员，用户

### 有没有在项目中用到过多线程

1. 做秒杀系统的时候，我是通过lua脚本进行减库存，库存数和订单存放在redis中，当库存数减为0或者秒杀结束的时候，会去更新数据库。然后就可以定义一个接口和方法，这个方法就带有@Async这个注解，则调用该方法都会去异步的执行。
2. MQ

### 联系

QQ ：1421311452

微信 ：MouMangTai