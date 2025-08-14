package cn.iocoder.yudao.learning.testing.service;

import cn.iocoder.yudao.learning.common.model.LearningDataModels;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import cn.iocoder.yudao.learning.common.model.LearningDataModels.MethodInvocation;
import cn.iocoder.yudao.learning.common.model.LearningDataModels.TestCaseTemplate;
import cn.iocoder.yudao.learning.common.model.LearningDataModels.GeneratedTestCase;

/**
 * 智能测试生成服务
 * 
 * 功能：
 * 1. 基于方法调用自动生成测试用例
 * 2. 边界值测试用例生成
 * 3. 异常场景测试用例生成
 * 4. 性能测试用例生成
 * 5. 集成测试场景生成
 * 
 * @author 学习扩展
 */
@Slf4j
@Service
public class IntelligentTestGenerationService {

    /**
     * 方法调用记录
     */
    private final Map<String, List<LearningDataModels.MethodInvocation>> methodInvocations = new ConcurrentHashMap<>();
    
    /**
     * 测试用例模板
     */
    private final Map<String, LearningDataModels.TestCaseTemplate> testTemplates = new HashMap<>();
    
    /**
     * 生成的测试用例
     */
    private final Map<String, List<LearningDataModels.GeneratedTestCase>> generatedTests = new ConcurrentHashMap<>();

    public IntelligentTestGenerationService() {
        initializeTestTemplates();
    }

    /**
     * 初始化测试模板
     */
    private void initializeTestTemplates() {
        // 用户管理测试模板
        testTemplates.put("createUser", LearningDataModels.TestCaseTemplate.builder()
                .methodName("createUser")
                .description("用户创建功能测试")
                .testScenarios(Arrays.asList(
                    LearningDataModels.TestScenario.builder()
                        .name("正常创建用户")
                        .type("POSITIVE")
                        .description("使用有效参数创建用户")
                        .expectedResult("返回用户ID")
                        .build(),
                    LearningDataModels.TestScenario.builder()
                        .name("用户名重复")
                        .type("NEGATIVE")
                        .description("使用已存在的用户名创建用户")
                        .expectedResult("抛出业务异常")
                        .build(),
                    LearningDataModels.TestScenario.builder()
                        .name("参数校验失败")
                        .type("BOUNDARY")
                        .description("使用无效参数创建用户")
                        .expectedResult("抛出参数校验异常")
                        .build()
                ))
                .build());
        
        // 权限校验测试模板
        testTemplates.put("hasPermission", LearningDataModels.TestCaseTemplate.builder()
                .methodName("hasPermission")
                .description("权限校验功能测试")
                .testScenarios(Arrays.asList(
                    LearningDataModels.TestScenario.builder()
                        .name("有权限用户")
                        .type("POSITIVE")
                        .description("用户拥有指定权限")
                        .expectedResult("返回true")
                        .build(),
                    LearningDataModels.TestScenario.builder()
                        .name("无权限用户")
                        .type("NEGATIVE")
                        .description("用户没有指定权限")
                        .expectedResult("返回false")
                        .build(),
                    LearningDataModels.TestScenario.builder()
                        .name("超级管理员")
                        .type("SPECIAL")
                        .description("超级管理员权限校验")
                        .expectedResult("返回true")
                        .build()
                ))
                .build());
    }

