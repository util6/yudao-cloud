package cn.iocoder.yudao.learning.config;

import cn.iocoder.yudao.learning.core.util.LearningLogger;
import cn.iocoder.yudao.learning.tenant.service.LearningTenantDataPermissionService;
import cn.iocoder.yudao.learning.tenant.service.LearningTenantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import javax.annotation.Resource;

/**
 * 学习扩展配置类
 * 
 * 负责学习扩展模块的初始化配置
 * 
 * 配置内容：
 * 1. 组件扫描配置
 * 2. 学习模块初始化
 * 3. 启动时的学习提示
 * 4. 统计信息定时输出
 * 
 * @author 学习者
 */
@Slf4j
@Configuration
public class LearningExtensionConfiguration {

    @Resource
    private LearningTenantService learningTenantService;

    @Resource
    private LearningTenantDataPermissionService learningTenantDataPermissionService;

    /**
     * 学习扩展模块启动初始化
     * 
     * 在应用启动时执行，用于：
     * 1. 输出学习扩展模块的启动信息
     * 2. 提供学习指导和使用说明
     * 3. 初始化学习统计功能
     */
    @Bean
    @Order(1) // 确保在其他组件之前执行
    public CommandLineRunner learningExtensionInitializer() {
        return args -> {
            printLearningBanner();
            printLearningGuide();
            initializeLearningStatistics();
            initializeMultiTenantLearning();
        };
    }

    /**
     * 打印学习扩展横幅
     */
    private void printLearningBanner() {
        String banner = "\n" +
                "╔══════════════════════════════════════════════════════════════════════════════════╗\n" +
                "║                           芋道云学习扩展模块                                        ║\n" +
                "║                        Yudao Cloud Learning Extension                           ║\n" +
                "║                                                                                  ║\n" +
                "║  通过Bean替换方式，在不修改原代码的情况下，深入学习企业级项目的核心业务逻辑           ║\n" +
                "║                                                                                  ║\n" +
                "║  学习模块：                                                                       ║\n" +
                "║  ✓ 鉴权模块 - 用户管理、认证服务、权限控制                                        ║\n" +
                "║  ✓ 缓存模块 - Redis应用、分布式锁、缓存策略                                       ║\n" +
                "║  ✓ 支付模块 - 多渠道支付、回调处理、对账机制                                      ║\n" +
                "║  ✓ 工作流模块 - Flowable引擎、流程设计、任务分配                                  ║\n" +
                "║  ✓ 多租户模块 - 数据隔离、上下文切换、SQL拦截器                                   ║\n" +
                "║                                                                                  ║\n" +
                "║  学习方式：通过@Primary注解优先注入，原代码调用时自动使用学习扩展实现               ║\n" +
                "╚══════════════════════════════════════════════════════════════════════════════════╝";
        
        log.info(banner);
    }

    /**
     * 打印学习指导
     */
    private void printLearningGuide() {
        log.info("=== 学习扩展模块使用指南 ===");
        log.info("1. 调试学习：在扩展类的方法中设置断点，step-by-step学习原方法执行流程");
        log.info("2. 日志学习：观察控制台输出的详细学习日志，了解业务逻辑和数据流转");
        log.info("3. 统计分析：通过LearningLogger.printMethodCallStatistics()查看方法调用统计");
        log.info("4. 测试验证：运行test包下的测试用例，验证学习效果");
        log.info("5. 扩展学习：参考现有扩展，为其他模块创建学习扩展");
        log.info("");
        log.info("=== 学习重点提示 ===");
        log.info("• 鉴权模块：重点学习JWT Token机制、RBAC权限模型、密码加密算法");
        log.info("• 缓存模块：重点学习Redis数据类型、缓存策略、分布式锁应用");
        log.info("• 支付模块：重点学习第三方API集成、异步回调处理、状态机设计");
        log.info("• 工作流模块：重点学习BPMN流程引擎、动态表单、任务分配策略");
        log.info("• 多租户模块：重点学习数据隔离机制、租户上下文管理、SQL拦截器原理");
        log.info("");
        log.info("=== 注意事项 ===");
        log.info("⚠️  学习扩展仅用于学习目的，生产环境请移除或禁用");
        log.info("⚠️  扩展方法会增加系统开销，注意性能影响");
        log.info("⚠️  学习日志较多，建议调整日志级别避免影响正常业务日志");
        log.info("=====================================");
    }

    /**
     * 初始化学习统计功能
     */
    private void initializeLearningStatistics() {
        // 清空之前的统计信息
        LearningLogger.clearStatistics();
        
        // 记录模块启动
        LearningLogger.logLearningInsight("系统启动", 
                "学习扩展模块已启动，开始记录方法调用和业务分析信息");
        
        log.info("学习统计功能已初始化，可通过LearningLogger查看详细统计信息");
    }

    /**
     * 初始化多租户学习功能
     */
    private void initializeMultiTenantLearning() {
        log.info("多租户学习功能已初始化");
        LearningLogger.logLearningInsight("多租户模块",
                "多租户学习功能初始化完成，可通过相关服务进行多租户机制学习");
    }

    /**
     * 学习扩展模块关闭时的清理工作
     * 
     * 在应用关闭时执行，用于：
     * 1. 输出最终的学习统计信息
     * 2. 清理资源
     * 3. 输出学习总结
     */
    @Bean
    public CommandLineRunner learningExtensionShutdownHook() {
        return args -> {
            // 注册关闭钩子
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("=== 学习扩展模块关闭 ===");
                
                // 输出最终统计信息
                try {
                    LearningLogger.printMethodCallStatistics();
                } catch (Exception e) {
                    log.warn("输出学习统计信息时发生异常", e);
                }
                
                // 输出学习总结
                printLearningConclusion();
                
                log.info("学习扩展模块已关闭，感谢使用！");
            }));
        };
    }

    /**
     * 打印学习总结
     */
    private void printLearningConclusion() {
        log.info("=== 学习总结 ===");
        log.info("通过学习扩展模块，您应该已经掌握了：");
        log.info("1. 企业级项目的分层架构设计");
        log.info("2. Spring框架的依赖注入和Bean管理机制");
        log.info("3. 数据库操作和事务管理的最佳实践");
        log.info("4. 缓存技术在高并发场景下的应用");
        log.info("5. 安全认证和权限控制的实现方案");
        log.info("6. 异常处理和日志记录的规范");
        log.info("7. 单元测试和集成测试的编写方法");
        log.info("");
        log.info("继续学习建议：");
        log.info("• 深入研究Spring Cloud微服务架构");
        log.info("• 学习分布式系统的设计模式");
        log.info("• 掌握高并发、高可用系统的优化技巧");
        log.info("• 了解DevOps和持续集成/持续部署");
        log.info("=====================================");
    }
}
