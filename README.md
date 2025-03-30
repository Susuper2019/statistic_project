# Excel数据汇总API

这是一个简单的Python应用，提供HTTP接口用于上传和汇总Excel文件和CSV文件中的数据。

## 功能

- 提供REST API接口
- 支持上传Excel文件(.xlsx和.xls格式)和CSV文件
- 自动分析Excel/CSV数据并提供统计信息
- 支持按小时或分钟汇总IP访问数据
- 返回JSON格式的统计结果

## 安装和运行

1. 安装依赖项：

```bash
pip install -r requirements.txt
```

2. 运行应用：

```bash
python app.py
```

服务器将在 http://localhost:5000 上启动。

## API使用方法

### 上传并处理Excel/CSV文件

**请求**:
```
POST /upload
```

使用表单数据上传文件，表单字段名为`file`。

**curl示例**:
```bash
curl -X POST -F "file=@your_excel_file.xlsx" http://localhost:5000/upload
```

**响应**:
```json
{
  "message": "文件上传成功并已处理",
  "filename": "example.xlsx",
  "summary": {
    "rows": 100,
    "columns": 5,
    "column_names": ["A", "B", "C", "D", "E"],
    "numeric_columns": {
      "A": {
        "sum": 5050.0,
        "mean": 50.5,
        "min": 1.0,
        "max": 100.0,
        "median": 50.5
      },
      "B": {
        "sum": 10100.0,
        "mean": 101.0,
        "min": 2.0,
        "max": 200.0,
        "median": 101.0
      }
    }
  }
}
```

### 按时间间隔汇总IP访问数据

**请求**:
```
GET /analyze?interval=1h
```

查询参数:
- `interval`: 时间间隔，格式可以是:
  - 小时: `1h`, `2h`, `3hour` 等
  - 分钟: `5m`, `10min`, `30m` 等
  - 默认为 `1h`（1小时）

**curl示例**:
```bash
# 按1小时汇总
curl http://localhost:5000/analyze?interval=1h

# 按30分钟汇总
curl http://localhost:5000/analyze?interval=30m
```

**响应**:
```json
{
  "message": "数据已按1h间隔汇总",
  "interval": "1h",
  "data": {
    "2025-03-01 06:00": {
      "1.1.1.10": {
        "3.3.3.4": 1
      }
    },
    "2025-03-01 11:00": {
      "1.1.1.10": {
        "3.3.3.4": 1
      }
    },
    "2025-03-01 16:00": {
      "1.1.1.8": {
        "3.3.3.6": 1
      }
    },
    "2025-03-01 21:00": {
      "1.1.1.8": {
        "3.3.3.3": 1
      },
      "1.1.1.11": {
        "3.3.3.4": 1
      }
    }
    // ... 更多数据
  }
}
```

返回数据格式说明:
- 每个时间键下包含源IP地址
- 每个源IP下包含目的IP地址
- 每个目的IP包含该时间段内从源IP到目的IP的访问次数

### 健康检查接口

**请求**:
```
GET /health
```

**响应**:
```json
{
  "status": "ok"
}
``` 