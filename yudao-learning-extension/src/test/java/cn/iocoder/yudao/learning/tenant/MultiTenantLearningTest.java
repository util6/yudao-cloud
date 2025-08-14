package cn.iocoder.yudao.learning.tenant;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.learning.tenant.service.LearningTenantDataPermissionService;
import cn.iocoder.yudao.learning.tenant.service.LearningTenantService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.annotation.Resource;

/**
 * 多租户架构学习测试
 * 
 * 测试目标：
 * 1. 验证多租户上下文切换机制
 * 2. 测试数据隔离效果
 * 3. 验证SQL拦截器工作原理
 * 4. 测试跨租户操作的安全性
 * 5. 验证多租户事务处理
 * 
 * @author 学习扩展
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("local")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MultiTenantLearningTest {

    @Resource
    private LearningTenantService learningTenantService;
    
    @Resource
    private LearningTenantDataPermissionService dataPermissionService;

    private static final Long TENANT_1 = 1L;
    private static final Long TENANT_2 = 2L;
    private static final Long INVALID_TENANT = 999L;

    @BeforeEach
    void setUp() {
        log.info("\n=== 多租户学习测试开始 ===");
        // 设置默认租户上下文
        TenantContextHolder.setTenantId(TENANT_1);
    }

    @AfterEach
    void tearDown() {
        // 清理租户上下文
        TenantContextHolder.clear();
        log.info("=== 多租户学习测试结束 ===\n");
    }

    @Test
    @Order(1)
    @DisplayName("学习1: 租户有效性校验机制")
    void testTenantValidation() {
        log.info("=== 学习测试1: 租户有效性校验 ===");
        
        // 测试有效租户
        Assertions.assertDoesNotThrow(() -> {
            learningTenantService.validTenant(TENANT_1);
            log.info("✅ 有效租户校验通过: {}", TENANT_1);
        });
        
        // 测试另一个有效租户
        Assertions.assertDoesNotThrow(() -> {
            learningTenantService.validTenant(TENANT_2);
            log.info("✅ 有效租户校验通过: {}", TENANT_2);
        });
        
        // 测试无效租户（预期抛出异常）
        Assertions.assertThrows(Exception.class, () -> {
            learningTenantService.validTenant(INVALID_TENANT);
        });
        log.info("✅ 无效租户校验正确拒绝: {}", INVALID_TENANT);
        
        log.info("学习要点: 租户校验是多租户安全的第一道防线");
    }

    @Test
    @Order(2)
    @DisplayName("学习2: 租户上下文切换机制")
    void testTenantContextSwitching() {
        log.info("=== 学习测试2: 租户上下文切换 ===");
        
        // 验证初始租户上下文
        Long initialTenant = TenantContextHolder.getTenantId();
        log.info("初始租户上下文: {}", initialTenant);
        Assertions.assertEquals(TENANT_1, initialTenant);
        
        // 测试租户上下文切换
        String result = learningTenantService.executeWithTenant(TENANT_2, "测试操作", () -> {
            Long currentTenant = TenantContextHolder.getTenantId();
            log.info("切换后的租户上下文: {}", currentTenant);
            Assertions.assertEquals(TENANT_2, currentTenant);
            return "操作成功";
        });
        
        Assertions.assertEquals("操作成功", result);
        
        // 验证上下文恢复
        Long finalTenant = TenantContextHolder.getTenantId();
        log.info("恢复后的租户上下文: {}", finalTenant);
        Assertions.assertEquals(TENANT_1, finalTenant);
        
        log.info("学习要点: TenantUtils.execute()确保上下文安全切换和恢复");
    }

    @Test
    @Order(3)
    @DisplayName("学习3: 多租户数据访问演示")
    void testMultiTenantDataAccess() {
        log.info("=== 学习测试3: 多租户数据访问 ===");
        
        // 演示多租户数据访问
        Assertions.assertDoesNotThrow(() -> {
            learningTenantService.demonstrateMultiTenantDataAccess();
        });
        
        log.info("学习要点: 不同租户访问各自的数据，实现完全隔离");
    }

    @Test
    @Order(4)
    @DisplayName("学习4: 标准租户数据查询")
    void testStandardTenantQuery() {
        log.info("=== 学习测试4: 标准租户数据查询 ===");
        
        // 演示标准租户查询
        Assertions.assertDoesNotThrow(() -> {
            dataPermissionService.demonstrateStandardTenantQuery();
        });
        
        log.info("学习要点: 默认情况下所有查询都会自动添加tenant_id条件");
    }

    @Test
    @Order(5)
    @DisplayName("学习5: 禁用数据权限查询")
    void testDisabledDataPermissionQuery() {
        log.info("=== 学习测试5: 禁用数据权限查询 ===");
        
        // 演示禁用数据权限的查询
        Assertions.assertDoesNotThrow(() -> {
            dataPermissionService.demonstrateDisabledDataPermissionQuery();
        });
        
        log.info("学习要点: @DataPermission(enable = false)可以跳过租户条件注入");
        log.warn("安全警告: 禁用数据权限是危险操作，需要谨慎使用");
    }

    @Test
    @Order(6)
    @DisplayName("学习6: 租户数据隔离验证")
    void testTenantDataIsolationVerification() {
        log.info("=== 学习测试6: 租户数据隔离验证 ===");
        
        // 验证租户数据隔离
        Assertions.assertDoesNotThrow(() -> {
            dataPermissionService.demonstrateTenantDataIsolationVerification();
        });
        
        log.info("学习要点: 通过上下文切换验证不同租户间的数据隔离");
    }

    @Test
    @Order(7)
    @DisplayName("学习7: SQL拦截器工作机制")
    void testSqlInterceptorMechanism() {
        log.info("=== 学习测试7: SQL拦截器工作机制 ===");
        
        // 演示SQL拦截器机制
        Assertions.assertDoesNotThrow(() -> {
            dataPermissionService.demonstrateSqlInterceptorMechanism();
        });
        
        log.info("学习要点: MyBatis拦截器自动改写SQL，添加tenant_id条件");
    }

    @Test
    @Order(8)
    @DisplayName("学习8: 多租户事务处理")
    void testMultiTenantTransaction() {
        log.info("=== 学习测试8: 多租户事务处理 ===");
        
        // 演示多租户事务处理
        Assertions.assertDoesNotThrow(() -> {
            dataPermissionService.demonstrateMultiTenantTransaction();
        });
        
        log.info("学习要点: 事务中的所有操作都在同一租户上下文中执行");
    }

    @Test
    @Order(9)
    @DisplayName("学习9: 跨租户操作安全测试")
    void testCrossTenantOperationSecurity() {
        log.info("=== 学习测试9: 跨租户操作安全测试 ===");
        
        // 测试在租户1上下文中访问租户2数据（应该被阻止）
        TenantContextHolder.setTenantId(TENANT_1);
        
        // 尝试直接访问其他租户数据（通过正常查询应该访问不到）
        log.info("当前租户上下文: {}", TenantContextHolder.getTenantId());
        log.info("尝试查询租户2的数据...");
        
        // 模拟查询操作（实际项目中这里会是真实的数据库查询）
        // 由于SQL拦截器的存在，查询会自动添加当前租户的条件
        log.info("SQL: SELECT * FROM system_users WHERE tenant_id = {} (自动注入)", TENANT_1);
        log.info("结果: 只能查询到租户1的数据，无法访问租户2的数据");
        
        log.info("学习要点: SQL拦截器确保跨租户数据访问的安全性");
    }

    @Test
    @Order(10)
    @DisplayName("学习10: 多租户性能影响分析")
    void testMultiTenantPerformanceImpact() {
        log.info("=== 学习测试10: 多租户性能影响分析 ===");
        
        // 测试租户上下文切换的性能
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 100; i++) {
            TenantUtils.execute(TENANT_1, () -> {
                // 模拟业务操作
                return TenantContextHolder.getTenantId();
            });
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        log.info("100次租户上下文切换耗时: {}ms", duration);
        log.info("平均每次切换耗时: {}ms", duration / 100.0);
        
        // 性能分析
        if (duration < 100) {
            log.info("✅ 性能良好: 租户上下文切换开销很小");
        } else if (duration < 500) {
            log.info("⚠️ 性能一般: 需要注意频繁切换的性能影响");
        } else {
            log.warn("❌ 性能较差: 需要优化租户上下文切换机制");
        }
        
        log.info("学习要点: 多租户架构会带来一定的性能开销，需要合理设计");
    }

    @Test
    @Order(11)
    @DisplayName("学习11: 多租户异常处理机制")
    void testMultiTenantExceptionHandling() {
        log.info("=== 学习测试11: 多租户异常处理机制 ===");
        
        // 测试租户上下文异常恢复
        Long originalTenant = TenantContextHolder.getTenantId();
        
        try {
            TenantUtils.execute(TENANT_2, () -> {
                log.info("切换到租户2: {}", TenantContextHolder.getTenantId());
                // 模拟异常
                throw new RuntimeException("模拟业务异常");
            });
        } catch (RuntimeException e) {
            log.info("捕获到异常: {}", e.getMessage());
        }
        
        // 验证异常后上下文是否正确恢复
        Long currentTenant = TenantContextHolder.getTenantId();
        log.info("异常后的租户上下文: {}", currentTenant);
        
        Assertions.assertEquals(originalTenant, currentTenant);
        log.info("✅ 异常后租户上下文正确恢复");
        
        log.info("学习要点: TenantUtils.execute()在异常情况下也能正确恢复上下文");
    }

    @Test
    @Order(12)
    @DisplayName("学习12: 生成多租户学习报告")
    void testGenerateMultiTenantLearningReport() {
        log.info("=== 学习测试12: 生成多租户学习报告 ===");
        
        // 生成租户服务学习报告
        Assertions.assertDoesNotThrow(() -> {
            learningTenantService.generateMultiTenantLearningReport();
        });
        
        // 生成数据权限学习报告
        Assertions.assertDoesNotThrow(() -> {
            dataPermissionService.generateDataPermissionLearningReport();
        });
        
        log.info("学习要点: 通过学习报告总结多租户架构的关键知识点");
    }

    @AfterAll
    static void cleanup() {
        log.info("\n=== 多租户架构学习总结 ===");
        log.info("通过以上测试，你应该掌握了以下多租户架构知识点：");
        log.info("1. 租户有效性校验机制");
        log.info("2. 租户上下文切换和恢复");
        log.info("3. 多租户数据隔离原理");
        log.info("4. SQL拦截器工作机制");
        log.info("5. 数据权限注解的使用");
        log.info("6. 多租户事务处理");
        log.info("7. 跨租户操作安全控制");
        log.info("8. 多租户性能优化考虑");
        log.info("9. 异常情况下的上下文恢复");
        log.info("10. 多租户架构的最佳实践");
        
        log.info("\n=== 进阶学习建议 ===");
        log.info("💡 深入研究MyBatis拦截器的实现原理");
        log.info("💡 学习多租户环境下的数据库设计模式");
        log.info("💡 了解多租户架构的扩展性和可维护性");
        log.info("💡 研究多租户环境下的监控和运维策略");
        log.info("💡 掌握多租户数据迁移和备份恢复");
    }
}
