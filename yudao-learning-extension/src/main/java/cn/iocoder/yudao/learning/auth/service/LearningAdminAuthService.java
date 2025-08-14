package cn.iocoder.yudao.learning.auth.service;

import cn.iocoder.yudao.learning.core.util.LearningLogger;
import cn.iocoder.yudao.module.system.controller.admin.auth.vo.*;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.service.auth.AdminAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.validation.Valid;

/**
 * 学习扩展 - 管理员认证服务学习包装器
 * 
 * 不修改原项目代码，通过包装器模式学习认证流程
 * 可以在启动后通过调用这个服务来学习认证机制
 * 
 * 学习重点：
 * 1. 用户认证流程：用户名密码验证 -> 用户状态检查 -> Token生成
 * 2. 验证码机制：图形验证码的生成、存储、校验
 * 3. Token管理：JWT Token的创建、刷新、销毁
 * 4. 登录日志：登录成功/失败的日志记录
 * 5. 短信登录：短信验证码的发送和验证
 * 6. 社交登录：第三方平台的OAuth2集成
 * 7. 安全机制：密码加密、登录限制、会话管理
 * 
 * @author 学习者
 */
@Slf4j
@Service
public class LearningAdminAuthService {

    private static final String MODULE_NAME = "鉴权模块-认证服务";
    
    @Autowired(required = false)
    private AdminAuthService adminAuthService;  // 注入原始的AdminAuthService，如果存在的话

