package cn.iocoder.yudao.learning.visualization.service;

import cn.iocoder.yudao.learning.common.model.LearningDataModels;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import cn.iocoder.yudao.learning.common.model.LearningDataModels.ExecutionTrace;
import cn.iocoder.yudao.learning.common.model.LearningDataModels.MethodCall;
import cn.iocoder.yudao.learning.common.model.LearningDataModels.DataFlow;

/**
 * 代码执行可视化服务
 * 
 * 功能：
 * 1. 方法调用链路可视化
 * 2. 执行时序图生成
 * 3. 数据流向追踪
 * 4. 性能热点可视化
 * 5. 异常传播路径分析
 * 
 * @author 学习扩展
 */
@Slf4j
@Service
public class CodeExecutionVisualizationService {

    /**
     * 调用链追踪
     */
    private final Map<String, ExecutionTrace> executionTraces = new ConcurrentHashMap<>();
    
    /**
     * 方法调用栈
     */
    private final ThreadLocal<Stack<MethodCall>> callStack = ThreadLocal.withInitial(Stack::new);
    
    /**
     * 执行序列号生成器
     */
    private final AtomicInteger sequenceGenerator = new AtomicInteger(0);
    
    /**
     * 数据流追踪
     */
    private final Map<String, List<DataFlow>> dataFlows = new ConcurrentHashMap<>();

    /**
     * 开始方法执行追踪
     */
    public String startMethodTrace(String module, String method, Object[] params) {
        String traceId = generateTraceId();
        String threadName = Thread.currentThread().getName();
        
        LearningDataModels.MethodCall methodCall = LearningDataModels.MethodCall.builder()
                .traceId(traceId)
                .module(module)
                .method(method)
                .parameters(params)
                .startTime(LocalDateTime.now())
                .threadName(threadName)
                .sequence(sequenceGenerator.incrementAndGet())
                .depth(callStack.get().size())
                .build();
        
        // 添加到调用栈
        callStack.get().push(methodCall);
        
        // 创建或更新执行追踪
        ExecutionTrace trace = executionTraces.computeIfAbsent(traceId, k -> 
            ExecutionTrace.builder()
                .traceId(traceId)
                .startTime(LocalDateTime.now())
                .threadName(threadName)
                .methodCalls(Collections.synchronizedList(new ArrayList<>()))
                .build()
        );
        
        trace.getMethodCalls().add(methodCall);
        
        // 记录数据流入
        recordDataFlow(traceId, module, method, "INPUT", params);
        
        // 输出可视化信息
        printMethodStart(methodCall);
        
        return traceId;
    }

    /**
     * 结束方法执行追踪
     */
    public void endMethodTrace(String traceId, Object result) {
        if (!callStack.get().isEmpty()) {
            MethodCall methodCall = callStack.get().pop();
            methodCall.setEndTime(LocalDateTime.now());
            methodCall.setResult(result);
            methodCall.setSuccess(true);
            
            // 记录数据流出
            recordDataFlow(traceId, methodCall.getModule(), methodCall.getMethod(), "OUTPUT", result);
            
            // 输出可视化信息
            printMethodEnd(methodCall);
            
            // 如果是根方法调用，生成完整的执行报告
            if (callStack.get().isEmpty()) {
                generateExecutionReport(traceId);
            }
        }
    }

    /**
     * 记录方法执行异常
     */
    public void recordMethodException(String traceId, Exception exception) {
        if (!callStack.get().isEmpty()) {
            MethodCall methodCall = callStack.get().peek();
            methodCall.setException(exception);
            methodCall.setSuccess(false);
            methodCall.setEndTime(LocalDateTime.now());
            
            // 输出异常可视化信息
            printMethodException(methodCall, exception);
        }
    }

