package cn.iocoder.yudao.learning.tenant.service;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.learning.core.util.LearningLogger;
import cn.iocoder.yudao.learning.common.model.LearningDataModels.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 多租户数据权限学习服务
 * 
 * 学习重点：
 * 1. 多租户数据权限控制机制
 * 2. @DataPermission注解的使用
 * 3. 租户数据隔离的底层实现
 * 4. SQL拦截器的工作原理
 * 5. 数据权限与业务权限的结合
 * 6. 跨租户数据访问的安全控制
 * 
 * @author 学习扩展
 */
@Slf4j
@Service
public class LearningTenantDataPermissionService {

    /**
     * 数据权限操作记录
     */
    private final Map<String, List<DataPermissionOperation>> dataPermissionOperations = new ConcurrentHashMap<>();
    
    /**
     * SQL拦截记录
     */
    private final List<SqlInterceptionRecord> sqlInterceptionRecords = Collections.synchronizedList(new ArrayList<>());
    
    /**
     * 租户数据访问记录
     */
    private final Map<Long, TenantDataAccessRecord> tenantDataAccessRecords = new ConcurrentHashMap<>();

    /**
     * 演示标准的租户数据查询（自动注入租户条件）
     */
    public void demonstrateStandardTenantQuery() {
        long startTime = LearningLogger.logMethodStart("多租户数据权限", "demonstrateStandardTenantQuery");
        
        try {
            Long currentTenantId = TenantContextHolder.getTenantId();
            
            // === 学习要点1: 标准租户查询 ===
            logLearningPoint("标准租户查询", 
                "默认情况下，所有查询都会自动添加tenant_id条件", currentTenantId);
            
            // 模拟标准查询操作
            simulateStandardQuery("system_users", currentTenantId);
            simulateStandardQuery("system_role", currentTenantId);
            simulateStandardQuery("system_menu", currentTenantId);
            
            // 记录数据权限操作
            recordDataPermissionOperation("standardQuery", "SUCCESS", 
                "自动注入租户条件的标准查询", currentTenantId);
            
            LearningLogger.logMethodEnd("多租户数据权限", "demonstrateStandardTenantQuery", startTime, "演示完成");
            
        } catch (Exception e) {
            LearningLogger.logMethodException("多租户数据权限", "demonstrateStandardTenantQuery", startTime, e);
        }
    }

    /**
     * 演示禁用数据权限的查询
     */
    public void demonstrateDisabledDataPermissionQuery() {
        long startTime = LearningLogger.logMethodStart("多租户数据权限", "demonstrateDisabledDataPermissionQuery");
        
        try {
            Long currentTenantId = TenantContextHolder.getTenantId();
            
            // === 学习要点2: 禁用数据权限查询 ===
            logLearningPoint("禁用数据权限查询", 
                "使用@DataPermission(enable = false)可以跳过租户条件注入", currentTenantId);
            
            log.warn("=== 多租户学习 === ⚠️ 当前查询已禁用数据权限，可以访问所有租户数据！");
            
            // 模拟跨租户查询（危险操作，仅用于学习）
            simulateCrossTenantQuery("system_users");
            simulateCrossTenantQuery("system_tenant");
            
            // 记录数据权限操作
            recordDataPermissionOperation("disabledDataPermission", "WARNING", 
                "禁用数据权限的跨租户查询", currentTenantId);
            
            // === 学习要点3: 安全警告 ===
            logLearningPoint("安全警告", 
                "禁用数据权限是危险操作，只应在特定的管理功能中使用", currentTenantId);
            
            LearningLogger.logMethodEnd("多租户数据权限", "demonstrateDisabledDataPermissionQuery", startTime, "演示完成");
            
        } catch (Exception e) {
            LearningLogger.logMethodException("多租户数据权限", "demonstrateDisabledDataPermissionQuery", startTime, e);
        }
    }

    /**
     * 演示租户数据隔离验证
     */
    public void demonstrateTenantDataIsolationVerification() {
        long startTime = LearningLogger.logMethodStart("多租户数据权限", "demonstrateTenantDataIsolationVerification");
        
        try {
            Long currentTenantId = TenantContextHolder.getTenantId();
            
            // === 学习要点4: 数据隔离验证 ===
            logLearningPoint("数据隔离验证", 
                "验证不同租户之间的数据完全隔离", currentTenantId);
            
            if (currentTenantId != null) {
                // 验证当前租户数据访问
                verifyTenantDataAccess(currentTenantId, "当前租户数据访问验证");
                
                // 模拟切换到其他租户进行验证
                Long otherTenantId = currentTenantId.equals(1L) ? 2L : 1L;
                
                // 使用TenantUtils切换租户上下文
                cn.iocoder.yudao.framework.tenant.core.util.TenantUtils.execute(otherTenantId, () -> {
                    verifyTenantDataAccess(otherTenantId, "其他租户数据访问验证");
                    return null;
                });
                
                // 验证数据隔离效果
                verifyDataIsolationEffectiveness(currentTenantId, otherTenantId);
            }
            
            LearningLogger.logMethodEnd("多租户数据权限", "demonstrateTenantDataIsolationVerification", startTime, "验证完成");
            
        } catch (Exception e) {
            LearningLogger.logMethodException("多租户数据权限", "demonstrateTenantDataIsolationVerification", startTime, e);
        }
    }

