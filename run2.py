import pandas as pd
from statsmodels.tsa.arima.model import ARIMA
from prophet import Prophet
import sys
import json


# 读取数据
def load_data(file_path):
    df = pd.read_csv(file_path, parse_dates=['time'])
    df['hour'] = df['time'].dt.floor('H')  # 按小时聚合
    return df


# 处理数据，按 (src_ip, dst_ip, hour) 计算 count
def preprocess_data(df):
    grouped = df.groupby(['src_ip', 'dst_ip', 'hour']).size().reset_index(name='count')
    return grouped


# 使用 ARIMA 预测
def predict_arima(df):
    results = []
    for (src_ip, dst_ip), group in df.groupby(['src_ip', 'dst_ip']):
        group = group.set_index('hour').asfreq('H').fillna(0)  # 补全缺失小时
        model = ARIMA(group['count'], order=(2, 1, 2))  # 设定 ARIMA 参数
        fit_model = model.fit()
        forecast = fit_model.forecast(steps=1)[0]  # 预测下 1 小时的 count

        lower_bound, upper_bound = forecast * 0.8, forecast * 1.2
        results.append((src_ip, dst_ip, forecast, lower_bound, upper_bound))

    return results


# 使用 Prophet 预测
def predict_prophet(df):
    results = []
    for (src_ip, dst_ip), group in df.groupby(['src_ip', 'dst_ip']):
        group = group[['hour', 'count']].rename(columns={'hour': 'ds', 'count': 'y'})
        model = Prophet()
        model.fit(group)

        future = pd.DataFrame({'ds': [group['ds'].max() + pd.Timedelta(hours=1)]})  # 预测下 1 小时
        forecast = model.predict(future)
        yhat = forecast['yhat'][0]

        lower_bound, upper_bound = yhat * 0.8, yhat * 1.2
        results.append((src_ip, dst_ip, yhat, lower_bound, upper_bound))

    return results


# 格式化预测结果为字典
def format_results(arima_results, prophet_results):
    result_dict = {
        "arima_results": [],
        "prophet_results": []
    }
    
    for src_ip, dst_ip, forecast, lb, ub in arima_results:
        result_dict["arima_results"].append({
            "src_ip": src_ip,
            "dst_ip": dst_ip,
            "forecast": round(float(forecast), 2),
            "lower_bound": round(float(lb), 2),
            "upper_bound": round(float(ub), 2)
        })

    for src_ip, dst_ip, forecast, lb, ub in prophet_results:
        result_dict["prophet_results"].append({
            "src_ip": src_ip,
            "dst_ip": dst_ip,
            "forecast": round(float(forecast), 2),
            "lower_bound": round(float(lb), 2),
            "upper_bound": round(float(ub), 2)
        })
    
    return result_dict


def main(file_path=None):
    if file_path is None:
        file_path = "generated_data.csv"  # 默认文件路径
    
    try:
        df = load_data(file_path)
        df = preprocess_data(df)
        arima_results = predict_arima(df)
        prophet_results = predict_prophet(df)
        result_dict = format_results(arima_results, prophet_results)
        
        return result_dict
    except Exception as e:
        return {"error": str(e)}


if __name__ == "__main__":
    # 如果有命令行参数，则使用第一个参数作为文件路径
    file_path = sys.argv[1] if len(sys.argv) > 1 else None
    result = main(file_path)
    print(json.dumps(result))
