package cn.iocoder.yudao.learning.workflow.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.learning.core.util.LearningLogger;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.*;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

/**
 * 学习扩展 - 工作流流程实例服务实现
 * 
 * 通过继承原BpmProcessInstanceServiceImpl类，使用@Primary注解让Spring优先使用这个实现
 * 在每个方法中调用原方法，前后添加学习日志和业务分析
 * 
 * 学习重点：
 * 1. 流程实例创建：流程定义选择 -> 变量设置 -> 实例启动
 * 2. 流程实例管理：查询、分页、状态跟踪
 * 3. 流程变量处理：变量传递、作用域、类型转换
 * 4. 流程状态机：实例状态的流转规则
 * 5. 任务分配策略：审批人的动态分配
 * 6. 流程监控：实例进度、性能统计
 * 7. 异常处理：流程异常的恢复机制
 * 8. 历史数据：流程执行历史的查询和分析
 * 9. BPMN引擎：Flowable引擎的核心概念
 * 10. 业务集成：工作流与业务系统的集成模式
 * 
 * @author 学习者
 */
@Slf4j
@Service
@Primary  // 关键注解：让Spring优先使用这个Bean，原代码调用BpmProcessInstanceService时会调用到这里
public class LearningBpmProcessInstanceService extends BpmProcessInstanceServiceImpl {

    private static final String MODULE_NAME = "工作流模块-流程实例";

