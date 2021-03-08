package com.peng.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * @author hqhuang
 * <p>
 * date 2019/9/26 14:59
 */

@Aspect
@Component
@Slf4j
public class MatchPanelAspect {


    @Pointcut("execution(* com.peng.frame.panel.*.showMatchPaneByDate(..))")
    public void showDatePanelCut() {

    }

    @Pointcut("execution(* com.peng.frame.panel.*.showMatchPaneByNum(..))")
    public void showNumPanelCut() {

    }

    /**
     * 审计方法
     *
     * @param joinPoint
     * @return
     * @throws Throwable
     */

    @Around("showDatePanelCut()")
    public Object audit(ProceedingJoinPoint joinPoint) throws Throwable {
        String panelName = joinPoint.getTarget().getClass().getName().replace("com.peng.frame.panel.", "").replace("Factory", "");
        log.info("开始计算{}的概览数据", panelName);
        long start = System.currentTimeMillis();
        Object res = joinPoint.proceed();

        log.info("计算{}数据结束，耗时{}ms", panelName, System.currentTimeMillis() - start);
        return res;
    }

    @Around("showNumPanelCut()")
    public Object audit1(ProceedingJoinPoint joinPoint) throws Throwable {
        String panelName = joinPoint.getTarget().getClass().getName().replace("com.peng.frame.panel.", "").replace("Factory", "");

        log.info("开始计算{}的详细数据", panelName);
        long start = System.currentTimeMillis();
        Object res = joinPoint.proceed();

        log.info("计算{}详细数据结束，耗时{}ms", panelName, System.currentTimeMillis() - start);
        return res;
    }
}
