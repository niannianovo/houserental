package com.example.aspect;

import com.example.annotation.AdminAction;
import com.example.entity.AdminLog;
import com.example.mapper.AdminLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Aspect
@Component
@Slf4j
public class AdminLogAspect {

    @Autowired
    private AdminLogMapper adminLogMapper;

    @Around("@annotation(adminAction)")
    public Object logAdminAction(ProceedingJoinPoint joinPoint, AdminAction adminAction) throws Throwable {
        Object result = joinPoint.proceed();

        // TODO 从参数中提取 adminId、targetId 等信息，记录操作日志
        try {
            AdminLog adminLog = new AdminLog();
            adminLog.setAction(adminAction.value());
            adminLog.setTargetType(adminAction.targetType());
            adminLog.setCreateTime(new Date());
            adminLogMapper.insert(adminLog);
        } catch (Exception e) {
            log.error("记录管理员操作日志失败", e);
        }

        return result;
    }
}
