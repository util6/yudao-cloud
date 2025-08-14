package cn.iocoder.yudao.learning.tenant.service;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.learning.core.util.LearningLogger;
import cn.iocoder.yudao.learning.common.model.LearningDataModels.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 多租户学习扩展服务
 * 
 * 学习重点：
 * 1. 多租户架构设计原理
 * 2. 数据隔离策略实现
 * 3. 租户上下文管理
 * 4. 租户间数据安全
 * 5. SaaS模式的技术实现
 * 6. 租户生命周期管理
 * 7. 多租户性能优化
 * 
 * @author 学习扩展
 */
@Slf4j
@Service
public class LearningTenantService {

    /**
     * 租户操作学习记录
     */
    private final Map<Long, TenantLearningRecord> tenantLearningRecords = new ConcurrentHashMap<>();
    
    /**
     * 租户上下文切换记录
     */
    private final List<TenantContextSwitchRecord> contextSwitchRecords = Collections.synchronizedList(new ArrayList<>());
    
    /**
     * 数据隔离验证记录
     */
    private final List<DataIsolationVerification> isolationVerifications = Collections.synchronizedList(new ArrayList<>());

    public void validTenant(Long tenantId) {
        long startTime = LearningLogger.logMethodStart("多租户模块", "validTenant", tenantId);

        try {
            // === 学习要点1: 租户有效性校验 ===
            logLearningPoint("租户有效性校验",
                "检查租户是否存在、是否启用、是否在有效期内", tenantId);

            // 记录当前租户上下文
            Long currentTenantId = TenantContextHolder.getTenantId();
            logTenantContext("validTenant调用前", currentTenantId, tenantId);

            // 模拟租户校验逻辑（在学习模式下）
            if (tenantId == null || tenantId <= 0) {
                throw new IllegalArgumentException("租户ID不能为空或小于等于0");
            }

            // 模拟检查租户是否存在（简化实现）
            if (tenantId > 100) {
                throw new RuntimeException("租户不存在");
            }

            // === 学习要点2: 租户校验成功后的处理 ===
            logLearningPoint("租户校验成功",
                "租户校验通过，可以安全访问该租户的数据", tenantId);

            // 记录租户学习信息
            recordTenantLearning(tenantId, "validTenant", true, null);

            LearningLogger.logMethodEnd("多租户模块", "validTenant", startTime, "校验成功");

        } catch (Exception e) {
            // === 学习要点3: 租户校验失败的处理 ===
            logLearningPoint("租户校验失败",
                "租户不存在或已禁用，需要拒绝访问并返回错误", tenantId);

            recordTenantLearning(tenantId, "validTenant", false, e.getMessage());

            LearningLogger.logMethodException("多租户模块", "validTenant", startTime, e);
            throw e;
        }
    }

    /**
     * 学习租户上下文切换机制
     */
    public <T> T executeWithTenant(Long tenantId, String operation, Callable<T> callable) {
        long startTime = LearningLogger.logMethodStart("多租户模块", "executeWithTenant", tenantId, operation);
        
        // === 学习要点4: 租户上下文切换 ===
        logLearningPoint("租户上下文切换", 
            "使用TenantUtils.execute()方法安全切换租户上下文", tenantId);
        
        Long originalTenantId = TenantContextHolder.getTenantId();
        
        try {
            // 记录上下文切换
            TenantContextSwitchRecord switchRecord = TenantContextSwitchRecord.builder()
                    .originalTenantId(originalTenantId)
                    .targetTenantId(tenantId)
                    .operation(operation)
                    .switchTime(LocalDateTime.now())
                    .threadName(Thread.currentThread().getName())
                    .build();
            
            contextSwitchRecords.add(switchRecord);
            
            log.info("=== 多租户学习 === 租户上下文切换: {} -> {} (操作: {})", 
                originalTenantId, tenantId, operation);
            
            // 使用TenantUtils执行租户切换
            T result = TenantUtils.execute(tenantId, callable);
            
            // === 学习要点5: 上下文切换后的数据访问 ===
            logLearningPoint("租户数据访问", 
                "在指定租户上下文中执行操作，确保数据隔离", tenantId);
            
            // 验证数据隔离
            verifyDataIsolation(tenantId, operation);
            
            switchRecord.setSuccess(true);
            switchRecord.setEndTime(LocalDateTime.now());
            
            LearningLogger.logMethodEnd("多租户模块", "executeWithTenant", startTime, result);
            
            return result;
            
        } catch (Exception e) {
            log.error("=== 多租户学习 === 租户上下文切换失败: {} -> {} (操作: {}), 错误: {}", 
                originalTenantId, tenantId, operation, e.getMessage());
            
            LearningLogger.logMethodException("多租户模块", "executeWithTenant", startTime, e);
            throw e;
            
        } finally {
            // === 学习要点6: 上下文恢复 ===
            logLearningPoint("租户上下文恢复", 
                "操作完成后，租户上下文会自动恢复到原始状态", originalTenantId);
            
            Long finalTenantId = TenantContextHolder.getTenantId();
            log.info("=== 多租户学习 === 租户上下文恢复: 当前租户ID = {}", finalTenantId);
        }
    }

