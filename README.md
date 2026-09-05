# car-online

打车学习项目，已具备**基本完整主流程**：注册乘客/司机 → 发单 → 接单 → 行程状态机 → 计费支付 → 评价。  
后端是纯 Java（`HttpServer` + JDBC），前端是 React。按课程版本对照代码学习，每课可打一个 Git tag。

当前进度：**v1～v12 均已落地**。

---

## 运行

JDK 17+，MySQL 已启动。账号在 `src/main/resources/db.properties`（默认 `root` / `root`）。

```powershell
.\mvnw.cmd compile
java -cp "target\classes;target\lib\*" com.caronline.App
```

```powershell
cd frontend
npm install
npm run dev
```

- 接口：http://localhost:8080/
- 前端：http://localhost:5173

改完 Java 后重新编译并重启后端。

---

## 主流程（实施总案例）

1. 乘客页创建一个乘客，记住 `id`。  
2. 司机页注册一名司机并填车牌，记住 `id`。  
3. 行程页发单：填乘客 ID、起点、终点、公里数。  
4. 点中该订单 → 填司机 ID → **接单** → **到达** → **开始** → **结束**。  
5. **看费用**（8 + 2.4×公里）→ **支付** → **评价**。  
6. 非法跳步（例如未到达就结束）会返回明确错误。

PowerShell 一条龙（把 id 换成你库里的）：

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/orders -ContentType "application/json; charset=utf-8" -Body '{"passengerId":1,"origin":"家","destination":"公司","distanceKm":5}'
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/orders/1/accept -ContentType "application/json; charset=utf-8" -Body '{"driverId":1}'
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/orders/1/arrive -ContentType "application/json" -Body '{}'
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/orders/1/start -ContentType "application/json" -Body '{}'
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/orders/1/finish -ContentType "application/json" -Body '{}'
Invoke-RestMethod http://localhost:8080/api/orders/1/fare
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/orders/1/pay -ContentType "application/json" -Body '{}'
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/orders/1/rating -ContentType "application/json; charset=utf-8" -Body '{"stars":5,"comment":"好"}'
```

状态只允许：

```
WAITING_ACCEPT → ACCEPTED → DRIVER_ARRIVED → IN_TRIP → COMPLETED
        ↓
   CANCELLED
```

---

## 课程与版本

学完一课可：`git commit -m "vN: ..." && git tag vN`。

| 版本 | 课程 | 知识点 | 作用 | 落地位置 |
|------|------|--------|------|----------|
| v1 | 类与入口 | `package`、`main`、`private`/`final` | 程序从哪启动、数据怎么封装 | `App.java`、`model/*` |
| v2 | HTTP/JSON | `HttpServer`、GET/POST、统一返回、CORS | 浏览器能调接口 | `http/*`、`handler/*` |
| v3 | JDBC | `Connection`、`PreparedStatement`、`ResultSet`、`try()` | 数据落库、防注入、关连接 | `db/Db.java`、各 Repository |
| v4 | 组合与事务 | `Driver` 持有 `Vehicle`、commit/rollback | 司机和车一起成功或一起失败 | `DriverRepository.insert` |
| v5 | 路径 id | `Paths.readId`、`Optional`、404 | 查一条，不存在不空指针 | `GET /api/passengers/{id}` |
| v6 | Service | Handler 只处理 HTTP；`BizException` | 规则集中，重复手机号在 Service 判断 | `service/*` |
| v7 | 枚举 | `OrderStatus` | 状态是固定集合 | `model/OrderStatus.java` |
| v8 | 发单/取消 | 进行中唯一、待接单可取消 | 一乘客不能同时两单 | `POST /api/orders`、`/cancel` |
| v9 | 接单 | 条件更新 `WHERE status=WAITING_ACCEPT` | 一单只能被一人接走 | `POST /api/orders/{id}/accept` |
| v10 | 状态机 | 当前状态+动作→下一状态 | 禁止跳步 | `arrive`/`start`/`finish` |
| v11 | 计费支付 | `BigDecimal`、支付流水唯一 | 金额准确、不能重复付 | `FareCalculator`、`/pay` |
| v12 | 评价 | 完成且已支付才能评 | 一单一条评价 | `POST /api/orders/{id}/rating` |

---

## 工程结构

```
com.caronline
├── App.java
├── common/BizException.java
├── db/          表访问
├── handler/     HTTP
├── http/        Http、Json、Paths
├── model/       实体与 OrderStatus
└── service/     业务规则与计价
frontend/        概览 / 乘客 / 司机 / 行程
```

---

## 主要接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/health` | 健康检查 |
| GET/POST | `/api/passengers` | 乘客列表 / 注册 |
| GET | `/api/passengers/{id}` | 乘客详情 |
| GET/POST | `/api/drivers` | 司机列表 / 注册绑车 |
| GET | `/api/drivers/{id}` | 司机详情 |
| GET | `/api/orders` | 订单列表，`?status=` 可选 |
| POST | `/api/orders` | 发单 `passengerId,origin,destination,distanceKm` |
| GET | `/api/orders/{id}` | 订单详情 |
| POST | `/api/orders/{id}/cancel` | 取消（仅待接单） |
| POST | `/api/orders/{id}/accept` | 接单 `{"driverId":1}` |
| POST | `/api/orders/{id}/arrive` | 到达上车点 |
| POST | `/api/orders/{id}/start` | 开始行程 |
| POST | `/api/orders/{id}/finish` | 结束并算费 |
| GET | `/api/orders/{id}/fare` | 费用明细 |
| POST | `/api/orders/{id}/pay` | 模拟支付 |
| POST | `/api/orders/{id}/rating` | 评价 `stars,comment` |
| GET | `/api/payments` | 支付流水 |

计价：`费用 = 8 + 2.4 × 公里`，保留两位小数。

---

## 编码约定

1. 不用 Spring / Lombok / MyBatis。  
2. Handler → Service → Repository。  
3. 统一 `{code,message,data}`。  
4. 状态用枚举，金额用 `BigDecimal`。  
5. 学习阶段 JSON 里传 `passengerId` / `driverId`。
