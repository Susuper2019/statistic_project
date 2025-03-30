import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import json
import os
from datetime import datetime, timedelta
import warnings
warnings.filterwarnings('ignore')

# 统计分析库
from statsmodels.tsa.arima.model import ARIMA
from statsmodels.tsa.statespace.sarimax import SARIMAX
from statsmodels.tsa.holtwinters import ExponentialSmoothing
# Prophet库可能需要安装: pip install prophet
# 如果安装失败可以注释掉相关代码，只使用ARIMA和LSTM
try:
    from prophet import Prophet
    prophet_available = True
except ImportError:
    print("Prophet 库未安装，将跳过Prophet模型")
    prophet_available = False

# TensorFlow库可能需要安装: pip install tensorflow
# 如果安装失败可以注释掉相关代码，只使用ARIMA和Prophet
try:
    import tensorflow as tf
    from tensorflow.keras.models import Sequential
    from tensorflow.keras.layers import LSTM, Dense, Dropout
    from sklearn.preprocessing import MinMaxScaler
    tensorflow_available = True
except ImportError:
    print("TensorFlow 库未安装，将跳过LSTM模型")
    tensorflow_available = False

def load_json_data(json_file):
    """加载并预处理已经按小时汇总过的JSON数据"""
    try:
        # 加载JSON数据
        with open(json_file, 'r') as f:
            data = json.load(f)
        
        # 转换为DataFrame
        df = pd.DataFrame(data)
        
        # 转换时间列
        df['startTime'] = pd.to_datetime(df['startTime'])
        df['endTime'] = pd.to_datetime(df['endTime'])
        
        # 创建时间戳列作为索引
        df['timestamp'] = df['startTime']
        
        # 重命名列以便于处理
        df = df.rename(columns={'count': 'value', 'subject': 'src_ip', 'object': 'dst_ip'})
        
        print(f"已加载JSON数据，共有 {len(df)} 条记录")
        return df
    
    except Exception as e:
        print(f"JSON数据加载错误: {e}")
        return None

def load_csv_data(csv_file):
    """加载并预处理原始CSV数据，按小时汇总"""
    try:
        # 读取CSV文件
        df = pd.read_csv(csv_file)
        
        # 确保列名符合预期
        expected_columns = ['src_ip', 'dst_ip', 'time']
        if not all(col in df.columns for col in expected_columns):
            raise ValueError(f"CSV文件缺少必要的列，期望列: {expected_columns}")
        
        # 转换时间列
        df['time'] = pd.to_datetime(df['time'], format='%Y/%m/%d %H:%M')
        
        # 按小时汇总数据 - 按src_ip和dst_ip分组
        # 创建小时级别的时间戳
        df['hour'] = df['time'].dt.floor('H')
        
        # 分组并计数
        aggregated = df.groupby(['hour', 'src_ip', 'dst_ip']).size().reset_index(name='value')
        
        # 重命名列以匹配JSON格式
        aggregated = aggregated.rename(columns={'hour': 'timestamp'})
        
        # 添加endTime列（开始时间加一小时）
        aggregated['endTime'] = aggregated['timestamp'] + pd.Timedelta(hours=1)
        
        print(f"已加载并汇总CSV数据，共有 {len(aggregated)} 条记录")
        return aggregated
    
    except Exception as e:
        print(f"CSV数据加载错误: {e}")
        return None

def load_data(file_path):
    """根据文件类型加载数据"""
    if file_path.endswith('.json'):
        return load_json_data(file_path)
    elif file_path.endswith('.csv'):
        return load_csv_data(file_path)
    else:
        print(f"不支持的文件类型: {file_path}")
        return None

def prepare_data_for_training(df, pair=None):
    """准备用于训练的数据，可以指定特定的IP对"""
    if pair:
        src_ip, dst_ip = pair
        filtered_df = df[(df['src_ip'] == src_ip) & (df['dst_ip'] == dst_ip)]
    else:
        # 找出出现频率最高的源-目的IP对
        pair_counts = df.groupby(['src_ip', 'dst_ip']).size().reset_index(name='count')
        pair_counts = pair_counts.sort_values('count', ascending=False)
        most_common_pair = pair_counts.iloc[0]
        src_ip, dst_ip = most_common_pair['src_ip'], most_common_pair['dst_ip']
        filtered_df = df[(df['src_ip'] == src_ip) & (df['dst_ip'] == dst_ip)]
        print(f"使用最常见的IP对进行预测: {src_ip} -> {dst_ip}, 共{len(filtered_df)}条记录")
    
    # 确保数据按时间排序
    filtered_df = filtered_df.sort_values('timestamp')
    
    # 设置时间索引
    time_series = filtered_df.set_index('timestamp')['value']
    
    # 如果存在缺失的小时，填充为0
    full_range = pd.date_range(start=time_series.index.min(), end=time_series.index.max(), freq='H')
    time_series = time_series.reindex(full_range, fill_value=0)
    
    return time_series, (src_ip, dst_ip)

