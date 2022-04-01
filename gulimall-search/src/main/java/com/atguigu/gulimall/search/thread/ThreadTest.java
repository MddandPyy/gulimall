package com.atguigu.gulimall.search.thread;

import java.util.concurrent.*;

/**
 * //可使用lambda表达式优化简写线程创建
 * //        new Thread(()->{
 * //            System.out.println(Thread.currentThread().getName()+"执行");
 * //        },"runnable线程").start();
 * //
 * //        FutureTask<String> futureTask1 = new FutureTask<>(()->{
 * //            System.out.println(Thread.currentThread().getName()+"执行");
 * //            return  "返回信息";
 * //        });
 * //
 * //        new Thread(futureTask1,"callable线程").start();
 * //        //阻塞，等未来任务处理完，再执行。
 * //        System.out.println(futureTask1.get());
 **/
public class ThreadTest {

    public static ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        /**
         * 方法一，继承thread类,异步处理，父线程不等结果，直接结束
         */
//        System.out.println("main......start.....");
//        Thread thread = new Thread01();
//        thread.setName("thread线程");
//        thread.start();
//        System.out.println("main......end.....");
//
//        Thread.sleep(1000);
//
        /**
         * 方法二，实现runnable接口,异步处理，父线程不等结果，直接结束
         */
//        System.out.println("main......start.....");
//        Runable01 runable01 = new Runable01();
//        new Thread(runable01, "runnable线程").start();
//        System.out.println("main......end.....");
//
//        Thread.sleep(1000);
//
        /**
         * 方法三，实现callable接口，需要借助一个中间类futureTask
         * futureTask，未来任务，主线程不受，单独开一个线程处理任务，在未来的某个时间节点，获取任务线程的处理结果。
         */
//        System.out.println("main......start.....");
//        FutureTask<Integer> futureTask = new FutureTask<>(new Callable01());
//        new Thread(futureTask, "callable线程").start();
//        while(!futureTask.isDone()){
//            System.out.println("wait...");
//        }
//        //异步处理，输出结果之前阻塞等待
//        System.out.println(futureTask.get());
//        //第二次直接获取第一次的处理结果，不会再进行处理
//        System.out.println(futureTask.get());
//        System.out.println("main......end.....");
//
//        Thread.sleep(1000);
//

        /**
         * 方法四，线程池获取线程，有线程池进行资源调配，防止过多线程创建导致的资源耗尽。
         *         线程池创建方法
         *         (1)executors创建 Executors.newFixedThreadPool(10);
         *         (2)原生线程池创建api  new ThreadPoolExecutor，指定相关参数。
         *         Executors中封装的创建线程池的方法，底层也是使用new ThreadPoolExecutor，只不过指定的参数不同。
         *         七个参数：核心线程数、最大线程数量、线程存活时间、存活时间单位、阻塞队列、线程创建工厂、拒绝策略
         */
//        //无返回值
//        executor.execute(new Runable01());
//        //有返回值
//        Future<Integer> submit = executor.submit(new Callable01());
//        System.out.println(submit.get());


        /**
         * CompletableFuture异步编排，对线程的执行顺序进行控制
         * CompletableFuture.runAsync 无返回值
         */

//         System.out.println("main......start.....");
//         CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
//             System.out.println("当前线程：" + Thread.currentThread().getId());
//             int i = 10 / 2;
//             System.out.println("运行结果：" + i);
//         }, executor);

        /**
         * CompletableFuture.supplyAsync 有返回值
         * whenComplete方法完成后的处理，只能感知异常无法处理，如果想处理需要使用exceptionally
         *
         * whenComplete 和 whenCompleteAsync 的区别：
         * whenComplete： 是执行当前任务的线程执行继续执行 whenComplete 的任务。
         * whenCompleteAsync： 是执行把 whenCompleteAsync 这个任务继续提交给线程池
         * 来进行执行。
         * 方法不以 Async 结尾， 意味着 Action 使用相同的线程执行， 而 Async 可能会使用其他线程
         * 执行（如果是使用相同的线程池， 也可能会被同一个线程选中执行）
         */
//         CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
//             System.out.println("当前线程：" + Thread.currentThread().getId());
//             int i = 10 / 0;
//             System.out.println("运行结果：" + i);
//             return i;
//         }, executor).whenComplete((res,exception) -> {
//             //虽然能得到异常信息，但是没法修改返回数据
//             System.out.println("异步任务成功完成了...结果是：" + res + "异常是：" + exception);
//         }).exceptionally(throwable -> {
//             //whenComplete只可以感知异常，无法处理，可以使用exceptionally感知异常并处理返回结果。
//             return 10;
//         });
//         System.out.println(future2.get());

        /**
         * CompletableFuture.supplyAsync 有返回值
         * handle方法完成后的处理，可以感知异常，并处理返回结果
         */
//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 0;
//            System.out.println("运行结果：" + i);
//            return i;
//        }, executor).handle((result, thr) -> {
//            if (result != null) {
//                return result * 2;
//            }
//            if (thr != null) {
//                System.out.println("异步任务成功完成了...结果是：" + result + "异常是：" + thr);
//                return 0;
//            }
//            return 0;
//        });
//        System.out.println(future.get());


