package cn.iocoder.yudao.learning.auth.service;

import cn.iocoder.yudao.framework.common.biz.system.permission.dto.DeptDataPermissionRespDTO;
import cn.iocoder.yudao.learning.core.util.LearningLogger;
import cn.iocoder.yudao.module.system.service.permission.PermissionService;
import cn.iocoder.yudao.module.system.service.permission.PermissionServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;

/**
 * 学习扩展 - 权限关联服务实现
 * 
 * 通过继承原PermissionServiceImpl类，使用@Primary注解让Spring优先使用这个实现
 * 在每个方法中调用原方法，前后添加学习日志和业务分析
 * 
 * 学习重点：
 * 1. RBAC权限模型：用户-角色-权限的三层关联
 * 2. 权限校验机制：运行时权限检查的实现
 * 3. 权限缓存策略：多级缓存提升权限校验性能
 * 4. 数据权限控制：基于部门的数据访问范围
 * 5. 权限继承规则：角色权限的传递机制
 * 6. 权限关联管理：角色-菜单、用户-角色的关联维护
 * 7. 超级管理员机制：特殊权限的处理逻辑
 * 8. 权限变更通知：权限变更时的缓存更新
 * 
 * @author 学习者
 */
@Slf4j
@Service
@Primary  // 关键注解：让Spring优先使用这个Bean，原代码调用PermissionService时会调用到这里
public class LearningPermissionService extends PermissionServiceImpl {

    private static final String MODULE_NAME = "鉴权模块-权限关联";

