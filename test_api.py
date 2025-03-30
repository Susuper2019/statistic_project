import requests
import json
import sys

def test_analyze_api(interval='1h'):
    """测试数据分析接口"""
    url = f'http://localhost:5000/analyze?interval={interval}'
    
    print(f'正在按 {interval} 间隔测试数据分析接口...')
    
    try:
        response = requests.get(url)
        
        # 格式化打印结果
        if response.status_code == 200:
            data = response.json()
            print(f'API调用成功! 结果摘要:')
            print(f'消息: {data["message"]}')
            print(f'间隔: {data["interval"]}')
            
            # 提取并显示时间段数量
            time_periods = data['data'].keys()
            print(f'共包含 {len(time_periods)} 个时间段')
            
            # 显示前3个时间段的详细信息
            print('\n前几个时间段的详细信息:')
            for i, period in enumerate(list(time_periods)[:3]):
                print(f'\n时间段 {i+1}: {period}')
                period_data = data['data'][period]
                for src_ip in period_data:
                    for dst_ip, count in period_data[src_ip].items():
                        print(f'  源IP {src_ip} -> 目的IP {dst_ip}: {count}次访问')
            
            # 保存完整结果到文件
            filename = f'analyze_result_{interval}.json'
            with open(filename, 'w') as f:
                json.dump(data, f, indent=2)
            print(f'\n完整结果已保存到 {filename}')
            
        else:
            print(f'API调用失败! 状态码: {response.status_code}')
            print(f'错误信息: {response.text}')
            
    except Exception as e:
        print(f'发生错误: {str(e)}')

if __name__ == '__main__':
    # 从命令行参数获取间隔
    interval = sys.argv[1] if len(sys.argv) > 1 else '1h'
    test_analyze_api(interval) 