def arima_forecast(time_series, forecast_hours=2, src_ip=None, dst_ip=None):
    """使用ARIMA模型预测未来数据"""
    try:
        # 如果数据不足，则报错
        if len(time_series) < 10:
            raise ValueError(f"数据点不足，ARIMA需要更多历史数据 (当前: {len(time_series)})")
        
        # 自动确定ARIMA参数
        p, d, q = 5, 1, 1  # 默认参数
        
        # 构建ARIMA模型
        model = ARIMA(time_series, order=(p, d, q))
        model_fit = model.fit()
        
        # 预测未来n小时
        forecast = model_fit.forecast(steps=forecast_hours)
        
        # 创建预测时间索引
        last_timestamp = time_series.index[-1]
        forecast_index = pd.date_range(start=last_timestamp + timedelta(hours=1), 
                                       periods=forecast_hours, 
                                       freq='H')
        
        # 创建结果DataFrame
        forecast_df = pd.DataFrame({
            'timestamp': forecast_index,
            'value': forecast,
            'src_ip': src_ip,
            'dst_ip': dst_ip,
            'model': 'ARIMA'
        })
        
        return forecast_df
    
    except Exception as e:
        print(f"ARIMA预测错误: {e}")
        return None

def sarima_forecast(time_series, forecast_hours=2, src_ip=None, dst_ip=None):
    """使用SARIMA模型预测未来数据"""
    try:
        # 如果数据不足，则报错
        if len(time_series) < 24:
            raise ValueError(f"数据点不足，SARIMA需要更多历史数据 (当前: {len(time_series)})")
        
        # 构建SARIMA模型，考虑数据的季节性（例如24小时周期）
        model = SARIMAX(
            time_series,
            order=(1, 1, 1),
            seasonal_order=(1, 1, 1, 24),  # 24小时周期
            enforce_stationarity=False,
            enforce_invertibility=False
        )
        model_fit = model.fit(disp=False)
        
        # 预测未来n小时
        forecast = model_fit.forecast(steps=forecast_hours)
        
        # 创建预测时间索引
        last_timestamp = time_series.index[-1]
        forecast_index = pd.date_range(start=last_timestamp + timedelta(hours=1), 
                                       periods=forecast_hours, 
                                       freq='H')
        
        # 创建结果DataFrame
        forecast_df = pd.DataFrame({
            'timestamp': forecast_index,
            'value': forecast,
            'src_ip': src_ip,
            'dst_ip': dst_ip,
            'model': 'SARIMA'
        })
        
        return forecast_df
    
    except Exception as e:
        print(f"SARIMA预测错误: {e}")
        return None

def holt_winters_forecast(time_series, forecast_hours=2, src_ip=None, dst_ip=None):
    """使用Holt-Winters指数平滑模型预测未来数据"""
    try:
        # 如果数据不足，则报错
        if len(time_series) < 24:
            raise ValueError(f"数据点不足，Holt-Winters需要更多历史数据 (当前: {len(time_series)})")
        
        # 构建Holt-Winters模型
        model = ExponentialSmoothing(
            time_series,
            trend='add',
            seasonal='add',
            seasonal_periods=24  # 24小时周期
        )
        model_fit = model.fit()
        
        # 预测未来n小时
        forecast = model_fit.forecast(forecast_hours)
        
        # 创建预测时间索引
        last_timestamp = time_series.index[-1]
        forecast_index = pd.date_range(start=last_timestamp + timedelta(hours=1), 
                                       periods=forecast_hours, 
                                       freq='H')
        
        # 创建结果DataFrame
        forecast_df = pd.DataFrame({
            'timestamp': forecast_index,
            'value': forecast,
            'src_ip': src_ip,
            'dst_ip': dst_ip,
            'model': 'Holt-Winters'
        })
        
        return forecast_df
    
    except Exception as e:
        print(f"Holt-Winters预测错误: {e}")
        return None