    /**
     * 学习扩展 - 判断用户是否有任一权限
     * 
     * 学习要点：
     * 1. 权限校验的核心逻辑
     * 2. 缓存机制的性能优化
     * 3. 超级管理员的特殊处理
     * 4. 权限继承的实现方式
     */
    @Override
    public boolean hasAnyPermissions(Long userId, String... permissions) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "hasAnyPermissions", userId, permissions);
        
        try {
            // 学习分析：权限校验的业务场景
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "权限校验", 
                    String.format("校验用户[%d]是否拥有权限: %s", userId, String.join(", ", permissions)));
            
            // 学习分析：权限校验的优先级
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "权限校验流程", 
                    "权限校验流程: 1.检查跨租户访问 2.获取用户角色 3.检查具体权限 4.检查超级管理员");
            
            // 调用原方法 - 这里可以设置断点，深入学习权限校验流程
            boolean hasPermission = super.hasAnyPermissions(userId, permissions);
            
            // 学习分析：权限校验结果
            if (hasPermission) {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "权限校验通过", 
                        String.format("用户[%d]拥有所需权限，允许访问", userId));
            } else {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "权限校验失败", 
                        String.format("用户[%d]不具备所需权限，拒绝访问", userId));
            }
            
            LearningLogger.logMethodEnd(MODULE_NAME, "hasAnyPermissions", startTime, String.valueOf(hasPermission));
            
            // 学习心得记录
            LearningLogger.logLearningInsight(MODULE_NAME, 
                    "权限校验是安全系统的核心：通过角色继承权限，支持缓存优化，确保访问控制的有效性");
            
            return hasPermission;
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "hasAnyPermissions", startTime, e);
            
            // 学习分析：权限校验异常的处理
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "权限校验异常", 
                    "权限校验异常时应该拒绝访问，确保系统安全");
            
            throw e;
        }
    }

    /**
     * 学习扩展 - 判断用户是否有任一角色
     * 
     * 学习要点：
     * 1. 角色校验与权限校验的区别
     * 2. 角色编码的使用方式
     * 3. 角色状态的影响
     */
    @Override
    public boolean hasAnyRoles(Long userId, String... roles) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "hasAnyRoles", userId, roles);
        
        try {
            // 学习分析：角色校验的应用场景
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "角色校验", 
                    String.format("校验用户[%d]是否拥有角色: %s", userId, String.join(", ", roles)));
            
            // 学习分析：角色校验与权限校验的区别
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "角色vs权限校验", 
                    "角色校验检查用户身份，权限校验检查具体操作权限，角色校验更粗粒度");
            
            // 调用原方法
            boolean hasRole = super.hasAnyRoles(userId, roles);
            
            // 学习分析：角色校验结果
            String resultDesc = hasRole ? "拥有所需角色" : "不具备所需角色";
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "角色校验结果", 
                    String.format("用户[%d] %s", userId, resultDesc));
            
            LearningLogger.logMethodEnd(MODULE_NAME, "hasAnyRoles", startTime, String.valueOf(hasRole));
            
            return hasRole;
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "hasAnyRoles", startTime, e);
            throw e;
        }
    }

    /**
     * 学习扩展 - 分配角色菜单权限
     * 
     * 学习要点：
     * 1. 角色-菜单关联的维护
     * 2. 权限变更的事务处理
     * 3. 缓存更新的时机
     * 4. 权限变更的影响范围
     */
    @Override
    public void assignRoleMenu(Long roleId, Set<Long> menuIds) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "assignRoleMenu", roleId, menuIds);
        
        try {
            // 学习分析：角色菜单分配的业务意义
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "角色菜单分配", 
                    String.format("为角色[%d]分配%d个菜单权限", roleId, menuIds != null ? menuIds.size() : 0));
            
            // 学习分析：权限分配的策略
            if (menuIds != null && !menuIds.isEmpty()) {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "权限分配策略", 
                        "采用全量替换策略：先删除原有权限关联，再添加新的权限关联");
            } else {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "权限回收", 
                        "菜单ID集合为空，将回收角色的所有菜单权限");
            }
            
            // 学习分析：事务处理的重要性
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "事务处理", 
                    "权限分配使用事务保证数据一致性，避免权限分配过程中的数据不一致");
            
            // 调用原方法 - 这里可以设置断点，深入学习权限分配流程
            super.assignRoleMenu(roleId, menuIds);
            
            // 学习分析：权限分配完成后的影响
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "权限分配完成", 
                    "角色菜单权限分配完成，相关缓存已清除，权限变更立即生效");
            
            LearningLogger.logMethodEnd(MODULE_NAME, "assignRoleMenu", startTime, "分配成功");
            
            // 学习心得记录
            LearningLogger.logLearningInsight(MODULE_NAME, 
                    "权限分配体现了RBAC模型的灵活性：通过角色-权限关联，实现权限的集中管理和灵活分配");
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "assignRoleMenu", startTime, e);
            
            // 学习分析：权限分配失败的处理
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "权限分配失败", 
                    "权限分配失败时，事务会回滚，确保权限数据的一致性");
            
            throw e;
        }
    }

    /**
     * 学习扩展 - 分配用户角色
     * 
     * 学习要点：
     * 1. 用户-角色关联的维护
     * 2. 角色变更对权限的影响
     * 3. 用户权限的实时更新
     */
    @Override
    public void assignUserRole(Long userId, Set<Long> roleIds) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "assignUserRole", userId, roleIds);
        
        try {
            // 学习分析：用户角色分配的业务意义
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "用户角色分配", 
                    String.format("为用户[%d]分配%d个角色", userId, roleIds != null ? roleIds.size() : 0));
            
            // 学习分析：角色分配的影响
            if (roleIds != null && !roleIds.isEmpty()) {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "角色分配影响", 
                        "用户角色变更将影响用户的所有权限，包括菜单访问权限和数据访问权限");
            } else {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "角色回收", 
                        "回收用户的所有角色，用户将失去除基础权限外的所有权限");
            }
            
            // 调用原方法
            super.assignUserRole(userId, roleIds);
            
            // 学习分析：用户角色分配完成
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "用户角色分配完成", 
                    "用户角色分配完成，用户权限缓存已清除，新权限立即生效");
            
            LearningLogger.logMethodEnd(MODULE_NAME, "assignUserRole", startTime, "分配成功");
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "assignUserRole", startTime, e);
            throw e;
        }
    }

    /**
     * 学习扩展 - 获取角色拥有的菜单权限
     * 
     * 学习要点：
     * 1. 角色权限的查询机制
     * 2. 超级管理员的特殊处理
     * 3. 权限继承的实现
     */
    @Override
    public Set<Long> getRoleMenuListByRoleId(Collection<Long> roleIds) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "getRoleMenuListByRoleId", roleIds);
        
        try {
            // 学习分析：角色权限查询的应用场景
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "角色权限查询", 
                    String.format("查询角色集合[%s]拥有的菜单权限", roleIds));
            
            // 调用原方法
            Set<Long> menuIds = super.getRoleMenuListByRoleId(roleIds);
            
            // 学习分析：权限查询结果
            if (menuIds != null) {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "权限查询结果", 
                        String.format("角色集合拥有%d个菜单权限", menuIds.size()));
                
                // 学习分析：超级管理员的权限范围
                if (menuIds.size() > 100) { // 假设超过100个权限可能是超级管理员
                    LearningLogger.logBusinessAnalysis(MODULE_NAME, "超级管理员权限", 
                            "权限数量较多，可能包含超级管理员角色，拥有系统全部权限");
                }
            }
            
            LearningLogger.logMethodEnd(MODULE_NAME, "getRoleMenuListByRoleId", startTime, 
                    String.format("返回%d个菜单权限", menuIds != null ? menuIds.size() : 0));
            
            return menuIds;
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "getRoleMenuListByRoleId", startTime, e);
            throw e;
        }
    }

    /**
     * 学习扩展 - 获取用户的数据权限
     * 
     * 学习要点：
     * 1. 数据权限的计算逻辑
     * 2. 部门权限的继承规则
     * 3. 数据权限的优先级
     */
    @Override
    public DeptDataPermissionRespDTO getDeptDataPermission(Long userId) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "getDeptDataPermission", userId);
        
        try {
            // 学习分析：数据权限查询的重要性
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "数据权限查询", 
                    String.format("查询用户[%d]的数据访问权限，用于数据查询时的权限过滤", userId));
            
            // 学习分析：数据权限的应用场景
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "数据权限应用", 
                    "数据权限通过MyBatis Plus插件在SQL执行时自动添加WHERE条件，实现数据级权限控制");
            
            // 调用原方法
            DeptDataPermissionRespDTO dataPermission = super.getDeptDataPermission(userId);
            
            // 学习分析：数据权限结果
            if (dataPermission != null) {
                analyzeDataPermissionResult(dataPermission);
            }
            
            LearningLogger.logMethodEnd(MODULE_NAME, "getDeptDataPermission", startTime, "数据权限查询完成");
            
            // 学习心得记录
            LearningLogger.logLearningInsight(MODULE_NAME, 
                    "数据权限实现了细粒度的数据访问控制：不仅控制功能权限，还控制数据访问范围");
            
            return dataPermission;
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "getDeptDataPermission", startTime, e);
            throw e;
        }
    }

    /**
     * 学习扩展 - 获取用户角色ID列表（缓存）
     * 
     * 学习要点：
     * 1. 用户角色缓存机制
     * 2. 缓存键的设计策略
     * 3. 缓存更新时机
     */
    @Override
    public Set<Long> getUserRoleIdListByUserIdFromCache(Long userId) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "getUserRoleIdListByUserIdFromCache", userId);
        
        try {
            // 学习分析：用户角色缓存的性能价值
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "用户角色缓存", 
                    String.format("从缓存查询用户[%d]的角色列表，提升权限校验性能", userId));
            
            // 调用原方法
            Set<Long> roleIds = super.getUserRoleIdListByUserIdFromCache(userId);
            
            // 学习分析：缓存查询结果
            if (roleIds != null && !roleIds.isEmpty()) {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "用户角色缓存命中", 
                        String.format("用户[%d]拥有%d个角色: %s", userId, roleIds.size(), roleIds));
            } else {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "用户角色缓存", 
                        String.format("用户[%d]没有分配角色或缓存未命中", userId));
            }
            
            LearningLogger.logMethodEnd(MODULE_NAME, "getUserRoleIdListByUserIdFromCache", startTime, 
                    String.format("返回%d个角色", roleIds != null ? roleIds.size() : 0));
            
            return roleIds;
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "getUserRoleIdListByUserIdFromCache", startTime, e);
            throw e;
        }
    }

    /**
     * 学习扩展 - 处理角色删除
     * 
     * 学习要点：
     * 1. 级联删除的数据一致性
     * 2. 权限回收的自动化
     * 3. 缓存清理的完整性
     */
    @Override
    public void processRoleDeleted(Long roleId) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "processRoleDeleted", roleId);
        
        try {
            // 学习分析：角色删除的级联影响
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "角色删除处理", 
                    String.format("处理角色[%d]删除的级联影响：清理用户-角色关联、角色-菜单关联", roleId));
            
            // 调用原方法
            super.processRoleDeleted(roleId);
            
            // 学习分析：删除处理完成
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "角色删除处理完成", 
                    "角色删除的级联处理完成，所有相关的权限关联已清理，缓存已更新");
            
            LearningLogger.logMethodEnd(MODULE_NAME, "processRoleDeleted", startTime, "处理完成");
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "processRoleDeleted", startTime, e);
            throw e;
        }
    }

    /**
     * 分析数据权限查询结果
     */
    private void analyzeDataPermissionResult(DeptDataPermissionRespDTO dataPermission) {
        if (dataPermission.getAll()) {
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "数据权限范围", 
                    "用户拥有全部数据访问权限，可以查看所有数据");
        } else if (dataPermission.getSelf()) {
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "数据权限范围", 
                    "用户只能查看自己创建的数据，数据权限最严格");
        } else if (dataPermission.getDeptIds() != null && !dataPermission.getDeptIds().isEmpty()) {
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "数据权限范围", 
                    String.format("用户可以查看指定部门的数据，部门数量: %d", 
                            dataPermission.getDeptIds().size()));
        }
    }
}
