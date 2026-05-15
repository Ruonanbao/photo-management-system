# 照片管理系统 - 需求与设计文档

## 1. 项目概述

### 1.1 项目名称
照片管理系统（Photo Management System）

### 1.2 项目目标
开发一个功能完善的本地部署照片管理Web应用，支持用户登录、权限验证、多文件夹管理、图片卡片式展示，并能够从照片中抽取人像、时间、地点信息进行智能分组展示。

### 1.3 参考项目
- iPhone相册应用
- Immich开源项目（https://github.com/immich-app/immich）

---

## 2. 功能需求

### 2.1 用户管理模块

#### 2.1.1 用户注册与登录
- 支持用户名/密码注册
- 支持用户名/密码登录
- 登录状态保持（Session/JWT）
- 登出功能

#### 2.1.2 权限管理
- 普通用户：查看、上传、管理自己的照片和相册
- 管理员：管理所有用户、照片和系统设置
- 权限验证中间件

### 2.2 照片管理模块

#### 2.2.1 照片上传
- 支持批量上传
- 支持拖拽上传
- 支持常见图片格式（JPG, PNG, HEIC, WebP等）
- 上传进度显示
- 照片元数据提取（EXIF信息：拍摄时间、GPS位置、相机型号等）

#### 2.2.2 照片展示
- 卡片式瀑布流布局
- 缩略图懒加载
- 照片详情查看（大图预览）
- 照片信息展示（拍摄时间、地点、设备信息）

#### 2.2.3 照片操作
- 下载原图
- 删除照片
- 移动照片到其他相册
- 照片收藏/标记

### 2.3 相册管理模块

#### 2.3.1 相册CRUD
- 创建相册
- 编辑相册名称、描述
- 删除相册
- 相册封面设置

#### 2.3.2 相册展示
- 相册列表展示（缩略图预览）
- 相册内照片展示
- 相册按时间排序

### 2.4 智能分组模块

#### 2.4.1 时间分组
- 按年/月/日自动分组
- 时间线视图

#### 2.4.2 地点分组
- 基于GPS信息自动分组
- 地图展示照片位置
- 按城市/国家分组

#### 2.4.3 人像分组
- 人脸检测
- 人脸聚类分组
- 人像标签管理

### 2.5 搜索功能
- 按时间范围搜索
- 按地点搜索
- 按标签搜索
- 按人物搜索

---

## 3. 技术架构

### 3.1 整体架构
```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   Frontend      │────▶│   Backend API   │────▶│    Database     │
│  (Vue/React)    │     │  (SpringBoot)   │     │   (PostgreSQL)  │
└─────────────────┘     └─────────────────┘     └─────────────────┘
                               │                       │
                               ▼                       ▼
                        ┌─────────────────┐     ┌─────────────────┐
                        │  File Storage   │     │   Cache (Redis) │
                        │  (Local/MinIO)  │     └─────────────────┘
                        └─────────────────┘
```

### 3.2 技术栈选型

#### 3.2.1 前端技术
- 框架：Vue 3 + TypeScript 或 React + TypeScript
- UI组件库：Element Plus / Ant Design
- 状态管理：Pinia / Redux
- 构建工具：Vite
- HTTP客户端：Axios

#### 3.2.2 后端技术
- 框架：Spring Boot 3.x
- 安全框架：Spring Security + JWT
- ORM：MyBatis-Plus
- API文档：Swagger/OpenAPI 3.0
- 文件处理：Apache Commons Imaging / metadata-extractor
- 人脸检测：OpenCV / DeepFace

#### 3.2.3 数据库
- 主数据库：PostgreSQL 14+
- 缓存：Redis 7+

#### 3.2.4 存储
- 照片存储：本地文件系统（可扩展至MinIO）

---

## 4. 数据库设计

