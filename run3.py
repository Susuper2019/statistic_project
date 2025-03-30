import pandas as pd
from statsmodels.tsa.arima.model import ARIMA
from prophet import Prophet
import sys
import json
import argparse


def load_and_process_data(file_path):
    # 读取数据
    df = pd.read_csv(file_path, parse_dates=['time'])

    # 计算每个 (src_ip, dst_ip) 组合的每小时访问次数
    df['hour'] = df['time'].dt.floor('H')
    grouped = df.groupby(['src_ip', 'dst_ip', 'hour']).size().reset_index(name='count')

    return df, grouped


def get_future_time(df):
    # 获取数据中的最大时间，并计算未来一个小时
    max_time = df['time'].max()
    future_time = max_time + pd.Timedelta(hours=1)
    # 向下取整到小时
    future_time = future_time.floor('H')
    return future_time


def arima_forecast(df, src_ip, dst_ip, future_time):
    data = df[(df['src_ip'] == src_ip) & (df['dst_ip'] == dst_ip)]
    if data.empty:
        return None, None, None

    ts = data.set_index('hour')['count']

    # 训练 ARIMA 模型
    model = ARIMA(ts, order=(2, 1, 2))  # 你可以调整 order 参数
    model_fit = model.fit()

    # 预测未来 1 小时
    forecast = model_fit.forecast(steps=1)
    mean_pred = forecast.iloc[0]
    upper_bound = mean_pred * 1.2
    lower_bound = mean_pred * 0.8

    return mean_pred, lower_bound, upper_bound


def prophet_forecast(df, src_ip, dst_ip, future_time):
    data = df[(df['src_ip'] == src_ip) & (df['dst_ip'] == dst_ip)]
    if data.empty:
        return None, None, None

    ts = data[['hour', 'count']].rename(columns={'hour': 'ds', 'count': 'y'})
    model = Prophet()
    model.fit(ts)

    # 预测未来 1 小时
    future = pd.DataFrame({'ds': [future_time]})
    forecast = model.predict(future)
    mean_pred = forecast['yhat'].iloc[0]
    lower_bound = mean_pred * 0.8
    upper_bound = mean_pred * 1.2

    return mean_pred, lower_bound, upper_bound


def format_results(src_ip, dst_ip, future_time, arima_results, prophet_results):
    result_dict = {
        "time": future_time.strftime("%Y-%m-%d %H:%M:%S"),
        "src_ip": src_ip,
        "dst_ip": dst_ip,
        "arima_prediction": None,
        "prophet_prediction": None
    }
    
    if all(r is not None for r in arima_results):
        mean_pred, lower_bound, upper_bound = arima_results
        result_dict["arima_prediction"] = {
            "forecast": round(float(mean_pred), 2),
            "lower_bound": round(float(lower_bound), 2),
            "upper_bound": round(float(upper_bound), 2)
        }
        
    if all(r is not None for r in prophet_results):
        mean_pred, lower_bound, upper_bound = prophet_results
        result_dict["prophet_prediction"] = {
            "forecast": round(float(mean_pred), 2),
            "lower_bound": round(float(lower_bound), 2),
            "upper_bound": round(float(upper_bound), 2)
        }
    
    return result_dict


def main():
    parser = argparse.ArgumentParser(description="使用时间序列预测方法预测特定IP组合的流量")
    parser.add_argument("--src_ip", required=True, help="源IP地址")
    parser.add_argument("--dst_ip", required=True, help="目标IP地址")
    parser.add_argument("--file", default="generated_data.csv", help="数据文件路径")
    
    args = parser.parse_args()
    
    # 读取和处理数据
    raw_df, grouped_df = load_and_process_data(args.file)
    
    # 获取未来时间点
    future_time = get_future_time(raw_df)
    
    # 使用 ARIMA 预测
    arima_results = arima_forecast(grouped_df, args.src_ip, args.dst_ip, future_time)
    
    # 使用 Prophet 预测
    prophet_results = prophet_forecast(grouped_df, args.src_ip, args.dst_ip, future_time)
    
    # 格式化结果
    result_dict = format_results(args.src_ip, args.dst_ip, future_time, arima_results, prophet_results)
    
    # 输出结果为JSON
    print(json.dumps(result_dict, indent=2, ensure_ascii=False))


if __name__ == "__main__":
    main()
