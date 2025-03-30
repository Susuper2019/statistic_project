# Java IP数据分析服务

这是一个使用Java Spring Boot实现的IP数据分析服务，提供HTTP接口用于上传和汇总IP访问数据。

## 功能

- 提供RESTful API接口
- 支持上传Excel文件(.xlsx和.xls格式)和CSV文件
- 自动分析Excel/CSV数据并提供统计信息
- 支持按小时或分钟汇总IP访问数据
- 返回JSON格式的统计结果
- 提供面向大模型友好的交互数据格式，便于后续预测分析

## 技术栈

- Java 11
- Spring Boot 2.7
- Apache POI (Excel处理)
- Apache Commons CSV (CSV处理)
- Lombok

## 安装和运行

### 前提条件

- JDK 11+
- Maven 3.6+

### 步骤

1. 克隆项目

2. 构建项目
```bash
cd java-ip-analyzer
mvn clean package
```

3. 运行应用
```bash
java -jar target/java-ip-analyzer-1.0.0.jar
```

服务器将在 http://localhost:8080 上启动。

## API使用说明

### 上传并处理Excel/CSV文件

**请求**:
```
POST /api/upload
```

使用表单数据上传文件，表单字段名为`file`。

**curl示例**:
```bash
curl -X POST -F "file=@your_excel_file.xlsx" http://localhost:8080/api/upload
```

**响应**:
```json
{
  "message": "文件上传成功并已处理",
  "data": {
    "rows": 100,
    "columns": 3,
    "column_names": ["src_ip", "dst_ip", "time"]
  }
}
```

### 按时间间隔汇总IP访问数据 (原格式)

**请求**:
```
GET /api/analyze?interval=1h
```

查询参数:
- `interval`: 时间间隔，格式可以是:
  - 小时: `1h`, `2h`, `3hour` 等
  - 分钟: `5m`, `10min`, `30m` 等
  - 默认为 `1h`（1小时）

**curl示例**:
```bash
# 按1小时汇总
curl http://localhost:8080/api/analyze?interval=1h

# 按30分钟汇总
curl http://localhost:8080/api/analyze?interval=30m
```

**响应**:
```json
{
  "message": "数据已按1h间隔汇总",
  "data": {
    "interval": "1h",
    "data": {
      "2024-05-01 06:00": {
        "1.1.1.10": {
          "3.3.3.4": 1
        }
      },
      "2024-05-01 11:00": {
        "1.1.1.10": {
          "3.3.3.4": 1
        }
      }
    }
  }
}
```

### 按时间间隔汇总交互数据 (大模型友好格式)

**请求**:
```
GET /api/analyze/interactions?interval=1h
```

查询参数:
- `interval`: 时间间隔，格式可以是:
  - 小时: `1h`, `2h`, `3hour` 等
  - 分钟: `5m`, `10min`, `30m` 等
  - 默认为 `1h`（1小时）

**curl示例**:
```bash
# 按1小时汇总
curl http://localhost:8080/api/analyze/interactions?interval=1h
```

**响应**:
```json
{
  "message": "主体客体交互数据已按1h间隔汇总",
  "data": {
    "interval": "1h",
    "totalInteractions": 456,
    "interactions": [
      {
        "startTime": "2024-04-28 08:00:00",
        "endTime": "2024-04-28 09:00:00",
        "count": 2,
        "subject": "1.1.1.1",
        "object": "2.2.2.2"
      },
      {
        "startTime": "2024-04-28 09:00:00",
        "endTime": "2024-04-28 10:00:00",
        "count": 2,
        "subject": "1.1.1.1",
        "object": "2.2.2.3"
      }
    ],
    "metadata": {
      "timeRange": "2024-04-28 至 2024-05-27",
      "uniqueSubjects": 3,
      "uniqueObjects": 4,
      "description": "IP交互关系分析，将源IP视为主体，目的IP视为客体，按1h时间间隔汇总"
    }
  }
}
```

这种格式设计有以下优点：
1. **更好的语义化**：使用主体(subject)和客体(object)来描述IP间的关系更加直观
2. **明确的时间窗口**：每条记录包含startTime和endTime，清晰表示时间范围
3. **额外元数据**：提供数据范围、唯一值统计等元信息，便于理解数据特征
4. **适合大模型分析**：线性的记录列表结构易于大模型处理和分析
5. **时间顺序排序**：所有交互记录按时间顺序排列，便于观察时间模式

### 健康检查接口

**请求**:
```
GET /api/health
```

**响应**:
```json
{
  "message": "OK",
  "data": "服务运行正常"
}
``` 