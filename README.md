# car-online

打车学习项目。用 **JDK 自带的 HttpServer** 提供 HTTP 接口，**不引入 Spring**。  
每一步都是加接口、用浏览器 / curl / Apifox 验证，顺带学 Java 的类、对象、集合、状态机。

当前进度：**第 1 步已完成（司机 / 车辆接口）**。

---

## 怎么学

1. 看本步的「要做什么」和「学什么」。
2. 对照 Handler 代码，看请求是怎么变成 JSON 返回的。
3. 启动服务，调用本步的接口。
4. 勾完「完成标准」再进入下一步。

Maven 只负责编译和下载 MySQL 驱动。业务代码里用 JDK 的 `java.sql`（JDBC）访问数据库，不引入 Spring。

---

## 运行

JDK 17+。先停掉占用 8080 的旧进程（终端里 `Ctrl + C`），再执行：

```powershell
.\mvnw.cmd compile
java -cp "target\classes;target\lib\*" com.caronline.App
```

`-cp` 里必须带上 `target\lib\*`，否则找不到 MySQL 驱动。也可以一条命令：`.\mvnw.cmd compile exec:java`

启动前确认 MySQL 服务已开。账号密码在 `src/main/resources/db.properties`，当前为 `root` / `root`。

看到 `MySQL 已连接` 和 `服务已启动` 后，浏览器打开：

- http://localhost:8080/ —— 当前接口列表
- http://localhost:8080/api/health —— 健康检查，`data.db` 应为 `"UP"`

### 前端（React）

另开一个终端：

```powershell
cd frontend
npm install
npm run dev
```

打开 http://localhost:5173 ，页面里可以查看健康状态、新增/列出乘客和司机。开发时 Vite 把 `/api` 转到 8080，所以 Java 后端必须同时在跑。

PowerShell 创建乘客：

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/passengers -ContentType "application/json; charset=utf-8" -Body '{"name":"张三","phone":"13800000000"}'
Invoke-RestMethod http://localhost:8080/api/passengers
```

注册司机（必须带车牌 `plate`）：

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/drivers -ContentType "application/json; charset=utf-8" -Body '{"name":"李四","phone":"13900000000","plate":"粤A12345","brand":"比亚迪","color":"白"}'
Invoke-RestMethod http://localhost:8080/api/drivers
```

---

## 工程结构

```
src/main/java/com/caronline/
├── App.java                 # main：先 Db.init()，再启动 HttpServer
├── db/
│   ├── Db.java              # 读配置、拿 Connection、建表
│   ├── PassengerRepository.java
│   └── DriverRepository.java
├── handler/
│   ├── HealthHandler.java
│   ├── PassengerHandler.java
│   └── DriverHandler.java
├── http/
│   ├── Http.java
│   └── Json.java
└── model/
    ├── Passenger.java
    ├── Driver.java          # 持有 Vehicle
    └── Vehicle.java
src/main/resources/
├── db.properties            # JDBC 地址、账号、密码
└── schema.sql               # 表结构对照
frontend/                    # React 管理台（Vite）
```

请求路径：

```
浏览器 / Apifox
    → HttpServer 按路径找到 Handler
    → Handler 里 new 对象、查列表
    → 返回 JSON：{"code":0,"message":"success","data":...}
```

---

## 第 0 步：HTTP 入口与乘客接口（已完成）

**要做什么**

- 用 `HttpServer` 监听 8080
- `GET /api/health` 证明服务活着
- `POST /api/passengers` 创建乘客，`GET /api/passengers` 列出乘客

**学什么**

- `main` 启动后进程一直运行，在等 HTTP 请求
- 路径 + 方法（GET/POST）对应一段处理代码
- JSON 是双方约定的文本格式，不是 Java 语法
- `new Passenger(...)` 得到对象，放进 `List` 后就能被 GET 查到

