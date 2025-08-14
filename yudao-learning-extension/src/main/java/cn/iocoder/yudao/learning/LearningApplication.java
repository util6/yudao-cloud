package cn.iocoder.yudao.learning;

import cn.iocoder.yudao.learning.tenant.service.LearningTenantService;
import cn.iocoder.yudao.learning.tenant.service.LearningTenantDataPermissionService;
import lombok.extern.slf4j.Slf4j; // 修复Lombok日志注解的导入
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import java.util.Scanner;

import org.springframework.context.ConfigurableApplicationContext;

/**
 * 独立学习模块启动器
 * 
 * 这是一个独立的学习应用，可以直接启动来学习YuDao Cloud的各个模块
 * 不需要启动完整的YuDao Cloud项目，专注于学习特定的业务模块
 * 
 * 学习模块：
 * 1. 多租户架构学习
 * 2. 用户认证授权学习
 * 3. 支付系统学习
 * 4. 工作流引擎学习
 * 5. 缓存应用学习
 * 
 * @author 学习扩展
 */
@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = {
    "cn.iocoder.yudao.learning",
    "cn.iocoder.yudao.module.system",
    "cn.iocoder.yudao.module.infra",
    "cn.iocoder.yudao.module.pay",
    "cn.iocoder.yudao.module.bpm"
})
public class LearningApplication {

    public static void main(String[] args) {
        log.info("\n" +
            "╔══════════════════════════════════════════════════════════════════════════════════╗\n" +
            "║                           YuDao Cloud 独立学习模块                                ║\n" +
            "║                        Independent Learning Module                              ║\n" +
            "║                                                                                  ║\n" +
            "║  🎯 专注学习，无需启动完整项目                                                     ║\n" +
            "║  🚀 交互式学习体验                                                               ║\n" +
            "║  📊 实时学习分析和反馈                                                           ║\n" +
            "║                                                                                  ║\n" +
            "╚══════════════════════════════════════════════════════════════════════════════════╝");

        // 启动Spring Boot应用
        ConfigurableApplicationContext context = SpringApplication.run(LearningApplication.class, args);
        
        // 启动交互式学习界面
        startInteractiveLearning(context);
    }

