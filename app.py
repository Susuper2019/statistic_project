import os
from flask import Flask, request, jsonify
import pandas as pd
from werkzeug.utils import secure_filename
from datetime import datetime

app = Flask(__name__)
app.config['UPLOAD_FOLDER'] = 'uploads'
app.config['ALLOWED_EXTENSIONS'] = {'xlsx', 'xls', 'csv'}

# 确保上传目录存在
os.makedirs(app.config['UPLOAD_FOLDER'], exist_ok=True)

def allowed_file(filename):
    """检查上传的文件扩展名是否允许"""
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in app.config['ALLOWED_EXTENSIONS']

@app.route('/upload', methods=['POST'])
def upload_file():
    """上传并处理Excel文件的接口"""
    if 'file' not in request.files:
        return jsonify({'error': '没有文件上传'}), 400
    
    file = request.files['file']
    
    if file.filename == '':
        return jsonify({'error': '没有选择文件'}), 400
    
    if file and allowed_file(file.filename):
        filename = secure_filename(file.filename)
        filepath = os.path.join(app.config['UPLOAD_FOLDER'], filename)
        file.save(filepath)
        
        # 使用pandas读取Excel文件
        try:
            if filename.endswith('.csv'):
                df = pd.read_csv(filepath)
            else:
                df = pd.read_excel(filepath)
            
            # 获取基本统计信息
            summary = {
                'rows': len(df),
                'columns': len(df.columns),
                'column_names': df.columns.tolist(),
                'numeric_columns': {}
            }
            
            # 对数值列进行统计
            for col in df.select_dtypes(include=['number']).columns:
                summary['numeric_columns'][col] = {
                    'sum': float(df[col].sum()),
                    'mean': float(df[col].mean()),
                    'min': float(df[col].min()),
                    'max': float(df[col].max()),
                    'median': float(df[col].median())
                }
            
            return jsonify({
                'message': '文件上传成功并已处理',
                'filename': filename,
                'summary': summary
            })
            
        except Exception as e:
            return jsonify({'error': f'处理文件时出错: {str(e)}'}), 500
    
    return jsonify({'error': '不允许的文件类型'}), 400

@app.route('/analyze', methods=['GET'])
def analyze_data():
    """按时间间隔分析数据，汇总源IP到目的IP的访问次数"""
    # 获取参数
    interval = request.args.get('interval', '1h')  # 默认按1小时汇总
    file_path = 'generated_data.csv'  # 使用固定的数据文件
    
    # 检查数据文件是否存在
    if not os.path.exists(file_path):
        return jsonify({'error': f'数据文件 {file_path} 不存在'}), 404
    
    try:
        # 读取CSV文件
        df = pd.read_csv(file_path)
        
        # 将时间列转换为datetime类型
        df['time'] = pd.to_datetime(df['time'], format='%Y/%m/%d %H:%M')
        
        # 根据interval参数创建时间分组
        if interval.endswith('m') or interval.endswith('min'):
            # 分钟级汇总
            try:
                minutes = int(interval.rstrip('min').rstrip('m'))
                if minutes <= 0:
                    return jsonify({'error': '间隔必须是正数'}), 400
                
                # 创建分钟级别的时间组
                df['time_group'] = df['time'].dt.floor(f'{minutes}min')
                group_format = '%Y-%m-%d %H:%M'
            except ValueError:
                return jsonify({'error': '无效的分钟格式，请使用如 "5m" 或 "10min"'}), 400
        else:
            # 小时级汇总
            try:
                hours = int(interval.rstrip('hour').rstrip('h'))
                if hours <= 0:
                    return jsonify({'error': '间隔必须是正数'}), 400
                
                # 创建小时级别的时间组
                df['time_group'] = df['time'].dt.floor(f'{hours}H')
                group_format = '%Y-%m-%d %H:00'
            except ValueError:
                return jsonify({'error': '无效的小时格式，请使用如 "1h" 或 "2hour"'}), 400
        
        # 按时间组、源IP和目的IP分组计数
        grouped = df.groupby(['time_group', 'src_ip', 'dst_ip']).size().reset_index(name='count')
        
        # 将结果转换为字典格式，更适合JSON响应
        result = {}
        for _, row in grouped.iterrows():
            time_key = row['time_group'].strftime(group_format)
            
            if time_key not in result:
                result[time_key] = {}
            
            src_ip = row['src_ip']
            if src_ip not in result[time_key]:
                result[time_key][src_ip] = {}
            
            dst_ip = row['dst_ip']
            result[time_key][src_ip][dst_ip] = int(row['count'])
            
        return jsonify({
            'message': f'数据已按{interval}间隔汇总',
            'interval': interval,
            'data': result
        })
        
    except Exception as e:
        return jsonify({'error': f'分析数据时出错: {str(e)}'}), 500

@app.route('/health', methods=['GET'])
def health_check():
    """健康检查接口"""
    return jsonify({'status': 'ok'})

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5000) 