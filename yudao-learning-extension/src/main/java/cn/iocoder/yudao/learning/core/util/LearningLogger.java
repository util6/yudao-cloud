package cn.iocoder.yudao.learning.core.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 学习日志工具类
 * 
 * 用于记录学习过程中的关键信息，包括：
 * 1. 方法调用链路追踪
 * 2. 参数和返回值记录
 * 3. 执行时间统计
 * 4. 异常信息记录
 * 5. 业务逻辑分析
 * 
 * @author 学习者
 */
@Slf4j
public class LearningLogger {

    /**
     * 日志前缀，用于区分学习日志和系统日志
     */
    private static final String LOG_PREFIX = "=== 学习扩展 ===";
    
    /**
     * 时间格式化器
     */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    /**
     * 方法调用计数器 - 用于统计各个方法的调用次数
     */
    private static final ConcurrentHashMap<String, AtomicLong> METHOD_CALL_COUNTER = new ConcurrentHashMap<>();
    
    /**
     * 方法执行时间统计 - 用于性能分析
     */
    private static final ConcurrentHashMap<String, Long> METHOD_EXECUTION_TIME = new ConcurrentHashMap<>();

    /**
     * 记录方法开始执行
     * 
     * @param moduleName 模块名称（如：鉴权模块、缓存模块等）
     * @param methodName 方法名称
     * @param params 方法参数
     * @return 执行开始时间戳，用于计算执行时长
     */
    public static long logMethodStart(String moduleName, String methodName, Object... params) {
        long startTime = System.currentTimeMillis();
        String currentTime = LocalDateTime.now().format(TIME_FORMATTER);
        
        // 增加方法调用计数
        String methodKey = moduleName + "." + methodName;
        METHOD_CALL_COUNTER.computeIfAbsent(methodKey, k -> new AtomicLong(0)).incrementAndGet();
        
        log.info("{} [{}] 方法开始执行: {}.{}", 
                LOG_PREFIX, currentTime, moduleName, methodName);
        
        // 记录方法参数（如果有的话）
        if (params != null && params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                log.info("{} [{}] 参数[{}]: {}", 
                        LOG_PREFIX, currentTime, i, formatObject(params[i]));
            }
        }
        
        return startTime;
    }

    /**
     * 记录方法执行结束
     * 
     * @param moduleName 模块名称
     * @param methodName 方法名称
     * @param startTime 开始时间戳
     * @param result 方法返回结果
     */
    public static void logMethodEnd(String moduleName, String methodName, long startTime, Object result) {
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        String currentTime = LocalDateTime.now().format(TIME_FORMATTER);
        
        // 记录执行时间
        String methodKey = moduleName + "." + methodName;
        METHOD_EXECUTION_TIME.put(methodKey, executionTime);
        
        log.info("{} [{}] 方法执行完成: {}.{}, 耗时: {}ms", 
                LOG_PREFIX, currentTime, moduleName, methodName, executionTime);
        
        // 记录返回结果
        if (result != null) {
            log.info("{} [{}] 返回结果: {}", 
                    LOG_PREFIX, currentTime, formatObject(result));
        }
        
        // 性能警告：如果执行时间超过1秒，记录警告日志
        if (executionTime > 1000) {
            log.warn("{} [{}] 性能警告: {}.{} 执行时间过长: {}ms", 
                    LOG_PREFIX, currentTime, moduleName, methodName, executionTime);
        }
    }

    /**
     * 记录方法执行异常
     * 
     * @param moduleName 模块名称
     * @param methodName 方法名称
     * @param startTime 开始时间戳
     * @param exception 异常信息
     */
    public static void logMethodException(String moduleName, String methodName, long startTime, Exception exception) {
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        String currentTime = LocalDateTime.now().format(TIME_FORMATTER);
        
        log.error("{} [{}] 方法执行异常: {}.{}, 耗时: {}ms, 异常: {}", 
                LOG_PREFIX, currentTime, moduleName, methodName, executionTime, exception.getMessage());
        
        // 记录异常堆栈（仅记录前5行，避免日志过长）
        StackTraceElement[] stackTrace = exception.getStackTrace();
        for (int i = 0; i < Math.min(5, stackTrace.length); i++) {
            log.error("{} [{}] 异常堆栈[{}]: {}", 
                    LOG_PREFIX, currentTime, i, stackTrace[i].toString());
        }
    }

    /**
     * 记录业务逻辑分析
     * 
     * @param moduleName 模块名称
     * @param analysisPoint 分析点描述
     * @param analysisContent 分析内容
     */
    public static void logBusinessAnalysis(String moduleName, String analysisPoint, String analysisContent) {
        String currentTime = LocalDateTime.now().format(TIME_FORMATTER);
        log.info("{} [{}] 业务分析 [{}] {}: {}", 
                LOG_PREFIX, currentTime, moduleName, analysisPoint, analysisContent);
    }

    /**
     * 记录数据流转
     * 
     * @param moduleName 模块名称
     * @param flowStep 流转步骤
     * @param data 数据内容
     */
    public static void logDataFlow(String moduleName, String flowStep, Object data) {
        String currentTime = LocalDateTime.now().format(TIME_FORMATTER);
        log.info("{} [{}] 数据流转 [{}] {}: {}", 
                LOG_PREFIX, currentTime, moduleName, flowStep, formatObject(data));
    }

    /**
     * 记录学习心得
     * 
     * @param moduleName 模块名称
     * @param insight 学习心得
     */
    public static void logLearningInsight(String moduleName, String insight) {
        String currentTime = LocalDateTime.now().format(TIME_FORMATTER);
        log.info("{} [{}] 学习心得 [{}]: {}", 
                LOG_PREFIX, currentTime, moduleName, insight);
    }

    /**
     * 打印方法调用统计信息
     */
    public static void printMethodCallStatistics() {
        log.info("{} ========== 方法调用统计 ==========", LOG_PREFIX);
        METHOD_CALL_COUNTER.forEach((method, count) -> {
            Long executionTime = METHOD_EXECUTION_TIME.get(method);
            log.info("{} 方法: {}, 调用次数: {}, 平均耗时: {}ms", 
                    LOG_PREFIX, method, count.get(), 
                    executionTime != null ? executionTime / count.get() : "未知");
        });
        log.info("{} ================================", LOG_PREFIX);
    }

    /**
     * 格式化对象为字符串，用于日志输出
     * 
     * @param obj 要格式化的对象
     * @return 格式化后的字符串
     */
    private static String formatObject(Object obj) {
        if (obj == null) {
            return "null";
        }
        
        // 如果是字符串且过长，则截取前200个字符
        if (obj instanceof String) {
            String str = (String) obj;
            return str.length() > 200 ? str.substring(0, 200) + "..." : str;
        }
        
        // 尝试转换为JSON格式，如果失败则使用toString
        try {
            String json = JSONUtil.toJsonStr(obj);
            return json.length() > 500 ? json.substring(0, 500) + "..." : json;
        } catch (Exception e) {
            return obj.toString();
        }
    }

    /**
     * 清空统计信息（用于测试或重置）
     */
    public static void clearStatistics() {
        METHOD_CALL_COUNTER.clear();
        METHOD_EXECUTION_TIME.clear();
        log.info("{} 统计信息已清空", LOG_PREFIX);
    }
}