    /**
     * 学习扩展 - 用户认证（用户名密码验证）
     * 
     * 学习要点：
     * 1. 用户名查找：通过用户名或手机号查找用户
     * 2. 密码验证：BCrypt密码加密算法的验证过程
     * 3. 用户状态检查：账户是否被禁用、锁定等
     * 4. 异常处理：认证失败的各种情况处理
     */
    public AdminUserDO learnAuthenticate(String username, String password) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "authenticate", username, "***密码已隐藏***");
        
        try {
            // 学习分析：认证过程的安全考虑
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "认证安全机制", 
                    "用户认证过程中密码不会明文记录到日志，体现了安全设计原则");
            
            // 学习分析：用户查找策略
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "用户查找策略", 
                    String.format("尝试通过用户名[%s]查找用户，支持用户名和手机号两种方式登录", username));
            
            // 如果服务存在，调用原方法 - 这里可以设置断点，深入学习认证流程
            AdminUserDO user = null;
            if (adminAuthService != null) {
                user = adminAuthService.authenticate(username, password);
            } else {
                log.warn("AdminAuthService未注入，无法执行实际认证，仅展示学习日志");
            }
            
            // 学习分析：认证成功后的用户信息
            if (user != null) {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "认证成功", 
                        String.format("用户认证成功 - 用户ID: %d, 昵称: %s, 部门ID: %d, 状态: %s", 
                                user.getId(), user.getNickname(), user.getDeptId(),
                                user.getStatus() == 1 ? "启用" : "禁用"));
                
                // 学习分析：密码验证机制
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "密码验证机制", 
                        "使用BCrypt算法验证密码，该算法具有自适应性，可以抵御彩虹表攻击");
            }
            
            LearningLogger.logMethodEnd(MODULE_NAME, "authenticate", startTime, 
                    user != null ? "认证成功" : "认证失败");
            
            return user;
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "authenticate", startTime, e);
            
            // 学习分析：认证失败的原因分析
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "认证失败分析", 
                    String.format("认证失败，可能原因：用户不存在、密码错误、账户被禁用。异常类型: %s", 
                            e.getClass().getSimpleName()));
            
            throw e;
        }
    }

    /**
     * 学习扩展 - 用户登录
     * 
     * 学习要点：
     * 1. 验证码校验：图形验证码的验证机制
     * 2. 用户认证：调用authenticate方法进行身份验证
     * 3. 社交绑定：支持社交账号的绑定登录
     * 4. Token创建：OAuth2 Token的生成和管理
     * 5. 登录日志：记录登录成功的日志信息
     */
    public AuthLoginRespVO learnLogin(@Valid AuthLoginReqVO reqVO) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "login", reqVO.getUsername());
        
        try {
            // 学习分析：登录前的准备工作
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "登录流程开始", 
                    String.format("用户[%s]尝试登录，是否需要验证码: %s, 是否绑定社交账号: %s", 
                            reqVO.getUsername(), 
                            reqVO.getCaptchaVerification() != null ? "是" : "否",
                            reqVO.getSocialType() != null ? "是" : "否"));
            
            // 学习分析：验证码机制
            if (reqVO.getCaptchaVerification() != null) {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "验证码校验", 
                        "系统启用了图形验证码，用于防止暴力破解和机器人攻击");
            }
            
            // 如果服务存在，调用原方法
            AuthLoginRespVO loginResp = null;
            if (adminAuthService != null) {
                loginResp = adminAuthService.login(reqVO);
            } else {
                log.warn("AdminAuthService未注入，无法执行实际登录，仅展示学习日志");
            }
            
            // 学习分析：登录成功后的Token信息
            if (loginResp != null) {
                LearningLogger.logDataFlow(MODULE_NAME, "Token生成结果", 
                        String.format("访问Token: %s..., 刷新Token: %s..., 过期时间: %s", 
                                loginResp.getAccessToken().substring(0, Math.min(20, loginResp.getAccessToken().length())),
                                loginResp.getRefreshToken().substring(0, Math.min(20, loginResp.getRefreshToken().length())),
                                loginResp.getExpiresTime()));
                
                // 学习分析：Token的安全特性
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "Token安全机制", 
                        "生成的Token包含用户信息和权限范围，支持自动刷新，提升了系统的安全性和用户体验");
            }
            
            LearningLogger.logMethodEnd(MODULE_NAME, "login", startTime, "登录成功");
            
            // 学习心得记录
            LearningLogger.logLearningInsight(MODULE_NAME, 
                    "登录流程体现了多层安全防护：验证码防暴力破解、密码加密存储、Token有效期控制、登录日志审计");
            
            return loginResp;
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "login", startTime, e);
            
            // 学习分析：登录失败的处理
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "登录失败处理", 
                    "登录失败会记录到登录日志中，便于安全审计和异常排查");
            
            throw e;
        }
    }

    /**
     * 学习扩展 - 用户登出
     * 
     * 学习要点：
     * 1. Token销毁：从Redis和数据库中删除Token
     * 2. 登出日志：记录用户登出行为
     * 3. 会话清理：清理用户相关的会话信息
     */
    public void learnLogout(String token, Integer logType) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "logout", 
                token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null", logType);
        
        try {
            // 学习分析：登出机制的重要性
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "登出安全机制", 
                    "主动登出会立即销毁Token，防止Token被恶意使用，提升系统安全性");
            
            // 如果服务存在，调用原方法
            if (adminAuthService != null) {
                adminAuthService.logout(token, logType);
            } else {
                log.warn("AdminAuthService未注入，无法执行实际登出，仅展示学习日志");
            }
            
            // 学习分析：登出后的清理工作
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "登出清理工作", 
                    "登出后系统会清理Token、记录登出日志，确保用户会话的完整生命周期管理");
            
            LearningLogger.logMethodEnd(MODULE_NAME, "logout", startTime, "登出成功");
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "logout", startTime, e);
            throw e;
        }
    }

    /**
     * 学习扩展 - 短信验证码发送
     * 
     * 学习要点：
     * 1. 短信服务集成：与第三方短信平台的对接
     * 2. 验证码生成：随机验证码的生成算法
     * 3. 验证码存储：Redis中的验证码缓存机制
     * 4. 频率限制：防止短信验证码被恶意发送
     */
    public void learnSendSmsCode(AuthSmsSendReqVO reqVO) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "sendSmsCode", reqVO.getMobile());
        
        try {
            // 学习分析：短信验证码的应用场景
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "短信验证码机制", 
                    String.format("向手机号[%s]发送验证码，用于短信登录或密码重置，提升账户安全性", 
                            reqVO.getMobile()));
            
            // 如果服务存在，调用原方法
            if (adminAuthService != null) {
                adminAuthService.sendSmsCode(reqVO);
            } else {
                log.warn("AdminAuthService未注入，无法发送短信，仅展示学习日志");
            }
            
            // 学习分析：短信发送的安全考虑
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "短信安全机制", 
                    "短信验证码具有时效性和一次性特点，同时有发送频率限制，防止被恶意利用");
            
            LearningLogger.logMethodEnd(MODULE_NAME, "sendSmsCode", startTime, "短信发送成功");
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "sendSmsCode", startTime, e);
            
            // 学习分析：短信发送失败的处理
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "短信发送失败", 
                    "短信发送失败可能由于：手机号格式错误、短信服务异常、发送频率过高等原因");
            
            throw e;
        }
    }

    /**
     * 学习扩展 - 短信登录
     * 
     * 学习要点：
     * 1. 验证码校验：短信验证码的验证逻辑
     * 2. 用户查找：通过手机号查找用户
     * 3. 免密登录：基于短信验证码的免密登录机制
     */
    public AuthLoginRespVO learnSmsLogin(AuthSmsLoginReqVO reqVO) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "smsLogin", reqVO.getMobile());
        
        try {
            // 学习分析：短信登录的便利性
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "短信登录机制", 
                    String.format("用户通过手机号[%s]和验证码进行免密登录，提升用户体验", reqVO.getMobile()));
            
            // 如果服务存在，调用原方法
            AuthLoginRespVO loginResp = null;
            if (adminAuthService != null) {
                loginResp = adminAuthService.smsLogin(reqVO);
            } else {
                log.warn("AdminAuthService未注入，无法执行短信登录，仅展示学习日志");
            }
            
            // 学习分析：短信登录的安全性
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "短信登录安全性", 
                    "短信登录虽然便利，但安全性依赖于手机号的安全性和短信验证码的时效性");
            
            LearningLogger.logMethodEnd(MODULE_NAME, "smsLogin", startTime, "短信登录成功");
            
            return loginResp;
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "smsLogin", startTime, e);
            throw e;
        }
    }

    /**
     * 学习扩展 - 刷新访问令牌
     * 
     * 学习要点：
     * 1. Token刷新机制：使用refresh token获取新的access token
     * 2. 无感刷新：前端可以实现Token的无感刷新
     * 3. 安全性考虑：refresh token的有效期管理
     */
    public AuthLoginRespVO learnRefreshToken(String refreshToken) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "refreshToken", 
                refreshToken != null ? refreshToken.substring(0, Math.min(20, refreshToken.length())) + "..." : "null");
        
        try {
            // 学习分析：Token刷新的重要性
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "Token刷新机制", 
                    "Token刷新机制允许在不重新登录的情况下延长用户会话，提升用户体验");
            
            // 如果服务存在，调用原方法
            AuthLoginRespVO loginResp = null;
            if (adminAuthService != null) {
                loginResp = adminAuthService.refreshToken(refreshToken);
            } else {
                log.warn("AdminAuthService未注入，无法刷新Token，仅展示学习日志");
            }
            
            // 学习分析：刷新后的新Token
            if (loginResp != null) {
                LearningLogger.logDataFlow(MODULE_NAME, "Token刷新结果", 
                        String.format("新的访问Token已生成，过期时间: %s", loginResp.getExpiresTime()));
            }
            
            LearningLogger.logMethodEnd(MODULE_NAME, "refreshToken", startTime, "Token刷新成功");
            
            return loginResp;
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "refreshToken", startTime, e);
            
            // 学习分析：Token刷新失败的处理
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "Token刷新失败", 
                    "Token刷新失败通常意味着refresh token已过期或无效，用户需要重新登录");
            
            throw e;
        }
    }
}
