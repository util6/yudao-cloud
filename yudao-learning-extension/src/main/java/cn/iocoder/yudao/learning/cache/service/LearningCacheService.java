package cn.iocoder.yudao.learning.cache.service;

import cn.iocoder.yudao.learning.core.util.LearningLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 学习扩展 - 缓存服务实现
 * 
 * 通过封装Redis操作，学习缓存在企业级应用中的使用模式
 * 
 * 学习重点：
 * 1. Redis数据类型的应用：String、Hash、List、Set、ZSet
 * 2. 缓存策略：过期时间设置、缓存更新策略
 * 3. 缓存穿透防护：空值缓存、布隆过滤器
 * 4. 缓存击穿防护：分布式锁、热点数据预加载
 * 5. 缓存雪崩防护：过期时间随机化、多级缓存
 * 6. 序列化机制：JSON序列化的优缺点
 * 7. 性能监控：缓存命中率统计、响应时间监控
 * 
 * @author 学习者
 */
@Slf4j
@Service
public class LearningCacheService {

    private static final String MODULE_NAME = "缓存模块";
    
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    
    // 缓存统计信息
    private final AtomicLong cacheHitCount = new AtomicLong(0);
    private final AtomicLong cacheMissCount = new AtomicLong(0);
    private final AtomicLong cacheSetCount = new AtomicLong(0);