    /**
     * 学习多租户数据查询
     */
    public void demonstrateMultiTenantDataAccess() {
        long startTime = LearningLogger.logMethodStart("多租户模块", "demonstrateMultiTenantDataAccess");
        
        try {
            // === 学习要点7: 多租户数据访问演示 ===
            logLearningPoint("多租户数据访问演示", 
                "演示不同租户访问各自数据的隔离效果", null);
            
            // 获取当前租户ID
            Long currentTenantId = TenantContextHolder.getTenantId();
            log.info("=== 多租户学习 === 当前租户ID: {}", currentTenantId);
            
            if (currentTenantId != null) {
                // 演示当前租户数据访问
                demonstrateTenantDataAccess(currentTenantId, "当前租户");
                
                // 演示切换到其他租户
                Long otherTenantId = findOtherTenantId(currentTenantId);
                if (otherTenantId != null) {
                    executeWithTenant(otherTenantId, "演示数据隔离", () -> {
                        demonstrateTenantDataAccess(otherTenantId, "其他租户");
                        return null;
                    });
                }
            } else {
                log.warn("=== 多租户学习 === 当前没有租户上下文，无法演示多租户数据访问");
            }
            
            LearningLogger.logMethodEnd("多租户模块", "demonstrateMultiTenantDataAccess", startTime, "演示完成");
            
        } catch (Exception e) {
            LearningLogger.logMethodException("多租户模块", "demonstrateMultiTenantDataAccess", startTime, e);
        }
    }

    /**
     * 学习租户数据隔离验证
     */
    private void verifyDataIsolation(Long tenantId, String operation) {
        // === 学习要点8: 数据隔离验证 ===
        logLearningPoint("数据隔离验证", 
            "验证当前操作只能访问指定租户的数据", tenantId);
        
        Long currentContextTenantId = TenantContextHolder.getTenantId();
        
        DataIsolationVerification verification = DataIsolationVerification.builder()
                .expectedTenantId(tenantId)
                .actualTenantId(currentContextTenantId)
                .operation(operation)
                .verificationTime(LocalDateTime.now())
                .isolated(Objects.equals(tenantId, currentContextTenantId))
                .build();
        
        isolationVerifications.add(verification);
        
        if (verification.isIsolated()) {
            log.info("=== 多租户学习 === ✅ 数据隔离验证通过: 期望租户={}, 实际租户={}", 
                tenantId, currentContextTenantId);
        } else {
            log.warn("=== 多租户学习 === ❌ 数据隔离验证失败: 期望租户={}, 实际租户={}", 
                tenantId, currentContextTenantId);
        }
    }

    /**
     * 演示租户数据访问
     */
    private void demonstrateTenantDataAccess(Long tenantId, String description) {
        log.info("=== 多租户学习 === {} (租户ID: {}) 数据访问演示:", description, tenantId);
        
        // 模拟查询租户数据
        log.info("  - 查询用户数据: SELECT * FROM system_users WHERE tenant_id = {}", tenantId);
        log.info("  - 查询角色数据: SELECT * FROM system_role WHERE tenant_id = {}", tenantId);
        log.info("  - 查询菜单数据: SELECT * FROM system_menu WHERE tenant_id = {}", tenantId);
        
        // === 学习要点9: SQL自动注入租户条件 ===
        logLearningPoint("SQL租户条件自动注入", 
            "MyBatis拦截器会自动在SQL中添加tenant_id条件", tenantId);
    }

    /**
     * 查找其他租户ID（用于演示）
     */
    private Long findOtherTenantId(Long currentTenantId) {
        // 简化实现：返回一个不同的租户ID用于演示
        return currentTenantId != null && currentTenantId.equals(1L) ? 2L : 1L;
    }

