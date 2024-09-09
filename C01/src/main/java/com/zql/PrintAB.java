package com.zql;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class PrintAB {
//    private static final AtomicInteger index = new AtomicInteger(0); // 线程安全的计数器
//    private static final int n = 10000; // 打印次数
//    private static final Object lock = new Object(); // 锁对象
    public static void main(String[] args) {

        printAB(100);
    }

    private static void printAB(int n){

        AtomicInteger flag = new AtomicInteger(0);
        AtomicInteger count = new AtomicInteger(0);
        Thread threadA = new Thread(() -> {
            while (count.get() < n) {
                if (flag.get() == 0) {
                    System.out.print(Thread.currentThread().getName() + "A");
                    flag.set(1);
                }
        }
        });

        Thread threadB = new Thread(() -> {
            while (count.get() < n) {
                if (flag.get() == 1) {
                    System.out.print(Thread.currentThread().getName()+"B");
                    flag.set(2);
                }
            }
        });

        Thread threadC = new Thread(() -> {
            while (true) {
                if (flag.get() == 2) {
                    System.out.println(Thread.currentThread().getName()+"C ");
                    if (count.incrementAndGet() < n) {
                        flag.set(0);
                    } else {
                        break;
                    }
                }
            }
        });
        threadA.start();
        threadB.start();
        threadC.start();
        try {
            threadA.join(); //使用 start() 启动线程，并使用 join() 确保主线程在两个子线程完成之后才结束。
            threadB.join();
            threadC.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void printAB1(int n) {
        AtomicInteger index = new AtomicInteger(0); // 线程安全的计数器
        Object lock = new Object();// 锁对象
        Thread threadA = new Thread(() -> {
            while (index.get() < n) {
                synchronized (lock) {
                    if (index.get() % 3 == 0) {
                        System.out.print("A");
                        index.incrementAndGet(); // 递增并获取新的值
                        lock.notifyAll(); // 唤醒其他线程
                    } else {
                        try {
                            lock.wait(); // 等待其他线程完成
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
        });

        Thread threadB = new Thread(() -> {
            while (index.get() < n) {
                synchronized (lock) {
                    if (index.get() % 3 == 1) {
                        System.out.print("B");
                        index.incrementAndGet(); // 递增并获取新的值
                        lock.notifyAll(); // 唤醒其他线程
                    } else {
                        try {
                            lock.wait(); // 等待其他线程完成
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
        });

        Thread threadC = new Thread(() -> {
            while (index.get() < n) {
                synchronized (lock) {
                    if (index.get() % 3 == 2) {
                        System.out.print("C");
                        index.incrementAndGet(); // 递增并获取新的值
                        lock.notifyAll(); // 唤醒其他线程
                    } else {
                        try {
                            lock.wait(); // 等待其他线程完成
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
        });


        threadA.start();
        threadB.start();
        threadC.start();

        try {
            threadA.join(); //使用 start() 启动线程，并使用 join() 确保主线程在两个子线程完成之后才结束。
            threadB.join();
            threadC.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    private static void printAB2(int n) {
        AtomicInteger index = new AtomicInteger(0);
        ReentrantLock lock = new ReentrantLock();
        Condition conditionA = lock.newCondition();
        Condition conditionB = lock.newCondition();
        Condition conditionC = lock.newCondition();
        Thread threadA = new Thread(() -> {
            while (index.get() < n) {
                lock.lock();//获取锁
                try {
                    while (index.get() % 3 != 0) {
                        conditionA.await(); // 等待条件A满足
                    }
                    System.out.print("A");
                    index.incrementAndGet(); // 递增并获取新的值
                    conditionB.signal();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock();
                }
            }
        });

        Thread threadB = new Thread(() -> {
            while (index.get() < n) {
                lock.lock();//获取锁
                try {
                    while (index.get() % 3 != 1) {
                        conditionB.await(); // 等待条件A满足
                    }
                    System.out.print("B");
                    index.incrementAndGet(); // 递增并获取新的值
                    conditionC.signal();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock();
                }
            }
        });

        Thread threadC = new Thread(() -> {
            while (index.get() <= n) {
                lock.lock();//获取锁
                try {
                    while (index.get() % 3 != 2) {
                        conditionC.await(); // 等待条件A满足
                    }
                    System.out.print("C");
                    index.incrementAndGet(); // 递增并获取新的值
                    conditionA.signal();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock();
                }
            }
        });
        threadA.start();
        threadB.start();
        threadC.start();
    }

    private static void printAB3(int n) {
        ReentrantLock lock = new ReentrantLock();
        Condition conditionA = lock.newCondition();
        Condition conditionB = lock.newCondition();
        Condition conditionC = lock.newCondition();
        AtomicInteger index = new AtomicInteger(0);

        // 创建自定义线程池
        int q = 10;
        ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(q);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                3,
                3,
                1L,
                TimeUnit.MILLISECONDS,
                queue,
                new CustomerThread1(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        Runnable taskA = () -> {
            for (int i = 0; i < n; i++) {
                lock.lock();
                try {
                    while (index.get() % 3 != 0) {
                        conditionA.await();
                    }
                    System.out.print(Thread.currentThread().getName() + "A");
                    index.incrementAndGet();
                    conditionB.signal();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock();
                }
            }
        };

        Runnable taskB = () -> {
            for (int i = 0; i < n; i++) {
                lock.lock();
                try {
                    while (index.get() % 3 != 1) {
                        conditionB.await();
                    }
                    System.out.print(Thread.currentThread().getName() + "B");
                    index.incrementAndGet();
                    conditionC.signal();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock();
                }
            }
        };

        Runnable taskC = () -> {
            for (int i = 0; i < n; i++) {
                lock.lock();
                try {
                    while (index.get() % 3 != 2) {
                        conditionC.await();
                    }
                    System.out.println(Thread.currentThread().getName() + "C");
                    index.incrementAndGet();
                    conditionA.signal();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock();
                }
            }
        };

        //提交任务
        executor.submit(taskA);
        executor.submit(taskB);
        executor.submit(taskC);

        executor.shutdown(); // 关闭线程池


    }

    static class CustomerThread1 implements ThreadFactory{
        private final AtomicInteger count = new AtomicInteger(1);
        private final String namePrefix = "Thread-";
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, namePrefix + count.incrementAndGet());
        }
    }

    private static void printABC(int n) {

        ReentrantLock lock = new ReentrantLock();
        Condition conditionA = lock.newCondition();
        Condition conditionB = lock.newCondition();
        Condition conditionC = lock.newCondition();
        AtomicInteger index = new AtomicInteger(0);
        // 创建自定义线程池
        // 创建一个 ArrayBlockingQueue，设置容量为 10
        int queueCapacity = 10; // 队列容量
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(queueCapacity);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                3, // core pool size
                3, // maximum pool size
                0L, // keep-alive time
                TimeUnit.MILLISECONDS, // keep-alive time unit
                //new LinkedBlockingQueue<>(), // work queue
                workQueue,
                new CustomThreadFactory(), // custom thread factory
                new ThreadPoolExecutor.CallerRunsPolicy() // rejection policy
        );

        // 定义任务
        Runnable taskA = () -> {
            for (int i = 0; i < n; i++) {
                lock.lock(); // 获取锁
                try {
                    while (index.get() % 3 != 0) {
                        conditionA.await(); // 等待条件A满足
                    }
                    System.out.print(Thread.currentThread().getName() + " A ");
                    index.incrementAndGet(); // 更新计数器
                    conditionB.signal(); // 唤醒线程B
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock(); // 释放锁
                }
            }
        };

        Runnable taskB = () -> {
            for (int i = 0; i < n; i++) {
                lock.lock(); // 获取锁
                try {
                    while (index.get() % 3 != 1) {
                        conditionB.await(); // 等待条件B满足
                    }
                    System.out.print(Thread.currentThread().getName() + " B ");
                    index.incrementAndGet(); // 更新计数器
                    conditionC.signal(); // 唤醒线程C
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock(); // 释放锁
                }
            }
        };

        Runnable taskC = () -> {
            for (int i = 0; i < n; i++) {
                lock.lock(); // 获取锁
                try {
                    while (index.get() % 3 != 2) {
                        conditionC.await(); // 等待条件C满足
                    }
                    System.out.print(Thread.currentThread().getName() + " C ");
                    index.incrementAndGet(); // 更新计数器
                    conditionA.signal(); // 唤醒线程A
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock(); // 释放锁
                }
            }
        };

        // 提交任务到自定义线程池
        executor.submit(taskA);
        executor.submit(taskB);
        executor.submit(taskC);

        executor.shutdown(); // 关闭线程池

        try {
            // 等待线程池中的所有任务完成
            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                executor.shutdownNow(); // 如果任务在1分钟内没有完成，强制关闭
            }
        } catch (InterruptedException e) {
            executor.shutdownNow(); // 如果当前线程被中断，强制关闭
            Thread.currentThread().interrupt();
        }
    }

    // 自定义线程工厂
    static class CustomThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix = "custom-thread-";

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, namePrefix + threadNumber.getAndIncrement());
//            System.out.println("Creating new thread: " + t.getName()); // 打印线程名称
            return t;
        }
    }
}
