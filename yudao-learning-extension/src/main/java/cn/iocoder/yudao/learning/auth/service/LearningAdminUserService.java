package cn.iocoder.yudao.learning.auth.service;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.learning.core.util.LearningLogger;
import cn.iocoder.yudao.module.system.controller.admin.auth.vo.AuthRegisterReqVO;

import cn.iocoder.yudao.module.system.controller.admin.user.vo.user.UserPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.user.vo.user.UserSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import cn.iocoder.yudao.module.system.service.user.AdminUserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 学习扩展 - 管理员用户服务实现
 * 
 * 通过继承原AdminUserServiceImpl类，使用@Primary注解让Spring优先使用这个实现
 * 在每个方法中调用原方法，前后添加学习日志和业务分析
 * 
 * 学习重点：
 * 1. 用户创建流程：参数校验 -> 密码加密 -> 数据插入 -> 岗位关联
 * 2. 用户更新流程：数据校验 -> 更新用户信息 -> 更新岗位关联
 * 3. 用户删除流程：存在性校验 -> 删除用户 -> 清理关联数据
 * 4. 用户查询流程：分页查询 -> 权限过滤 -> 数据返回
 * 5. 密码处理：BCrypt加密算法的应用
 * 6. 数据校验：唯一性校验、状态校验、关联数据校验
 * 
 * @author 学习者
 */
@Slf4j
@Service
@Primary  // 关键注解：让Spring优先使用这个Bean，原代码调用AdminUserService时会调用到这里
public class LearningAdminUserService extends AdminUserServiceImpl {

    private static final String MODULE_NAME = "鉴权模块-用户管理";

    /**
     * 学习扩展 - 创建用户
     * 
     * 学习要点：
     * 1. 租户账户数量限制校验
     * 2. 用户信息唯一性校验（用户名、手机号、邮箱）
     * 3. 密码加密处理（BCrypt算法）
     * 4. 用户岗位关联数据处理
     * 5. 事务处理和日志记录
     */
    @Override
    public Long createUser(@Valid UserSaveReqVO createReqVO) {
        // 记录方法开始执行
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "createUser", createReqVO);
        
        try {
            // 学习分析：用户创建前的业务逻辑分析
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "用户创建前置校验", 
                    String.format("即将创建用户: %s, 部门ID: %s, 岗位数量: %d", 
                            createReqVO.getUsername(), 
                            createReqVO.getDeptId(),
                            CollUtil.size(createReqVO.getPostIds())));
            
