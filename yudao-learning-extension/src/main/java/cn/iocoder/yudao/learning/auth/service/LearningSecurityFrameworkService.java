package cn.iocoder.yudao.learning.auth.service;

import cn.iocoder.yudao.framework.common.biz.system.permission.PermissionCommonApi;
import cn.iocoder.yudao.framework.security.core.service.SecurityFrameworkService;
import cn.iocoder.yudao.framework.security.core.service.SecurityFrameworkServiceImpl;
import cn.iocoder.yudao.learning.core.util.LearningLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * 学习扩展 - 权限校验框架服务实现
 * 
 * 通过继承原SecurityFrameworkServiceImpl类，使用@Primary注解让Spring优先使用这个实现
 * 在每个方法中调用原方法，前后添加学习日志和业务分析
 * 
 * 学习重点：
 * 1. 注解权限校验：@PreAuthorize注解的底层实现
 * 2. 权限校验缓存：本地缓存提升权限校验性能
 * 3. 跨租户访问：特殊场景的权限跳过机制
 * 4. 权限校验链路：从注解到最终权限判断的完整流程
 * 5. 权限上下文：当前登录用户的权限信息获取
 * 6. 权限作用域：OAuth2 scope的权限控制
 * 7. 权限缓存策略：缓存键设计和过期时间控制
 * 8. 权限校验异常：权限不足时的异常处理
 * 
 * @author 学习者
 */
@Slf4j
@Service
@Primary  // 关键注解：让Spring优先使用这个Bean，原代码调用SecurityFrameworkService时会调用到这里
public class LearningSecurityFrameworkService extends SecurityFrameworkServiceImpl {

    private static final String MODULE_NAME = "鉴权模块-权限校验框架";

    public LearningSecurityFrameworkService(@Qualifier("permissionApiImpl") PermissionCommonApi permissionApi) {
        super(permissionApi);
    }

