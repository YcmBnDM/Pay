package com.example.aop;

import com.alibaba.fastjson.JSONObject;
import com.example.bean.RequestKitBean;
import com.example.constants.CS;
import com.example.entity.SysLog;
import com.example.exception.BizException;
import com.example.model.security.PayUserDetails;
import com.example.service.impl.SysLogService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


/**
 * 方法级日志切面组件
 */
@Component
@Aspect
public class MethodLogAop {

    private static final Logger logger = LoggerFactory.getLogger(MethodLogAop.class);

    @Resource
    private SysLogService sysLogService;

    @Resource
    private RequestKitBean requestKitBean;

    /**
     * 异步处理线程池
     */
    private final static ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(10);

    /**
     * 切点
     */
    @Pointcut("@annotation(com.example.aop.MethodLog)")
    public void methodCachePointcut() {
    }

    /**
     * 切面
     *
     * @param point
     * @return
     * @throws Throwable
     */
    @Around("methodCachePointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {

        final SysLog sysLog = new SysLog();

        //处理切面任务 发生异常将向外抛出 不记录日志
        Object result = point.proceed();

        try {
            // 基础日志信息
            setBaseLogInfo(point, sysLog, PayUserDetails.getCurrentUserDetails());
            sysLog.setOptResInfo(JSONObject.toJSON(result).toString());
            scheduledThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    sysLogService.save(sysLog);
                }
            });
        } catch (Exception e) {
            logger.error("methodLogError", e);
        }

        return result;
    }

    /**
     * @describe: 记录异常操作请求信息
     */
    @AfterThrowing(pointcut = "methodCachePointcut()", throwing = "e")
    public void doException(JoinPoint joinPoint, Throwable e) throws Exception {
        final SysLog sysLog = new SysLog();
        // 基础日志信息
        setBaseLogInfo(joinPoint, sysLog, PayUserDetails.getCurrentUserDetails());
        sysLog.setOptResInfo(e instanceof BizException ? e.getMessage() : "请求异常");
        scheduledThreadPool.execute(() -> sysLogService.save(sysLog));
    }

    /**
     * 获取方法中的中文备注
     */
    public static String getAnnotationRemark(JoinPoint joinPoint) throws Exception {

        Signature sig = joinPoint.getSignature();
        Method m = joinPoint.getTarget().getClass().getMethod(joinPoint.getSignature().getName(), ((MethodSignature) sig).getParameterTypes());

        MethodLog methodCache = m.getAnnotation(MethodLog.class);
        if (methodCache != null) {
            return methodCache.remark();
        }
        return "";
    }

    /**
     * @describe: 日志基本信息 公共方法
     */
    private void setBaseLogInfo(JoinPoint joinPoint, SysLog sysLog, PayUserDetails userDetails) throws Exception {
        // 使用point.getArgs()可获取request，仅限于spring MVC参数包含request，改为通过contextHolder获取。
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        //请求参数
        sysLog.setOptReqParam(requestKitBean.getReqParamJSON().toJSONString());

        //注解备注
        sysLog.setMethodRemark(getAnnotationRemark(joinPoint));
        //包名 方法名
        String methodName = joinPoint.getSignature().getName();
        String packageName = joinPoint.getThis().getClass().getName();
        if (packageName.indexOf("$$EnhancerByCGLIB$$") > -1 || packageName.indexOf("$$EnhancerBySpringCGLIB$$") > -1) { // 如果是CGLIB动态生成的类
            packageName = packageName.substring(0, packageName.indexOf("$$"));
        }
        sysLog.setMethodName(packageName + "." + methodName);
        sysLog.setReqUrl(request.getRequestURL().toString());
        sysLog.setUserIp(requestKitBean.getClientIp());
        sysLog.setCreatedAt(new Date());
        sysLog.setSysType(CS.SYS_TYPE.MCH);

        if (userDetails != null) {
            sysLog.setUserId(PayUserDetails.getCurrentUserDetails().getSysUser().getSysUserId());
            sysLog.setUserName(PayUserDetails.getCurrentUserDetails().getSysUser().getRealname());
            sysLog.setSysType(PayUserDetails.getCurrentUserDetails().getSysUser().getSysType());
        }
    }

}