            // 学习分析：密码处理机制
            if (createReqVO.getPassword() != null) {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "密码加密机制", 
                        "使用BCrypt算法对用户密码进行加密，原密码长度: " + createReqVO.getPassword().length());
            }
            
            // 调用原方法 - 这里可以设置断点，step-by-step学习原方法的执行流程
            Long userId = super.createUser(createReqVO);
            
            // 学习分析：用户创建成功后的数据分析
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "用户创建成功", 
                    String.format("用户创建成功，分配的用户ID: %d", userId));
            
            // 学习分析：岗位关联处理
            if (CollUtil.isNotEmpty(createReqVO.getPostIds())) {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "岗位关联处理", 
                        String.format("为用户[%d]关联了%d个岗位: %s", 
                                userId, createReqVO.getPostIds().size(), createReqVO.getPostIds()));
            }
            
            // 记录方法执行结束
            LearningLogger.logMethodEnd(MODULE_NAME, "createUser", startTime, userId);
            
            // 学习心得记录
            LearningLogger.logLearningInsight(MODULE_NAME, 
                    "用户创建流程体现了企业级应用的严谨性：多层校验、密码加密、事务保证、日志记录");
            
            return userId;
            
        } catch (Exception e) {
            // 记录异常信息
            LearningLogger.logMethodException(MODULE_NAME, "createUser", startTime, e);
            
            // 学习分析：异常处理机制
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "异常处理分析", 
                    String.format("用户创建失败，异常类型: %s, 可能原因: 数据校验失败或数据库约束冲突", 
                            e.getClass().getSimpleName()));
            
            throw e; // 重新抛出异常，保持原有的异常处理逻辑
        }
    }

    /**
     * 学习扩展 - 用户注册
     * 
     * 学习要点：
     * 1. 注册开关配置的动态读取
     * 2. 注册用户与管理员创建用户的区别
     * 3. 配置中心的使用方式
     */
    @Override
    public Long registerUser(@Valid AuthRegisterReqVO registerReqVO) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "registerUser", registerReqVO);
        
        try {
            // 学习分析：注册功能的配置化管理
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "注册配置检查", 
                    "系统通过配置中心动态控制用户注册功能的开启/关闭，体现了配置与代码分离的设计理念");
            
            // 调用原方法
            Long userId = super.registerUser(registerReqVO);
            
            // 学习分析：注册与创建的区别
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "注册流程特点", 
                    "用户注册相比管理员创建用户更简化：无需指定部门和岗位，默认启用状态");
            
            LearningLogger.logMethodEnd(MODULE_NAME, "registerUser", startTime, userId);
            return userId;
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "registerUser", startTime, e);
            throw e;
        }
    }

    /**
     * 学习扩展 - 更新用户
     * 
     * 学习要点：
     * 1. 更新操作的数据校验机制
     * 2. 岗位关联的更新处理
     * 3. 操作日志的上下文记录
     * 4. 数据变更的diff记录
     */
    @Override
    public void updateUser(@Valid UserSaveReqVO updateReqVO) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "updateUser", updateReqVO);
        
        try {
            // 学习分析：更新前的数据获取
            AdminUserDO oldUser = super.getUser(updateReqVO.getId());
            if (oldUser != null) {
                LearningLogger.logDataFlow(MODULE_NAME, "更新前用户数据", 
                        String.format("原用户信息 - 用户名: %s, 昵称: %s, 状态: %d", 
                                oldUser.getUsername(), oldUser.getNickname(), oldUser.getStatus()));
            }
            
            // 学习分析：密码更新的特殊处理
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "密码更新策略", 
                    "用户信息更新时不允许修改密码，密码修改需要通过专门的密码更新接口，体现了安全设计原则");
            
            // 调用原方法
            super.updateUser(updateReqVO);
            
            // 学习分析：更新后的数据变化
            AdminUserDO newUser = super.getUser(updateReqVO.getId());
            if (oldUser != null && newUser != null) {
                analyzeUserDataChanges(oldUser, newUser);
            }
            
            LearningLogger.logMethodEnd(MODULE_NAME, "updateUser", startTime, "更新成功");
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "updateUser", startTime, e);
            throw e;
        }
    }

    /**
     * 学习扩展 - 更新用户登录信息
     * 
     * 学习要点：
     * 1. 登录信息的实时更新机制
     * 2. IP地址的记录和安全考虑
     */
    @Override
    public void updateUserLogin(Long id, String loginIp) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "updateUserLogin", id, loginIp);
        
        try {
            // 学习分析：登录信息更新的意义
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "登录信息更新", 
                    String.format("更新用户[%d]的登录信息，IP: %s，用于安全审计和用户行为分析", id, loginIp));
            
            // 调用原方法
            super.updateUserLogin(id, loginIp);
            
            LearningLogger.logMethodEnd(MODULE_NAME, "updateUserLogin", startTime, "登录信息更新成功");
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "updateUserLogin", startTime, e);
            throw e;
        }
    }

    /**
     * 学习扩展 - 删除用户
     * 
     * 学习要点：
     * 1. 删除前的存在性校验
     * 2. 关联数据的级联删除处理
     * 3. 权限数据的清理机制
     * 4. 事务保证数据一致性
     */
    @Override
    public void deleteUser(Long id) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "deleteUser", id);
        
        try {
            // 学习分析：删除前获取用户信息用于日志记录
            AdminUserDO user = super.getUser(id);
            if (user != null) {
                LearningLogger.logDataFlow(MODULE_NAME, "删除前用户信息", 
                        String.format("即将删除用户 - ID: %d, 用户名: %s, 昵称: %s", 
                                user.getId(), user.getUsername(), user.getNickname()));
            }
            
            // 学习分析：删除操作的复杂性
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "用户删除复杂性", 
                    "用户删除不仅要删除用户表记录，还要清理权限关联、岗位关联等数据，体现了关联数据的一致性维护");
            
            // 调用原方法
            super.deleteUser(id);
            
            // 学习分析：删除后的数据清理
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "关联数据清理", 
                    "删除用户后，系统自动清理了用户的权限关联和岗位关联，保证了数据的完整性");
            
            LearningLogger.logMethodEnd(MODULE_NAME, "deleteUser", startTime, "删除成功");
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "deleteUser", startTime, e);
            throw e;
        }
    }

    /**
     * 学习扩展 - 分页查询用户
     *
     * 学习要点：
     * 1. 分页查询的实现机制
     * 2. 角色权限的数据过滤
     * 3. 部门权限的数据范围控制
     * 4. 查询性能优化
     */
    @Override
    public PageResult<AdminUserDO> getUserPage(UserPageReqVO reqVO) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "getUserPage", reqVO);

        try {
            // 学习分析：分页查询参数
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "分页查询参数",
                    String.format("页码: %d, 页大小: %d, 角色ID: %s, 部门ID: %s",
                            reqVO.getPageNo(), reqVO.getPageSize(), reqVO.getRoleId(), reqVO.getDeptId()));

            // 学习分析：权限过滤机制
            if (reqVO.getRoleId() != null) {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "角色权限过滤",
                        "查询指定角色的用户，体现了基于角色的访问控制(RBAC)模型");
            }

            // 调用原方法
            PageResult<AdminUserDO> result = super.getUserPage(reqVO);

            // 学习分析：查询结果统计
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "查询结果统计",
                    String.format("查询到 %d 条记录，总计 %d 条", result.getList().size(), result.getTotal()));

            LearningLogger.logMethodEnd(MODULE_NAME, "getUserPage", startTime,
                    String.format("返回%d条记录", result.getList().size()));

            return result;

        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "getUserPage", startTime, e);
            throw e;
        }
    }

    /**
     * 学习扩展 - 根据用户名查询用户
     *
     * 学习要点：
     * 1. 唯一索引的查询优化
     * 2. 登录认证中的用户查找
     */
    @Override
    public AdminUserDO getUserByUsername(String username) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "getUserByUsername", username);

        try {
            // 学习分析：用户名查询的重要性
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "用户名查询",
                    "根据用户名查询是登录认证的核心步骤，通常配合密码验证完成身份认证");

            // 调用原方法
            AdminUserDO user = super.getUserByUsername(username);

            // 学习分析：查询结果
            String resultDesc = user != null ?
                    String.format("找到用户，ID: %d, 状态: %d", user.getId(), user.getStatus()) :
                    "用户不存在";

            LearningLogger.logMethodEnd(MODULE_NAME, "getUserByUsername", startTime, resultDesc);

            return user;

        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "getUserByUsername", startTime, e);
            throw e;
        }
    }

    /**
     * 学习扩展 - 根据手机号查询用户
     *
     * 学习要点：
     * 1. 手机号作为登录凭证的应用
     * 2. 多种登录方式的支持
     */
    @Override
    public AdminUserDO getUserByMobile(String mobile) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "getUserByMobile", mobile);

        try {
            // 学习分析：手机号登录的便利性
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "手机号查询",
                    "支持手机号登录，提升用户体验，同时手机号具有唯一性和真实性");

            AdminUserDO user = super.getUserByMobile(mobile);

            String resultDesc = user != null ?
                    String.format("找到用户，用户名: %s", user.getUsername()) :
                    "手机号对应的用户不存在";

            LearningLogger.logMethodEnd(MODULE_NAME, "getUserByMobile", startTime, resultDesc);

            return user;

        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "getUserByMobile", startTime, e);
            throw e;
        }
    }

    /**
     * 学习扩展 - 根据ID查询用户
     *
     * 学习要点：
     * 1. 主键查询的高效性
     * 2. 用户信息获取的基础方法
     */
    @Override
    public AdminUserDO getUser(Long id) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "getUser", id);

        try {
            AdminUserDO user = super.getUser(id);

            // 学习分析：用户状态检查
            if (user != null) {
                LearningLogger.logDataFlow(MODULE_NAME, "用户基本信息",
                        String.format("用户名: %s, 昵称: %s, 状态: %s, 部门ID: %d",
                                user.getUsername(), user.getNickname(),
                                user.getStatus() == 1 ? "启用" : "禁用", user.getDeptId()));
            }

            LearningLogger.logMethodEnd(MODULE_NAME, "getUser", startTime,
                    user != null ? "用户存在" : "用户不存在");

            return user;

        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "getUser", startTime, e);
            throw e;
        }
    }

    /**
     * 学习扩展 - 批量验证用户有效性
     *
     * 学习要点：
     * 1. 批量操作的性能优化
     * 2. 用户状态的业务校验
     * 3. 异常处理的统一性
     */
    @Override
    public void validateUserList(Collection<Long> ids) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "validateUserList", ids);

        try {
            if (CollUtil.isEmpty(ids)) {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "参数校验", "用户ID列表为空，跳过验证");
                return;
            }

            // 学习分析：批量验证的必要性
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "批量验证",
                    String.format("验证%d个用户的有效性，包括存在性和状态检查", ids.size()));

            super.validateUserList(ids);

            LearningLogger.logMethodEnd(MODULE_NAME, "validateUserList", startTime, "验证通过");

        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "validateUserList", startTime, e);

            // 学习分析：验证失败的原因分析
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "验证失败分析",
                    "用户验证失败，可能原因：用户不存在或用户被禁用");

            throw e;
        }
    }

    /**
     * 分析用户数据变更
     *
     * @param oldUser 更新前的用户数据
     * @param newUser 更新后的用户数据
     */
    private void analyzeUserDataChanges(AdminUserDO oldUser, AdminUserDO newUser) {
        StringBuilder changes = new StringBuilder();

        // 比较昵称变化
        if (!oldUser.getNickname().equals(newUser.getNickname())) {
            changes.append(String.format("昵称: %s -> %s; ", oldUser.getNickname(), newUser.getNickname()));
        }

        // 比较邮箱变化
        if (!oldUser.getEmail().equals(newUser.getEmail())) {
            changes.append(String.format("邮箱: %s -> %s; ", oldUser.getEmail(), newUser.getEmail()));
        }

        // 比较手机号变化
        if (!oldUser.getMobile().equals(newUser.getMobile())) {
            changes.append(String.format("手机号: %s -> %s; ", oldUser.getMobile(), newUser.getMobile()));
        }

        // 比较部门变化
        if (!oldUser.getDeptId().equals(newUser.getDeptId())) {
            changes.append(String.format("部门ID: %d -> %d; ", oldUser.getDeptId(), newUser.getDeptId()));
        }

        if (!changes.isEmpty()) {
            LearningLogger.logDataFlow(MODULE_NAME, "用户数据变更详情", changes.toString());
        } else {
            LearningLogger.logDataFlow(MODULE_NAME, "用户数据变更详情", "无实际数据变更");
        }
    }
}
