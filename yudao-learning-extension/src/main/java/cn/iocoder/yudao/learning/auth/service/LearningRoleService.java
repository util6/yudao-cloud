package cn.iocoder.yudao.learning.auth.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.learning.core.util.LearningLogger;
import cn.iocoder.yudao.module.system.controller.admin.permission.vo.role.RolePageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.permission.vo.role.RoleSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.RoleDO;
import cn.iocoder.yudao.module.system.service.permission.RoleService;
import cn.iocoder.yudao.module.system.service.permission.RoleServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 学习扩展 - 角色权限服务实现
 * 
 * 通过继承原RoleServiceImpl类，使用@Primary注解让Spring优先使用这个实现
 * 在每个方法中调用原方法，前后添加学习日志和业务分析
 * 
 * 学习重点：
 * 1. RBAC角色模型：角色作为用户和权限的中间层
 * 2. 角色类型设计：系统角色vs业务角色的区分
 * 3. 超级管理员机制：特殊角色的权限处理
 * 4. 数据权限范围：基于部门的数据访问控制
 * 5. 角色缓存策略：提升角色权限查询性能
 * 6. 角色状态管理：启用/禁用对权限的影响
 * 7. 角色继承关系：角色间的权限继承设计
 * 8. 角色权限校验：运行时权限检查机制
 * 
 * @author 学习者
 */
@Slf4j
@Service
@Primary  // 关键注解：让Spring优先使用这个Bean，原代码调用RoleService时会调用到这里
public class LearningRoleService extends RoleServiceImpl {

    private static final String MODULE_NAME = "鉴权模块-角色权限";