    /**
     * 记录数据流
     */
    private void recordDataFlow(String traceId, String module, String method, String direction, Object data) {
        DataFlow dataFlow = DataFlow.builder()
                .traceId(traceId)
                .module(module)
                .method(method)
                .direction(direction)
                .dataType(data != null ? data.getClass().getSimpleName() : "null")
                .dataSize(calculateDataSize(data))
                .timestamp(LocalDateTime.now())
                .build();
        
        dataFlows.computeIfAbsent(traceId, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(dataFlow);
    }

    /**
     * 生成执行报告
     */
    private void generateExecutionReport(String traceId) {
        ExecutionTrace trace = executionTraces.get(traceId);
        if (trace == null) return;
        
        trace.setEndTime(LocalDateTime.now());
        
        // 生成调用链可视化
        generateCallChainVisualization(trace);
        
        // 生成时序图
        generateSequenceDiagram(trace);
        
        // 生成性能分析
        generatePerformanceAnalysis(trace);
        
        // 生成数据流图
        generateDataFlowDiagram(traceId);
    }

    /**
     * 生成调用链可视化
     */
    private void generateCallChainVisualization(ExecutionTrace trace) {
        log.info("\n=== 调用链可视化 === TraceId: {}", trace.getTraceId());
        log.info("执行时间: {} -> {}", 
            trace.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")),
            trace.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")));
        log.info("总耗时: {}ms", calculateDuration(trace.getStartTime(), trace.getEndTime()));
        
        StringBuilder callChain = new StringBuilder();
        callChain.append("\n调用链路:\n");
        
        for (MethodCall call : trace.getMethodCalls()) {
            String indent = "  ".repeat(call.getDepth());
            String status = call.isSuccess() ? "✅" : "❌";
            long duration = call.getEndTime() != null ? 
                calculateDuration(call.getStartTime(), call.getEndTime()) : 0;
            
            callChain.append(String.format("%s%s [%d] %s.%s (%dms)%s\n",
                indent, status, call.getSequence(), call.getModule(), call.getMethod(), 
                duration, call.getException() != null ? " - " + call.getException().getMessage() : ""));
        }
        
        log.info(callChain.toString());
    }

    /**
     * 生成时序图
     */
    private void generateSequenceDiagram(ExecutionTrace trace) {
        log.info("\n=== 时序图 === TraceId: {}", trace.getTraceId());
        
        StringBuilder diagram = new StringBuilder();
        diagram.append("\n时间轴 (相对时间):\n");
        
        LocalDateTime baseTime = trace.getStartTime();
        
        for (MethodCall call : trace.getMethodCalls()) {
            long startOffset = calculateDuration(baseTime, call.getStartTime());
            long endOffset = call.getEndTime() != null ? 
                calculateDuration(baseTime, call.getEndTime()) : startOffset;
            
            String timeline = generateTimeline(startOffset, endOffset, call.getDepth());
            diagram.append(String.format("[%3dms-%3dms] %s %s.%s\n",
                startOffset, endOffset, timeline, call.getModule(), call.getMethod()));
        }
        
        log.info(diagram.toString());
    }

    /**
     * 生成性能分析
     */
    private void generatePerformanceAnalysis(ExecutionTrace trace) {
        log.info("\n=== 性能分析 === TraceId: {}", trace.getTraceId());
        
        // 统计各模块耗时
        Map<String, Long> moduleDurations = new HashMap<>();
        Map<String, Integer> moduleCounts = new HashMap<>();
        
        for (MethodCall call : trace.getMethodCalls()) {
            if (call.getEndTime() != null) {
                long duration = calculateDuration(call.getStartTime(), call.getEndTime());
                moduleDurations.merge(call.getModule(), duration, Long::sum);
                moduleCounts.merge(call.getModule(), 1, Integer::sum);
            }
        }
        
        // 输出模块性能统计
        StringBuilder analysis = new StringBuilder();
        analysis.append("\n模块性能统计:\n");
        
        moduleDurations.entrySet().stream()
            .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
            .forEach(entry -> {
                String module = entry.getKey();
                long totalDuration = entry.getValue();
                int count = moduleCounts.get(module);
                long avgDuration = totalDuration / count;
                
                analysis.append(String.format("  %s: 总耗时=%dms, 调用次数=%d, 平均耗时=%dms\n",
                    module, totalDuration, count, avgDuration));
            });
        
        // 找出最慢的方法
        Optional<MethodCall> slowestCall = trace.getMethodCalls().stream()
            .filter(call -> call.getEndTime() != null)
            .max((c1, c2) -> Long.compare(
                calculateDuration(c1.getStartTime(), c1.getEndTime()),
                calculateDuration(c2.getStartTime(), c2.getEndTime())
            ));
        
        if (slowestCall.isPresent()) {
            MethodCall call = slowestCall.get();
            long duration = calculateDuration(call.getStartTime(), call.getEndTime());
            analysis.append(String.format("\n🐌 最慢方法: %s.%s (%dms)\n", 
                call.getModule(), call.getMethod(), duration));
        }
        
        log.info(analysis.toString());
    }

    /**
     * 生成数据流图
     */
    private void generateDataFlowDiagram(String traceId) {
        List<DataFlow> flows = dataFlows.get(traceId);
        if (flows == null || flows.isEmpty()) return;
        
        log.info("\n=== 数据流图 === TraceId: {}", traceId);
        
        StringBuilder diagram = new StringBuilder();
        diagram.append("\n数据流向:\n");
        
        for (DataFlow flow : flows) {
            String arrow = "INPUT".equals(flow.getDirection()) ? "→" : "←";
            diagram.append(String.format("  %s %s %s.%s [%s, %d bytes]\n",
                flow.getDirection(), arrow, flow.getModule(), flow.getMethod(), 
                flow.getDataType(), flow.getDataSize()));
        }
        
        log.info(diagram.toString());
    }

    /**
     * 生成方法调用树
     */
    public void generateMethodCallTree(String traceId) {
        ExecutionTrace trace = executionTraces.get(traceId);
        if (trace == null) return;
        
        log.info("\n=== 方法调用树 === TraceId: {}", traceId);
        
        StringBuilder tree = new StringBuilder();
        tree.append("\n");
        
        // 构建树形结构
        Map<Integer, List<MethodCall>> depthMap = new HashMap<>();
        for (MethodCall call : trace.getMethodCalls()) {
            depthMap.computeIfAbsent(call.getDepth(), k -> new ArrayList<>()).add(call);
        }
        
        // 递归构建树
        buildCallTree(tree, depthMap, 0, "");
        
        log.info(tree.toString());
    }

    /**
     * 递归构建调用树
     */
    private void buildCallTree(StringBuilder tree, Map<Integer, List<MethodCall>> depthMap, 
                              int currentDepth, String prefix) {
        List<MethodCall> callsAtDepth = depthMap.get(currentDepth);
        if (callsAtDepth == null) return;
        
        for (int i = 0; i < callsAtDepth.size(); i++) {
            MethodCall call = callsAtDepth.get(i);
            boolean isLast = i == callsAtDepth.size() - 1;
            
            String connector = isLast ? "└── " : "├── ";
            String status = call.isSuccess() ? "✅" : "❌";
            long duration = call.getEndTime() != null ? 
                calculateDuration(call.getStartTime(), call.getEndTime()) : 0;
            
            tree.append(String.format("%s%s%s %s.%s (%dms)\n",
                prefix, connector, status, call.getModule(), call.getMethod(), duration));
            
            // 递归处理子调用
            String newPrefix = prefix + (isLast ? "    " : "│   ");
            buildCallTree(tree, depthMap, currentDepth + 1, newPrefix);
        }
    }

    /**
     * 输出方法开始信息
     */
    private void printMethodStart(MethodCall methodCall) {
        String indent = "  ".repeat(methodCall.getDepth());
        log.info("{}🚀 [{}] 开始执行: {}.{}", 
            indent, methodCall.getSequence(), methodCall.getModule(), methodCall.getMethod());
        
        if (methodCall.getParameters() != null && methodCall.getParameters().length > 0) {
            log.info("{}   参数: {}", indent, Arrays.toString(methodCall.getParameters()));
        }
    }

    /**
     * 输出方法结束信息
     */
    private void printMethodEnd(MethodCall methodCall) {
        String indent = "  ".repeat(methodCall.getDepth());
        long duration = calculateDuration(methodCall.getStartTime(), methodCall.getEndTime());
        
        log.info("{}✅ [{}] 执行完成: {}.{} ({}ms)", 
            indent, methodCall.getSequence(), methodCall.getModule(), methodCall.getMethod(), duration);
        
        if (methodCall.getResult() != null) {
            log.info("{}   返回: {}", indent, methodCall.getResult());
        }
    }

    /**
     * 输出方法异常信息
     */
    private void printMethodException(MethodCall methodCall, Exception exception) {
        String indent = "  ".repeat(methodCall.getDepth());
        long duration = calculateDuration(methodCall.getStartTime(), methodCall.getEndTime());
        
        log.error("{}❌ [{}] 执行异常: {}.{} ({}ms) - {}", 
            indent, methodCall.getSequence(), methodCall.getModule(), methodCall.getMethod(), 
            duration, exception.getMessage());
    }

    /**
     * 生成追踪ID
     */
    private String generateTraceId() {
        return "trace-" + System.currentTimeMillis() + "-" + 
               Thread.currentThread().getId() + "-" + 
               sequenceGenerator.incrementAndGet();
    }

    /**
     * 生成时间轴
     */
    private String generateTimeline(long startOffset, long endOffset, int depth) {
        int maxWidth = 20;
        int startPos = (int) (startOffset * maxWidth / 1000); // 假设1000ms为最大宽度
        int endPos = (int) (endOffset * maxWidth / 1000);
        
        StringBuilder timeline = new StringBuilder();
        for (int i = 0; i < maxWidth; i++) {
            if (i >= startPos && i <= endPos) {
                timeline.append("█");
            } else {
                timeline.append("·");
            }
        }
        
        return timeline.toString();
    }

    /**
     * 计算数据大小
     */
    private int calculateDataSize(Object data) {
        if (data == null) return 0;
        if (data instanceof String) return ((String) data).length();
        if (data instanceof Collection) return ((Collection<?>) data).size() * 10; // 估算
        return data.toString().length(); // 简化计算
    }

    /**
     * 计算时间差（毫秒）
     */
    private long calculateDuration(LocalDateTime start, LocalDateTime end) {
        return java.time.Duration.between(start, end).toMillis();
    }

    /**
     * 清空追踪数据
     */
    public void clearTraceData() {
        executionTraces.clear();
        dataFlows.clear();
        callStack.remove();
        sequenceGenerator.set(0);
        log.info("=== 可视化 === 已清空所有追踪数据");
    }

    /**
     * 获取所有追踪信息
     */
    public Map<String, ExecutionTrace> getAllTraces() {
        return new HashMap<>(executionTraces);
    }
}