    /**
     * 记录租户学习信息
     */
    private void recordTenantLearning(Long tenantId, String operation, boolean success, String errorMessage) {
        TenantLearningRecord record = tenantLearningRecords.computeIfAbsent(tenantId, 
            k -> TenantLearningRecord.builder()
                    .tenantId(tenantId)
                    .operations(new ArrayList<>())
                    .totalOperations(0)
                    .successfulOperations(0)
                    .build());
        
        TenantOperation tenantOperation = TenantOperation.builder()
                .operation(operation)
                .success(success)
                .errorMessage(errorMessage)
                .timestamp(LocalDateTime.now())
                .build();
        
        record.getOperations().add(tenantOperation);
        record.setTotalOperations(record.getTotalOperations() + 1);
        
        if (success) {
            record.setSuccessfulOperations(record.getSuccessfulOperations() + 1);
        }
    }

    /**
     * 记录租户上下文信息
     */
    private void logTenantContext(String phase, Long currentTenantId, Long targetTenantId) {
        log.info("=== 多租户学习 === {}: 当前租户={}, 目标租户={}", 
            phase, currentTenantId, targetTenantId);
    }

    /**
     * 记录学习要点
     */
    private void logLearningPoint(String title, String description, Long tenantId) {
        log.info("=== 多租户学习要点 === {}: {} (租户ID: {})", title, description, tenantId);
    }

    /**
     * 生成多租户学习报告
     */
    public void generateMultiTenantLearningReport() {
        log.info("\n=== 多租户架构学习报告 ===");
        
        // 租户操作统计
        log.info("租户操作统计:");
        tenantLearningRecords.forEach((tenantId, record) -> {
            double successRate = record.getTotalOperations() > 0 ? 
                (double) record.getSuccessfulOperations() / record.getTotalOperations() * 100 : 0;
            
            log.info("  租户 {}: 总操作={}, 成功={}, 成功率={:.1f}%", 
                tenantId, record.getTotalOperations(), record.getSuccessfulOperations(), successRate);
        });
        
        // 上下文切换统计
        log.info("\n租户上下文切换统计:");
        Map<String, Long> operationCounts = new HashMap<>();
        contextSwitchRecords.forEach(record -> {
            operationCounts.merge(record.getOperation(), 1L, Long::sum);
        });
        
        operationCounts.forEach((operation, count) -> {
            log.info("  操作 '{}': {}次切换", operation, count);
        });
        
        // 数据隔离验证统计
        log.info("\n数据隔离验证统计:");
        long totalVerifications = isolationVerifications.size();
        long successfulIsolations = isolationVerifications.stream()
                .mapToLong(v -> v.isIsolated() ? 1 : 0)
                .sum();
        
        double isolationRate = totalVerifications > 0 ? 
            (double) successfulIsolations / totalVerifications * 100 : 0;
        
        log.info("  总验证次数: {}", totalVerifications);
        log.info("  隔离成功次数: {}", successfulIsolations);
        log.info("  隔离成功率: {:.1f}%", isolationRate);
        
        // 学习建议
        generateMultiTenantLearningAdvice();
    }

    /**
     * 生成多租户学习建议
     */
    private void generateMultiTenantLearningAdvice() {
        log.info("\n=== 多租户学习建议 ===");
        
        List<String> advice = new ArrayList<>();
        
        // 基于统计数据生成建议
        if (contextSwitchRecords.size() < 5) {
            advice.add("建议多练习租户上下文切换操作，理解TenantUtils.execute()的使用");
        }
        
        if (isolationVerifications.isEmpty()) {
            advice.add("建议验证数据隔离效果，确保理解多租户数据安全机制");
        }
        
        if (tenantLearningRecords.size() < 2) {
            advice.add("建议使用多个租户进行测试，体验真正的多租户环境");
        }
        
        // 通用学习建议
        advice.add("深入理解多租户架构的三种模式：共享数据库共享Schema、共享数据库独立Schema、独立数据库");
        advice.add("学习租户数据迁移和备份策略");
        advice.add("了解多租户环境下的性能优化技巧");
        advice.add("研究多租户计费和资源配额管理");
        
        advice.forEach(tip -> log.info("  💡 {}", tip));
    }

    /**
     * 清空学习数据
     */
    public void clearMultiTenantLearningData() {
        tenantLearningRecords.clear();
        contextSwitchRecords.clear();
        isolationVerifications.clear();
        log.info("=== 多租户学习 === 已清空所有学习数据");
    }

    /**
     * 获取学习统计信息
     */
    public MultiTenantLearningStats getLearningStats() {
        return MultiTenantLearningStats.builder()
                .totalTenants(tenantLearningRecords.size())
                .totalContextSwitches(contextSwitchRecords.size())
                .totalIsolationVerifications(isolationVerifications.size())
                .tenantRecords(new HashMap<>(tenantLearningRecords))
                .contextSwitches(new ArrayList<>(contextSwitchRecords))
                .isolationVerifications(new ArrayList<>(isolationVerifications))
                .build();
    }
}
