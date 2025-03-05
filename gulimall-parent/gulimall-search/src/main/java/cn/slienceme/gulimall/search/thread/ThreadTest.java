package cn.slienceme.gulimall.search.thread;

import java.util.concurrent.*;

public class ThreadTest {
    public static ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("main thread start");
        /**
         * ==============================================================================================
         * 创建异步对象CompletableFuture
         * runAsync 和 supplyAsync方法
         * CompletableFuture 提供了四个静态方法来创建一个异步操作。
         * public static CompletableFuture<Void> runAsync(Runnable runnable)
         * public static CompletableFuture<Void> runAsync(Runnable runnable, Executor executor)
         * public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier)
         * public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier, Executor executor)
         *
         * CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
         *             System.out.println("当前线程ID: " + Thread.currentThread().getId());
         *             int i = 10 / 2;
         *             System.out.println("运行结果: " + i);
         *         }, executor);
         *
         * CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
         *             System.out.println("当前线程ID: " + Thread.currentThread().getId());
         *             int i = 10 / 2;
         *             System.out.println("运行结果: " + i);
         *             return i;
         *         }, executor);
         *         System.out.println(future.get());
         * ==============================================================================================
         */

        // 开启线程 完成后操作 异常后操作(==方法完成后的感知==)
        /*CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程ID: " + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果: " + i);
            return i;
        }, executor).whenComplete((result, exception) -> {  // 当异步任务执行完成以后，会回调这个方法
            System.out.println("异步任务执行完成，结果是：" + result + "，异常是：" + exception);
        }).exceptionally(throwable -> {
            // 当异步任务执行过程中发生异常，会回调这个方法
            return 10;
         });*/

        // 异步任务执行完成以后，还可以继续执行新的任务(==方法完成后的处理==)
        /*CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程ID: " + Thread.currentThread().getId());
            int i = 10 / 0;
            System.out.println("运行结果: " + i);
            return i;
        }, executor).handle((result, exception) -> {  // 当异步任务执行完成以后，会回调这个方法
            // 优势： 如果上一个任务执行成功，则result为上一步任务的结果，如果上一个任务执行失败，则result为null，exception为异常对象
            if(result != null) {
                System.out.println(result * 2);
                return 200;
            } else {
                System.out.println(exception.getMessage());
                return 500;
            }
        });*/

        /**
         * ==============================================================================================
         * 线程串行化
         * 1) thenRun: 不能获取上一步的执行结果。1个任务执行完执行2，不关心1的结果，自己也没有返回值
         * .thenRunAsync(() -> {
         *             System.out.println("任务2启动了");
         *         }, executor);
         * 2) thenAcceptAsync: 可以获取上一步的执行结果，同时也可以返回当前任务的执行结果
         * .thenAcceptAsync((result) -> {
         *             System.out.println("任务2启动了" + result);
         *         }, executor);
         * 3) thenApplyAsync: 可以获取上一步的执行结果，同时也可以返回当前任务的执行结果
         * .thenApplyAsync((result) -> {
         *             System.out.println("任务2启动了" + result);
         *             return result + 20;
         *         }, executor);
         */
        /*CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程ID: " + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果: " + i);
            return i;
        }, executor).thenApplyAsync((result) -> {
            System.out.println("任务2启动了" + result);
            return result + 20;
        }, executor);
        System.out.println(future.get());*/

        /**
         * ==============================================================================================
         * 两个都完成
         */
        /*CompletableFuture<Integer> future01 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务1线程ID: " + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("任务1运行结束: " + i);
            return i;
        }, executor);
        CompletableFuture<String> future02 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务2线程ID: " + Thread.currentThread().getId());
            System.out.println("任务2运行结束: ");
            return "hello";
        }, executor);*/

        // runAfterBothAsync不能感知前两个的结果
        /*future01.runAfterBothAsync(future02, ()->{
            System.out.println("任务1和任务2都完成了");
        },executor);*/

        // thenAcceptBothAsync可以感知前两个的结果  但是不返回数据
        /*future01.thenAcceptBothAsync(future02, (f1, f2) -> {
            System.out.println("任务1和任务2都完成了，并且感知到了结果" + f1 + " " + f2);
        }, executor);*/

        // thenCombineAsync可以感知前两个的结果，并且返回数据
        /*CompletableFuture<Integer> future = future01.thenCombineAsync(future02, (f1, f2) -> {
            System.out.println("任务1和任务2都完成了，并且感知到了结果" + f1 + " " + f2);
            return 200;
        }, executor);
        System.out.println(future.get());
        //==============================================================================================
        */


        /**
         * 两个任务，只要有一个完成，我们就执行任务3
         */
        /*CompletableFuture<String> future01 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务1线程ID: " + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("任务1运行结束: " + i);
            return "111";
        }, executor);
        CompletableFuture<String> future02 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务2线程ID: " + Thread.currentThread().getId());
            try {
                Thread.sleep(3000);
                System.out.println("任务2运行结束: ");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "hello";
        }, executor);*/

        // 两个任务，只要有一个完成，我们就执行任务3
        // runAfterEitherAsync 不接收结果 不返回值
        /*future01.runAfterEitherAsync(future02, ()-> {
            System.out.println("任务3开始执行");
        }, executor);*/

        // acceptEitherAsync 感知结果 没有返回值
        /*future01.acceptEitherAsync(future02, (res) -> {
            System.out.println("任务3开始执行，result: " + res);
        }, executor);*/

        // applyToEitherAsync 感知结果 有返回值
        /*CompletableFuture<String> stringCompletableFuture = future01.applyToEitherAsync(future02, (res) -> {
            System.out.println("任务3开始执行，result: " + res);
            return "1";
        }, executor);
        System.out.println(stringCompletableFuture);*/