### 4.1 用户表 (sys_user)
```sql
CREATE TABLE sys_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50),
    email VARCHAR(100),
    avatar VARCHAR(255),
    role VARCHAR(20) DEFAULT 'USER',
    status SMALLINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 4.2 相册表 (pm_album)
```sql
CREATE TABLE pm_album (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES sys_user(id),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    cover_photo_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 4.3 照片表 (pm_photo)
```sql
CREATE TABLE pm_photo (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES sys_user(id),
    filename VARCHAR(255) NOT NULL,
    original_name VARCHAR(255),
    file_path VARCHAR(500) NOT NULL,
    thumbnail_path VARCHAR(500),
    file_size BIGINT,
    mime_type VARCHAR(50),
    width INTEGER,
    height INTEGER,
    shot_at TIMESTAMP,
    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    location_name VARCHAR(255),
    camera_make VARCHAR(100),
    camera_model VARCHAR(100),
    is_favorite BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 4.4 相册-照片关联表 (pm_album_photo)
```sql
CREATE TABLE pm_album_photo (
    id BIGSERIAL PRIMARY KEY,
    album_id BIGINT NOT NULL REFERENCES pm_album(id),
    photo_id BIGINT NOT NULL REFERENCES pm_photo(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(album_id, photo_id)
);
```

### 4.5 人脸表 (pm_face)
```sql
CREATE TABLE pm_face (
    id BIGSERIAL PRIMARY KEY,
    photo_id BIGINT NOT NULL REFERENCES pm_photo(id),
    feature_vector FLOAT8[],
    bbox_x FLOAT,
    bbox_y FLOAT,
    bbox_width FLOAT,
    bbox_height FLOAT,
    person_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 4.6 人物表 (pm_person)
```sql
CREATE TABLE pm_person (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES sys_user(id),
    name VARCHAR(100),
    cover_face_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 5. API接口设计

### 5.1 接口规范
- 基础路径：`/api/v1`
- 认证方式：JWT Token（Header: Authorization: Bearer {token}）
- 响应格式：
```json
{
    "code": 200,
    "message": "success",
    "data": {}
}
```

### 5.2 用户接口

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/v1/auth/register | 用户注册 |
| POST | /api/v1/auth/login | 用户登录 |
| POST | /api/v1/auth/logout | 用户登出 |
| GET | /api/v1/user/profile | 获取当前用户信息 |
| PUT | /api/v1/user/profile | 更新用户信息 |

### 5.3 相册接口

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/v1/albums | 获取相册列表 |
| POST | /api/v1/albums | 创建相册 |
| GET | /api/v1/albums/{id} | 获取相册详情 |
| PUT | /api/v1/albums/{id} | 更新相册 |
| DELETE | /api/v1/albums/{id} | 删除相册 |
| GET | /api/v1/albums/{id}/photos | 获取相册内照片 |
| POST | /api/v1/albums/{id}/photos | 添加照片到相册 |
| DELETE | /api/v1/albums/{id}/photos/{photoId} | 从相册移除照片 |

### 5.4 照片接口

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/v1/photos | 获取照片列表 |
| POST | /api/v1/photos/upload | 上传照片 |
| GET | /api/v1/photos/{id} | 获取照片详情 |
| DELETE | /api/v1/photos/{id} | 删除照片 |
| PUT | /api/v1/photos/{id}/favorite | 收藏/取消收藏 |
| GET | /api/v1/photos/{id}/download | 下载原图 |
| GET | /api/v1/photos/timeline | 时间线照片 |
| GET | /api/v1/photos/locations | 地点分组照片 |
| GET | /api/v1/photos/people | 人物分组照片 |

### 5.5 人物接口

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/v1/persons | 获取人物列表 |
| GET | /api/v1/persons/{id} | 获取人物详情 |
| PUT | /api/v1/persons/{id} | 更新人物名称 |
| DELETE | /api/v1/persons/{id} | 删除人物 |
| GET | /api/v1/persons/{id}/photos | 获取人物照片 |

---

## 6. 项目结构

### 6.1 后端项目结构
```
backend/
├── src/main/java/com/example/photomanagementsystem/
│   ├── common/                    # 公共模块
│   │   ├── config/               # 配置类
│   │   ├── exception/            # 异常处理
│   │   ├── result/               # 统一响应
│   │   └── utils/                # 工具类
│   ├── module/
│   │   ├── auth/                 # 认证模块
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   └── dto/
│   │   ├── user/                 # 用户模块
│   │   ├── album/                # 相册模块
│   │   ├── photo/                # 照片模块
│   │   ├── person/               # 人物模块
│   │   └── file/                 # 文件存储模块
│   └── PhotoManagementSystemApplication.java
├── src/main/resources/
│   ├── application.yml
│   └── mapper/                   # MyBatis映射文件
└── pom.xml
```

### 6.2 前端项目结构
```
frontend/
├── src/
│   ├── api/                      # API接口
│   ├── assets/                   # 静态资源
│   ├── components/               # 公共组件
│   ├── views/                    # 页面视图
│   │   ├── auth/                 # 登录注册
│   │   ├── home/                 # 首页
│   │   ├── album/                # 相册
│   │   ├── photo/                # 照片
│   │   └── person/               # 人物
│   ├── store/                    # 状态管理
│   ├── router/                   # 路由配置
│   ├── utils/                    # 工具函数
│   ├── App.vue
│   └── main.ts
├── public/
├── package.json
└── vite.config.ts
```

---

## 7. 开发规范

### 7.1 代码规范
- 遵循《阿里巴巴Java开发手册》
- 前端遵循ESLint + Prettier规范

### 7.2 Git规范
- 分支管理：
  - main：主分支
  - develop：开发分支
  - feature/*：功能分支
  - hotfix/*：热修复分支
- 提交信息格式：`<type>(<scope>): <description>`
  - type: feat, fix, docs, style, refactor, test, chore
  - scope: 模块名称
  - description: 简短描述

### 7.3 接口规范
- RESTful风格
- 版本管理：/api/v1/
- 统一响应格式
- 合理的HTTP状态码

---

## 8. 部署方案

### 8.1 本地部署
- 后端：Java 17+ 运行环境
- 前端：Node.js 18+ 运行环境
- 数据库：PostgreSQL 14+
- 缓存：Redis 7+

### 8.2 Docker部署（可选）
```yaml
version: '3.8'
services:
  backend:
    build: ./backend
    ports:
      - "8080:8080"
    depends_on:
      - postgres
      - redis
  frontend:
    build: ./frontend
    ports:
      - "80:80"
  postgres:
    image: postgres:14
    environment:
      POSTGRES_DB: photo_management
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: admin123
    volumes:
      - postgres_data:/var/lib/postgresql/data
  redis:
    image: redis:7-alpine
```

---

## 9. 项目管理

### 9.1 GitHub项目管理
- 使用GitHub Issues进行需求管理
- 使用GitHub Projects进行任务看板
- 使用GitHub Actions进行CI/CD

### 9.2 开发计划
1. **Phase 1：基础功能**（2周）
   - 用户注册登录
   - 照片上传展示
   - 基础相册管理

2. **Phase 2：核心功能**（3周）
   - 时间分组
   - 地点分组
   - 搜索功能

3. **Phase 3：高级功能**（3周）
   - 人脸检测与分组
   - 人脸聚类
   - 性能优化

4. **Phase 4：优化完善**（2周）
   - UI优化
   - 性能优化
   - 测试与修复

---

## 10. 附录

### 10.1 参考资料
- [Spring Boot官方文档](https://spring.io/projects/spring-boot)
- [Vue 3官方文档](https://vuejs.org/)
- [Immich项目](https://github.com/immich-app/immich)
- [阿里巴巴Java开发手册](https://github.com/alibaba/p3c)

### 10.2 环境要求
- JDK 17+
- Node.js 18+
- PostgreSQL 14+
- Redis 7+
- Maven 3.8+