    /**
     * 学习扩展 - 设置缓存
     * 
     * 学习要点：
     * 1. Redis的String数据类型应用
     * 2. 过期时间的设置策略
     * 3. JSON序列化的使用
     */
    public void set(String key, Object value, long timeout, TimeUnit timeUnit) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "set", key, value, timeout, timeUnit);
        
        try {
            // 学习分析：缓存设置的业务意义
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "缓存设置策略", 
                    String.format("设置缓存 key: %s, 过期时间: %d %s, 数据类型: %s", 
                            key, timeout, timeUnit.name(), value != null ? value.getClass().getSimpleName() : "null"));
            
            // 学习分析：过期时间的重要性
            if (timeout > 0) {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "过期时间机制", 
                        "设置过期时间可以防止内存泄漏，同时实现数据的自动清理");
                redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
            } else {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "永久缓存", 
                        "未设置过期时间，数据将永久保存，需要注意内存使用情况");
                redisTemplate.opsForValue().set(key, value);
            }
            
            // 统计缓存设置次数
            cacheSetCount.incrementAndGet();
            
            // 学习分析：序列化机制
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "序列化机制", 
                    "使用JSON序列化存储对象，具有可读性好、跨语言支持的优点，但序列化开销相对较大");
            
            LearningLogger.logMethodEnd(MODULE_NAME, "set", startTime, "缓存设置成功");
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "set", startTime, e);
            
            // 学习分析：缓存设置失败的处理
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "缓存设置失败", 
                    "缓存设置失败可能由于：Redis连接异常、序列化失败、内存不足等原因");
            
            throw e;
        }
    }

    /**
     * 学习扩展 - 获取缓存
     * 
     * 学习要点：
     * 1. 缓存命中率的统计
     * 2. 缓存穿透的识别
     * 3. 反序列化的处理
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "get", key, clazz.getSimpleName());
        
        try {
            // 从Redis获取数据
            Object value = redisTemplate.opsForValue().get(key);
            
            // 统计缓存命中情况
            if (value != null) {
                cacheHitCount.incrementAndGet();
                
                // 学习分析：缓存命中的好处
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "缓存命中", 
                        String.format("缓存命中，避免了数据库查询，提升了响应速度。当前命中率: %.2f%%", 
                                getCacheHitRate()));
                
                LearningLogger.logDataFlow(MODULE_NAME, "缓存数据", 
                        String.format("从缓存获取到数据，类型: %s", value.getClass().getSimpleName()));
                
            } else {
                cacheMissCount.incrementAndGet();
                
                // 学习分析：缓存未命中的处理
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "缓存未命中", 
                        String.format("缓存未命中，需要从数据源获取数据。当前命中率: %.2f%%", 
                                getCacheHitRate()));
            }
            
            LearningLogger.logMethodEnd(MODULE_NAME, "get", startTime, 
                    value != null ? "缓存命中" : "缓存未命中");
            
            return (T) value;
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "get", startTime, e);
            
            // 学习分析：缓存获取失败的处理
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "缓存获取失败", 
                    "缓存获取失败时应该降级到数据库查询，保证系统的可用性");
            
            throw e;
        }
    }

    /**
     * 学习扩展 - 删除缓存
     * 
     * 学习要点：
     * 1. 缓存删除的时机
     * 2. 缓存一致性的保证
     */
    public void delete(String key) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "delete", key);
        
        try {
            // 学习分析：缓存删除的业务场景
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "缓存删除场景", 
                    "缓存删除通常发生在数据更新时，用于保证缓存与数据库的一致性");
            
            Boolean result = redisTemplate.delete(key);
            
            // 学习分析：删除结果的意义
            if (Boolean.TRUE.equals(result)) {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "删除成功", 
                        "缓存删除成功，下次访问将从数据库重新加载数据");
            } else {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "删除结果", 
                        "缓存key不存在或已过期，删除操作无实际影响");
            }
            
            LearningLogger.logMethodEnd(MODULE_NAME, "delete", startTime, 
                    Boolean.TRUE.equals(result) ? "删除成功" : "key不存在");
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "delete", startTime, e);
            throw e;
        }
    }

    /**
     * 学习扩展 - 检查key是否存在
     * 
     * 学习要点：
     * 1. 存在性检查的应用场景
     * 2. 布尔值返回的处理
     */
    public boolean exists(String key) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "exists", key);
        
        try {
            Boolean result = redisTemplate.hasKey(key);
            boolean exists = Boolean.TRUE.equals(result);
            
            // 学习分析：存在性检查的用途
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "存在性检查", 
                    String.format("key[%s] %s，存在性检查常用于缓存预热、重复处理防护等场景", 
                            key, exists ? "存在" : "不存在"));
            
            LearningLogger.logMethodEnd(MODULE_NAME, "exists", startTime, String.valueOf(exists));
            
            return exists;
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "exists", startTime, e);
            throw e;
        }
    }

    /**
     * 学习扩展 - 设置过期时间
     * 
     * 学习要点：
     * 1. 动态过期时间设置
     * 2. 过期时间的业务意义
     */
    public boolean expire(String key, long timeout, TimeUnit timeUnit) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "expire", key, timeout, timeUnit);
        
        try {
            // 学习分析：动态设置过期时间的场景
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "动态过期时间", 
                    String.format("为key[%s]设置过期时间 %d %s，常用于延长热点数据的缓存时间", 
                            key, timeout, timeUnit.name()));
            
            Boolean result = redisTemplate.expire(key, timeout, timeUnit);
            boolean success = Boolean.TRUE.equals(result);
            
            if (success) {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "过期时间设置成功", 
                        "过期时间设置成功，数据将在指定时间后自动清理");
            } else {
                LearningLogger.logBusinessAnalysis(MODULE_NAME, "过期时间设置失败", 
                        "可能原因：key不存在或Redis版本不支持该操作");
            }
            
            LearningLogger.logMethodEnd(MODULE_NAME, "expire", startTime, String.valueOf(success));
            
            return success;
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "expire", startTime, e);
            throw e;
        }
    }

    /**
     * 学习扩展 - 获取剩余过期时间
     * 
     * 学习要点：
     * 1. TTL的业务应用
     * 2. 时间单位的转换
     */
    public long getExpire(String key, TimeUnit timeUnit) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "getExpire", key, timeUnit);
        
        try {
            Long expire = redisTemplate.getExpire(key, timeUnit);
            long expireTime = expire != null ? expire : -1;
            
            // 学习分析：TTL值的含义
            String expireDesc;
            if (expireTime == -1) {
                expireDesc = "key不存在";
            } else if (expireTime == -2) {
                expireDesc = "key存在但未设置过期时间";
            } else {
                expireDesc = String.format("剩余过期时间: %d %s", expireTime, timeUnit.name());
            }
            
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "TTL查询结果", expireDesc);
            
            LearningLogger.logMethodEnd(MODULE_NAME, "getExpire", startTime, String.valueOf(expireTime));
            
            return expireTime;
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "getExpire", startTime, e);
            throw e;
        }
    }

    /**
     * 学习扩展 - 模糊查询key
     * 
     * 学习要点：
     * 1. KEYS命令的性能影响
     * 2. 生产环境的替代方案
     */
    public Set<String> keys(String pattern) {
        long startTime = LearningLogger.logMethodStart(MODULE_NAME, "keys", pattern);
        
        try {
            // 学习分析：KEYS命令的性能警告
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "KEYS命令性能警告", 
                    "KEYS命令会阻塞Redis服务器，生产环境建议使用SCAN命令替代");
            
            Set<String> keys = redisTemplate.keys(pattern);
            int keyCount = keys != null ? keys.size() : 0;
            
            LearningLogger.logBusinessAnalysis(MODULE_NAME, "模糊查询结果", 
                    String.format("匹配模式[%s]找到%d个key", pattern, keyCount));
            
            LearningLogger.logMethodEnd(MODULE_NAME, "keys", startTime, 
                    String.format("找到%d个key", keyCount));
            
            return keys;
            
        } catch (Exception e) {
            LearningLogger.logMethodException(MODULE_NAME, "keys", startTime, e);
            throw e;
        }
    }

    /**
     * 获取缓存命中率
     * 
     * @return 缓存命中率（百分比）
     */
    public double getCacheHitRate() {
        long totalAccess = cacheHitCount.get() + cacheMissCount.get();
        if (totalAccess == 0) {
            return 0.0;
        }
        return (double) cacheHitCount.get() / totalAccess * 100;
    }

    /**
     * 获取缓存统计信息
     */
    public void printCacheStatistics() {
        LearningLogger.logBusinessAnalysis(MODULE_NAME, "缓存统计信息", 
                String.format("命中次数: %d, 未命中次数: %d, 设置次数: %d, 命中率: %.2f%%", 
                        cacheHitCount.get(), cacheMissCount.get(), cacheSetCount.get(), getCacheHitRate()));
    }

    /**
     * 重置缓存统计信息
     */
    public void resetCacheStatistics() {
        cacheHitCount.set(0);
        cacheMissCount.set(0);
        cacheSetCount.set(0);
        
        LearningLogger.logBusinessAnalysis(MODULE_NAME, "统计重置", "缓存统计信息已重置");
    }
}