    /**
     * 演示SQL拦截器工作原理
     */
    public void demonstrateSqlInterceptorMechanism() {
        long startTime = LearningLogger.logMethodStart("多租户数据权限", "demonstrateSqlInterceptorMechanism");
        
        try {
            Long currentTenantId = TenantContextHolder.getTenantId();
            
            // === 学习要点5: SQL拦截器机制 ===
            logLearningPoint("SQL拦截器机制", 
                "MyBatis拦截器自动在SQL中注入tenant_id条件", currentTenantId);
            
            // 演示不同类型的SQL拦截
            demonstrateSqlInterception("SELECT", "SELECT * FROM system_users WHERE status = 1", currentTenantId);
            demonstrateSqlInterception("UPDATE", "UPDATE system_users SET nickname = 'test' WHERE id = 1", currentTenantId);
            demonstrateSqlInterception("DELETE", "DELETE FROM system_users WHERE id = 1", currentTenantId);
            demonstrateSqlInterception("INSERT", "INSERT INTO system_users (username, password) VALUES ('test', 'password')", currentTenantId);
            
            // === 学习要点6: SQL改写规则 ===
            logLearningPoint("SQL改写规则", 
                "拦截器会分析SQL结构，在WHERE子句中添加tenant_id条件", currentTenantId);
            
            LearningLogger.logMethodEnd("多租户数据权限", "demonstrateSqlInterceptorMechanism", startTime, "演示完成");
            
        } catch (Exception e) {
            LearningLogger.logMethodException("多租户数据权限", "demonstrateSqlInterceptorMechanism", startTime, e);
        }
    }

    /**
     * 演示多租户事务处理
     */
    public void demonstrateMultiTenantTransaction() {
        long startTime = LearningLogger.logMethodStart("多租户数据权限", "demonstrateMultiTenantTransaction");
        
        try {
            Long currentTenantId = TenantContextHolder.getTenantId();
            
            // === 学习要点7: 多租户事务处理 ===
            logLearningPoint("多租户事务处理", 
                "事务中的所有操作都在同一租户上下文中执行", currentTenantId);
            
            // 模拟事务操作
            simulateTransactionOperation(currentTenantId);
            
            // === 学习要点8: 跨租户事务限制 ===
            logLearningPoint("跨租户事务限制", 
                "单个事务不能跨越多个租户，确保数据一致性", currentTenantId);
            
            LearningLogger.logMethodEnd("多租户数据权限", "demonstrateMultiTenantTransaction", startTime, "演示完成");
            
        } catch (Exception e) {
            LearningLogger.logMethodException("多租户数据权限", "demonstrateMultiTenantTransaction", startTime, e);
        }
    }

    /**
     * 模拟标准查询
     */
    private void simulateStandardQuery(String tableName, Long tenantId) {
        String originalSql = String.format("SELECT * FROM %s WHERE status = 1", tableName);
        String interceptedSql = String.format("SELECT * FROM %s WHERE status = 1 AND tenant_id = %d", tableName, tenantId);
        
        log.info("=== SQL拦截演示 ===");
        log.info("  原始SQL: {}", originalSql);
        log.info("  拦截后SQL: {}", interceptedSql);
        
        // 记录SQL拦截
        recordSqlInterception(originalSql, interceptedSql, tenantId, "STANDARD_QUERY");
    }

    /**
     * 模拟跨租户查询
     */
    private void simulateCrossTenantQuery(String tableName) {
        String sql = String.format("SELECT * FROM %s", tableName);
        
        log.info("=== 跨租户查询演示 ===");
        log.info("  SQL: {} (未添加tenant_id条件)", sql);
        log.warn("  ⚠️ 此查询可以访问所有租户的数据！");
        
        // 记录SQL拦截
        recordSqlInterception(sql, sql, null, "CROSS_TENANT_QUERY");
    }

    /**
     * 验证租户数据访问
     */
    private void verifyTenantDataAccess(Long tenantId, String description) {
        log.info("=== {} ===", description);
        log.info("  当前租户上下文: {}", TenantContextHolder.getTenantId());
        log.info("  预期访问租户: {}", tenantId);
        
        // 模拟数据访问验证
        boolean accessValid = Objects.equals(TenantContextHolder.getTenantId(), tenantId);
        
        if (accessValid) {
            log.info("  ✅ 数据访问验证通过");
        } else {
            log.warn("  ❌ 数据访问验证失败");
        }
        
        // 记录租户数据访问
        recordTenantDataAccess(tenantId, description, accessValid);
    }

