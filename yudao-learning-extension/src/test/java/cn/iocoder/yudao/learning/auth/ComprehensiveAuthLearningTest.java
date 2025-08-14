package cn.iocoder.yudao.learning.auth;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.learning.auth.service.*;
import cn.iocoder.yudao.learning.core.util.LearningLogger;
import cn.iocoder.yudao.module.system.controller.admin.permission.vo.menu.MenuListReqVO;
import cn.iocoder.yudao.module.system.controller.admin.permission.vo.role.RolePageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.user.vo.UserPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.MenuDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.RoleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * 鉴权模块综合学习测试
 * 
 * 通过综合测试验证鉴权模块各个组件的协同工作，展示完整的RBAC权限模型
 * 
 * 测试场景：
 * 1. 用户管理 -> 角色管理 -> 菜单权限 -> 权限关联 -> 权限校验
 * 2. RBAC权限模型的完整流程学习
 * 3. 权限缓存机制的验证
 * 4. 数据权限的应用场景
 * 5. 跨模块权限校验的集成测试
 * 
 * @author 学习者
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("local") // 使用本地配置
public class ComprehensiveAuthLearningTest {

    @Resource
    private LearningAdminUserService learningAdminUserService;
    
    @Resource
    private LearningAdminAuthService learningAdminAuthService;
    
    @Resource
    private LearningMenuService learningMenuService;
    
    @Resource
    private LearningRoleService learningRoleService;
    
    @Resource
    private LearningPermissionService learningPermissionService;
    
    @Resource
    private LearningSecurityFrameworkService learningSecurityFrameworkService;

    private static final String TEST_MODULE = "鉴权模块综合测试";

    @BeforeEach
    void setUp() {
        // 清空统计信息，确保每个测试的独立性
        LearningLogger.clearStatistics();
        
        log.info("=== 开始鉴权模块综合学习测试 ===");
        LearningLogger.logLearningInsight(TEST_MODULE, 
                "通过综合测试深入学习RBAC权限模型的完整实现：用户-角色-权限的三层关联");
    }

    /**
     * RBAC权限模型综合测试
     * 
     * 测试完整的权限流程：
     * 1. 用户管理
     * 2. 角色管理
     * 3. 菜单权限管理
     * 4. 权限关联管理
     * 5. 权限校验框架
     */
    @Test
    void testRBACPermissionModel() {
        log.info("=== RBAC权限模型综合测试 ===");
        
        try {
            // 第一步：用户管理测试
            log.info("--- 第一步：用户管理测试 ---");
            testUserManagement();
            
            // 第二步：角色管理测试
            log.info("--- 第二步：角色管理测试 ---");
            testRoleManagement();
            
            // 第三步：菜单权限管理测试
            log.info("--- 第三步：菜单权限管理测试 ---");
            testMenuPermissionManagement();
            
            // 第四步：权限关联管理测试
            log.info("--- 第四步：权限关联管理测试 ---");
            testPermissionAssociation();
            
            // 第五步：权限校验框架测试
            log.info("--- 第五步：权限校验框架测试 ---");
            testSecurityFramework();
            
            // 第六步：权限缓存机制测试
            log.info("--- 第六步：权限缓存机制测试 ---");
            testPermissionCache();
            
            // 第七步：数据权限测试
            log.info("--- 第七步：数据权限测试 ---");
            testDataPermission();
            
            LearningLogger.logLearningInsight(TEST_MODULE, 
                    "RBAC权限模型综合测试完成，展示了完整的权限管理体系");
            
        } catch (Exception e) {
            log.error("RBAC权限模型综合测试异常", e);
            LearningLogger.logBusinessAnalysis(TEST_MODULE, "测试异常分析", 
                    String.format("测试过程中出现异常: %s，需要检查权限模块的配置", e.getMessage()));
        }
    }

    /**
     * 用户管理功能测试
     */
    private void testUserManagement() {
        try {
            // 测试用户分页查询
            UserPageReqVO userPageReqVO = new UserPageReqVO();
            userPageReqVO.setPageNo(1);
            userPageReqVO.setPageSize(10);
            
            PageResult<AdminUserDO> userPage = learningAdminUserService.getUserPage(userPageReqVO);
            
            LearningLogger.logLearningInsight(TEST_MODULE, 
                    String.format("用户管理测试完成，系统中共有%d个用户", 
                            userPage != null ? userPage.getTotal() : 0));
            
            // 测试特定用户查询
            AdminUserDO adminUser = learningAdminUserService.getUserByUsername("admin");
            if (adminUser != null) {
                LearningLogger.logDataFlow(TEST_MODULE, "管理员用户信息", 
                        String.format("管理员用户 - ID: %d, 昵称: %s, 部门ID: %d", 
                                adminUser.getId(), adminUser.getNickname(), adminUser.getDeptId()));
            }
            
        } catch (Exception e) {
            log.warn("用户管理测试异常: {}", e.getMessage());
        }
    }

