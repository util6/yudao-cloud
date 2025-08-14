package cn.iocoder.yudao.learning.analytics.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import cn.iocoder.yudao.learning.common.model.LearningDataModels.*;


/**
 * 学习数据分析服务
 * 
 * 功能：
 * 1. 方法调用性能分析
 * 2. 业务模块学习进度统计
 * 3. 代码执行路径分析
 * 4. 学习热点识别
 * 5. 性能瓶颈分析
 * 
 * @author 学习扩展
 */
@Slf4j
@Service
public class LearningAnalyticsService {

    /**
     * 方法调用性能数据
     */
    private final Map<String, List<MethodCallRecord>> methodCallRecords = new ConcurrentHashMap<>();
    
    /**
     * 学习路径记录
     */
    private final List<LearningPathRecord> learningPaths = Collections.synchronizedList(new ArrayList<>());
    
    /**
     * 业务模块访问统计
     */
    private final Map<String, ModuleAccessStats> moduleStats = new ConcurrentHashMap<>();

    /**
     * 记录方法调用
     */
    public void recordMethodCall(String module, String method, long duration, boolean success, Object... params) {
        String key = module + "." + method;
        
        MethodCallRecord record = MethodCallRecord.builder()
                .module(module)
                .method(method)
                .duration(duration)
                .success(success)
                .timestamp(LocalDateTime.now())
                .paramCount(params != null ? params.length : 0)
                .build();
        
        methodCallRecords.computeIfAbsent(key, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(record);
        
        // 更新模块统计
        updateModuleStats(module, duration, success);
        
        // 记录学习路径
        recordLearningPath(module, method);
    }

    /**
     * 更新模块统计
     */
    private void updateModuleStats(String module, long duration, boolean success) {
        moduleStats.computeIfAbsent(module, k -> new ModuleAccessStats(module))
                .addCall(duration, success);
    }

    /**
     * 记录学习路径
     */
    private void recordLearningPath(String module, String method) {
        LearningPathRecord pathRecord = LearningPathRecord.builder()
                .module(module)
                .method(method)
                .timestamp(LocalDateTime.now())
                .threadName(Thread.currentThread().getName())
                .build();
        
        learningPaths.add(pathRecord);
        
        // 保持最近1000条记录
        if (learningPaths.size() > 1000) {
            learningPaths.subList(0, learningPaths.size() - 1000).clear();
        }
    }

    /**
     * 生成学习报告
     */
    public LearningReport generateLearningReport() {
        return LearningReport.builder()
                .reportTime(LocalDateTime.now())
                .methodAnalysis(analyzeMethodCalls())
                .moduleAnalysis(analyzeModules())
                .pathAnalysis(analyzeLearningPaths())
                .performanceAnalysis(analyzePerformance())
                .recommendations(generateRecommendations())
                .build();
    }

    /**
     * 分析方法调用
     */
    private MethodAnalysisResult analyzeMethodCalls() {
        Map<String, MethodStats> methodStats = new HashMap<>();
        
        methodCallRecords.forEach((key, records) -> {
            if (!records.isEmpty()) {
                MethodStats stats = calculateMethodStats(records);
                methodStats.put(key, stats);
            }
        });
        
        // 找出最频繁调用的方法
        List<String> mostFrequentMethods = methodStats.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().getCallCount(), e1.getValue().getCallCount()))
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        // 找出最慢的方法
        List<String> slowestMethods = methodStats.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue().getAvgDuration(), e1.getValue().getAvgDuration()))
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        return MethodAnalysisResult.builder()
                .totalMethods(methodStats.size())
                .totalCalls(methodStats.values().stream().mapToInt(MethodStats::getCallCount).sum())
                .mostFrequentMethods(mostFrequentMethods)
                .slowestMethods(slowestMethods)
                .methodStats(methodStats)
                .build();
    }

    /**
     * 计算方法统计信息
     */
    private MethodStats calculateMethodStats(List<MethodCallRecord> records) {
        int callCount = records.size();
        long totalDuration = records.stream().mapToLong(MethodCallRecord::getDuration).sum();
        long avgDuration = totalDuration / callCount;
        long maxDuration = records.stream().mapToLong(MethodCallRecord::getDuration).max().orElse(0);
        long minDuration = records.stream().mapToLong(MethodCallRecord::getDuration).min().orElse(0);
        
        int successCount = (int) records.stream().filter(MethodCallRecord::isSuccess).count();
        double successRate = (double) successCount / callCount * 100;
        
        return MethodStats.builder()
                .callCount(callCount)
                .totalDuration(totalDuration)
                .avgDuration(avgDuration)
                .maxDuration(maxDuration)
                .minDuration(minDuration)
                .successRate(successRate)
                .build();
    }

    /**
     * 分析模块访问情况
     */
    private ModuleAnalysisResult analyzeModules() {
        List<ModuleAccessStats> sortedStats = moduleStats.values().stream()
                .sorted((s1, s2) -> Integer.compare(s2.getTotalCalls(), s1.getTotalCalls()))
                .collect(Collectors.toList());
        
        return ModuleAnalysisResult.builder()
                .totalModules(moduleStats.size())
                .moduleStats(sortedStats)
                .mostActiveModule(sortedStats.isEmpty() ? null : sortedStats.get(0).getModuleName())
                .build();
    }

    /**
     * 分析学习路径
     */
    private PathAnalysisResult analyzeLearningPaths() {
        // 分析最近的学习路径
        List<LearningPathRecord> recentPaths = learningPaths.stream()
                .filter(path -> path.getTimestamp().isAfter(LocalDateTime.now().minusHours(1)))
                .collect(Collectors.toList());
        
        // 统计模块访问顺序
        Map<String, Integer> moduleSequence = new LinkedHashMap<>();
        for (LearningPathRecord path : recentPaths) {
            moduleSequence.merge(path.getModule(), 1, Integer::sum);
        }
        
        return PathAnalysisResult.builder()
                .totalPathRecords(learningPaths.size())
                .recentPathRecords(recentPaths.size())
                .moduleSequence(moduleSequence)
                .learningDuration(calculateLearningDuration())
                .build();
    }

    /**
     * 性能分析
     */
    private PerformanceAnalysisResult analyzePerformance() {
        // 找出性能瓶颈
        List<String> performanceBottlenecks = methodCallRecords.entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .filter(entry -> {
                    double avgDuration = entry.getValue().stream()
                            .mapToLong(MethodCallRecord::getDuration)
                            .average()
                            .orElse(0);
                    return avgDuration > 100; // 超过100ms的方法
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        // 计算总体性能指标
        long totalCalls = methodCallRecords.values().stream()
                .mapToInt(List::size)
                .sum();
        
        double avgResponseTime = methodCallRecords.values().stream()
                .flatMap(List::stream)
                .mapToLong(MethodCallRecord::getDuration)
                .average()
                .orElse(0);
        
        return PerformanceAnalysisResult.builder()
                .totalCalls(totalCalls)
                .avgResponseTime(avgResponseTime)
                .performanceBottlenecks(performanceBottlenecks)
                .build();
    }

    /**
     * 生成学习建议
     */
    private List<String> generateRecommendations() {
        List<String> recommendations = new ArrayList<>();
        
        // 基于访问频率的建议
        ModuleAnalysisResult moduleAnalysis = analyzeModules();
        if (moduleAnalysis.getMostActiveModule() != null) {
            recommendations.add("建议深入学习 " + moduleAnalysis.getMostActiveModule() + " 模块，这是你最常使用的模块");
        }
        
        // 基于性能的建议
        PerformanceAnalysisResult perfAnalysis = analyzePerformance();
        if (!perfAnalysis.getPerformanceBottlenecks().isEmpty()) {
            recommendations.add("建议关注以下性能瓶颈方法的实现原理：" + 
                String.join(", ", perfAnalysis.getPerformanceBottlenecks()));
        }
        
        // 基于学习路径的建议
        PathAnalysisResult pathAnalysis = analyzeLearningPaths();
        if (pathAnalysis.getRecentPathRecords() < 10) {
            recommendations.add("建议增加实践操作，通过更多的功能调用来深入学习");
        }
        
        return recommendations;
    }

    /**
     * 计算学习时长
     */
    private long calculateLearningDuration() {
        if (learningPaths.isEmpty()) {
            return 0;
        }
        
        LocalDateTime start = learningPaths.get(0).getTimestamp();
        LocalDateTime end = learningPaths.get(learningPaths.size() - 1).getTimestamp();
        
        return java.time.Duration.between(start, end).toMinutes();
    }

    /**
     * 打印学习分析报告
     */
    public void printLearningAnalysisReport() {
        LearningReport report = generateLearningReport();
        
        log.info("\n" + 
            "=== 学习分析报告 === [{}]\n" +
            "=== 方法调用分析 ===\n" +
            "总方法数: {}, 总调用次数: {}\n" +
            "最频繁方法: {}\n" +
            "最慢方法: {}\n" +
            "=== 模块分析 ===\n" +
            "总模块数: {}, 最活跃模块: {}\n" +
            "=== 学习路径分析 ===\n" +
            "学习时长: {}分钟, 最近操作: {}次\n" +
            "=== 性能分析 ===\n" +
            "平均响应时间: {:.2f}ms\n" +
            "性能瓶颈: {}\n" +
            "=== 学习建议 ===\n" +
            "{}",
            report.getReportTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            report.getMethodAnalysis().getTotalMethods(),
            report.getMethodAnalysis().getTotalCalls(),
            String.join(", ", report.getMethodAnalysis().getMostFrequentMethods()),
            String.join(", ", report.getMethodAnalysis().getSlowestMethods()),
            report.getModuleAnalysis().getTotalModules(),
            report.getModuleAnalysis().getMostActiveModule(),
            report.getPathAnalysis().getLearningDuration(),
            report.getPathAnalysis().getRecentPathRecords(),
            report.getPerformanceAnalysis().getAvgResponseTime(),
            String.join(", ", report.getPerformanceAnalysis().getPerformanceBottlenecks()),
            String.join("\n", report.getRecommendations())
        );
    }

    /**
     * 清空分析数据
     */
    public void clearAnalysisData() {
        methodCallRecords.clear();
        learningPaths.clear();
        moduleStats.clear();
        log.info("=== 学习扩展 === 已清空所有分析数据");
    }

    // 内部数据类定义...
    // (由于篇幅限制，这里省略了所有的Builder类和数据类定义)
}