def prophet_forecast(time_series, forecast_hours=2, src_ip=None, dst_ip=None):
    """使用Prophet模型预测未来数据"""
    if not prophet_available:
        print("Prophet库未安装，跳过Prophet预测")
        return None
        
    try:
        # 准备Prophet格式数据
        prophet_df = pd.DataFrame({
            'ds': time_series.index,
            'y': time_series.values
        })
        
        # 如果数据不足，则报错
        if len(prophet_df) < 10:
            raise ValueError(f"数据点不足，Prophet需要更多历史数据 (当前: {len(prophet_df)})")
        
        # 创建并训练模型
        model = Prophet(
            daily_seasonality=True,
            yearly_seasonality=False,
            weekly_seasonality=True,
            changepoint_prior_scale=0.05
        )
        model.fit(prophet_df)
        
        # 创建未来数据框
        future = model.make_future_dataframe(periods=forecast_hours, freq='H')
        
        # 预测
        forecast = model.predict(future)
        
        # 提取最后n小时的预测
        forecast_df = forecast[['ds', 'yhat']].tail(forecast_hours)
        
        # 转换为结果DataFrame
        result_df = pd.DataFrame({
            'timestamp': forecast_df['ds'],
            'value': forecast_df['yhat'],
            'src_ip': src_ip,
            'dst_ip': dst_ip,
            'model': 'Prophet'
        })
        
        return result_df
    
    except Exception as e:
        print(f"Prophet预测错误: {e}")
        return None

