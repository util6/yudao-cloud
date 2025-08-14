package cn.iocoder.yudao.learning.workflow.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.learning.core.util.LearningLogger;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.*;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskService;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.validation.Valid;

/**
 * 学习扩展 - 工作流任务服务实现
 * 
 * 通过继承原BpmTaskServiceImpl类，使用@Primary注解让Spring优先使用这个实现
 * 在每个方法中调用原方法，前后添加学习日志和业务分析
 * 
 * 学习重点：
 * 1. 任务生命周期：创建 -> 分配 -> 执行 -> 完成
 * 2. 任务分配策略：候选人、候选组、动态分配
 * 3. 任务审批流程：通过、拒绝、转办、委派
 * 4. 任务查询机制：待办、已办、分页查询
 * 5. 任务权限控制：任务可见性、操作权限
 * 6. 任务变量处理：局部变量、全局变量
 * 7. 任务监听器：任务事件的处理
 * 8. 任务加签减签：动态调整审批人
 * 9. 任务退回机制：流程回退处理
 * 10. 任务性能优化：查询优化、缓存策略
 * 
 * @author 学习者
 */
@Slf4j
@Service
@Primary  // 关键注解：让Spring优先使用这个Bean，原代码调用BpmTaskService时会调用到这里
public class LearningBpmTaskService extends BpmTaskServiceImpl {

    private static final String MODULE_NAME = "工作流模块-任务服务";