        CompletableFuture<String> futureImg = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品的图片信息");
            return "hello";
        }, executor);

        CompletableFuture<String> futureAttr = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(5000);
                System.out.println("查询商品的属性信息");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "黑色256G";
        }, executor);

        CompletableFuture<String> futureDesc = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品的介绍");
            return "华为";
        }, executor);

        // 阻塞式等待 不推荐
        /*futureImg.get();futureAttr.get();futureDesc.get();*/
        //CompletableFuture<Void> allOf = CompletableFuture.allOf(futureImg, futureAttr, futureDesc);
        CompletableFuture<Object> anyOf = CompletableFuture.anyOf(futureImg, futureAttr, futureDesc);
        anyOf.get(); // 阻塞式等待
        //allOf.join();  join和get区别:
        // join()方法在任务完成之后会返回一个null
        // get()方法在任务完成之后会返回一个具体的结果值


        System.out.println("main thread end");
        System.out.println("main thread end: "+anyOf.get());
        //System.out.println("main thread end"+futureImg.get()+futureAttr.get()+futureDesc.get());
    }


    public void thread(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("main thread start");
        /**
         * 1) 继承Thread
         *         Thread01 thread = new Thread01();
         *         thread.start();// 启动线程，调用当前线程的run()方法
         *         System.out.println("main thread end");
         * 2) 实现Runnable接口
         *         Runnable02 runnable02 = new Runnable02();
         *         new Thread(runnable02).start();// 启动线程，调用当前线程的run()方法
         *         System.out.println("main thread end");
         * 3) 实现Callable接口 + FutureTask（可以拿到返回结果，可以处理异常）
         *         Callable03 callable03 = new Callable03();
         *         FutureTask<Integer> futureTask = new FutureTask<>(callable03);
         *         new Thread(futureTask).start();// 启动线程，调用当前线程的run()方法
         *         Integer i = futureTask.get(); // 获取Callable线程的返回结果 阻塞等待
         *         System.out.println("main thread end");
         * 4) 线程池
         *         给线程池直接提交任务
         *         1、创建
         *            1)、Executors
         *            2)、原生 new ThreadPoolExecutor()
         *
         * 区别：
         *          1、2不能得到返回值 3可以得到
         *          1、2、3不能控制资源
         *          4可以控制资源，性能稳定
         */
        // 前三种一般不用
        // 当前系统中池只有一两个，每个异步任务，交给线程池，让他自己去执行
        /**
         * int corePoolSize,    // 核心线程池数
         int maximumPoolSize,  // 最大线程池大小
         long keepAliveTime,  // 超时时间
         TimeUnit unit,        // 超时单位
         BlockingQueue<Runnable> workQueue,   // 阻塞队列
         ThreadFactory threadFactory,     // 线程工厂
         RejectedExecutionHandler handler   // 拒绝策略

         */
        /**
         * Creates a new {@code ThreadPoolExecutor} with the given initial
         * parameters.
         *
         * @param corePoolSize the number of threads to keep in the pool, even
         *        if they are idle, unless {@code allowCoreThreadTimeOut} is set // 核心线程数[一直存在的]
         * @param maximumPoolSize the maximum number of threads to allow in the // 最大线程数
         *        pool
         * @param keepAliveTime when the number of threads is greater than
         *        the core, this is the maximum time that excess idle threads
         *        will wait for new tasks before terminating. // 释放空闲的线程(超过核心线程的数目的)
         * @param unit the time unit for the {@code keepAliveTime} argument  // 时间单位
         * @param workQueue the queue to use for holding tasks before they are
         *        executed.  This queue will hold only the {@code Runnable}
         *        tasks submitted by the {@code execute} method. //阻塞队列 如果任务很多，多的任务会放到队列里
         * @param threadFactory the factory to use when the executor
         *        creates a new thread  // 线程的创建工厂
         * @param handler the handler to use when execution is blocked  // 拒绝策略  队列满了后，按照拒绝策略 拒绝执行任务
         *        because the thread bounds and queue capacities are reached
         * @throws IllegalArgumentException if one of the following holds:<br>
         *         {@code corePoolSize < 0}<br>
         *         {@code keepAliveTime < 0}<br>
         *         {@code maximumPoolSize <= 0}<br>
         *         {@code maximumPoolSize < corePoolSize}
         * @throws NullPointerException if {@code workQueue}
         *         or {@code threadFactory} or {@code handler} is null
         *
         *
         *
         *   工作顺序：
         *      1. 如果当前运行的线程，少于corePoolSize，则创建新线程来执行任务
         *      2. 当前运行的线程等于corePoolSize，则将任务放入队列
         *      3. 如果队列满了，当前运行的线程小于maximumPoolSize，则创建新线程来执行任务
         *      4. 如果队列满了，且当前运行的线程等于maximumPoolSize，则执行拒绝策略
         *      5. 线程池的任务队列放满了，且当前运行的线程等于maximumPoolSize，且线程池中的线程都处于工作状态，则交给拒绝策略处理
         */
        ThreadPoolExecutor executor = new ThreadPoolExecutor(5,
                200,
                10, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(100000),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());


        System.out.println("main thread end");
    }

    // 方案一
    public static class Thread01 extends Thread {
        @Override
        public void run() {
            System.out.println("当前线程ID: " + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果: " + i);
        }
    }

    // 方案二
    public static class Runnable02 implements Runnable {
        @Override
        public void run() {
            System.out.println("当前线程ID: " + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果: " + i);
        }
    }

    // 方案三
    public static class Callable03 implements Callable<Integer> {
        @Override
        public Integer call() throws Exception {
            System.out.println("当前线程ID: " + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果: " + i);
            return i;
        }
    }
}