**已有接口**

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/` | 接口列表 |
| GET | `/api/health` | 健康检查 |
| GET | `/api/passengers` | 乘客列表 |
| POST | `/api/passengers` | 创建乘客，body：`{"name":"张三","phone":"13800000000"}` |

**完成标准**

- [x] 不依赖 Spring，浏览器能打开 `/api/health`
- [x] POST 创建一个乘客后，GET 列表能看到他

---

## 数据库：JDBC 连接 MySQL（已完成）

**要做什么**

- 引入 MySQL 驱动（目前项目唯一的第三方 jar）
- 用 `DriverManager.getConnection` 拿到连接
- 建库 `car_online`、表 `passenger`
- 乘客的增/查从内存 `List` 改成 SQL

**学什么**

- JDBC：`Connection` → `PreparedStatement` → `ResultSet`
- 必须用 `?` 占位符，不要把用户输入拼进 SQL（防注入）
- `try-with-resources` 用完关闭连接
- 对象字段和表的列是对应关系：`Passenger.name` ↔ `passenger.name`

**完成标准**

- [x] `/api/health` 的 `data.db` 为 `UP`
- [x] POST 乘客后重启程序，GET 仍能查到

---

## 第 1 步：司机与车辆接口（已完成）

**要做什么**

- 增加 `Driver`、`Vehicle`
- `POST /api/drivers` 注册司机（姓名、手机号、车牌）
- `GET /api/drivers` 司机列表（能看到绑定的车）

**学什么**

- 一个司机对象里可以持有一个车辆对象（组合）
- 接口路径按资源划分：`/passengers`、`/drivers`
- 两张表一起写入时，用同一条 `Connection` + 事务，避免只写下司机、没写下车

**完成标准**

- [x] POST 司机后 GET 能看到车牌
- [x] 缺少车牌时返回明确错误 JSON

**接口示例**

```
POST /api/drivers
{"name":"李四","phone":"13900000000","plate":"粤A12345","brand":"比亚迪","color":"白"}
```

---

## 第 2 步：按 id 查询

**要做什么**

- `GET /api/passengers/{id}`
- `GET /api/drivers/{id}`
- id 不存在时返回 `code != 0`

**学什么**

- 路径里的 `{id}` 怎么解析
- 为什么需要 id：列表里定位某一个对象
- 循环查找 vs 后面用 Map

**完成标准**

- [ ] 存在的 id 返回对象
- [ ] 不存在的 id 有错误信息，而不是空指针

---

## 第 3 步：拆 Service、做校验

**要做什么**

- Handler 只负责读 HTTP、写 JSON
- `PassengerService` / `DriverService` 负责注册规则
- 同一手机号不能重复注册

**学什么**

- 分层：接口层 vs 业务层
- 校验失败返回 400 和 `message`

**完成标准**

- [ ] 重复手机号 POST 失败
- [ ] Handler 里看不到「怎么判断重复」的循环

---

## 第 4 步：订单对象与状态枚举

**要做什么**

- 枚举：`WAITING_ACCEPT`、`ACCEPTED`、`DRIVER_ARRIVED`、`IN_TRIP`、`COMPLETED`、`CANCELLED`
- 增加 `RideOrder`（还不必开放全部接口，可先 `POST` 创建一个待接单看看）

**学什么**

- `enum` 比字符串状态更安全
- 订单 = 一次打车请求

**完成标准**

- [ ] 能创建一个待接单订单，GET 详情里能看到状态

---

## 第 5 步：发单与取消

**建议接口**

```
POST /api/orders                  发单 {"passengerId","origin","destination"}
POST /api/orders/{id}/cancel      取消
GET  /api/orders                  订单列表
GET  /api/orders/{id}             订单详情
```

**学什么**

- 同一乘客同时只能有一单进行中
- 改对象字段 = 改状态

**完成标准**

- [ ] 有进行中订单时再次发单失败
- [ ] 待接单可取消，状态变为 `CANCELLED`

---

## 第 6 步：接单

**建议接口**

```
GET  /api/orders?status=WAITING_ACCEPT   待接订单
POST /api/orders/{id}/accept             {"driverId":1}
```

**学什么**

- 按状态过滤
- 一单只能被一名司机接走

**完成标准**

- [ ] 接单后详情里有司机信息
- [ ] 同一单第二次接单失败

---

## 第 7 步：行程状态机

**建议接口**

```
POST /api/orders/{id}/arrive    到达上车点
POST /api/orders/{id}/start     开始行程
POST /api/orders/{id}/finish    结束行程
```

```
WAITING_ACCEPT → ACCEPTED → DRIVER_ARRIVED → IN_TRIP → COMPLETED
        ↓
   CANCELLED
```

**学什么**

- 当前状态 + 动作 → 下一状态，禁止跳步
- 非法操作返回明确 `message`

**完成标准**

- [ ] 不能从 `ACCEPTED` 直接 `finish`
- [ ] 按顺序走完后状态为 `COMPLETED`

---

## 第 8 步：计费

**建议接口**

```
GET  /api/orders/{id}/fare      费用明细
POST /api/orders/{id}/pay       模拟支付
```

计价（可改）：起步价 8 元 + 2.4 元/公里。距离可先放在发单 JSON 里。

**学什么**

- 金额用 `BigDecimal`，不用 `double`
- 同一单不能重复支付成功

**完成标准**

- [ ] 同样距离得到同样价格
- [ ] 支付记录能查到

---

## 第 9 步：评价（可选）

```
POST /api/orders/{id}/rating    {"stars":5,"comment":"好"}
```

**完成标准**

- [ ] 未完成不能评价
- [ ] 不能重复评价

---

## 阶段二：Spring Boot（以后）

接口路径可以保持不变，换用 Spring 的 `@RestController` 重写 Handler。那时再学注解、依赖注入。现在先把「请求 → 业务 → JSON」走熟。

---

## 编码约定

1. 不引入 Spring、Lombok、MyBatis。
2. Handler 只处理 HTTP；规则放到 `service`（第 3 步开始拆）。
3. 统一返回：`{"code","message","data"}`。
4. 状态用 `enum`。金额用 `BigDecimal`。
5. 当前用户以后用登录 Token 识别，现在先在 JSON 里传 `passengerId` / `driverId`（学习阶段允许）。

---

## 进度记录

| 步骤 | 内容 | 状态 |
|------|------|------|
| 0 | HttpServer、健康检查、乘客增/查 | 已完成 |
| — | JDBC 连接 MySQL，乘客持久化 | 已完成 |
| 1 | 司机、车辆接口 | 已完成 |
| 2 | 按 id 查询 | 未开始 |
| 3 | Service 拆分、校验 | 未开始 |
| 4 | 订单与状态枚举 | 未开始 |
| 5 | 发单、取消 | 未开始 |
| 6 | 接单 | 未开始 |
| 7 | 行程状态机 | 未开始 |
| 8 | 计费 | 未开始 |
| 9 | 评价（可选） | 未开始 |
