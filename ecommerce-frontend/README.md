# ecommerce-frontend

前端独立仓库占位说明。当前课程主前端位于 `ecommerce-minimal/frontend`，微服务部署时通过 `docker-compose.microservices.yml` 复用该 Vue3 工程，并把 API 入口指向 `ecommerce-gateway`。

正式拆仓时可将 `ecommerce-minimal/frontend` 内容复制到本目录，保持 `package.json`、`Dockerfile` 和 `nginx.conf` 独立提交。
