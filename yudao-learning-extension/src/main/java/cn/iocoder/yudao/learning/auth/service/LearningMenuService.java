package cn.iocoder.yudao.learning.auth.service;

import cn.iocoder.yudao.learning.core.util.LearningLogger;
import cn.iocoder.yudao.module.system.controller.admin.permission.vo.menu.MenuListReqVO;
import cn.iocoder.yudao.module.system.controller.admin.permission.vo.menu.MenuSaveVO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.MenuDO;
import cn.iocoder.yudao.module.system.service.permission.MenuService;
import cn.iocoder.yudao.module.system.service.permission.MenuServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 学习扩展 - 菜单权限服务实现
 * 
 * 通过继承原MenuServiceImpl类，使用@Primary注解让Spring优先使用这个实现
 * 在每个方法中调用原方法，前后添加学习日志和业务分析
 * 
 * 学习重点：
 * 1. 菜单权限体系：目录、菜单、按钮三级结构
 * 2. 权限标识设计：基于资源路径的权限编码
 * 3. 菜单层级管理：父子关系和树形结构
 * 4. 权限缓存机制：Redis缓存提升权限校验性能
 * 5. 菜单路由生成：前端路由的动态生成
 * 6. 权限继承规则：父菜单权限对子菜单的影响
 * 7. 租户权限隔离：多租户环境下的菜单权限管理
 * 8. 菜单状态控制：启用/禁用状态的级联影响
 * 
 * @author 学习者
 */
@Slf4j
@Service
@Primary  // 关键注解：让Spring优先使用这个Bean，原代码调用MenuService时会调用到这里
public class LearningMenuService extends MenuServiceImpl {

    private static final String MODULE_NAME = "鉴权模块-菜单权限";