    /**
     * 学习扩展 - 获取待办任务分页
     * 
     * 学习要点：
     * 1. 待办任务的查询逻辑
     * 2. 任务权限的过滤机制
     * 3. 分页查询的性能优化
     * 4. 任务状态的业务含义
     */
    @Override
    public PageResult<Task> getTaskTodoPage(Long userId, BpmTaskPageReqVO pageReqVO) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "getTaskTodoPage", userId, pageReqVO);
        
        try {
            // 学习分析：待办任务查询的业务意义
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "待办任务查询", 
                    String.format("用户[%d]查询待办任务，页码: %d, 页大小: %d", 
                            userId, pageReqVO.getPageNo(), pageReqVO.getPageSize()));
            
            // 学习分析：查询条件分析
            if (pageReqVO.getName() != null) {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "任务名称筛选", 
                        String.format("按任务名称筛选: %s", pageReqVO.getName()));
            }
            
            if (pageReqVO.getCreateTime() != null && pageReqVO.getCreateTime().length == 2) {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "创建时间筛选", 
                        String.format("按创建时间筛选: %s 到 %s", 
                                pageReqVO.getCreateTime()[0], pageReqVO.getCreateTime()[1]));
            }
            
            // 调用原方法 - 这里可以设置断点，深入学习任务查询流程
            PageResult<Task> pageResult = super.getTaskTodoPage(userId, pageReqVO);
            
            // 学习分析：查询结果统计
            if (pageResult != null) {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "待办任务统计", 
                        String.format("用户有%d个待办任务，当前页显示%d个", 
                                pageResult.getTotal(), pageResult.getList().size()));
                
                // 分析任务类型分布
                if (!pageResult.getList().isEmpty()) {
                    LearningLogger.logDataFlow(MODULE_NAME, "任务类型分析", 
                            String.format("任务类型分布 - 第一个任务: %s, 流程实例: %s", 
                                    pageResult.getList().get(0).getName(),
                                    pageResult.getList().get(0).getProcessInstanceId()));
                }
                
                // 学习分析：任务优先级
                long highPriorityCount = pageResult.getList().stream()
                        .filter(task -> task.getPriority() > 50)
                        .count();
                
                if (highPriorityCount > 0) {
                    LearningLogger.logBusinessAnalysis(MODULE_NAME, "任务优先级分析", 
                            String.format("当前页面中有%d个高优先级任务，需要优先处理", highPriorityCount));
                }
            }
            
            LearningLogger.logMethodEnd(MODULE_NAME, "getTaskTodoPage", startTime, 
                    String.format("返回%d个待办任务", pageResult != null ? pageResult.getList().size() : 0));
            
            return pageResult;
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "getTaskTodoPage", startTime, e);
            throw e;
        }
    }

    /**
     * 学习扩展 - 获取已办任务分页
     * 
     * 学习要点：
     * 1. 历史任务的查询机制
     * 2. 已办任务的状态分析
     * 3. 任务处理时间统计
     */
    @Override
    public PageResult<HistoricTaskInstance> getTaskDonePage(Long userId, BpmTaskPageReqVO pageReqVO) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "getTaskDonePage", userId, pageReqVO);
        
        try {
            // 学习分析：已办任务查询的价值
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "已办任务查询", 
                    String.format("用户[%d]查询已办任务，用于工作回顾和绩效统计", userId));
            
            // 调用原方法
            PageResult<HistoricTaskInstance> pageResult = super.getTaskDonePage(userId, pageReqVO);
            
            // 学习分析：已办任务统计
            if (pageResult != null && !pageResult.getList().isEmpty()) {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "已办任务统计", 
                        String.format("用户已处理%d个任务，当前页显示%d个", 
                                pageResult.getTotal(), pageResult.getList().size()));
                
                // 计算平均处理时间
                double avgDuration = pageResult.getList().stream()
                        .filter(task -> task.getDurationInMillis() != null)
                        .mapToLong(HistoricTaskInstance::getDurationInMillis)
                        .average()
                        .orElse(0.0);
                
                if (avgDuration > 0) {
                    LearningLogger.logBusinessAnalysis(MODULE_NAME, "任务处理效率", 
                            String.format("平均任务处理时间: %.2f分钟，可用于效率分析", avgDuration / 60000));
                }
                
                // 分析任务结果分布
                long approvedCount = pageResult.getList().stream()
                        .filter(task -> task.getDeleteReason() != null && task.getDeleteReason().contains("通过"))
                        .count();
                
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "任务结果分析", 
                        String.format("当前页面中通过的任务: %d个，占比: %.1f%%", 
                                approvedCount, (double) approvedCount / pageResult.getList().size() * 100));
            }
            
            LearningLogger.logMethodEnd(MODULE_NAME, "getTaskDonePage", startTime, 
                    String.format("返回%d个已办任务", pageResult != null ? pageResult.getList().size() : 0));
            
            return pageResult;
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "getTaskDonePage", startTime, e);
            throw e;
        }
    }

    /**
     * 学习扩展 - 审批通过任务
     * 
     * 学习要点：
     * 1. 任务审批的业务逻辑
     * 2. 流程变量的传递机制
     * 3. 下一节点审批人的选择
     * 4. 任务完成后的流程推进
     */
    @Override
    public void approveTask(Long userId, @Valid BpmTaskApproveReqVO reqVO) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "approveTask", userId, reqVO);
        
        try {
            // 学习分析：任务审批的业务含义
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "任务审批操作", 
                    String.format("用户[%d]审批通过任务[%s]，审批意见: %s", 
                            userId, reqVO.getId(), reqVO.getReason()));
            
            // 学习分析：流程变量的作用
            if (reqVO.getVariables() != null && !reqVO.getVariables().isEmpty()) {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "流程变量传递", 
                        String.format("设置了%d个流程变量，用于传递审批结果和业务数据", 
                                reqVO.getVariables().size()));
                
                // 记录关键变量
                reqVO.getVariables().forEach((key, value) -> {
                    LearningLogger.logDataFlow(MODULE_NAME, "审批变量", 
                            String.format("变量: %s = %s", key, value));
                });
            }
            
            // 学习分析：下一节点审批人选择
            if (reqVO.getNextAssignees() != null && !reqVO.getNextAssignees().isEmpty()) {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "下一节点审批人", 
                        String.format("为%d个下一节点指定了审批人，体现了灵活的任务分配", 
                                reqVO.getNextAssignees().size()));
            }
            
            // 调用原方法 - 这里可以设置断点，深入学习任务审批流程
            super.approveTask(userId, reqVO);
            
            // 学习分析：审批完成后的影响
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "审批完成影响", 
                    "任务审批完成后，流程引擎会自动推进到下一个节点，可能触发新的任务创建");
            
            LearningLogger.logMethodEnd(MODULE_NAME, "approveTask", startTime, "审批通过成功");
            
            // 学习心得记录
            LearningLogger.logLearningInsight(MODULE_NAME, 
                    "任务审批体现了工作流的核心价值：流程自动化、状态流转、数据传递、权限控制");
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "approveTask", startTime, e);
            
            // 学习分析：审批失败的原因
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "审批失败分析", 
                    "审批失败可能由于：任务不存在、权限不足、流程异常、变量类型错误等");
            
            throw e;
        }
    }

    /**
     * 学习扩展 - 审批拒绝任务
     * 
     * 学习要点：
     * 1. 拒绝操作的流程处理
     * 2. 拒绝后的流程流转
     * 3. 拒绝原因的记录机制
     */
    @Override
    public void rejectTask(Long userId, @Valid BpmTaskRejectReqVO reqVO) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "rejectTask", userId, reqVO);
        
        try {
            // 学习分析：任务拒绝的业务场景
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "任务拒绝操作", 
                    String.format("用户[%d]拒绝任务[%s]，拒绝原因: %s", 
                            userId, reqVO.getId(), reqVO.getReason()));
            
            // 学习分析：拒绝操作的影响
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "拒绝操作影响", 
                    "任务拒绝可能导致流程回退、流程终止或触发异常处理流程");
            
            // 调用原方法
            super.rejectTask(userId, reqVO);
            
            // 学习分析：拒绝处理完成
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "拒绝处理完成", 
                    "任务拒绝处理完成，流程已按照拒绝逻辑进行流转");
            
            LearningLogger.logMethodEnd(MODULE_NAME, "rejectTask", startTime, "拒绝处理成功");
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "rejectTask", startTime, e);
            throw e;
        }
    }

    /**
     * 学习扩展 - 转办任务
     * 
     * 学习要点：
     * 1. 任务转办的业务场景
     * 2. 任务所有权的转移
     * 3. 转办记录的追踪
     */
    @Override
    public void transferTask(Long userId, BpmTaskTransferReqVO reqVO) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "transferTask", userId, reqVO);
        
        try {
            // 学习分析：任务转办的应用场景
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "任务转办操作", 
                    String.format("用户[%d]将任务[%s]转办给用户[%d]，转办原因: %s", 
                            userId, reqVO.getId(), reqVO.getAssigneeUserId(), reqVO.getReason()));
            
            // 学习分析：转办的业务价值
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "转办业务价值", 
                    "任务转办支持工作协作和负载均衡，提高组织效率");
            
            // 调用原方法
            super.transferTask(userId, reqVO);
            
            // 学习分析：转办完成
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "转办完成", 
                    String.format("任务转办完成，新的任务处理人: %d", reqVO.getAssigneeUserId()));
            
            LearningLogger.logMethodEnd(MODULE_NAME, "transferTask", startTime, "转办成功");
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "transferTask", startTime, e);
            throw e;
        }
    }

    /**
     * 学习扩展 - 委派任务
     * 
     * 学习要点：
     * 1. 委派与转办的区别
     * 2. 委派任务的生命周期
     * 3. 委派完成后的回归机制
     */
    @Override
    public void delegateTask(Long userId, BpmTaskDelegateReqVO reqVO) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "delegateTask", userId, reqVO);
        
        try {
            // 学习分析：任务委派的特点
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "任务委派操作", 
                    String.format("用户[%d]将任务[%s]委派给用户[%d]，委派原因: %s", 
                            userId, reqVO.getId(), reqVO.getDelegateUserId(), reqVO.getReason()));
            
            // 学习分析：委派与转办的区别
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "委派机制特点", 
                    "委派任务处理完成后会回到原委派人手中，而转办则是永久转移任务所有权");
            
            // 调用原方法
            super.delegateTask(userId, reqVO);
            
            // 学习分析：委派完成
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "委派完成", 
                    String.format("任务委派完成，被委派人处理后任务将回到委派人手中", reqVO.getDelegateUserId()));
            
            LearningLogger.logMethodEnd(MODULE_NAME, "delegateTask", startTime, "委派成功");
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "delegateTask", startTime, e);
            throw e;
        }
    }

    /**
     * 学习扩展 - 任务加签
     * 
     * 学习要点：
     * 1. 加签的业务场景
     * 2. 加签类型：前加签、后加签、并行加签
     * 3. 加签对流程的影响
     */
    @Override
    public void createSignTask(Long userId, BpmTaskSignCreateReqVO reqVO) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "createSignTask", userId, reqVO);
        
        try {
            // 学习分析：任务加签的业务需求
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "任务加签操作", 
                    String.format("用户[%d]为任务[%s]加签，加签类型: %s", 
                            userId, reqVO.getId(), reqVO.getType()));
            
            // 学习分析：加签类型的含义
            String signTypeDesc = "";
            switch (reqVO.getType()) {
                case "before":
                    signTypeDesc = "前加签 - 新增的审批人在当前节点之前处理";
                    break;
                case "after":
                    signTypeDesc = "后加签 - 新增的审批人在当前节点之后处理";
                    break;
                default:
                    signTypeDesc = "未知加签类型";
            }
            
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "加签类型分析", signTypeDesc);
            
            // 调用原方法
            super.createSignTask(userId, reqVO);
            
            // 学习分析：加签完成影响
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "加签完成影响", 
                    "任务加签完成，流程审批链已动态调整，增强了流程的灵活性");
            
            LearningLogger.logMethodEnd(MODULE_NAME, "createSignTask", startTime, "加签成功");
            
            // 学习心得记录
            LearningLogger.logLearningInsight(MODULE_NAME, 
                    "任务加签体现了现代工作流的灵活性：动态调整审批链、支持复杂业务场景");
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "createSignTask", startTime, e);
            throw e;
        }
    }

    /**
     * 学习扩展 - 任务减签
     * 
     * 学习要点：
     * 1. 减签的应用场景
     * 2. 减签对流程完整性的影响
     * 3. 减签权限的控制
     */
    @Override
    public void deleteSignTask(Long userId, BpmTaskSignDeleteReqVO reqVO) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "deleteSignTask", userId, reqVO);
        
        try {
            // 学习分析：任务减签的业务场景
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "任务减签操作", 
                    String.format("用户[%d]减签任务[%s]，减签原因: %s", 
                            userId, reqVO.getId(), reqVO.getReason()));
            
            // 学习分析：减签的影响
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "减签操作影响", 
                    "任务减签会移除审批环节，可能加快流程进度但需要确保审批完整性");
            
            // 调用原方法
            super.deleteSignTask(userId, reqVO);
            
            // 学习分析：减签完成
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "减签完成", 
                    "任务减签完成，审批链已调整，流程继续按新的路径执行");
            
            LearningLogger.logMethodEnd(MODULE_NAME, "deleteSignTask", startTime, "减签成功");
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "deleteSignTask", startTime, e);
            throw e;
        }
    }
}