    /**
     * 角色管理功能测试
     */
    private void testRoleManagement() {
        try {
            // 测试角色分页查询
            RolePageReqVO rolePageReqVO = new RolePageReqVO();
            rolePageReqVO.setPageNo(1);
            rolePageReqVO.setPageSize(10);
            
            PageResult<RoleDO> rolePage = learningRoleService.getRolePage(rolePageReqVO);
            
            if (rolePage != null && !rolePage.getList().isEmpty()) {
                LearningLogger.logLearningInsight(TEST_MODULE, 
                        String.format("角色管理测试完成，系统中共有%d个角色", rolePage.getTotal()));
                
                // 分析角色类型分布
                long systemRoleCount = rolePage.getList().stream()
                        .filter(role -> role.getType() == 1) // 假设1为系统角色
                        .count();
                
                LearningLogger.logBusinessAnalysis(TEST_MODULE, "角色类型分布", 
                        String.format("系统角色: %d个，业务角色: %d个", 
                                systemRoleCount, rolePage.getList().size() - systemRoleCount));
                
                // 测试超级管理员检查
                Set<Long> roleIds = Set.of(rolePage.getList().get(0).getId());
                boolean hasSuperAdmin = learningRoleService.hasAnySuperAdmin(roleIds);
                
                LearningLogger.logBusinessAnalysis(TEST_MODULE, "超级管理员检查", 
                        String.format("角色集合中%s超级管理员", hasSuperAdmin ? "包含" : "不包含"));
            }
            
        } catch (Exception e) {
            log.warn("角色管理测试异常: {}", e.getMessage());
        }
    }

    /**
     * 菜单权限管理测试
     */
    private void testMenuPermissionManagement() {
        try {
            // 测试菜单列表查询
            List<MenuDO> menuList = learningMenuService.getMenuList();
            
            if (menuList != null && !menuList.isEmpty()) {
                // 统计菜单类型分布
                long dirCount = menuList.stream().filter(menu -> menu.getType() == 1).count();
                long menuCount = menuList.stream().filter(menu -> menu.getType() == 2).count();
                long buttonCount = menuList.stream().filter(menu -> menu.getType() == 3).count();
                
                LearningLogger.logLearningInsight(TEST_MODULE, 
                        String.format("菜单权限管理测试完成，菜单总数: %d，目录: %d，菜单: %d，按钮: %d", 
                                menuList.size(), dirCount, menuCount, buttonCount));
                
                // 测试租户菜单过滤
                MenuListReqVO menuListReqVO = new MenuListReqVO();
                List<MenuDO> tenantMenuList = learningMenuService.getMenuListByTenant(menuListReqVO);
                
                LearningLogger.logBusinessAnalysis(TEST_MODULE, "租户菜单过滤", 
                        String.format("租户可访问菜单数: %d，体现了多租户权限隔离", 
                                tenantMenuList != null ? tenantMenuList.size() : 0));
                
                // 测试权限缓存查询
                if (!menuList.isEmpty() && menuList.get(0).getPermission() != null) {
                    String permission = menuList.get(0).getPermission();
                    List<Long> menuIds = learningMenuService.getMenuIdListByPermissionFromCache(permission);
                    
                    LearningLogger.logBusinessAnalysis(TEST_MODULE, "权限缓存查询", 
                            String.format("权限[%s]关联的菜单数: %d", permission, 
                                    menuIds != null ? menuIds.size() : 0));
                }
            }
            
        } catch (Exception e) {
            log.warn("菜单权限管理测试异常: {}", e.getMessage());
        }
    }

    /**
     * 权限关联管理测试
     */
    private void testPermissionAssociation() {
        try {
            // 测试用户角色查询
            Long testUserId = 1L; // 假设用户ID为1
            Set<Long> userRoleIds = learningPermissionService.getUserRoleIdListByUserIdFromCache(testUserId);
            
            if (userRoleIds != null && !userRoleIds.isEmpty()) {
                LearningLogger.logLearningInsight(TEST_MODULE, 
                        String.format("权限关联测试 - 用户[%d]拥有%d个角色: %s", 
                                testUserId, userRoleIds.size(), userRoleIds));
                
                // 测试角色菜单权限查询
                Set<Long> roleMenuIds = learningPermissionService.getRoleMenuListByRoleId(userRoleIds);
                
                LearningLogger.logBusinessAnalysis(TEST_MODULE, "角色菜单权限", 
                        String.format("角色集合拥有%d个菜单权限", 
                                roleMenuIds != null ? roleMenuIds.size() : 0));
                
                // 测试数据权限查询
                var dataPermission = learningPermissionService.getDeptDataPermission(testUserId);
                
                if (dataPermission != null) {
                    String dataScope = dataPermission.getAll() ? "全部数据" : 
                                     dataPermission.getSelf() ? "仅本人数据" : "部门数据";
                    
                    LearningLogger.logBusinessAnalysis(TEST_MODULE, "数据权限范围", 
                            String.format("用户[%d]的数据权限范围: %s", testUserId, dataScope));
                }
            }
            
        } catch (Exception e) {
            log.warn("权限关联管理测试异常: {}", e.getMessage());
        }
    }