    /**
     * 学习扩展 - 创建流程实例（前端调用）
     * 
     * 学习要点：
     * 1. 流程定义的选择和校验
     * 2. 流程变量的初始化
     * 3. 启动用户的权限检查
     * 4. 流程实例的生命周期开始
     */
    @Override
    public String createProcessInstance(Long userId, @Valid BpmProcessInstanceCreateReqVO createReqVO) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "createProcessInstance", userId, createReqVO);
        
        try {
            // 学习分析：流程实例创建的业务意义
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "流程实例创建", 
                    String.format("用户[%d]发起流程实例 - 流程定义ID: %s", 
                            userId, createReqVO.getProcessDefinitionId()));
            
            // 学习分析：流程变量的重要性
            if (createReqVO.getVariables() != null && !createReqVO.getVariables().isEmpty()) {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "流程变量设置", 
                        String.format("设置了%d个流程变量，变量在整个流程生命周期中传递业务数据", 
                                createReqVO.getVariables().size()));
                
                // 记录关键变量
                createReqVO.getVariables().forEach((key, value) -> {
                    LearningLogger.logDataFlow(MODULE_NAME, "流程变量", 
                            String.format("变量名: %s, 值: %s, 类型: %s", 
                                    key, value, value != null ? value.getClass().getSimpleName() : "null"));
                });
            }
            
            // 学习分析：审批人选择机制
            if (createReqVO.getStartUserSelectAssignees() != null && !createReqVO.getStartUserSelectAssignees().isEmpty()) {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "审批人选择", 
                        String.format("发起人选择了%d个节点的审批人，体现了灵活的任务分配机制", 
                                createReqVO.getStartUserSelectAssignees().size()));
            }
            
            // 调用原方法 - 这里可以设置断点，深入学习流程实例创建流程
            String processInstanceId = super.createProcessInstance(userId, createReqVO);
            
            // 学习分析：流程实例创建成功
            if (processInstanceId != null) {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "流程实例创建成功", 
                        String.format("流程实例创建成功，实例ID: %s，流程正式进入执行状态", processInstanceId));
                
                // 学习分析：流程引擎的工作机制
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "流程引擎机制", 
                        "Flowable引擎根据BPMN定义创建流程实例，并自动推进到第一个用户任务节点");
            }
            
            LearningLogger.logMethodEnd(MODULE_NAME, "createProcessInstance", startTime, processInstanceId);
            
            // 学习心得记录
            LearningLogger.logLearningInsight(MODULE_NAME, 
                    "流程实例创建体现了工作流引擎的核心能力：流程定义解析、实例管理、任务分配、变量传递");
            
            return processInstanceId;
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "createProcessInstance", startTime, e);
            
            // 学习分析：流程创建失败的原因
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "流程创建失败", 
                    String.format("流程实例创建失败，可能原因：流程定义不存在、权限不足、变量类型错误。异常类型: %s", 
                            e.getClass().getSimpleName()));
            
            throw e;
        }
    }

    /**
     * 学习扩展 - 创建流程实例（内部API调用）
     * 
     * 学习要点：
     * 1. 内部API与前端API的区别
     * 2. 流程定义Key的使用
     * 3. 业务Key的关联机制
     */
    @Override
    public String createProcessInstance(Long userId, @Valid BpmProcessInstanceCreateReqDTO createReqDTO) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "createProcessInstance-API", userId, createReqDTO);
        
        try {
            // 学习分析：内部API调用的特点
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "内部API调用", 
                    String.format("内部系统调用 - 流程定义Key: %s, 业务Key: %s", 
                            createReqDTO.getProcessDefinitionKey(), createReqDTO.getBusinessKey()));
            
            // 学习分析：业务Key的重要性
            if (createReqDTO.getBusinessKey() != null) {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "业务Key关联", 
                        String.format("业务Key[%s]建立了流程实例与业务数据的关联，便于后续查询和管理", 
                                createReqDTO.getBusinessKey()));
            }
            
            // 学习分析：流程定义Key vs ID
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "流程定义标识", 
                    "使用流程定义Key而非ID，系统会自动选择最新版本的流程定义，支持流程版本管理");
            
            // 调用原方法
            String processInstanceId = super.createProcessInstance(userId, createReqDTO);
            
            // 学习分析：API调用成功
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "API调用成功", 
                    String.format("内部API调用成功，流程实例ID: %s", processInstanceId));
            
            LearningLogger.logMethodEnd(MODULE_NAME, "createProcessInstance-API", startTime, processInstanceId);
            
            return processInstanceId;
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "createProcessInstance-API", startTime, e);
            throw e;
        }
    }

    /**
     * 学习扩展 - 获取流程实例
     * 
     * 学习要点：
     * 1. 流程实例的状态查询
     * 2. 运行时数据的获取
     */
    @Override
    public ProcessInstance getProcessInstance(String id) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "getProcessInstance", id);
        
        try {
            // 学习分析：流程实例查询的应用场景
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "流程实例查询", 
                    String.format("查询流程实例[%s]的运行时状态，用于流程监控和业务展示", id));
            
            // 调用原方法
            ProcessInstance instance = super.getProcessInstance(id);
            
            // 学习分析：查询结果
            if (instance != null) {
                LearningLogger.logDataFlow(MODULE_NAME, "流程实例信息", 
                        String.format("流程实例存在 - 定义ID: %s, 业务Key: %s, 是否挂起: %s", 
                                instance.getProcessDefinitionId(), 
                                instance.getBusinessKey(), 
                                instance.isSuspended() ? "是" : "否"));
                
                // 学习分析：流程实例状态
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "流程实例状态", 
                        instance.isSuspended() ? "流程实例已挂起，暂停执行" : "流程实例正在运行中");
                
            } else {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "流程实例不存在", 
                        String.format("流程实例[%s]不存在或已结束", id));
            }
            
            LearningLogger.logMethodEnd(MODULE_NAME, "getProcessInstance", startTime, 
                    instance != null ? "实例存在" : "实例不存在");
            
            return instance;
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "getProcessInstance", startTime, e);
            throw e;
        }
    }

    /**
     * 学习扩展 - 获取历史流程实例
     * 
     * 学习要点：
     * 1. 历史数据的查询机制
     * 2. 已结束流程的信息获取
     */
    @Override
    public HistoricProcessInstance getHistoricProcessInstance(String id) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "getHistoricProcessInstance", id);
        
        try {
            // 学习分析：历史数据查询的意义
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "历史数据查询", 
                    String.format("查询流程实例[%s]的历史信息，包括已结束的流程数据", id));
            
            // 调用原方法
            HistoricProcessInstance historicInstance = super.getHistoricProcessInstance(id);
            
            // 学习分析：历史实例信息
            if (historicInstance != null) {
                LearningLogger.logDataFlow(MODULE_NAME, "历史流程实例", 
                        String.format("历史实例信息 - 开始时间: %s, 结束时间: %s, 持续时间: %dms", 
                                historicInstance.getStartTime(), 
                                historicInstance.getEndTime(),
                                historicInstance.getDurationInMillis() != null ? historicInstance.getDurationInMillis() : 0));
                
                // 学习分析：流程执行统计
                if (historicInstance.getEndTime() != null) {
                    LearningLogger.logBusinessAnalysis(MODULE_NAME, "流程执行统计", 
                            String.format("流程已结束，总耗时: %d毫秒，可用于性能分析和优化", 
                                    historicInstance.getDurationInMillis()));
                } else {
                    LearningLogger.logBusinessAnalysis(MODULE_NAME, "流程执行状态", 
                            "流程仍在执行中，可以继续跟踪进度");
                }
            }
            
            LearningLogger.logMethodEnd(MODULE_NAME, "getHistoricProcessInstance", startTime, 
                    historicInstance != null ? "历史实例存在" : "历史实例不存在");
            
            return historicInstance;
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "getHistoricProcessInstance", startTime, e);
            throw e;
        }
    }

    /**
     * 学习扩展 - 获取我的流程实例分页
     * 
     * 学习要点：
     * 1. 分页查询的实现
     * 2. 用户权限的数据过滤
     * 3. 流程实例的状态筛选
     */
    @Override
    public PageResult<HistoricProcessInstance> getProcessInstancePage(Long userId, BpmProcessInstancePageReqVO pageReqVO) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "getProcessInstancePage", userId, pageReqVO);
        
        try {
            // 学习分析：个人流程实例查询
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "个人流程查询", 
                    String.format("用户[%d]查询个人流程实例，页码: %d, 页大小: %d", 
                            userId, pageReqVO.getPageNo(), pageReqVO.getPageSize()));
            
            // 学习分析：查询条件分析
            if (pageReqVO.getStatus() != null) {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "状态筛选", 
                        String.format("按状态筛选流程实例: %d", pageReqVO.getStatus()));
            }
            
            if (pageReqVO.getProcessDefinitionKey() != null) {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "流程定义筛选", 
                        String.format("按流程定义筛选: %s", pageReqVO.getProcessDefinitionKey()));
            }
            
            // 调用原方法
            PageResult<HistoricProcessInstance> pageResult = super.getProcessInstancePage(userId, pageReqVO);
            
            // 学习分析：查询结果统计
            if (pageResult != null) {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "查询结果统计", 
                        String.format("查询到%d条记录，总计%d条，展示了用户的流程参与情况", 
                                pageResult.getList().size(), pageResult.getTotal()));
                
                // 分析流程实例状态分布
                if (!pageResult.getList().isEmpty()) {
                    long runningCount = pageResult.getList().stream()
                            .filter(instance -> instance.getEndTime() == null) // 运行中的流程
                            .count();
                    
                    LearningLogger.logBusinessAnalysis(MODULE_NAME, "流程状态分布", 
                            String.format("当前页面中运行中的流程: %d个，已结束的流程: %d个", 
                                    runningCount, pageResult.getList().size() - runningCount));
                }
            }
            
            LearningLogger.logMethodEnd(MODULE_NAME, "getProcessInstancePage", startTime, 
                    String.format("返回%d条记录", pageResult != null ? pageResult.getList().size() : 0));
            
            return pageResult;
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "getProcessInstancePage", startTime, e);
            throw e;
        }
    }

    /**
     * 学习扩展 - 取消流程实例
     * 
     * 学习要点：
     * 1. 流程实例的生命周期管理
     * 2. 取消操作的权限控制
     * 3. 取消后的数据清理
     */
    @Override
    public void cancelProcessInstanceByStartUser(Long userId, BpmProcessInstanceCancelReqVO cancelReqVO) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "cancelProcessInstanceByStartUser", userId, cancelReqVO);
        
        try {
            // 学习分析：流程取消的业务场景
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "流程取消操作", 
                    String.format("用户[%d]取消流程实例[%s]，原因: %s", 
                            userId, cancelReqVO.getId(), cancelReqVO.getReason()));
            
            // 学习分析：取消操作的影响
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "取消操作影响", 
                    "流程取消会终止所有进行中的任务，并记录取消原因，影响相关业务数据");
            
            // 调用原方法
            super.cancelProcessInstanceByStartUser(userId, cancelReqVO);
            
            // 学习分析：取消操作完成
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "取消操作完成", 
                    "流程实例已成功取消，相关任务已终止，历史数据已保留");
            
            LearningLogger.logMethodEnd(MODULE_NAME, "cancelProcessInstanceByStartUser", startTime, "取消成功");
            
            // 学习心得记录
            LearningLogger.logLearningInsight(MODULE_NAME, 
                    "流程取消功能体现了工作流的灵活性：支持流程中断、原因记录、数据保留");
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "cancelProcessInstanceByStartUser", startTime, e);
            
            // 学习分析：取消失败的原因
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "取消失败分析", 
                    "取消失败可能由于：权限不足、流程已结束、流程状态不允许取消等");
            
            throw e;
        }
    }
}