    /**
     * 学习扩展 - 创建角色
     * 
     * 学习要点：
     * 1. 角色类型的业务含义
     * 2. 角色编码的唯一性约束
     * 3. 角色排序的显示逻辑
     * 4. 角色状态的初始化
     */
    @Override
    public Long createRole(@Valid RoleSaveReqVO createReqVO, Integer type) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "createRole", createReqVO, type);
        
        try {
            // 学习分析：角色创建的业务意义
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "角色创建", 
                    String.format("创建角色 - 名称: %s, 编码: %s, 类型: %s, 排序: %d", 
                            createReqVO.getName(), 
                            createReqVO.getCode(),
                            getRoleTypeDesc(type),
                            createReqVO.getSort()));
            
            // 学习分析：角色类型的重要性
            analyzeRoleType(type);
            
            // 学习分析：角色编码的设计原则
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "角色编码设计", 
                    String.format("角色编码[%s]用于程序中的权限判断，应遵循命名规范，具有业务语义", 
                            createReqVO.getCode()));
            
            // 学习分析：角色排序的作用
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "角色排序机制", 
                    String.format("角色排序[%d]决定了角色在列表中的显示顺序，便于管理和查找", 
                            createReqVO.getSort()));
            
            // 调用原方法 - 这里可以设置断点，深入学习角色创建流程
            Long roleId = super.createRole(createReqVO, type);
            
            // 学习分析：角色创建成功后的状态
            if (roleId != null) {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "角色创建成功", 
                        String.format("角色创建成功，分配ID: %d，可以开始分配菜单权限和用户", roleId));
                
                // 学习分析：新角色的权限状态
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "新角色权限状态", 
                        "新创建的角色默认没有任何菜单权限，需要通过权限分配功能进行授权");
            }
            
            LearningLogger.logMethodEnd(MODULE_NAME, "createRole", startTime, roleId);
            
            // 学习心得记录
            LearningLogger.logLearningInsight(MODULE_NAME, 
                    "角色创建体现了RBAC模型的核心：角色作为权限载体，连接用户和具体权限");
            
            return roleId;
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "createRole", startTime, e);
            
            // 学习分析：角色创建失败的原因分析
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "角色创建失败", 
                    String.format("角色创建失败，可能原因：角色名称重复、角色编码冲突、参数校验失败。异常类型: %s", 
                            e.getClass().getSimpleName()));
            
            throw e;
        }
    }

    /**
     * 学习扩展 - 更新角色
     * 
     * 学习要点：
     * 1. 角色信息变更的影响范围
     * 2. 角色编码变更的风险控制
     * 3. 角色状态变更的权限影响
     */
    @Override
    public void updateRole(@Valid RoleSaveReqVO updateReqVO) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "updateRole", updateReqVO);
        
        try {
            // 学习分析：角色更新的复杂性
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "角色更新", 
                    String.format("更新角色[%d] - 名称: %s, 编码: %s, 状态: %d", 
                            updateReqVO.getId(), updateReqVO.getName(), 
                            updateReqVO.getCode(), updateReqVO.getStatus()));
            
            // 学习分析：角色状态变更的影响
            if (updateReqVO.getStatus() != null) {
                String statusDesc = updateReqVO.getStatus() == 1 ? "启用" : "禁用";
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "角色状态变更", 
                        String.format("角色状态变更为[%s]，将影响所有拥有该角色的用户权限", statusDesc));
            }
            
            // 调用原方法
            super.updateRole(updateReqVO);
            
            // 学习分析：更新完成后的影响
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "角色更新影响", 
                    "角色信息更新后，相关的权限缓存会自动刷新，确保权限变更的实时生效");
            
            LearningLogger.logMethodEnd(MODULE_NAME, "updateRole", startTime, "更新成功");
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "updateRole", startTime, e);
            throw e;
        }
    }

    /**
     * 学习扩展 - 删除角色
     * 
     * 学习要点：
     * 1. 角色删除的安全检查
     * 2. 关联数据的级联清理
     * 3. 用户权限的自动回收
     */
    @Override
    public void deleteRole(Long id) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "deleteRole", id);
        
        try {
            // 学习分析：角色删除前的检查
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "角色删除检查", 
                    String.format("删除角色[%d]前需要检查：是否有用户使用该角色、是否为系统内置角色", id));
            
            // 学习分析：级联删除的影响
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "级联删除影响", 
                    "角色删除会触发用户-角色关联、角色-菜单关联的自动清理");
            
            // 调用原方法
            super.deleteRole(id);
            
            // 学习分析：删除后的清理工作
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "删除后清理", 
                    "角色删除后，所有相关的权限关联关系已清理，用户权限已自动更新");
            
            LearningLogger.logMethodEnd(MODULE_NAME, "deleteRole", startTime, "删除成功");
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "deleteRole", startTime, e);
            
            // 学习分析：删除失败的原因
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "删除失败分析", 
                    "删除失败可能由于：角色不存在、角色被用户使用、系统内置角色不可删除等");
            
            throw e;
        }
    }

    /**
     * 学习扩展 - 设置角色数据权限
     * 
     * 学习要点：
     * 1. 数据权限的范围控制
     * 2. 部门级数据隔离机制
     * 3. 数据权限的继承规则
     */
    @Override
    public void updateRoleDataScope(Long id, Integer dataScope, Set<Long> dataScopeDeptIds) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "updateRoleDataScope", id, dataScope, dataScopeDeptIds);
        
        try {
            // 学习分析：数据权限的重要性
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "数据权限设置", 
                    String.format("设置角色[%d]的数据权限范围: %s", id, getDataScopeDesc(dataScope)));
            
            // 学习分析：数据权限范围的含义
            analyzeDataScope(dataScope, dataScopeDeptIds);
            
            // 调用原方法
            super.updateRoleDataScope(id, dataScope, dataScopeDeptIds);
            
            // 学习分析：数据权限设置完成
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "数据权限生效", 
                    "数据权限设置完成，用户查询数据时将自动应用权限过滤");
            
            LearningLogger.logMethodEnd(MODULE_NAME, "updateRoleDataScope", startTime, "数据权限设置成功");
            
            // 学习心得记录
            LearningLogger.logLearningInsight(MODULE_NAME, 
                    "数据权限体现了细粒度权限控制：不仅控制功能访问，还控制数据访问范围");
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "updateRoleDataScope", startTime, e);
            throw e;
        }
    }

    /**
     * 学习扩展 - 获取角色（从缓存）
     * 
     * 学习要点：
     * 1. 角色缓存的性能优势
     * 2. 缓存一致性保证
     * 3. 缓存失效策略
     */
    @Override
    public RoleDO getRoleFromCache(Long id) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "getRoleFromCache", id);
        
        try {
            // 学习分析：角色缓存的性能价值
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "角色缓存查询", 
                    String.format("从缓存查询角色[%d]，避免频繁数据库访问，提升权限校验性能", id));
            
            // 调用原方法
            RoleDO role = super.getRoleFromCache(id);
            
            // 学习分析：缓存查询结果
            if (role != null) {
                LearningLogger.logDataFlow(MODULE_NAME, "角色缓存命中", 
                        String.format("角色缓存命中 - 名称: %s, 编码: %s, 状态: %s", 
                                role.getName(), role.getCode(), 
                                role.getStatus() == 1 ? "启用" : "禁用"));
            } else {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "角色缓存未命中", 
                        String.format("角色[%d]缓存未命中或角色不存在", id));
            }
            
            LearningLogger.logMethodEnd(MODULE_NAME, "getRoleFromCache", startTime, 
                    role != null ? "缓存命中" : "缓存未命中");
            
            return role;
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "getRoleFromCache", startTime, e);
            throw e;
        }
    }

    /**
     * 学习扩展 - 判断是否有超级管理员
     * 
     * 学习要点：
     * 1. 超级管理员的特殊权限
     * 2. 权限校验的优先级
     * 3. 安全设计的考虑
     */
    @Override
    public boolean hasAnySuperAdmin(Collection<Long> ids) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "hasAnySuperAdmin", ids);
        
        try {
            // 学习分析：超级管理员机制
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "超级管理员检查", 
                    String.format("检查角色集合[%s]中是否包含超级管理员角色", ids));
            
            // 调用原方法
            boolean hasSuperAdmin = super.hasAnySuperAdmin(ids);
            
            // 学习分析：超级管理员的权限特性
            if (hasSuperAdmin) {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "超级管理员权限", 
                        "检测到超级管理员角色，拥有系统所有权限，无需进行具体权限校验");
            } else {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "普通角色权限", 
                        "未检测到超级管理员角色，需要进行具体的权限校验");
            }
            
            LearningLogger.logMethodEnd(MODULE_NAME, "hasAnySuperAdmin", startTime, String.valueOf(hasSuperAdmin));
            
            return hasSuperAdmin;
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "hasAnySuperAdmin", startTime, e);
            throw e;
        }
    }

    /**
     * 学习扩展 - 获取角色分页
     * 
     * 学习要点：
     * 1. 角色查询的过滤条件
     * 2. 分页查询的性能优化
     * 3. 角色状态的业务含义
     */
    @Override
    public PageResult<RoleDO> getRolePage(RolePageReqVO reqVO) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "getRolePage", reqVO);
        
        try {
            // 学习分析：角色分页查询条件
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "角色分页查询", 
                    String.format("角色分页查询 - 页码: %d, 页大小: %d, 名称: %s, 编码: %s, 状态: %s", 
                            reqVO.getPageNo(), reqVO.getPageSize(), 
                            reqVO.getName(), reqVO.getCode(), reqVO.getStatus()));
            
            // 调用原方法
            PageResult<RoleDO> pageResult = super.getRolePage(reqVO);
            
            // 学习分析：查询结果统计
            if (pageResult != null) {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "角色查询统计", 
                        String.format("查询到%d个角色，总计%d个", 
                                pageResult.getList().size(), pageResult.getTotal()));
                
                // 分析角色状态分布
                if (!pageResult.getList().isEmpty()) {
                    long enabledCount = pageResult.getList().stream()
                            .filter(role -> role.getStatus() == 1)
                            .count();
                    
                    LearningLogger.logBusinessAnalysis(MODULE_NAME, "角色状态分布", 
                            String.format("当前页面中启用的角色: %d个，禁用的角色: %d个", 
                                    enabledCount, pageResult.getList().size() - enabledCount));
                }
            }
            
            LearningLogger.logMethodEnd(MODULE_NAME, "getRolePage", startTime, 
                    String.format("返回%d个角色", pageResult != null ? pageResult.getList().size() : 0));
            
            return pageResult;
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "getRolePage", startTime, e);
            throw e;
        }
    }

    /**
     * 分析角色类型的业务含义
     */
    private void analyzeRoleType(Integer type) {
        String typeDesc = getRoleTypeDesc(type);
        
        switch (type) {
            case 1: // 系统角色
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "角色类型分析", 
                        String.format("%s - 系统内置角色，通常具有管理权限，不建议删除", typeDesc));
                break;
            case 2: // 业务角色
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "角色类型分析", 
                        String.format("%s - 业务自定义角色，根据业务需要创建和管理", typeDesc));
                break;
            default:
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "角色类型分析", 
                        String.format("未知角色类型: %d", type));
        }
    }

    /**
     * 分析数据权限范围
     */
    private void analyzeDataScope(Integer dataScope, Set<Long> dataScopeDeptIds) {
        String scopeDesc = getDataScopeDesc(dataScope);
        
        LearningLogger.logBusinessAnalysis(MODULE_NAME, "数据权限范围", scopeDesc);
        
        if (dataScope == 2 && dataScopeDeptIds != null && !dataScopeDeptIds.isEmpty()) {
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "自定义部门权限", 
                    String.format("指定了%d个部门的数据访问权限: %s", 
                            dataScopeDeptIds.size(), dataScopeDeptIds));
        }
    }

    /**
     * 获取角色类型描述
     */
    private String getRoleTypeDesc(Integer type) {
        switch (type) {
            case 1: return "系统角色";
            case 2: return "业务角色";
            default: return "未知类型";
        }
    }

    /**
     * 获取数据权限范围描述
     */
    private String getDataScopeDesc(Integer dataScope) {
        switch (dataScope) {
            case 1: return "全部数据权限";
            case 2: return "指定部门数据权限";
            case 3: return "本部门数据权限";
            case 4: return "本部门及以下数据权限";
            case 5: return "仅本人数据权限";
            default: return "未知数据权限范围";
        }
    }
}
