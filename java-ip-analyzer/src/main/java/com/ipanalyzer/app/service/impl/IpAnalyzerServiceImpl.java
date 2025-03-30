package com.ipanalyzer.app.service.impl;

import com.ipanalyzer.app.model.AnalysisResult;
import com.ipanalyzer.app.model.InteractionAnalysisResult;
import com.ipanalyzer.app.model.InteractionRecord;
import com.ipanalyzer.app.model.IpRecord;
import com.ipanalyzer.app.service.IpAnalyzerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class IpAnalyzerServiceImpl implements IpAnalyzerService {

    @Value("${upload.dir}")
    private String uploadDir;
    
    private static final String DATA_FILE = "generated_data.csv";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
    
    @Override
    public Map<String, Object> processFile(MultipartFile file) throws IOException {
        // 确保上传目录存在
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // 保存文件
        String filename = file.getOriginalFilename();
        Path filePath = uploadPath.resolve(filename);
        file.transferTo(filePath.toFile());
        
        // 处理文件
        List<IpRecord> records;
        if (filename.endsWith(".csv")) {
            records = readCsvFile(filePath.toFile());
        } else if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
            records = readExcelFile(filePath.toFile());
        } else {
            throw new IllegalArgumentException("不支持的文件格式");
        }
        
        // 生成统计信息
        Map<String, Object> summary = new HashMap<>();
        summary.put("rows", records.size());
        
        if (!records.isEmpty()) {
            // 获取列名
            summary.put("columns", 3); // src_ip, dst_ip, time
            summary.put("column_names", Arrays.asList("src_ip", "dst_ip", "time"));
        }
        
        return summary;
    }
    
    @Override
    public AnalysisResult analyzeData(String interval) throws IOException {
        // 读取数据文件
        File dataFile = new File(DATA_FILE);
        if (!dataFile.exists()) {
            throw new FileNotFoundException("数据文件不存在: " + DATA_FILE);
        }
        
        List<IpRecord> records = readCsvFile(dataFile);
        
        // 解析间隔
        int value;
        ChronoUnit unit;
        
        if (interval.endsWith("m") || interval.endsWith("min")) {
            value = Integer.parseInt(interval.replaceAll("[^0-9]", ""));
            unit = ChronoUnit.MINUTES;
        } else {
            value = Integer.parseInt(interval.replaceAll("[^0-9]", ""));
            unit = ChronoUnit.HOURS;
        }
        
        // 分组格式
        DateTimeFormatter groupFormat = unit == ChronoUnit.MINUTES 
                ? DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                : DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00");
        
        // 按时间间隔分组
        Map<String, Map<String, Map<String, Integer>>> resultMap = new HashMap<>();
        
        for (IpRecord record : records) {
            // 向下取整到最近的时间间隔
            LocalDateTime roundedTime = roundToInterval(record.getTime(), value, unit);
            String timeKey = roundedTime.format(groupFormat);
            
            // 初始化嵌套Map
            resultMap.putIfAbsent(timeKey, new HashMap<>());
            
            Map<String, Map<String, Integer>> timeGroup = resultMap.get(timeKey);
            timeGroup.putIfAbsent(record.getSrcIp(), new HashMap<>());
            
            Map<String, Integer> srcGroup = timeGroup.get(record.getSrcIp());
            srcGroup.put(record.getDstIp(), srcGroup.getOrDefault(record.getDstIp(), 0) + 1);
        }
        
        // 将结果按时间键排序
        Map<String, Map<String, Map<String, Integer>>> sortedResult = new TreeMap<>(
            (key1, key2) -> {
                try {
                    // 创建适当的格式化器来解析日期时间
                    DateTimeFormatter parser = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    if (!key1.contains(":")) {
                        // 仅有小时的情况
                        parser = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00");
                    }
                    
                    LocalDateTime dt1 = LocalDateTime.parse(key1, parser);
                    LocalDateTime dt2 = LocalDateTime.parse(key2, parser);
                    return dt1.compareTo(dt2);
                } catch (Exception e) {
                    log.warn("日期解析错误，使用字符串比较: {} vs {}", key1, key2);
                    return key1.compareTo(key2);
                }
            }
        );
        sortedResult.putAll(resultMap);
        
        return new AnalysisResult(interval, sortedResult);
    }
    
    @Override
    public InteractionAnalysisResult analyzeInteractions(String interval) throws IOException {
        // 读取数据文件
        File dataFile = new File(DATA_FILE);
        if (!dataFile.exists()) {
            throw new FileNotFoundException("数据文件不存在: " + DATA_FILE);
        }
        
        List<IpRecord> records = readCsvFile(dataFile);
        
        // 解析间隔
        int value;
        ChronoUnit unit;
        
        if (interval.endsWith("m") || interval.endsWith("min")) {
            value = Integer.parseInt(interval.replaceAll("[^0-9]", ""));
            unit = ChronoUnit.MINUTES;
        } else {
            value = Integer.parseInt(interval.replaceAll("[^0-9]", ""));
            unit = ChronoUnit.HOURS;
        }
        
        // 分组并统计
        Map<LocalDateTime, Map<String, Map<String, Integer>>> timeGroups = new HashMap<>();
        
        // 记录数据的时间范围
        LocalDateTime minTime = null;
        LocalDateTime maxTime = null;
        
        // 记录唯一的主体和客体
        Set<String> uniqueSubjects = new HashSet<>();
        Set<String> uniqueObjects = new HashSet<>();
        
        for (IpRecord record : records) {
            // 向下取整到最近的时间间隔
            LocalDateTime roundedStartTime = roundToInterval(record.getTime(), value, unit);
            
            // 更新时间范围
            if (minTime == null || roundedStartTime.isBefore(minTime)) {
                minTime = roundedStartTime;
            }
            
            if (maxTime == null || roundedStartTime.isAfter(maxTime)) {
                maxTime = roundedStartTime;
            }
            
            // 记录唯一主体和客体
            uniqueSubjects.add(record.getSrcIp());
            uniqueObjects.add(record.getDstIp());
            
            // 初始化嵌套Map
            timeGroups.putIfAbsent(roundedStartTime, new HashMap<>());
            
            Map<String, Map<String, Integer>> subjectGroups = timeGroups.get(roundedStartTime);
            subjectGroups.putIfAbsent(record.getSrcIp(), new HashMap<>());
            
            Map<String, Integer> objectCounts = subjectGroups.get(record.getSrcIp());
            objectCounts.put(record.getDstIp(), objectCounts.getOrDefault(record.getDstIp(), 0) + 1);
        }
        
        // 将时间分组转换为排序的交互记录列表
        List<InteractionRecord> interactions = new ArrayList<>();
        int totalInteractions = 0;
        
        // 按时间排序
        List<LocalDateTime> sortedTimes = new ArrayList<>(timeGroups.keySet());
        Collections.sort(sortedTimes);
        
        for (LocalDateTime startTime : sortedTimes) {
            LocalDateTime endTime = startTime.plus(value, unit);
            
            Map<String, Map<String, Integer>> subjectGroups = timeGroups.get(startTime);
            
            for (String subject : subjectGroups.keySet()) {
                Map<String, Integer> objectCounts = subjectGroups.get(subject);
                
                for (Map.Entry<String, Integer> entry : objectCounts.entrySet()) {
                    String object = entry.getKey();
                    int count = entry.getValue();
                    
                    interactions.add(new InteractionRecord(startTime, endTime, count, subject, object));
                    totalInteractions += count;
                }
            }
        }
        
        // 创建元数据信息
        String timeRange = (minTime != null && maxTime != null) 
                ? minTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 至 " + 
                  maxTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                : "无数据";
        
        InteractionAnalysisResult.Metadata metadata = new InteractionAnalysisResult.Metadata(
                timeRange, 
                uniqueSubjects.size(), 
                uniqueObjects.size(),
                "IP交互关系分析，将源IP视为主体，目的IP视为客体，按" + interval + "时间间隔汇总"
        );
        
        return new InteractionAnalysisResult(interval, totalInteractions, interactions, metadata);
    }
    
    /**
     * 读取CSV文件
     */
    private List<IpRecord> readCsvFile(File file) throws IOException {
        List<IpRecord> records = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            CSVParser csvParser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);
            
            for (CSVRecord record : csvParser) {
                String srcIp = record.get("src_ip");
                String dstIp = record.get("dst_ip");
                String timeStr = record.get("time");
                
                LocalDateTime time = LocalDateTime.parse(timeStr, DATE_FORMATTER);
                
                records.add(new IpRecord(srcIp, dstIp, time));
            }
        }
        
        return records;
    }
    
    /**
     * 读取Excel文件
     */
    private List<IpRecord> readExcelFile(File file) throws IOException {
        List<IpRecord> records = new ArrayList<>();
        
        try (Workbook workbook = WorkbookFactory.create(file)) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // 获取标题行
            Row headerRow = sheet.getRow(0);
            int srcIpIndex = -1;
            int dstIpIndex = -1;
            int timeIndex = -1;
            
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                String header = cell.getStringCellValue().toLowerCase();
                
                if (header.equals("src_ip")) {
                    srcIpIndex = i;
                } else if (header.equals("dst_ip")) {
                    dstIpIndex = i;
                } else if (header.equals("time")) {
                    timeIndex = i;
                }
            }
            
            // 确保找到所有必要的列
            if (srcIpIndex == -1 || dstIpIndex == -1 || timeIndex == -1) {
                throw new IllegalArgumentException("Excel文件缺少必要的列");
            }
            
            // 读取数据
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                
                String srcIp = getCellValueAsString(row.getCell(srcIpIndex));
                String dstIp = getCellValueAsString(row.getCell(dstIpIndex));
                String timeStr = getCellValueAsString(row.getCell(timeIndex));
                
                LocalDateTime time = LocalDateTime.parse(timeStr, DATE_FORMATTER);
                
                records.add(new IpRecord(srcIp, dstIp, time));
            }
        }
        
        return records;
    }
    
    /**
     * 获取单元格的值作为字符串
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().format(DATE_FORMATTER);
                }
                return Double.toString(cell.getNumericCellValue());
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
    
    /**
     * 将时间向下取整到最近的间隔
     */
    private LocalDateTime roundToInterval(LocalDateTime time, int value, ChronoUnit unit) {
        if (unit == ChronoUnit.MINUTES) {
            int minute = time.getMinute();
            int roundedMinute = (minute / value) * value;
            return time.withMinute(roundedMinute).withSecond(0).withNano(0);
        } else {
            int hour = time.getHour();
            int roundedHour = (hour / value) * value;
            return time.withHour(roundedHour).withMinute(0).withSecond(0).withNano(0);
        }
    }
} 