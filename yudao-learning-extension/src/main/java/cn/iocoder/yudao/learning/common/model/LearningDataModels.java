package cn.iocoder.yudao.learning.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import java.util.*;



/**
 * 学习模块通用数据模型定义
 * 
 * @author 学习扩展
 */
public class LearningDataModels {

    // ==================== 多租户相关模型 ====================
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TenantLearningRecord {
        private Long tenantId;
        private List<TenantOperation> operations;
        private Integer totalOperations;
        private Integer successfulOperations;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TenantOperation {
        private String operation;
        private Boolean success;
        private String errorMessage;
        private LocalDateTime timestamp;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TenantContextSwitchRecord {
        private Long originalTenantId;
        private Long targetTenantId;
        private String operation;
        private LocalDateTime switchTime;
        private LocalDateTime endTime;
        private String threadName;
        private Boolean success;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataIsolationVerification {
        private Long expectedTenantId;
        private Long actualTenantId;
        private String operation;
        private LocalDateTime verificationTime;
        private boolean isolated;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MultiTenantLearningStats {
        private Integer totalTenants;
        private Integer totalContextSwitches;
        private Integer totalIsolationVerifications;
        private Map<Long, TenantLearningRecord> tenantRecords;
        private List<TenantContextSwitchRecord> contextSwitches;
        private List<DataIsolationVerification> isolationVerifications;
    }
    
    // ==================== 数据权限相关模型 ====================
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataPermissionOperation {
        private String operation;
        private String result;
        private String description;
        private Long tenantId;
        private LocalDateTime timestamp;
    }
    
    // ==================== 知识图谱相关模型 ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KnowledgeNode {
        private String id;
        private String name;
        private String type;
        private String description;
        private Map<String, Object> properties;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KnowledgeRelation {
        private String id;
        private String sourceNodeId;
        private String targetNodeId;
        private String relationType;
        private String description;
        private Double weight;
        private Map<String, Object> properties;
        private LocalDateTime createTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LearningPath {
        private String id;
        private String name;
        private String description;
        private List<String> nodeIds;
        private List<String> relationIds;
        private Integer difficulty;
        private Integer estimatedHours;
        private String category;
        private LocalDateTime createTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TechStack {
        private String id;
        private String name;
        private String version;
        private String description;
        private List<String> dependencies;
        private String category;
        private String officialSite;
        private LocalDateTime createTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LearningPathRecommendation {
        private String pathId;
        private String pathName;
        private Double score;
        private String reason;
        private List<String> prerequisites;
        private Integer estimatedHours;
        private String difficulty;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KnowledgeAnalysisResult {
        private String analysisId;
        private String analysisType;
        private Map<String, Object> results;
        private List<String> insights;
        private List<String> recommendations;
        private LocalDateTime analysisTime;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SqlInterceptionRecord {
        private String originalSql;
        private String interceptedSql;
        private Long tenantId;
        private String sqlType;
        private boolean intercepted;
        private LocalDateTime timestamp;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TenantDataAccessRecord {
        private Long tenantId;
        private List<TenantDataAccessOperation> accessOperations;
        private Integer totalAccess;
        private Integer successfulAccess;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TenantDataAccessOperation {
        private String operation;
        private Boolean success;
        private LocalDateTime timestamp;
    }
    
    // ==================== 学习分析相关模型 ====================
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MethodCallRecord {
        private String module;
        private String method;
        private long duration;
        private boolean success;
        private LocalDateTime timestamp;
        private int paramCount;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModuleAccessStats {
        private String moduleName;
        private int totalCalls;
        private int successfulCalls;
        
        public ModuleAccessStats(String moduleName) {
            this.moduleName = moduleName;
            this.totalCalls = 0;
            this.successfulCalls = 0;
        }
        
        public void addCall(long duration, boolean success) {
            this.totalCalls++;
            if (success) {
                this.successfulCalls++;
            }
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LearningPathRecord {
        private String module;
        private String method;
        private LocalDateTime timestamp;
        private String threadName;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LearningReport {
        private LocalDateTime reportTime;
        private MethodAnalysisResult methodAnalysis;
        private ModuleAnalysisResult moduleAnalysis;
        private PathAnalysisResult pathAnalysis;
        private PerformanceAnalysisResult performanceAnalysis;
        private List<String> recommendations;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MethodAnalysisResult {
        private int totalMethods;
        private int totalCalls;
        private List<String> mostFrequentMethods;
        private List<String> slowestMethods;
        private Map<String, MethodStats> methodStats;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MethodStats {
        private int callCount;
        private long totalDuration;
        private long avgDuration;
        private long maxDuration;
        private long minDuration;
        private double successRate;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModuleAnalysisResult {
        private int totalModules;
        private List<ModuleAccessStats> moduleStats;
        private String mostActiveModule;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PathAnalysisResult {
        private int totalPathRecords;
        private int recentPathRecords;
        private Map<String, Integer> moduleSequence;
        private long learningDuration;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceAnalysisResult {
        private long totalCalls;
        private double avgResponseTime;
        private List<String> performanceBottlenecks;
    }
    
    // ==================== 学习指导相关模型 ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LearningGuide {
        private String pathId;
        private String name;
        private String description;
        private String difficulty;
        private int estimatedHours;
        private int estimatedDays;
        private List<LearningModule> modules;
        private List<String> knowledgeSequence;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LearningModule {
        private String moduleId;
        private String name;
        private String description;
        private List<String> keyMethods;
        private List<String> learningGoals;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LearningTip {
        private String category;
        private String content;
        private String importance;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserLearningProgress {
        private String userId;
        private Map<String, Integer> moduleCallCounts;
        private Set<String> completedModules;
        private Set<String> completedPaths;
        private int totalMethodsCalled;
        private int successfulCalls;
        private long totalLearningTime;
        
        public UserLearningProgress(String userId) {
            this.userId = userId;
            this.moduleCallCounts = new HashMap<>();
            this.completedModules = new HashSet<>();
            this.completedPaths = new HashSet<>();
            this.totalMethodsCalled = 0;
            this.successfulCalls = 0;
            this.totalLearningTime = 0;
        }
        
        public void addMethodCall(String module, String method, boolean success) {
            this.totalMethodsCalled++;
            if (success) {
                this.successfulCalls++;
            }
            this.moduleCallCounts.merge(module, 1, Integer::sum);
        }
        
        public double getSuccessRate() {
            return totalMethodsCalled > 0 ? (double) successfulCalls / totalMethodsCalled : 0.0;
        }
        
        public int getModuleCallCount(String module) {
            return moduleCallCounts.getOrDefault(module, 0);
        }
        
        public boolean isModuleCompleted(String moduleId) {
            return completedModules.contains(moduleId);
        }
        
        public void markModuleCompleted(String moduleId) {
            completedModules.add(moduleId);
        }
        
        public boolean isPathCompleted(String pathId) {
            return completedPaths.contains(pathId);
        }
        
        public void markPathCompleted(String pathId) {
            completedPaths.add(pathId);
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LearningGuideRecommendation {
        private String userId;
        private LearningPath recommendedPath;
        private LearningModule nextModule;
        private double completionPercentage;
        private int estimatedTimeToComplete;
        private List<String> personalizedTips;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MethodExecutionGuide {
        private String module;
        private String method;
        private String description;
        private List<LearningTip> learningTips;
        private List<String> contextualGuides;
        private List<String> analysisPoints;
        private List<String> relatedConcepts;
        private List<String> debuggingTips;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LearningProgressSummary {
        private String userId;
        private long totalLearningTime;
        private Set<String> completedModules;
        private int totalMethodsCalled;
        private double successRate;
        private String currentLevel;
        private List<String> achievements;
        private List<String> nextSteps;
    }

    // ==================== 可视化相关模型 ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExecutionTrace {
        private String traceId;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String threadName;
        private List<MethodCall> methodCalls;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MethodCall {
        private String traceId;
        private String module;
        private String method;
        private Object[] parameters;
        private Object result;
        private Exception exception;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String threadName;
        private int sequence;
        private int depth;
        private boolean success;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataFlow {
        private String traceId;
        private String module;
        private String method;
        private String direction;
        private String dataType;
        private int dataSize;
        private LocalDateTime timestamp;
    }

    // ==================== 测试生成相关模型 ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MethodInvocation {
        private String module;
        private String method;
        private Object[] parameters;
        private Object result;
        private Exception exception;
        private long duration;
        private LocalDateTime timestamp;
        private boolean success;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestCaseTemplate {
        private String methodName;
        private String description;
        private List<TestScenario> testScenarios;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestScenario {
        private String name;
        private String type;
        private String description;
        private String expectedResult;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeneratedTestCase {
        private String testName;
        private String testType;
        private String description;
        private String testCode;
        private Object expectedResult;
        private LocalDateTime generatedTime;
    }

    // ==================== 知识图谱相关模型 ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KnowledgeGraphNode {
        private String id;
        private String name;
        private String type;
        private String description;
        private List<String> keywords;
        private int importance;
        private int difficulty;
        private LocalDateTime createdTime;
        private int learningCount;
        private int successCount;

        public void incrementLearningCount() {
            this.learningCount++;
        }

        public void incrementSuccessCount() {
            this.successCount++;
        }

        public double getSuccessRate() {
            return learningCount > 0 ? (double) successCount / learningCount : 0.0;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KnowledgeGraphRelation {
        private String fromNodeId;
        private String toNodeId;
        private String relationType;
        private String description;
        private double strength;
        private LocalDateTime createdTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KnowledgeGraphAnalysisResult {
        private KnowledgeNode targetNode;
        private List<KnowledgeNode> directlyConnected;
        private List<KnowledgeNode> indirectlyConnected;
        private double influenceScore;
        private List<String> learningAdvice;
        private LocalDateTime analysisTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TechStackInfo {
        private String id;
        private String name;
        private String category;
        private String description;
        private List<String> dependencies;
    }
}