    /**
     * 学习扩展 - 判断是否有权限
     * 
     * 学习要点：
     * 1. 单个权限校验的实现
     * 2. 权限校验的性能优化
     * 3. 权限校验的缓存机制
     */
    @Override
    public boolean hasPermission(String permission) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "hasPermission", permission);
        
        try {
            // 学习分析：单个权限校验的应用场景
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "单个权限校验", 
                    String.format("校验当前用户是否拥有权限: %s", permission));
            
            // 学习分析：权限校验的底层实现
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "权限校验实现", 
                    "单个权限校验通过调用hasAnyPermissions方法实现，保持逻辑一致性");
            
            // 调用原方法 - 这里可以设置断点，深入学习权限校验流程
            boolean hasPermission = super.hasPermission(permission);
            
            // 学习分析：权限校验结果
            String resultDesc = hasPermission ? "拥有权限" : "权限不足";
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "权限校验结果", 
                    String.format("权限[%s]校验结果: %s", permission, resultDesc));
            
            LearningLogger.logMethodEnd(MODULE_NAME, "hasPermission", startTime, String.valueOf(hasPermission));
            
            return hasPermission;
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "hasPermission", startTime, e);
            
            // 学习分析：权限校验异常的安全处理
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "权限校验异常", 
                    "权限校验异常时应该拒绝访问，确保系统安全");
            
            throw e;
        }
    }

    /**
     * 学习扩展 - 判断是否有任一权限
     * 
     * 学习要点：
     * 1. 多权限校验的OR逻辑
     * 2. 跨租户访问的特殊处理
     * 3. 权限校验缓存的使用
     * 4. 登录用户信息的获取
     */
    @Override
    public boolean hasAnyPermissions(String... permissions) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "hasAnyPermissions", (Object) permissions);
        
        try {
            // 学习分析：多权限校验的业务场景
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "多权限校验", 
                    String.format("校验当前用户是否拥有任一权限: %s", String.join(", ", permissions)));
            
            // 学习分析：跨租户访问的特殊处理
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "跨租户访问检查", 
                    "首先检查是否为跨租户访问，跨租户访问时跳过权限校验");
            
            // 学习分析：权限校验的缓存机制
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "权限校验缓存", 
                    "使用本地缓存(LoadingCache)提升权限校验性能，缓存过期时间1分钟");
            
            // 调用原方法
            boolean hasPermission = super.hasAnyPermissions(permissions);
            
            // 学习分析：权限校验的性能考虑
            if (hasPermission) {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "权限校验通过", 
                        "用户拥有所需权限，通过缓存机制提升了校验性能");
            } else {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "权限校验失败", 
                        "用户不具备任何所需权限，访问被拒绝");
            }
            
            LearningLogger.logMethodEnd(MODULE_NAME, "hasAnyPermissions", startTime, String.valueOf(hasPermission));
            
            // 学习心得记录
            LearningLogger.logLearningInsight(MODULE_NAME, 
                    "权限校验框架体现了安全设计的核心原则：默认拒绝、缓存优化、异常安全、跨租户支持");
            
            return hasPermission;
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "hasAnyPermissions", startTime, e);
            throw e;
        }
    }

    /**
     * 学习扩展 - 判断是否有角色
     * 
     * 学习要点：
     * 1. 角色校验与权限校验的区别
     * 2. 角色编码的使用方式
     * 3. 角色校验的应用场景
     */
    @Override
    public boolean hasRole(String role) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "hasRole", role);
        
        try {
            // 学习分析：角色校验的业务含义
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "角色校验", 
                    String.format("校验当前用户是否拥有角色: %s", role));
            
            // 学习分析：角色编码的使用
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "角色编码使用", 
                    String.format("使用角色编码[%s]进行校验，角色编码是角色的唯一标识", role));
            
            // 调用原方法
            boolean hasRole = super.hasRole(role);
            
            // 学习分析：角色校验结果
            String resultDesc = hasRole ? "拥有角色" : "角色不匹配";
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "角色校验结果", 
                    String.format("角色[%s]校验结果: %s", role, resultDesc));
            
            LearningLogger.logMethodEnd(MODULE_NAME, "hasRole", startTime, String.valueOf(hasRole));
            
            return hasRole;
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "hasRole", startTime, e);
            throw e;
        }
    }

    /**
     * 学习扩展 - 判断是否有任一角色
     * 
     * 学习要点：
     * 1. 多角色校验的OR逻辑
     * 2. 角色校验缓存机制
     * 3. 角色校验的性能优化
     */
    @Override
    public boolean hasAnyRoles(String... roles) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "hasAnyRoles", (Object) roles);
        
        try {
            // 学习分析：多角色校验的应用场景
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "多角色校验", 
                    String.format("校验当前用户是否拥有任一角色: %s", String.join(", ", roles)));
            
            // 学习分析：角色校验的缓存策略
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "角色校验缓存", 
                    "角色校验同样使用本地缓存，缓存键为用户ID+角色列表的组合");
            
            // 调用原方法
            boolean hasRole = super.hasAnyRoles(roles);
            
            // 学习分析：角色校验的业务价值
            if (hasRole) {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "角色校验通过", 
                        "用户拥有所需角色，角色校验比权限校验更粗粒度但更高效");
            } else {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "角色校验失败", 
                        "用户不具备任何所需角色，访问被拒绝");
            }
            
            LearningLogger.logMethodEnd(MODULE_NAME, "hasAnyRoles", startTime, String.valueOf(hasRole));
            
            return hasRole;
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "hasAnyRoles", startTime, e);
            throw e;
        }
    }

    /**
     * 学习扩展 - 判断是否有授权范围
     * 
     * 学习要点：
     * 1. OAuth2 scope的权限控制
     * 2. 授权范围与权限的区别
     * 3. 第三方应用的权限控制
     */
    @Override
    public boolean hasScope(String scope) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "hasScope", scope);
        
        try {
            // 学习分析：授权范围的业务含义
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "授权范围校验", 
                    String.format("校验当前用户是否拥有授权范围: %s", scope));
            
            // 学习分析：OAuth2 scope的应用场景
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "OAuth2授权范围", 
                    "授权范围(scope)主要用于OAuth2场景，控制第三方应用的访问权限");
            
            // 调用原方法
            boolean hasScope = super.hasScope(scope);
            
            // 学习分析：授权范围校验结果
            String resultDesc = hasScope ? "拥有授权范围" : "授权范围不足";
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "授权范围校验结果", 
                    String.format("授权范围[%s]校验结果: %s", scope, resultDesc));
            
            LearningLogger.logMethodEnd(MODULE_NAME, "hasScope", startTime, String.valueOf(hasScope));
            
            return hasScope;
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "hasScope", startTime, e);
            throw e;
        }
    }

    /**
     * 学习扩展 - 判断是否有任一授权范围
     * 
     * 学习要点：
     * 1. 多授权范围的OR逻辑
     * 2. 授权范围的集合操作
     * 3. 第三方应用权限的细粒度控制
     */
    @Override
    public boolean hasAnyScopes(String... scopes) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "hasAnyScopes", (Object) scopes);
        
        try {
            // 学习分析：多授权范围校验的应用
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "多授权范围校验", 
                    String.format("校验当前用户是否拥有任一授权范围: %s", String.join(", ", scopes)));
            
            // 学习分析：授权范围的校验机制
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "授权范围校验机制", 
                    "授权范围校验通过集合包含关系判断，不使用缓存，直接从用户上下文获取");
            
            // 调用原方法
            boolean hasScope = super.hasAnyScopes(scopes);
            
            // 学习分析：授权范围校验的特点
            if (hasScope) {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "授权范围校验通过", 
                        "用户拥有所需授权范围，第三方应用可以访问对应的资源");
            } else {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "授权范围校验失败", 
                        "用户不具备任何所需授权范围，第三方应用访问被拒绝");
            }
            
            LearningLogger.logMethodEnd(MODULE_NAME, "hasAnyScopes", startTime, String.valueOf(hasScope));
            
            // 学习心得记录
            LearningLogger.logLearningInsight(MODULE_NAME, 
                    "授权范围机制体现了OAuth2的精细化权限控制：限制第三方应用的访问范围，保护用户隐私");
            
            return hasScope;
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "hasAnyScopes", startTime, e);
            throw e;
        }
    }
}
