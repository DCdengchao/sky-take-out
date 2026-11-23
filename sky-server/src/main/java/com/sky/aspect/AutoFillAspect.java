package com.sky.aspect;


import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import org.aspectj.lang.reflect.MethodSignature;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

import static org.springframework.util.ReflectionUtils.setField;


/**
 * 自定义切面类
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    /**
     * 定义切点
     */

    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointcut() {
    }

    /**
     * 前置通知
     */
    @Before("autoFillPointcut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始执行自动填充字段的操作...");
        // 获取当前方法的数据库操作类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        OperationType operationType = autoFill.value();

        //获取到当前被拦截到的方法参数
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return;
        }

        Object entity = args[0];

        //准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentUserId = BaseContext.getCurrentId();


        //根据不同的属性，通过反射给对应的属性赋值
        if (OperationType.INSERT == operationType) {
            // 设置创建时间和创建人
            try {
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);

                //通过反射给对应的属性赋值
                setCreateUser.invoke(entity, currentUserId);
                setUpdateUser.invoke(entity, currentUserId);
                setUpdateTime.invoke(entity, now);
                setCreateTime.invoke(entity, now);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (OperationType.UPDATE == operationType) {
            // 设置修改时间和修改人
            try {
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);

                //通过反射给对应的属性赋值
                setUpdateUser.invoke(entity, currentUserId);
                setUpdateTime.invoke(entity, now);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