    /**
     * 验证数据隔离效果
     */
    private void verifyDataIsolationEffectiveness(Long tenant1, Long tenant2) {
        log.info("=== 数据隔离效果验证 ===");
        log.info("  租户1: {} 的数据对租户2: {} 不可见", tenant1, tenant2);
        log.info("  租户2: {} 的数据对租户1: {} 不可见", tenant2, tenant1);
        
        // === 学习要点9: 数据隔离保证 ===
        logLearningPoint("数据隔离保证", 
            "通过SQL拦截器确保租户间数据完全隔离", null);
    }

    /**
     * 演示SQL拦截
     */
    private void demonstrateSqlInterception(String sqlType, String originalSql, Long tenantId) {
        String interceptedSql;
        
        switch (sqlType) {
            case "SELECT":
                interceptedSql = addTenantConditionToSelect(originalSql, tenantId);
                break;
            case "UPDATE":
                interceptedSql = addTenantConditionToUpdate(originalSql, tenantId);
                break;
            case "DELETE":
                interceptedSql = addTenantConditionToDelete(originalSql, tenantId);
                break;
            case "INSERT":
                interceptedSql = addTenantFieldToInsert(originalSql, tenantId);
                break;
            default:
                interceptedSql = originalSql;
        }
        
        log.info("=== {} SQL拦截演示 ===", sqlType);
        log.info("  原始SQL: {}", originalSql);
        log.info("  拦截后SQL: {}", interceptedSql);
        
        recordSqlInterception(originalSql, interceptedSql, tenantId, sqlType);
    }

    /**
     * 模拟事务操作
     */
    private void simulateTransactionOperation(Long tenantId) {
        log.info("=== 多租户事务操作演示 ===");
        log.info("  事务开始 - 租户ID: {}", tenantId);
        
        // 模拟多个数据库操作
        log.info("  操作1: INSERT INTO system_users (username, tenant_id) VALUES ('user1', {})", tenantId);
        log.info("  操作2: UPDATE system_users SET status = 1 WHERE tenant_id = {}", tenantId);
        log.info("  操作3: SELECT COUNT(*) FROM system_users WHERE tenant_id = {}", tenantId);
        
        log.info("  事务提交 - 所有操作都在租户 {} 的上下文中执行", tenantId);
    }

    /**
     * 添加租户条件到SELECT语句
     */
    private String addTenantConditionToSelect(String sql, Long tenantId) {
        if (sql.toUpperCase().contains("WHERE")) {
            return sql + " AND tenant_id = " + tenantId;
        } else {
            return sql + " WHERE tenant_id = " + tenantId;
        }
    }

    /**
     * 添加租户条件到UPDATE语句
     */
    private String addTenantConditionToUpdate(String sql, Long tenantId) {
        if (sql.toUpperCase().contains("WHERE")) {
            return sql + " AND tenant_id = " + tenantId;
        } else {
            return sql + " WHERE tenant_id = " + tenantId;
        }
    }

    /**
     * 添加租户条件到DELETE语句
     */
    private String addTenantConditionToDelete(String sql, Long tenantId) {
        if (sql.toUpperCase().contains("WHERE")) {
            return sql + " AND tenant_id = " + tenantId;
        } else {
            return sql + " WHERE tenant_id = " + tenantId;
        }
    }

    /**
     * 添加租户字段到INSERT语句
     */
    private String addTenantFieldToInsert(String sql, Long tenantId) {
        // 简化实现：在VALUES前添加tenant_id字段
        if (sql.toUpperCase().contains("VALUES")) {
            String[] parts = sql.split("(?i)VALUES");
            if (parts.length == 2) {
                String fieldsPart = parts[0].trim();
                String valuesPart = parts[1].trim();
                
                // 添加tenant_id字段
                fieldsPart = fieldsPart.replace(")", ", tenant_id)");
                valuesPart = valuesPart.replace("(", "(" + tenantId + ", ");
                
                return fieldsPart + " VALUES " + valuesPart;
            }
        }
        return sql;
    }