        /**
         * 线程串行化
         * 1、thenRunL：不能获取上一步的执行结果
         * 2、thenAcceptAsync：能接受上一步结果，但是无返回值
         * 3、thenApplyAsync：能接受上一步结果，有返回值
         *
         */

//		CompletableFuture.supplyAsync(() -> {
//			System.out.println("当前线程" + Thread.currentThread().getId());
//			int i = 10 / 2;
//			System.out.println("运行结束" + i);
//			return i;
//		}, executor).thenRunAsync(() -> {
//			// thenRunAsync 不能获取执行结果
//			System.out.println("任务2启动了...");
//		},executor);

        /**
         * 使用上一步的结果 但是没有返回结果
         */
//        CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程" + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("运行结束" + i);
//            return i;
//        }, executor).thenAcceptAsync(res -> System.out.println("thenAcceptAsync获取上一步执行结果：" + res));

        /**
         * 能接受上一步的结果 还有返回值
         */
//		CompletableFuture<String> async = CompletableFuture.supplyAsync(() -> {
//			System.out.println("当前线程" + Thread.currentThread().getId());
//			int i = 10 / 2;
//			System.out.println("运行结束" + i);
//			return i;
//		}, executor).thenApplyAsync(res -> {
//			System.out.println("任务2启动了...");
//			try {
//				Thread.sleep(2000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			return "thenApplyAsync" + res;
//		});
//		System.out.println("thenApplyAsync获取结果:" + async.get());

        /**
         * 两任务合并
         */
//        CompletableFuture<Object> async1 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("任务1线程" + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("任务1结束" + i);
//            return i;
//        }, executor);
//
//        CompletableFuture<Object> async2 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("任务2线程" + Thread.currentThread().getId());
//            int i = 10 / 5;
//            try {
//                Thread.sleep(2000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            System.out.println("任务2结束" + i);
//            return "任务合并";
//        }, executor);

        /**
         * 等待任务都执行完再执行
         * runAfterBothAsync合并上面两个任务 这个不能感知结果
         *
         */
//		async1.runAfterBothAsync(async2,() ->{
//			System.out.println("任务3开始...");
//		} ,executor);

        /**
         * thenAcceptBothAsync合并上面两个任务 可以感知前面任务的结果,但是自身不能返回结果
         */
//		async1.thenAcceptBothAsync(async2,(res1, res2) -> {
//			System.out.println("任务3开始... 任务1的结果：" + res1 + "任务2的结果：" + res2);
//		},executor);

        /**
         * thenCombineAsync合并两个任何 还可以返回结果
         */
//		CompletableFuture<String> async = async1.thenCombineAsync(async2, (res1, res2) -> res1 + ":" + res2 + "-> fire", executor);
//		System.out.println("自定义返回结果：" + async.get());

        /**
         * 合并两个任务 其中任何一个完成了 ，就执行
         * runAfterEitherAsync 不感知前面任务结果，自身无返回值
         */
//		async1.runAfterEitherAsync(async2, () ->{
//			System.out.println("任务3开始...之前的结果:");
//		},executor);

        /**
         * acceptEitherAsync感知结果 自己没有返回值
         */
//		async1.acceptEitherAsync(async2, (res)-> System.out.println("任务3开始...之前的结果:" + res), executor);


        /**
         * applyToEitherAsync感知前面任务结果 自己可以返回值
         */
//		CompletableFuture<String> async = async1.applyToEitherAsync(async2, (res) -> {
//
//			System.out.println("任务3开始...之前的结果:" + res);
//			return res.toString() + "-> fire";
//		}, executor);
//		System.out.println("任务3返回的结果：" + async.get());


        /**
         * 多任务合并
         */
        CompletableFuture<String> img = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品图片信息");
            return "1.jpg";
        }, executor);

        CompletableFuture<String> attr = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("查询商品属性");
            return "麒麟990 5G  钛空银";
        }, executor);


        CompletableFuture<String> desc = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品介绍");
            return "华为";
        }, executor);

        /**
         * 等这三个都做完
         */
        CompletableFuture<Void> allOf = CompletableFuture.allOf(img, attr, desc);
//        System.out.println(allOf.get());
        allOf.join();
//
        System.out.println("main....end" + desc.get() + attr.get() + img.get());

        /**
         * 任何一个做完就执行
         */
//        CompletableFuture<Object> anyOf = CompletableFuture.anyOf(img, attr, desc);
//        anyOf.get();
//
//        System.out.println("main....end" + anyOf.get());
//        executor.shutdown();


    }

    private static void threadPool() {

        ExecutorService threadPool = new ThreadPoolExecutor(
                200,
                10,
                10L,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<Runnable>(10000),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
        );

        //定时任务的线程池
        ExecutorService service = Executors.newScheduledThreadPool(2);
    }


    public static class Thread01 extends Thread {
        @Override
        public void run() {
            System.out.println("当前线程：" + Thread.currentThread().getName());
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
        }
    }


    public static class Runable01 implements Runnable {
        @Override
        public void run() {
            System.out.println("当前线程：" + Thread.currentThread().getName());
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
        }
    }


    public static class Callable01 implements Callable<Integer> {
        @Override
        public Integer call() throws Exception {
            System.out.println("当前线程：" + Thread.currentThread().getName());
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
            // Thread.sleep(1000);
            return i;
        }
    }

}
