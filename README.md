Spring retry基本使用
================

## 背景介绍

在实际工作过程中，**重试**是一个经常使用的手段。比如`MQ`发送消息失败，会采取重试手段，比如工程中使用`RPC`请求外部服务,可能因为网络
波动出现超时而采取重试手段......可以看见重试操作是非常常见的一种处理问题,系统设计的手段

而在之前我们项目中处理重拾操作依赖MQ自身的重试机制,但是这种机制不是很灵活,如果某些功能没有使用MQ的话,那么就不是那么方便了,而本文介绍的
`Spring-Retry`却能够以一种很优雅的方式解决这种问题,当然目前版本的Spring-retry还不是完美的,还是有待改进的.不过已经很不错了.

## 基本使用

- 例子1

        @Configuration
        @EnableRetry
        public class Application {
        
            @Bean
            public Service service() {
                return new Service();
            }
        
        }
        
        @Service
        class Service {
            @Retryable(RemoteAccessException.class)
            public void service() {
                // ... do something
            }
            @Recover
            public void recover(RemoteAccessException e) {
               // ... panic
            }
        }
 
 - 例子2
 
        @org.springframework.stereotype.Service
        public class Service1 {
        
            @Retryable(value = {RemoteAccessException.class, RuntimeException.class},
                    maxAttempts = 2,
                    backoff = @Backoff(value = 2000))
            public void service() {
                System.out.println("do some things");
                // this exception will just trigger recover1, do not trigger recover3
                throw new RemoteAccessException("remote access exception");
                // this exception will just trigger recover2
        //        throw new RuntimeException("runtime exception");
        
        //        System.out.println("do another things");
            }
        
            // 如果使用注解的话,这个recover貌似只能写在本类中,我测试了如果将recover方法写在
            // recoverService中,好像找不到
        
            @Recover
            public void recover1(RemoteAccessException e) {
                System.out.println(e.getMessage());
                System.out.println("do recover operation1");
            }
        
            @Recover
            public void recover2(RuntimeException e) {
                System.out.println(e.getMessage());
                System.out.println("do recover operation2");
            }
        
            @Recover
            public void recover3(RemoteAccessException e) {
                System.out.println(e.getMessage());
                System.out.println("do recover operation3");
            }
        
        }
        
 - 例子3
 
        @Service
        public class Service2 {
        
            public void test(){
                final RetryTemplate retryTemplate = new RetryTemplate();
                final SimpleRetryPolicy policy = new SimpleRetryPolicy(3, Collections.<Class<? extends Throwable>, Boolean>
                        singletonMap(Exception.class, true));
                FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
                fixedBackOffPolicy.setBackOffPeriod(100);
                retryTemplate.setRetryPolicy(policy);
                retryTemplate.setBackOffPolicy(fixedBackOffPolicy);
                final RetryCallback<Object, Exception> retryCallback = new RetryCallback<Object, Exception>() {
                    public Object doWithRetry(RetryContext context) throws Exception {
                        System.out.println("do some thing");
                        //设置context一些属性,给RecoveryCallback传递一些属性
                        context.setAttribute("key1", "value1");
                        System.out.println(context.getRetryCount());
                        throw new Exception("exception");
        //                return null;
                    }
                };
        
                // 如果RetryCallback执行出现指定异常, 并且超过最大重试次数依旧出现指定异常的话,就执行RecoveryCallback动作
                final RecoveryCallback<Object> recoveryCallback = new RecoveryCallback<Object>() {
                    public Object recover(RetryContext context) throws Exception {
                        System.out.println("do recory operation");
                        System.out.println(context.getAttribute("key1"));
                        return null;
                    }
                };
        
                try {
                    final Object execute = retryTemplate.execute(retryCallback, recoveryCallback);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }



## 参考资料

- [spring-retry](https://github.com/spring-projects/spring-retry)
- [Spring retry – ways to integrate with your project](http://www.javacodegeeks.com/2014/12/spring-retry-ways-to-integrate-with-your-project.html)