    /**
     * 学习扩展 - 创建菜单
     * 
     * 学习要点：
     * 1. 菜单层级结构的维护
     * 2. 权限标识的唯一性校验
     * 3. 菜单类型的业务规则
     * 4. 缓存更新策略
     */
    @Override
    public Long createMenu(@Valid MenuSaveVO createReqVO) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "createMenu", createReqVO);
        
        try {
            // 学习分析：菜单创建的业务意义
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "菜单创建", 
                    String.format("创建菜单 - 名称: %s, 类型: %s, 父菜单ID: %s, 权限标识: %s", 
                            createReqVO.getName(), 
                            getMenuTypeDesc(createReqVO.getType()),
                            createReqVO.getParentId(),
                            createReqVO.getPermission()));
            
            // 学习分析：菜单类型的业务含义
            analyzeMenuType(createReqVO.getType(), createReqVO.getPermission());
            
            // 学习分析：父子菜单关系
            if (createReqVO.getParentId() != null && createReqVO.getParentId() > 0) {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "菜单层级关系", 
                        String.format("创建子菜单，父菜单ID: %d，形成树形权限结构", createReqVO.getParentId()));
            } else {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "菜单层级关系", 
                        "创建根级菜单，作为权限树的顶层节点");
            }
            
            // 调用原方法 - 这里可以设置断点，深入学习菜单创建流程
            Long menuId = super.createMenu(createReqVO);
            
            // 学习分析：菜单创建成功后的影响
            if (menuId != null) {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "菜单创建成功", 
                        String.format("菜单创建成功，分配ID: %d，权限体系已更新", menuId));
                
                // 学习分析：缓存更新机制
                if (createReqVO.getPermission() != null) {
                    LearningLogger.logBusinessAnalysis(MODULE_NAME, "权限缓存更新", 
                            String.format("权限标识[%s]的缓存已清除，下次访问将重新加载", createReqVO.getPermission()));
                }
            }
            
            LearningLogger.logMethodEnd(MODULE_NAME, "createMenu", startTime, menuId);
            
            // 学习心得记录
            LearningLogger.logLearningInsight(MODULE_NAME, 
                    "菜单创建体现了权限系统的层次化设计：树形结构、类型区分、权限标识、缓存管理");
            
            return menuId;
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "createMenu", startTime, e);
            
            // 学习分析：菜单创建失败的原因分析
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "菜单创建失败", 
                    String.format("菜单创建失败，可能原因：父菜单不存在、菜单名称重复、权限标识冲突。异常类型: %s", 
                            e.getClass().getSimpleName()));
            
            throw e;
        }
    }

    /**
     * 学习扩展 - 更新菜单
     * 
     * 学习要点：
     * 1. 菜单更新的数据一致性
     * 2. 权限变更的影响范围
     * 3. 缓存失效策略
     */
    @Override
    public void updateMenu(@Valid MenuSaveVO updateReqVO) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "updateMenu", updateReqVO);
        
        try {
            // 学习分析：菜单更新的复杂性
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "菜单更新", 
                    String.format("更新菜单[%d] - 名称: %s, 权限标识: %s", 
                            updateReqVO.getId(), updateReqVO.getName(), updateReqVO.getPermission()));
            
            // 学习分析：权限标识变更的影响
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "权限变更影响", 
                    "菜单权限标识变更会影响所有相关的权限缓存，需要全量清除缓存");
            
            // 调用原方法
            super.updateMenu(updateReqVO);
            
            // 学习分析：更新完成后的缓存处理
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "缓存清理策略", 
                    "菜单更新后采用全量缓存清理策略，确保权限数据的一致性");
            
            LearningLogger.logMethodEnd(MODULE_NAME, "updateMenu", startTime, "更新成功");
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "updateMenu", startTime, e);
            throw e;
        }
    }

    /**
     * 学习扩展 - 删除菜单
     * 
     * 学习要点：
     * 1. 级联删除的安全检查
     * 2. 关联数据的清理机制
     * 3. 权限回收处理
     */
    @Override
    public void deleteMenu(Long id) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "deleteMenu", id);
        
        try {
            // 学习分析：删除前的安全检查
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "删除安全检查", 
                    String.format("删除菜单[%d]前需要检查：是否有子菜单、是否被角色引用", id));
            
            // 学习分析：级联删除的影响
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "级联删除影响", 
                    "菜单删除会触发角色权限的自动清理，保证权限数据的完整性");
            
            // 调用原方法
            super.deleteMenu(id);
            
            // 学习分析：删除后的清理工作
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "删除后清理", 
                    "菜单删除后，相关的角色-菜单关联关系已自动清理，权限缓存已更新");
            
            LearningLogger.logMethodEnd(MODULE_NAME, "deleteMenu", startTime, "删除成功");
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "deleteMenu", startTime, e);
            
            // 学习分析：删除失败的原因
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "删除失败分析", 
                    "删除失败可能由于：存在子菜单、菜单不存在、数据库约束等");
            
            throw e;
        }
    }

    /**
     * 学习扩展 - 获取菜单列表
     * 
     * 学习要点：
     * 1. 菜单树形结构的构建
     * 2. 权限过滤机制
     * 3. 状态控制逻辑
     */
    @Override
    public List<MenuDO> getMenuList() {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "getMenuList");
        
        try {
            // 学习分析：菜单列表查询的应用场景
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "菜单列表查询", 
                    "获取所有菜单列表，用于权限管理界面和菜单树构建");
            
            // 调用原方法
            List<MenuDO> menuList = super.getMenuList();
            
            // 学习分析：查询结果统计
            if (menuList != null) {
                long dirCount = menuList.stream().filter(menu -> menu.getType() == 1).count();
                long menuCount = menuList.stream().filter(menu -> menu.getType() == 2).count();
                long buttonCount = menuList.stream().filter(menu -> menu.getType() == 3).count();
                
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "菜单结构统计", 
                        String.format("菜单总数: %d，其中目录: %d个，菜单: %d个，按钮: %d个", 
                                menuList.size(), dirCount, menuCount, buttonCount));
            }
            
            LearningLogger.logMethodEnd(MODULE_NAME, "getMenuList", startTime, 
                    String.format("返回%d个菜单", menuList != null ? menuList.size() : 0));
            
            return menuList;
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "getMenuList", startTime, e);
            throw e;
        }
    }

    /**
     * 学习扩展 - 基于租户筛选菜单列表
     * 
     * 学习要点：
     * 1. 多租户权限隔离
     * 2. 菜单权限的租户级控制
     * 3. 系统租户的特殊处理
     */
    @Override
    public List<MenuDO> getMenuListByTenant(MenuListReqVO reqVO) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "getMenuListByTenant", reqVO);
        
        try {
            // 学习分析：租户权限隔离的重要性
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "租户权限隔离", 
                    "多租户环境下，不同租户只能访问被授权的菜单，实现数据和功能的隔离");
            
            // 调用原方法
            List<MenuDO> menuList = super.getMenuListByTenant(reqVO);
            
            // 学习分析：租户菜单过滤结果
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "租户菜单过滤", 
                    String.format("租户菜单过滤后返回%d个菜单，确保了租户间的权限隔离", 
                            menuList != null ? menuList.size() : 0));
            
            LearningLogger.logMethodEnd(MODULE_NAME, "getMenuListByTenant", startTime, 
                    String.format("返回%d个菜单", menuList != null ? menuList.size() : 0));
            
            return menuList;
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "getMenuListByTenant", startTime, e);
            throw e;
        }
    }

    /**
     * 学习扩展 - 根据权限标识获取菜单ID列表（缓存）
     * 
     * 学习要点：
     * 1. 权限缓存的设计原理
     * 2. 缓存键的设计策略
     * 3. 缓存更新时机
     */
    @Override
    public List<Long> getMenuIdListByPermissionFromCache(String permission) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "getMenuIdListByPermissionFromCache", permission);
        
        try {
            // 学习分析：权限缓存的性能优势
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "权限缓存机制", 
                    String.format("通过缓存查询权限[%s]对应的菜单ID，避免频繁数据库查询，提升权限校验性能", permission));
            
            // 调用原方法
            List<Long> menuIds = super.getMenuIdListByPermissionFromCache(permission);
            
            // 学习分析：缓存查询结果
            if (menuIds != null && !menuIds.isEmpty()) {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "权限缓存命中", 
                        String.format("权限[%s]缓存命中，找到%d个关联菜单", permission, menuIds.size()));
            } else {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "权限缓存未命中", 
                        String.format("权限[%s]缓存未命中或无关联菜单", permission));
            }
            
            LearningLogger.logMethodEnd(MODULE_NAME, "getMenuIdListByPermissionFromCache", startTime, 
                    String.format("返回%d个菜单ID", menuIds != null ? menuIds.size() : 0));
            
            return menuIds;
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "getMenuIdListByPermissionFromCache", startTime, e);
            throw e;
        }
    }

    /**
     * 分析菜单类型的业务含义
     */
    private void analyzeMenuType(Integer type, String permission) {
        String typeDesc = getMenuTypeDesc(type);
        
        switch (type) {
            case 1: // 目录
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "菜单类型分析", 
                        String.format("%s - 用于组织菜单结构，通常不包含具体权限标识", typeDesc));
                break;
            case 2: // 菜单
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "菜单类型分析", 
                        String.format("%s - 对应前端路由页面，通常包含页面访问权限", typeDesc));
                break;
            case 3: // 按钮
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "菜单类型分析", 
                        String.format("%s - 对应页面内的操作按钮，权限标识: %s", typeDesc, permission));
                if (permission != null) {
                    LearningLogger.logBusinessAnalysis(MODULE_NAME, "按钮权限设计", 
                            "按钮权限通过权限标识控制，实现页面内的细粒度权限控制");
                }
                break;
            default:
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "菜单类型分析", 
                        String.format("未知菜单类型: %d", type));
        }
    }

    /**
     * 获取菜单类型描述
     */
    private String getMenuTypeDesc(Integer type) {
        switch (type) {
            case 1: return "目录";
            case 2: return "菜单";
            case 3: return "按钮";
            default: return "未知类型";
        }
    }
}
