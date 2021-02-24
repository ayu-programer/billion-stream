package com.roncoo.eshop.cache.zookeeper;

import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * zookeeper分布式锁
 */
public class ZooKeeperSession {

   private ZooKeeper zooKeeper;

   private static CountDownLatch connectedSemaphore = new CountDownLatch(1);

   public ZooKeeperSession(){
       //去连接zookeeper  server
       // 所以要给一个监听器，说告诉我们什么时候才是真正完成了跟zk server的连接
        try {
            this.zooKeeper = new ZooKeeper(
                    "192.168.31.187:2181,192.168.31.19:2181,192.168.31.227:2181",
                    50000,
                    new ZookeeperWatch());

            //2 给一个连接状态  connecting  连接中
            System.out.println(zooKeeper.getState());

            // CountDownLatch
            // java多线程并发同步的一个工具类
            // 会传递进去一些数字，比如说1,2 ，3 都可以
            // 然后await()，如果数字不是0，那么久卡住，等待

            // 其他的线程可以调用coutnDown()，减1
            // 如果数字减到0，那么之前所有在await的线程，都会逃出阻塞的状态
            // 继续向下运行
            try {
                connectedSemaphore.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 获取分布式锁
     * @param productId
     */
    public void acquireDistributeLock(Long productId) {
        String path = "/product-lock-" + productId;
        try {
            zooKeeper.create(path, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            System.out.println("success to acquire lock for product[id=" + productId + "]");

        } catch (Exception e) {
            int count = 0;
            //如果那个商品对应的锁的node已经被别人加锁，那么这里就会报错
            while (true) {
                try {
                    Thread.sleep(20);//等待20ms
                    zooKeeper.create(path, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                } catch (Exception ex) {
                    count++;
                    continue;
                }
                System.out.println("success to acquire lock for product[id=" + productId + "] after " + count + " times try......");
                break;
            }
        }
    }

    /**
     * 释放掉一个分布式锁
     * @param productId
     */
    public void releaseDistributedLock(Long productId) {
        String path = "/product-lock-" + productId;
        try {
            zooKeeper.delete(path, -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

        /**
         * 封装单例的静态内部类
         */
        private static class Singleton {

            private static ZooKeeperSession zooKeeperSession;

            static {
                zooKeeperSession = new ZooKeeperSession();
            }

            public static ZooKeeperSession getZooKeeperSession() {
                return zooKeeperSession;
            }
        }

        /**
         * 获取单例对象
         * @return ZooKeeperSession
         */
        public static ZooKeeperSession getZookeeperSession () {
            return Singleton.getZooKeeperSession();
        }

        /**
         * 初始化单例的便捷方法
         */
        public static void init () {
            getZookeeperSession();
        }

        /**
         * 建立zk session的watch
         */
        private class ZookeeperWatch implements Watcher {

            @Override
            public void process(WatchedEvent event) {
                System.out.println("Receive watched event: " + event.getState());
                if (Event.KeeperState.SyncConnected == event.getState()) {
                    connectedSemaphore.countDown();
                }
            }
        }

}