    /**
     * 记录方法调用
     */
    public void recordMethodInvocation(String module, String method, Object[] params, 
                                     Object result, Exception exception, long duration) {
        MethodInvocation invocation = MethodInvocation.builder()
                .module(module)
                .method(method)
                .parameters(params)
                .result(result)
                .exception(exception)
                .duration(duration)
                .timestamp(LocalDateTime.now())
                .success(exception == null)
                .build();
        
        String key = module + "." + method;
        methodInvocations.computeIfAbsent(key, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(invocation);
        
        // 自动生成测试用例
        generateTestCasesForMethod(module, method, invocation);
    }

    /**
     * 为方法生成测试用例
     */
    private void generateTestCasesForMethod(String module, String method, MethodInvocation invocation) {
        String key = module + "." + method;
        
        // 基于实际调用生成正向测试用例
        GeneratedTestCase positiveTest = generatePositiveTestCase(module, method, invocation);
        
        // 生成边界值测试用例
        List<GeneratedTestCase> boundaryTests = generateBoundaryTestCases(module, method, invocation);
        
        // 生成异常测试用例
        List<GeneratedTestCase> exceptionTests = generateExceptionTestCases(module, method, invocation);
        
        // 保存生成的测试用例
        List<GeneratedTestCase> allTests = new ArrayList<>();
        allTests.add(positiveTest);
        allTests.addAll(boundaryTests);
        allTests.addAll(exceptionTests);
        
        generatedTests.put(key, allTests);
        
        log.info("=== 测试生成 === 为 {}.{} 生成了 {} 个测试用例", module, method, allTests.size());
    }

    /**
     * 生成正向测试用例
     */
    private GeneratedTestCase generatePositiveTestCase(String module, String method, MethodInvocation invocation) {
        StringBuilder testCode = new StringBuilder();
        
        testCode.append("@Test\n");
        testCode.append("void test").append(capitalize(method)).append("_Success() {\n");
        testCode.append("    // Given\n");
        
        // 生成参数准备代码
        if (invocation.getParameters() != null) {
            for (int i = 0; i < invocation.getParameters().length; i++) {
                Object param = invocation.getParameters()[i];
                testCode.append("    ").append(generateParameterCode(param, "param" + i)).append("\n");
            }
        }
        
        testCode.append("\n    // When\n");
        testCode.append("    ").append(generateMethodCallCode(module, method, invocation.getParameters())).append("\n");
        
        testCode.append("\n    // Then\n");
        testCode.append("    ").append(generateAssertionCode(invocation.getResult())).append("\n");
        testCode.append("}\n");
        
        return GeneratedTestCase.builder()
                .testName("test" + capitalize(method) + "_Success")
                .testType("POSITIVE")
                .description("正常场景测试")
                .testCode(testCode.toString())
                .expectedResult(invocation.getResult())
                .generatedTime(LocalDateTime.now())
                .build();
    }

    /**
     * 生成边界值测试用例
     */
    private List<GeneratedTestCase> generateBoundaryTestCases(String module, String method, MethodInvocation invocation) {
        List<GeneratedTestCase> boundaryTests = new ArrayList<>();
        
        if (invocation.getParameters() != null) {
            for (int i = 0; i < invocation.getParameters().length; i++) {
                Object param = invocation.getParameters()[i];
                
                // 为每个参数生成边界值测试
                List<Object> boundaryValues = generateBoundaryValues(param);
                
                for (Object boundaryValue : boundaryValues) {
                    GeneratedTestCase boundaryTest = generateBoundaryTestCase(
                        module, method, invocation, i, boundaryValue);
                    boundaryTests.add(boundaryTest);
                }
            }
        }
        
        return boundaryTests;
    }

    /**
     * 生成异常测试用例
     */
    private List<GeneratedTestCase> generateExceptionTestCases(String module, String method, MethodInvocation invocation) {
        List<GeneratedTestCase> exceptionTests = new ArrayList<>();
        
        // 空参数测试
        GeneratedTestCase nullParamTest = generateNullParameterTestCase(module, method);
        exceptionTests.add(nullParamTest);
        
        // 无效参数测试
        if (invocation.getParameters() != null) {
            for (int i = 0; i < invocation.getParameters().length; i++) {
                Object param = invocation.getParameters()[i];
                List<Object> invalidValues = generateInvalidValues(param);
                
                for (Object invalidValue : invalidValues) {
                    GeneratedTestCase invalidTest = generateInvalidParameterTestCase(
                        module, method, invocation, i, invalidValue);
                    exceptionTests.add(invalidTest);
                }
            }
        }
        
        return exceptionTests;
    }

    /**
     * 生成边界值
     */
    private List<Object> generateBoundaryValues(Object param) {
        List<Object> boundaryValues = new ArrayList<>();
        
        if (param instanceof String) {
            String str = (String) param;
            boundaryValues.add(""); // 空字符串
            boundaryValues.add("a"); // 单字符
            boundaryValues.add("a".repeat(255)); // 长字符串
        } else if (param instanceof Integer) {
            boundaryValues.add(0);
            boundaryValues.add(-1);
            boundaryValues.add(Integer.MAX_VALUE);
            boundaryValues.add(Integer.MIN_VALUE);
        } else if (param instanceof Long) {
            boundaryValues.add(0L);
            boundaryValues.add(-1L);
            boundaryValues.add(Long.MAX_VALUE);
            boundaryValues.add(Long.MIN_VALUE);
        }
        
        return boundaryValues;
    }

    /**
     * 生成无效值
     */
    private List<Object> generateInvalidValues(Object param) {
        List<Object> invalidValues = new ArrayList<>();
        
        if (param instanceof String) {
            invalidValues.add(null);
            invalidValues.add("   "); // 空白字符串
        } else if (param instanceof Number) {
            invalidValues.add(null);
            invalidValues.add(-1); // 负数（如果不允许）
        }
        
        return invalidValues;
    }

    /**
     * 生成边界值测试用例
     */
    private GeneratedTestCase generateBoundaryTestCase(String module, String method, 
                                                     MethodInvocation invocation, int paramIndex, Object boundaryValue) {
        StringBuilder testCode = new StringBuilder();
        
        testCode.append("@Test\n");
        testCode.append("void test").append(capitalize(method)).append("_Boundary_Param").append(paramIndex).append("() {\n");
        testCode.append("    // Given\n");
        
        // 生成参数代码，替换指定位置的参数为边界值
        Object[] testParams = Arrays.copyOf(invocation.getParameters(), invocation.getParameters().length);
        testParams[paramIndex] = boundaryValue;
        
        for (int i = 0; i < testParams.length; i++) {
            testCode.append("    ").append(generateParameterCode(testParams[i], "param" + i)).append("\n");
        }
        
        testCode.append("\n    // When & Then\n");
        testCode.append("    ").append(generateBoundaryAssertionCode(module, method, testParams)).append("\n");
        testCode.append("}\n");
        
        return GeneratedTestCase.builder()
                .testName("test" + capitalize(method) + "_Boundary_Param" + paramIndex)
                .testType("BOUNDARY")
                .description("边界值测试 - 参数" + paramIndex)
                .testCode(testCode.toString())
                .expectedResult("边界值处理")
                .generatedTime(LocalDateTime.now())
                .build();
    }

    /**
     * 生成空参数测试用例
     */
    private GeneratedTestCase generateNullParameterTestCase(String module, String method) {
        StringBuilder testCode = new StringBuilder();
        
        testCode.append("@Test\n");
        testCode.append("void test").append(capitalize(method)).append("_NullParameter() {\n");
        testCode.append("    // Given\n");
        testCode.append("    // 空参数\n");
        testCode.append("\n    // When & Then\n");
        testCode.append("    assertThrows(IllegalArgumentException.class, () -> {\n");
        testCode.append("        ").append(generateMethodCallCode(module, method, new Object[]{null})).append("\n");
        testCode.append("    });\n");
        testCode.append("}\n");
        
        return GeneratedTestCase.builder()
                .testName("test" + capitalize(method) + "_NullParameter")
                .testType("EXCEPTION")
                .description("空参数异常测试")
                .testCode(testCode.toString())
                .expectedResult("IllegalArgumentException")
                .generatedTime(LocalDateTime.now())
                .build();
    }

    /**
     * 生成无效参数测试用例
     */
    private GeneratedTestCase generateInvalidParameterTestCase(String module, String method, 
                                                             MethodInvocation invocation, int paramIndex, Object invalidValue) {
        StringBuilder testCode = new StringBuilder();
        
        testCode.append("@Test\n");
        testCode.append("void test").append(capitalize(method)).append("_InvalidParam").append(paramIndex).append("() {\n");
        testCode.append("    // Given\n");
        
        Object[] testParams = Arrays.copyOf(invocation.getParameters(), invocation.getParameters().length);
        testParams[paramIndex] = invalidValue;
        
        for (int i = 0; i < testParams.length; i++) {
            testCode.append("    ").append(generateParameterCode(testParams[i], "param" + i)).append("\n");
        }
        
        testCode.append("\n    // When & Then\n");
        testCode.append("    assertThrows(Exception.class, () -> {\n");
        testCode.append("        ").append(generateMethodCallCode(module, method, testParams)).append("\n");
        testCode.append("    });\n");
        testCode.append("}\n");
        
        return GeneratedTestCase.builder()
                .testName("test" + capitalize(method) + "_InvalidParam" + paramIndex)
                .testType("EXCEPTION")
                .description("无效参数异常测试")
                .testCode(testCode.toString())
                .expectedResult("Exception")
                .generatedTime(LocalDateTime.now())
                .build();
    }

    /**
     * 生成参数代码
     */
    private String generateParameterCode(Object param, String varName) {
        if (param == null) {
            return "Object " + varName + " = null;";
        } else if (param instanceof String) {
            return "String " + varName + " = \"" + param + "\";";
        } else if (param instanceof Integer) {
            return "Integer " + varName + " = " + param + ";";
        } else if (param instanceof Long) {
            return "Long " + varName + " = " + param + "L;";
        } else if (param instanceof Boolean) {
            return "Boolean " + varName + " = " + param + ";";
        } else {
            return param.getClass().getSimpleName() + " " + varName + " = " + 
                   generateObjectCreationCode(param) + ";";
        }
    }

    /**
     * 生成对象创建代码
     */
    private String generateObjectCreationCode(Object obj) {
        if (obj == null) return "null";
        
        // 简化的对象创建代码生成
        return "/* TODO: 创建 " + obj.getClass().getSimpleName() + " 对象 */";
    }

    /**
     * 生成方法调用代码
     */
    private String generateMethodCallCode(String module, String method, Object[] params) {
        StringBuilder code = new StringBuilder();
        
        String serviceName = module.toLowerCase() + "Service";
        code.append(serviceName).append(".").append(method).append("(");
        
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                if (i > 0) code.append(", ");
                code.append("param").append(i);
            }
        }
        