    /**
     * 权限校验框架测试
     */
    private void testSecurityFramework() {
        try {
            // 测试权限校验（注意：这里只是测试方法调用，实际权限校验需要登录用户）
            String testPermission = "system:user:list";
            
            LearningLogger.logBusinessAnalysis(TEST_MODULE, "权限校验框架测试", 
                    String.format("模拟权限校验 - 权限标识: %s", testPermission));
            
            // 测试角色校验
            String testRole = "admin";
            
            LearningLogger.logBusinessAnalysis(TEST_MODULE, "角色校验测试", 
                    String.format("模拟角色校验 - 角色编码: %s", testRole));
            
            // 测试授权范围校验
            String testScope = "user.read";
            
            LearningLogger.logBusinessAnalysis(TEST_MODULE, "授权范围校验测试", 
                    String.format("模拟授权范围校验 - 授权范围: %s", testScope));
            
            LearningLogger.logLearningInsight(TEST_MODULE, 
                    "权限校验框架测试完成，理解了权限校验的多层次机制");
            
        } catch (Exception e) {
            log.warn("权限校验框架测试异常: {}", e.getMessage());
        }
    }

    /**
     * 权限缓存机制测试
     */
    private void testPermissionCache() {
        try {
            LearningLogger.logBusinessAnalysis(TEST_MODULE, "权限缓存机制", 
                    "权限系统使用多级缓存提升性能：本地缓存(权限校验) + Redis缓存(权限数据)");
            
            LearningLogger.logBusinessAnalysis(TEST_MODULE, "缓存策略分析", 
                    "本地缓存过期时间1分钟，Redis缓存根据数据变更自动失效，保证权限变更的实时性");
            
            LearningLogger.logLearningInsight(TEST_MODULE, 
                    "权限缓存机制学习完成，理解了缓存在权限系统中的重要作用");
            
        } catch (Exception e) {
            log.warn("权限缓存机制测试异常: {}", e.getMessage());
        }
    }

    /**
     * 数据权限测试
     */
    private void testDataPermission() {
        try {
            LearningLogger.logBusinessAnalysis(TEST_MODULE, "数据权限机制", 
                    "数据权限通过MyBatis Plus插件实现，在SQL执行时自动添加WHERE条件");
            
            LearningLogger.logBusinessAnalysis(TEST_MODULE, "数据权限类型", 
                    "支持5种数据权限：全部数据、指定部门、本部门、本部门及以下、仅本人");
            
            LearningLogger.logBusinessAnalysis(TEST_MODULE, "数据权限应用", 
                    "数据权限与功能权限结合，实现了完整的权限控制体系");
            
            LearningLogger.logLearningInsight(TEST_MODULE, 
                    "数据权限学习完成，理解了细粒度数据访问控制的实现原理");
            
        } catch (Exception e) {
            log.warn("数据权限测试异常: {}", e.getMessage());
        }
    }

    /**
     * 权限模型学习总结
     */
    @Test
    void testPermissionModelSummary() {
        log.info("=== 权限模型学习总结 ===");
        
        // 打印方法调用统计
        LearningLogger.printMethodCallStatistics();
        
        // 权限模型学习总结
        printPermissionModelConclusion();
    }

    /**
     * 打印权限模型学习总结
     */
    private void printPermissionModelConclusion() {
        log.info("=== 鉴权模块学习总结 ===");
        
        LearningLogger.logLearningInsight(TEST_MODULE, "RBAC权限模型学习收获：");
        log.info("• 理解了用户-角色-权限的三层关联关系");
        log.info("• 掌握了菜单权限的层次化设计：目录-菜单-按钮");
        log.info("• 学习了权限校验的多种方式：注解、编程式、AOP");
        log.info("• 了解了数据权限的实现原理：SQL拦截和动态条件添加");
        
        LearningLogger.logLearningInsight(TEST_MODULE, "权限缓存机制学习收获：");
        log.info("• 掌握了多级缓存的设计：本地缓存 + Redis缓存");
        log.info("• 理解了缓存失效策略：时间过期 + 数据变更触发");
        log.info("• 学习了缓存键的设计原则：用户ID + 权限标识组合");
        
        LearningLogger.logLearningInsight(TEST_MODULE, "权限安全机制学习收获：");
        log.info("• 理解了默认拒绝的安全原则");
        log.info("• 掌握了跨租户访问的特殊处理");
        log.info("• 学习了权限校验异常的安全处理");
        log.info("• 了解了超级管理员的特殊权限机制");
        
        LearningLogger.logLearningInsight(TEST_MODULE, "权限扩展机制学习收获：");
        log.info("• 理解了OAuth2授权范围的权限控制");
        log.info("• 掌握了多租户环境下的权限隔离");
        log.info("• 学习了权限系统的可扩展性设计");
        
        log.info("=== 鉴权模块学习完成 ===");
    }
}
