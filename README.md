# LingoFlow — AI-powered i+1 Reading Platform (云端 Docker 部署指南)

LingoFlow 是一个基于 AI 的 i+1 智能阅读平台。本项目采用前后端分离架构，且已完成 Docker 容器化配置，**支持克隆项目后在云服务器上一键启动**。

---

## 项目结构

* `LingoFlowBackend/` — 基于 Spring Boot 3 开发的后端服务。
* `LingoFlowFrontend/` — 基于 Vue 3 + Vite 运行的前端 Web 应用。
* `LingoFlowFrontend/nginx.conf` — Nginx 反向代理与前端静态资源托管配置。
* `LingoFlowSQL/` — 数据库初始化 SQL 脚本（首次运行容器时会自动导入）。
* `docker-compose.yml` — Docker 服务编排配置文件。
* `.env` — 环境变量配置文件（用于定义数据库密码及 API Key）。

---

## 云服务器一键启动步骤

### 1. 克隆项目到服务器

登录您的云服务器，进入您想部署这个项目的文件夹，将项目代码克隆下来并进入项目根目录：

```bash
git clone https://github.com/FY0627/UML.git
cd UML
```

### 2. 配置端口（解决云服务器端口冲突）

在部署前，**请先确认您的云服务器是否有端口被占用**。您可以打开 `docker-compose.yml` 修改对外暴露的端口。

在 `docker-compose.yml` 中，端口映射格式为：`"宿主机端口:容器内端口"`。

* **前端访问端口（默认 80）**
  如果云服务器上的 80 端口已被占用（例如运行了其他网页服务），可将 `lingoflow-frontend` 下的 `ports` 映射改为其他空闲端口（例如 `8000`）：

  ```yaml
    lingoflow-frontend:
      ports:
        - "8000:80"  # 将 8000 改为您的空闲端口，冒号右侧的 80 保持不变
  ```

  *修改后，您在浏览器访问的地址将变为：`http://<您的服务器IP>:8000`*
* **后端服务端口（默认 8080）**
  如果 8080 端口被占用，可修改 `lingoflow-backend` 下的 `ports`：

  ```yaml
    lingoflow-backend:
      ports:
        - "8090:8080" # 将 8090 改为您的空闲端口，冒号右侧的 8080 保持不变
  ```

  *(注意：因为容器内部通信使用虚拟网络，Nginx 会自动通过容器名 `http://lingoflow-backend:8080` 访问后端，所以**仅修改冒号左边的宿主机端口，不需要修改 nginx.conf 的配置**)*
* **数据库端口（默认 3306）**
  如果 3306 被占用，可修改 `lingoflow-db` 下的 `ports`（例如改为 `3307`）：

  ```yaml
    lingoflow-db:
      ports:
        - "3307:3306" # 将 3307 改为您的空闲端口，冒号右侧的 3306 保持不变
  ```

### 3. 配置 API 密钥与数据库密码

项目根目录下的 `.env` 文件已配置了默认密钥及密码。如有需要，可直接打开并编辑 `.env`：

* `SPRING_AI_OPENAI_API_KEY`：AI 大模型接口密钥（已经配置好）。
* `MYSQL_ROOT_PASSWORD`：项目数据库容器管理员密码。

### 4. 一键构建并启动

在项目根目录下，直接运行以下命令：

```bash
docker compose up --build -d
```

Docker 将自动下载基础镜像、编译 Java 后端、构建前端包、配置 Nginx，并自动运行 `LingoFlowSQL/sql.sql` 初始化数据库。

---

## Nginx 配置文件说明

如果您需要修改 Nginx 的内部反向代理规则，配置文件位于：`LingoFlowFrontend/nginx.conf`。

其核心配置段如下：

```nginx
server {
    listen 80;               # 容器内部监听端口（对应 docker-compose.yml 冒号右侧的 80）
    server_name localhost;

    location / {
        root /usr/share/nginx/html;
        index index.html index.htm;
        try_files $uri $uri/ /index.html; # 支持 Vue Router 路由跳转
    }

    location /api {
        proxy_pass http://lingoflow-backend:8080; # 将前端 /api 请求反向代理到后端容器
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

* **提示**：若您希望修改 Nginx 容器内的默认监听端口（例如不想让容器内部跑在 80 上），需要同步修改 `nginx.conf` 中的 `listen` 以及 `docker-compose.yml` 中 `lingoflow-frontend` 冒号右侧的端口。但在绝大多数情况下，您**只需要修改 `docker-compose.yml` 冒号左侧的外部映射端口**即可。

---

## 📋 常用维护命令

* **查看服务运行状态**：`docker compose ps`
* **查看实时运行日志**（可查看 AI 调用情况或报错）：
  ```bash
  docker compose logs -f lingoflow-backend
  ```
* **停止并卸载所有容器**：`docker compose down`
* **强制清空数据库内容以重新初始化**（谨慎操作）：
  ```bash
  docker compose down -v
  ```

---

## 🗑️ 完全卸载与清理指南 (答辩后清理)

在答辩结束后，您可以选择将该项目从云服务器上**彻底清理干净**（不留任何残留数据或镜像占空间），请在项目根目录下按照以下步骤操作：

### 1. 停止服务并清理 Docker 资源

根据您服务器上是否运行了其他 Docker 项目，选择以下**其中一种**清理方式：

* **情况 A：服务器上没有其他 Docker 项目（全新服务器，推荐）**
  直接运行以下命令，不仅删除容器和数据库，还会把下载的 MySQL/Nginx 公共镜像一并删除，最彻底地释放磁盘空间：
  ```bash
  docker compose down -v --rmi all
  ```
* **情况 B：服务器上运行了其他 Docker 项目（共享服务器）**
  运行以下命令，只会清除本项目的自定义镜像，**绝不会**影响其他项目：
  ```bash
  docker compose down -v --rmi local
  ```

> 🔒 **容器隔离与安全性说明**：
>
> 1. **数据库容器完全独立**：即使在共享服务器（情况 B）下，当前命令也**只会删除属于本项目的数据库容器（`lingoflow-db`）**，绝不会影响或删除您其他项目里的任何数据库容器。它们是通过不同的容器名和项目名称彻底隔离运行的。
> 2. **`-v` 参数的安全范围**：只会彻底删除当前项目挂载的本地数据卷（`db_data`），清理当前项目的数据库数据，不影响其他项目的 Volume。
> 3. **`--rmi local` 的安全范围**：使用 `local` 而非 `all` 可以确保**绝对安全**，它只删除由我们自己编译的本地前端和后端镜像，而不会删除公共的官方镜像（如 `mysql:8.0` 和 `nginx:alpine`），避免其他项目也使用到这些基础镜像而受到影响。

### 2. 清理未使用的构建缓存 (可选)

如果您希望释放构建时产生的 Docker 临时缓存，可安全运行：

```bash
docker builder prune -f
```

### 3. 删除项目源码文件

返回上一级目录，并直接删除整个项目源码文件夹：

```bash
cd ..
rm -rf UML
```

只需以上三步，即可将项目的所有端口占用、容器、镜像、数据文件在云服务器上清理得一干二净。

---

> 💡 **部署提醒**：请确保您的云服务器后台（如阿里云的安全组规则、腾讯云的防火墙）已开放您在 `docker-compose.yml` 中映射出来的外部端口（如默认的 `80`、`8080` 等），否则外部浏览器将无法访问。