        code.append(");");
        return code.toString();
    }

    /**
     * 生成断言代码
     */
    private String generateAssertionCode(Object result) {
        if (result == null) {
            return "assertNull(result);";
        } else if (result instanceof Boolean) {
            return "assertTrue(result);";
        } else if (result instanceof Number) {
            return "assertNotNull(result);";
        } else if (result instanceof String) {
            return "assertNotNull(result);\n    assertFalse(result.isEmpty());";
        } else {
            return "assertNotNull(result);";
        }
    }

    /**
     * 生成边界值断言代码
     */
    private String generateBoundaryAssertionCode(String module, String method, Object[] params) {
        return "// 边界值测试，根据具体业务逻辑添加断言\n    " + 
               generateMethodCallCode(module, method, params);
    }

    /**
     * 首字母大写
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * 生成完整的测试类
     */
    public String generateTestClass(String module) {
        StringBuilder testClass = new StringBuilder();
        
        String className = capitalize(module) + "ServiceTest";
        
        testClass.append("package cn.iocoder.yudao.learning.test;\n\n");
        testClass.append("import org.junit.jupiter.api.Test;\n");
        testClass.append("import org.springframework.boot.test.context.SpringBootTest;\n");
        testClass.append("import static org.junit.jupiter.api.Assertions.*;\n\n");
        testClass.append("/**\n");
        testClass.append(" * ").append(module).append("模块自动生成测试类\n");
        testClass.append(" * 生成时间: ").append(LocalDateTime.now()).append("\n");
        testClass.append(" */\n");
        testClass.append("@SpringBootTest\n");
        testClass.append("class ").append(className).append(" {\n\n");
        
        // 添加所有相关的测试方法
        methodInvocations.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith(module + "."))
            .forEach(entry -> {
                String methodKey = entry.getKey();
                List<GeneratedTestCase> tests = generatedTests.get(methodKey);
                if (tests != null) {
                    for (GeneratedTestCase test : tests) {
                        testClass.append("    ").append(test.getTestCode()).append("\n");
                    }
                }
            });
        
        testClass.append("}\n");
        
        return testClass.toString();
    }

    /**
     * 输出测试生成报告
     */
    public void printTestGenerationReport() {
        log.info("\n=== 测试生成报告 ===");
        log.info("已记录方法调用: {} 个", methodInvocations.size());
        log.info("已生成测试用例: {} 个", 
            generatedTests.values().stream().mapToInt(List::size).sum());
        
        generatedTests.forEach((methodKey, tests) -> {
            log.info("方法 {}: {} 个测试用例", methodKey, tests.size());
            tests.forEach(test -> {
                log.info("  - {} ({}): {}", test.getTestName(), test.getTestType(), test.getDescription());
            });
        });
    }

    /**
     * 清空测试数据
     */
    public void clearTestData() {
        methodInvocations.clear();
        generatedTests.clear();
        log.info("=== 测试生成 === 已清空所有测试数据");
    }
}
