# car-online 前端

Vite + React，对接当前 Java 后端已有接口。

需要同时启动：

1. Java：`.\mvnw.cmd compile` 后 `java -cp "target\classes;target\lib\*" com.caronline.App`
2. 前端：

```powershell
cd frontend
npm install
npm run dev
```

浏览器打开 http://localhost:5173

开发时 `/api` 由 Vite 代理到 `http://localhost:8080`，不必改后端地址。
