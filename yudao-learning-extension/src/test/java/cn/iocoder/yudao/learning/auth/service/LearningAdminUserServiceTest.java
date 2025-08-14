package cn.iocoder.yudao.learning.auth.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.learning.core.util.LearningLogger;
import cn.iocoder.yudao.module.system.controller.admin.user.vo.user.UserPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.user.vo.user.UserSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.annotation.Resource;
import java.util.HashSet;

/**
 * 学习扩展 - 管理员用户服务测试
 * 
 * 通过测试用例学习用户管理的核心功能
 * 
 * 测试目标：
 * 1. 验证用户创建流程的正确性
 * 2. 学习用户查询的各种方式
 * 3. 理解用户更新的业务逻辑
 * 4. 掌握用户删除的关联处理
 * 5. 分析异常情况的处理机制
 * 
 * @author 学习者
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("local") // 使用本地配置
public class LearningAdminUserServiceTest {

    @Resource
    private LearningAdminUserService learningAdminUserService;

    private static final String TEST_MODULE = "用户服务测试";

    @BeforeEach
    void setUp() {
        // 清空统计信息，确保每个测试的独立性
        LearningLogger.clearStatistics();
        
        log.info("=== 开始用户服务学习测试 ===");
        LearningLogger.logLearningInsight(TEST_MODULE, "通过测试用例深入学习用户管理的业务流程");
    }

    /**
     * 测试用户创建功能
     * 
     * 学习要点：
     * 1. 用户创建的完整流程
     * 2. 参数校验机制
     * 3. 密码加密处理
     * 4. 岗位关联处理
     */
    @Test
    void testCreateUser() {
        log.info("=== 测试用户创建功能 ===");
        
        try {
            // 准备测试数据
            UserSaveReqVO createReqVO = new UserSaveReqVO();
            createReqVO.setUsername("test_user_" + System.currentTimeMillis());
            createReqVO.setNickname("测试用户");
            createReqVO.setPassword("123456");
            createReqVO.setEmail("test@example.com");
            createReqVO.setMobile("13800138000");
            createReqVO.setDeptId(1L);
            createReqVO.setPostIds(new HashSet<>());
            
            // 学习分析：测试数据的准备
            LearningLogger.logBusinessAnalysis(TEST_MODULE, "测试数据准备", 
                    "创建用户需要提供：用户名、昵称、密码、邮箱、手机号、部门ID、岗位ID集合");
            
            // 执行用户创建 - 这里会调用我们的学习扩展方法
            Long userId = learningAdminUserService.createUser(createReqVO);
            
            // 验证创建结果
            if (userId != null && userId > 0) {
                LearningLogger.logLearningInsight(TEST_MODULE, 
                        String.format("用户创建成功，分配ID: %d，学习到了完整的用户创建流程", userId));
                
                // 查询创建的用户，验证数据完整性
                AdminUserDO createdUser = learningAdminUserService.getUser(userId);
                if (createdUser != null) {
                    LearningLogger.logDataFlow(TEST_MODULE, "创建结果验证", 
                            String.format("用户创建成功 - 用户名: %s, 昵称: %s, 状态: %d", 
                                    createdUser.getUsername(), createdUser.getNickname(), createdUser.getStatus()));
                }
                
            } else {
                log.error("用户创建失败，返回的用户ID为空");
            }
            
        } catch (Exception e) {
            // 学习分析：异常处理的重要性
            LearningLogger.logBusinessAnalysis(TEST_MODULE, "异常处理学习", 
                    String.format("用户创建过程中可能出现的异常：%s，需要合理处理保证系统稳定性", 
                            e.getClass().getSimpleName()));
            
            log.error("用户创建测试异常", e);
        }
    }

    /**
     * 测试用户查询功能
     * 
     * 学习要点：
     * 1. 分页查询的实现
     * 2. 查询条件的处理
     * 3. 数据权限的过滤
     */
    @Test
    void testGetUserPage() {
        log.info("=== 测试用户分页查询功能 ===");
        
        try {
            // 准备查询条件
            UserPageReqVO pageReqVO = new UserPageReqVO();
            pageReqVO.setPageNo(1);
            pageReqVO.setPageSize(10);
            // 可以设置其他查询条件，如部门ID、角色ID等
            
            // 学习分析：分页查询的参数
            LearningLogger.logBusinessAnalysis(TEST_MODULE, "分页查询参数", 
                    String.format("页码: %d, 页大小: %d，分页查询是处理大量数据的重要手段", 
                            pageReqVO.getPageNo(), pageReqVO.getPageSize()));
            
            // 执行分页查询
            PageResult<AdminUserDO> pageResult = learningAdminUserService.getUserPage(pageReqVO);
            
            // 分析查询结果
            if (pageResult != null) {
                LearningLogger.logLearningInsight(TEST_MODULE, 
                        String.format("分页查询成功，总记录数: %d，当前页记录数: %d", 
                                pageResult.getTotal(), pageResult.getList().size()));
                
                // 分析用户数据结构
                if (!pageResult.getList().isEmpty()) {
                    AdminUserDO firstUser = pageResult.getList().get(0);
                    LearningLogger.logDataFlow(TEST_MODULE, "用户数据结构", 
                            String.format("用户实体包含：ID、用户名、昵称、邮箱、手机号、部门ID、状态等字段"));
                }
            }
            
        } catch (Exception e) {
            LearningLogger.logBusinessAnalysis(TEST_MODULE, "查询异常处理", 
                    "分页查询异常可能由于：数据库连接问题、SQL语法错误、权限不足等");
            
            log.error("用户分页查询测试异常", e);
        }
    }

    /**
     * 测试用户名查询功能
     * 
     * 学习要点：
     * 1. 唯一索引查询的性能
     * 2. 登录认证中的应用
     */
    @Test
    void testGetUserByUsername() {
        log.info("=== 测试用户名查询功能 ===");
        
        try {
            // 使用一个可能存在的用户名进行测试
            String testUsername = "admin";
            
            // 学习分析：用户名查询的重要性
            LearningLogger.logBusinessAnalysis(TEST_MODULE, "用户名查询应用", 
                    "用户名查询是登录认证的第一步，通常配合密码验证完成身份认证");
            
            // 执行用户名查询
            AdminUserDO user = learningAdminUserService.getUserByUsername(testUsername);
            
            // 分析查询结果
            if (user != null) {
                LearningLogger.logLearningInsight(TEST_MODULE, 
                        String.format("用户名查询成功，找到用户: %s，ID: %d", 
                                user.getUsername(), user.getId()));
                
                // 学习分析：用户状态的重要性
                String statusDesc = user.getStatus() == 1 ? "启用" : "禁用";
                LearningLogger.logBusinessAnalysis(TEST_MODULE, "用户状态检查", 
                        String.format("用户状态: %s，登录时需要检查用户状态是否为启用", statusDesc));
                
            } else {
                LearningLogger.logBusinessAnalysis(TEST_MODULE, "用户不存在", 
                        String.format("用户名[%s]不存在，这在登录认证中是常见情况", testUsername));
            }
            
        } catch (Exception e) {
            log.error("用户名查询测试异常", e);
        }
    }

    /**
     * 测试手机号查询功能
     * 
     * 学习要点：
     * 1. 多种登录方式的支持
     * 2. 手机号的唯一性约束
     */
    @Test
    void testGetUserByMobile() {
        log.info("=== 测试手机号查询功能 ===");
        
        try {
            // 使用一个测试手机号
            String testMobile = "15601691300";
            
            // 学习分析：手机号登录的便利性
            LearningLogger.logBusinessAnalysis(TEST_MODULE, "手机号登录", 
                    "支持手机号登录可以提升用户体验，特别是在移动端应用中");
            
            // 执行手机号查询
            AdminUserDO user = learningAdminUserService.getUserByMobile(testMobile);
            
            // 分析查询结果
            if (user != null) {
                LearningLogger.logLearningInsight(TEST_MODULE, 
                        String.format("手机号查询成功，用户名: %s，手机号: %s", 
                                user.getUsername(), user.getMobile()));
            } else {
                LearningLogger.logBusinessAnalysis(TEST_MODULE, "手机号未绑定", 
                        String.format("手机号[%s]未绑定用户，可能需要先注册", testMobile));
            }
            
        } catch (Exception e) {
            log.error("手机号查询测试异常", e);
        }
    }

    /**
     * 测试用户登录信息更新
     * 
     * 学习要点：
     * 1. 登录信息的实时更新
     * 2. IP地址记录的安全意义
     */
    @Test
    void testUpdateUserLogin() {
        log.info("=== 测试用户登录信息更新功能 ===");
        
        try {
            // 假设用户ID为1的用户存在
            Long userId = 1L;
            String loginIp = "192.168.1.100";
            
            // 学习分析：登录信息更新的意义
            LearningLogger.logBusinessAnalysis(TEST_MODULE, "登录信息更新", 
                    "更新用户登录信息用于安全审计和用户行为分析");
            
            // 执行登录信息更新
            learningAdminUserService.updateUserLogin(userId, loginIp);
            
            LearningLogger.logLearningInsight(TEST_MODULE, 
                    String.format("用户[%d]登录信息更新成功，IP: %s", userId, loginIp));
            
        } catch (Exception e) {
            log.error("用户登录信息更新测试异常", e);
        }
    }

    /**
     * 打印学习统计信息
     */
    @Test
    void testPrintStatistics() {
        log.info("=== 打印学习统计信息 ===");
        
        // 执行一些操作来产生统计数据
        testGetUserByUsername();
        testGetUserByMobile();
        
        // 打印统计信息
        LearningLogger.printMethodCallStatistics();
        
        LearningLogger.logLearningInsight(TEST_MODULE, 
                "通过统计信息可以分析方法调用频率和性能，有助于系统优化");
    }
}