def lstm_forecast(time_series, forecast_hours=2, src_ip=None, dst_ip=None):
    """使用LSTM模型预测未来数据"""
    if not tensorflow_available:
        print("TensorFlow库未安装，跳过LSTM预测")
        return None
        
    try:
        # 设置序列长度
        sequence_length = min(24, len(time_series) // 2)  # 使用不超过数据长度一半的序列窗口
        
        # 如果数据不足，则报错
        if len(time_series) < sequence_length + 5:
            raise ValueError(f"数据点不足，LSTM需要更多历史数据 (当前: {len(time_series)})")
        
        # 数据归一化
        scaler = MinMaxScaler(feature_range=(0, 1))
        data_scaled = scaler.fit_transform(time_series.values.reshape(-1, 1))
        
        # 准备序列数据
        X, y = [], []
        for i in range(len(data_scaled) - sequence_length):
            X.append(data_scaled[i:i+sequence_length])
            y.append(data_scaled[i+sequence_length])
        
        X, y = np.array(X), np.array(y)
        
        # 构建LSTM模型
        model = Sequential([
            LSTM(64, activation='relu', return_sequences=True, input_shape=(sequence_length, 1)),
            Dropout(0.2),
            LSTM(32, activation='relu'),
            Dropout(0.2),
            Dense(16, activation='relu'),
            Dense(1)
        ])
        
        # 编译模型
        model.compile(optimizer='adam', loss='mse')
        
        # 训练模型
        model.fit(X, y, epochs=50, batch_size=32, verbose=0)
        
        # 准备预测数据
        prediction_data = data_scaled[-sequence_length:].reshape(1, sequence_length, 1)
        
        # 递归预测未来n小时
        predictions = []
        current_batch = prediction_data
        
        for _ in range(forecast_hours):
            # 预测下一个值
            current_pred = model.predict(current_batch, verbose=0)[0]
            predictions.append(current_pred)
            
            # 更新批次数据用于下一个预测
            current_batch = np.append(current_batch[:,1:,:], 
                                      [[current_pred]], 
                                      axis=1)
        
        # 转换回原始比例
        predictions = np.array(predictions).reshape(-1, 1)
        predictions = scaler.inverse_transform(predictions)
        
        # 对负值进行修正（如果有）
        predictions = np.maximum(predictions, 0)
        
        # 将小数四舍五入到最接近的整数
        predictions = np.round(predictions)
        
        # 创建预测时间索引
        last_timestamp = time_series.index[-1]
        forecast_index = pd.date_range(start=last_timestamp + timedelta(hours=1), 
                                       periods=forecast_hours, 
                                       freq='H')
        
        # 创建结果DataFrame
        forecast_df = pd.DataFrame({
            'timestamp': forecast_index,
            'value': predictions.flatten(),
            'src_ip': src_ip,
            'dst_ip': dst_ip,
            'model': 'LSTM'
        })
        
        return forecast_df
    
    except Exception as e:
        print(f"LSTM预测错误: {e}")
        return None

def plot_analysis(time_series, forecasts, src_ip, dst_ip, save_path=None):
    """绘制历史数据和预测结果"""
    try:
        plt.figure(figsize=(14, 7))
        
        # 绘制实际数据
        plt.plot(time_series.index, time_series.values, 'k-', 
                 label='历史数据', linewidth=2, alpha=0.7)
        
        # 颜色映射
        colors = {
            'ARIMA': 'tab:blue',
            'SARIMA': 'tab:orange',
            'Holt-Winters': 'tab:green',
            'Prophet': 'tab:red',
            'LSTM': 'tab:purple'
        }
        
        # 绘制各种预测
        for name, forecast in forecasts.items():
            if forecast is not None:
                plt.plot(forecast['timestamp'], forecast['value'], '--', 
                         color=colors.get(name, 'gray'),
                         label=f'{name}预测', linewidth=2)
        
        # 添加标题和标签
        plt.title(f'IP交互预测结果 ({src_ip} -> {dst_ip})', fontsize=14)
        plt.xlabel('时间', fontsize=12)
        plt.ylabel('交互次数', fontsize=12)
        plt.grid(True, linestyle='--', alpha=0.7)
        plt.legend(loc='best')
        
        # 格式化x轴日期
        plt.gcf().autofmt_xdate()
        
        plt.tight_layout()
        
        # 保存图表
        if save_path:
            plt.savefig(save_path)
            print(f"预测图表已保存为 '{save_path}'")
        
        # 显示图表
        plt.show()
        
    except Exception as e:
        print(f"绘图错误: {e}")

def calculate_forecast_metrics(forecasts):
    """计算和比较不同预测模型的结果"""
    if not forecasts:
        return None
        
    # 收集所有有效预测
    valid_forecasts = {k: v for k, v in forecasts.items() if v is not None}
    
    if not valid_forecasts:
        return None
    
    # 创建比较表格
    comparison = pd.DataFrame()
    
    for model, forecast in valid_forecasts.items():
        # 提取预测值
        values = forecast['value'].values
        
        # 计算基本统计量
        comparison.loc['最小值', model] = values.min()
        comparison.loc['最大值', model] = values.max()
        comparison.loc['平均值', model] = values.mean()
        comparison.loc['标准差', model] = values.std()
        comparison.loc['总量', model] = values.sum()
    
    return comparison

def main(data_file, forecast_hours=2, result_prefix="forecast"):
    """主函数"""
    try:
        # 加载数据
        df = load_data(data_file)
        if df is None:
            return
        
        # 准备训练数据
        time_series, (src_ip, dst_ip) = prepare_data_for_training(df)
        
        print("\n数据概览:")
        print(f"- 时间范围: {time_series.index.min()} 到 {time_series.index.max()}")
        print(f"- 数据点数量: {len(time_series)}")
        print(f"- 平均每小时交互量: {time_series.mean():.2f}")
        print(f"- 最大每小时交互量: {time_series.max()}")
        
        # 进行各种预测
        print(f"\n开始预测未来{forecast_hours}小时的数据...")
        
        forecasts = {}
        
        print("\n运行ARIMA模型...")
        forecasts['ARIMA'] = arima_forecast(time_series, forecast_hours, src_ip, dst_ip)
        
        if len(time_series) >= 24:
            print("\n运行SARIMA模型...")
            forecasts['SARIMA'] = sarima_forecast(time_series, forecast_hours, src_ip, dst_ip)
            
            print("\n运行Holt-Winters模型...")
            forecasts['Holt-Winters'] = holt_winters_forecast(time_series, forecast_hours, src_ip, dst_ip)
        
        if prophet_available:
            print("\n运行Prophet模型...")
            forecasts['Prophet'] = prophet_forecast(time_series, forecast_hours, src_ip, dst_ip)
        
        if tensorflow_available:
            print("\n运行LSTM模型...")
            forecasts['LSTM'] = lstm_forecast(time_series, forecast_hours, src_ip, dst_ip)
        
        # 打印预测结果
        print("\n预测结果摘要:")
        for model, forecast in forecasts.items():
            if forecast is not None:
                print(f"\n{model}预测结果:")
                print(forecast[['timestamp', 'value']].to_string(index=False))
        
        # 计算预测指标
        metrics = calculate_forecast_metrics(forecasts)
        if metrics is not None:
            print("\n预测统计比较:")
            print(metrics)
        
        # 绘制和保存结果
        save_path = f"{result_prefix}_results_{src_ip}_to_{dst_ip}.png"
        plot_analysis(time_series, forecasts, src_ip, dst_ip, save_path)
        
        # 保存预测结果到CSV
        all_forecasts = []
        for model, forecast in forecasts.items():
            if forecast is not None:
                all_forecasts.append(forecast)
        
        if all_forecasts:
            combined = pd.concat(all_forecasts)
            csv_path = f"{result_prefix}_results.csv"
            combined.to_csv(csv_path, index=False)
            print(f"\n预测结果已保存到CSV文件: {csv_path}")
        
    except Exception as e:
        print(f"执行错误: {e}")

if __name__ == "__main__":
    print("开始分析数据...")
    
    # 检查两种可能的数据源
    # if os.path.exists("data.json"):
    #     main("data.json", forecast_hours=2)
    # elif os.path.exists("generated_data.csv"):
    main("generated_data.csv", forecast_hours=2)
    # else:
    #     print("错误: 未找到数据文件 'data.json' 或 'generated_data.csv'")
    #     print("请确保数据文件位于当前目录下")