    /**
     * 记录数据权限操作
     */
    private void recordDataPermissionOperation(String operation, String result, String description, Long tenantId) {
        DataPermissionOperation dpOperation = DataPermissionOperation.builder()
                .operation(operation)
                .result(result)
                .description(description)
                .tenantId(tenantId)
                .timestamp(LocalDateTime.now())
                .build();
        
        dataPermissionOperations.computeIfAbsent(operation, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(dpOperation);
    }

    /**
     * 记录SQL拦截
     */
    private void recordSqlInterception(String originalSql, String interceptedSql, Long tenantId, String sqlType) {
        SqlInterceptionRecord record = SqlInterceptionRecord.builder()
                .originalSql(originalSql)
                .interceptedSql(interceptedSql)
                .tenantId(tenantId)
                .sqlType(sqlType)
                .intercepted(!originalSql.equals(interceptedSql))
                .timestamp(LocalDateTime.now())
                .build();
        
        sqlInterceptionRecords.add(record);
    }

    /**
     * 记录租户数据访问
     */
    private void recordTenantDataAccess(Long tenantId, String operation, boolean success) {
        TenantDataAccessRecord record = tenantDataAccessRecords.computeIfAbsent(tenantId,
            k -> TenantDataAccessRecord.builder()
                    .tenantId(tenantId)
                    .accessOperations(new ArrayList<>())
                    .totalAccess(0)
                    .successfulAccess(0)
                    .build());
        
        TenantDataAccessOperation accessOp = TenantDataAccessOperation.builder()
                .operation(operation)
                .success(success)
                .timestamp(LocalDateTime.now())
                .build();
        
        record.getAccessOperations().add(accessOp);
        record.setTotalAccess(record.getTotalAccess() + 1);
        
        if (success) {
            record.setSuccessfulAccess(record.getSuccessfulAccess() + 1);
        }
    }

    /**
     * 记录学习要点
     */
    private void logLearningPoint(String title, String description, Long tenantId) {
        log.info("=== 多租户数据权限学习要点 === {}: {} (租户ID: {})", title, description, tenantId);
    }

    /**
     * 生成多租户数据权限学习报告
     */
    public void generateDataPermissionLearningReport() {
        log.info("\n=== 多租户数据权限学习报告 ===");
        
        // 数据权限操作统计
        log.info("数据权限操作统计:");
        dataPermissionOperations.forEach((operation, operations) -> {
            long successCount = operations.stream().filter(op -> "SUCCESS".equals(op.getResult())).count();
            long warningCount = operations.stream().filter(op -> "WARNING".equals(op.getResult())).count();
            
            log.info("  操作 '{}': 总计={}, 成功={}, 警告={}", 
                operation, operations.size(), successCount, warningCount);
        });
        
        // SQL拦截统计
        log.info("\nSQL拦截统计:");
        Map<String, Long> sqlTypeCounts = new HashMap<>();
        long interceptedCount = 0;
        
        for (SqlInterceptionRecord record : sqlInterceptionRecords) {
            sqlTypeCounts.merge(record.getSqlType(), 1L, Long::sum);
            if (record.isIntercepted()) {
                interceptedCount++;
            }
        }
        
        sqlTypeCounts.forEach((sqlType, count) -> {
            log.info("  {} SQL: {}次", sqlType, count);
        });
        
        log.info("  总SQL数: {}, 被拦截数: {}, 拦截率: {:.1f}%", 
            sqlInterceptionRecords.size(), interceptedCount, 
            sqlInterceptionRecords.size() > 0 ? (double) interceptedCount / sqlInterceptionRecords.size() * 100 : 0);
        
        // 租户数据访问统计
        log.info("\n租户数据访问统计:");
        tenantDataAccessRecords.forEach((tenantId, record) -> {
            double successRate = record.getTotalAccess() > 0 ? 
                (double) record.getSuccessfulAccess() / record.getTotalAccess() * 100 : 0;
            
            log.info("  租户 {}: 总访问={}, 成功={}, 成功率={:.1f}%", 
                tenantId, record.getTotalAccess(), record.getSuccessfulAccess(), successRate);
        });
        
        // 学习建议
        generateDataPermissionLearningAdvice();
    }

    /**
     * 生成数据权限学习建议
     */
    private void generateDataPermissionLearningAdvice() {
        log.info("\n=== 多租户数据权限学习建议 ===");
        
        List<String> advice = Arrays.asList(
            "深入理解MyBatis拦截器的工作原理和SQL改写机制",
            "学习@DataPermission注解的各种使用场景和注意事项",
            "掌握多租户环境下的事务管理和数据一致性保证",
            "了解多租户数据权限与业务权限的结合使用",
            "研究多租户环境下的性能优化策略",
            "学习多租户数据备份和恢复的最佳实践",
            "掌握多租户环境下的数据迁移和升级策略"
        );
        
        advice.forEach(tip -> log.info("  💡 {}", tip));
    }

    /**
     * 清空学习数据
     */
    public void clearDataPermissionLearningData() {
        dataPermissionOperations.clear();
        sqlInterceptionRecords.clear();
        tenantDataAccessRecords.clear();
        log.info("=== 多租户数据权限学习 === 已清空所有学习数据");
    }
}