    /**
     * 启动交互式学习界面
     */
    private static void startInteractiveLearning(ConfigurableApplicationContext context) {
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            printLearningMenu();
            
            System.out.print("请选择学习模块 (输入数字): ");
            String choice = scanner.nextLine().trim();
            
            try {
                switch (choice) {
                    case "1":
                        learnMultiTenantArchitecture(context);
                        break;
                    case "2":
                        learnUserAuthenticationAndAuthorization(context);
                        break;
                    case "3":
                        learnPaymentSystem(context);
                        break;
                    case "4":
                        learnWorkflowEngine(context);
                        break;
                    case "5":
                        learnCacheApplication(context);
                        break;
                    case "6":
                        runAllLearningModules(context);
                        break;
                    case "7":
                        generateLearningReport(context);
                        break;
                    case "0":
                        log.info("感谢使用YuDao Cloud学习模块！");
                        System.exit(0);
                        break;
                    default:
                        log.warn("无效选择，请重新输入！");
                }
            } catch (Exception e) {
                log.error("学习模块执行出错: {}", e.getMessage(), e);
            }
            
            System.out.println("\n按回车键继续...");
            scanner.nextLine();
        }
    }

    /**
     * 打印学习菜单
     */
    private static void printLearningMenu() {
        System.out.println("\n" +
            "╔══════════════════════════════════════════════════════════════════════════════════╗\n" +
            "║                              学习模块选择菜单                                      ║\n" +
            "╠══════════════════════════════════════════════════════════════════════════════════╣\n" +
            "║  1. 🏢 多租户架构学习 - 数据隔离、上下文切换、SQL拦截器                            ║\n" +
            "║  2. 🔐 用户认证授权学习 - JWT认证、RBAC权限、密码加密                             ║\n" +
            "║  3. 💰 支付系统学习 - 第三方支付、回调处理、订单状态机                            ║\n" +
            "║  4. 🔄 工作流引擎学习 - BPMN流程、任务分配、流程监控                              ║\n" +
            "║  5. 🚀 缓存应用学习 - Redis应用、分布式锁、缓存策略                               ║\n" +
            "║  6. 🎯 运行所有学习模块 - 完整的学习体验                                          ║\n" +
            "║  7. 📊 生成学习报告 - 查看学习统计和建议                                          ║\n" +
            "║  0. 🚪 退出学习模块                                                              ║\n" +
            "╚══════════════════════════════════════════════════════════════════════════════════╝");
    }

    /**
     * 学习多租户架构
     */
    private static void learnMultiTenantArchitecture(ConfigurableApplicationContext context) {
        log.info("\n=== 🏢 开始多租户架构学习 ===");
        
        try {
            LearningTenantService tenantService = context.getBean(LearningTenantService.class);
            LearningTenantDataPermissionService dataPermissionService = context.getBean(LearningTenantDataPermissionService.class);
            
            log.info("1. 学习租户有效性校验...");
            tenantService.validTenant(1L);
            
            log.info("2. 学习租户上下文切换...");
            tenantService.executeWithTenant(2L, "学习演示", () -> {
                log.info("在租户2的上下文中执行操作");
                return "成功";
            });
            
            log.info("3. 学习多租户数据访问...");
            tenantService.demonstrateMultiTenantDataAccess();
            
            log.info("4. 学习标准租户查询...");
            dataPermissionService.demonstrateStandardTenantQuery();
            
            log.info("5. 学习SQL拦截器机制...");
            dataPermissionService.demonstrateSqlInterceptorMechanism();
            
            log.info("6. 学习数据隔离验证...");
            dataPermissionService.demonstrateTenantDataIsolationVerification();
            
            log.info("7. 生成多租户学习报告...");
            tenantService.generateMultiTenantLearningReport();
            dataPermissionService.generateDataPermissionLearningReport();
            
            log.info("✅ 多租户架构学习完成！");
            
        } catch (Exception e) {
            log.error("多租户架构学习失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 学习用户认证授权
     */
    private static void learnUserAuthenticationAndAuthorization(ConfigurableApplicationContext context) {
        log.info("\n=== 🔐 开始用户认证授权学习 ===");

        try {
            log.info("1. 学习用户创建流程...");
            log.info("   - 用户数据校验机制");
            log.info("   - 密码加密存储");
            log.info("   - 用户角色分配");

            log.info("2. 学习密码加密机制...");
            log.info("   - BCrypt加密算法");
            log.info("   - 密码强度校验");
            log.info("   - 密码重置流程");

            log.info("3. 学习JWT Token生成...");
            log.info("   - Token结构分析");
            log.info("   - 签名验证机制");
            log.info("   - Token刷新策略");

            log.info("4. 学习权限校验机制...");
            log.info("   - RBAC权限模型");
            log.info("   - 权限注解使用");
            log.info("   - 动态权限控制");

            log.info("✅ 用户认证授权学习完成！");

        } catch (Exception e) {
            log.error("用户认证授权学习失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 学习支付系统
     */
    private static void learnPaymentSystem(ConfigurableApplicationContext context) {
        log.info("\n=== 💰 开始支付系统学习 ===");

        try {
            log.info("1. 学习支付订单创建...");
            log.info("   - 订单数据模型设计");
            log.info("   - 订单状态管理");
            log.info("   - 订单金额计算");

            log.info("2. 学习支付渠道集成...");
            log.info("   - 微信支付集成");
            log.info("   - 支付宝集成");
            log.info("   - 银联支付集成");

            log.info("3. 学习支付回调处理...");
            log.info("   - 异步回调机制");
            log.info("   - 回调安全验证");
            log.info("   - 重复回调处理");

            log.info("4. 学习订单状态机...");
            log.info("   - 状态流转设计");
            log.info("   - 异常状态处理");
            log.info("   - 状态一致性保证");

            log.info("✅ 支付系统学习完成！");

        } catch (Exception e) {
            log.error("支付系统学习失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 学习工作流引擎
     */
    private static void learnWorkflowEngine(ConfigurableApplicationContext context) {
        log.info("\n=== 🔄 开始工作流引擎学习 ===");

        try {
            log.info("1. 学习流程实例创建...");
            log.info("   - BPMN流程定义");
            log.info("   - 流程实例启动");
            log.info("   - 流程变量管理");

            log.info("2. 学习任务分配机制...");
            log.info("   - 用户任务分配");
            log.info("   - 角色任务分配");
            log.info("   - 动态任务分配");

            log.info("3. 学习流程流转...");
            log.info("   - 任务完成处理");
            log.info("   - 流程网关判断");
            log.info("   - 流程结束处理");

            log.info("✅ 工作流引擎学习完成！");

        } catch (Exception e) {
            log.error("工作流引擎学习失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 学习缓存应用
     */
    private static void learnCacheApplication(ConfigurableApplicationContext context) {
        log.info("\n=== 🚀 开始缓存应用学习 ===");

        try {
            log.info("1. 学习缓存基本操作...");
            log.info("   - Redis数据类型");
            log.info("   - 缓存读写操作");
            log.info("   - 缓存过期策略");

            log.info("2. 学习分布式锁...");
            log.info("   - Redis分布式锁");
            log.info("   - 锁的获取和释放");
            log.info("   - 锁超时处理");

            log.info("3. 学习缓存策略...");
            log.info("   - 缓存更新策略");
            log.info("   - 缓存穿透处理");
            log.info("   - 缓存雪崩预防");

            log.info("✅ 缓存应用学习完成！");

        } catch (Exception e) {
            log.error("缓存应用学习失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 运行所有学习模块
     */
    private static void runAllLearningModules(ConfigurableApplicationContext context) {
        log.info("\n=== 🎯 开始完整学习体验 ===");
        
        learnMultiTenantArchitecture(context);
        learnUserAuthenticationAndAuthorization(context);
        learnPaymentSystem(context);
        learnWorkflowEngine(context);
        learnCacheApplication(context);
        
        log.info("🎉 恭喜！您已完成所有学习模块！");
    }

    /**
     * 生成学习报告
     */
    private static void generateLearningReport(ConfigurableApplicationContext context) {
        log.info("\n=== 📊 生成学习报告 ===");
        
        try {
            // 生成各模块的学习报告
            LearningTenantService tenantService = context.getBean(LearningTenantService.class);
            tenantService.generateMultiTenantLearningReport();
            
            // 可以添加其他模块的报告生成
            
            log.info("✅ 学习报告生成完成！");
            
        } catch (Exception e) {
            log.error("生成学习报告失败: {}", e.getMessage(), e);
        }
    }
}
