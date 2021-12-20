package com.zk.op;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:zhao___li@163.com">清汤白面<a/>
 * @description
 * @date 2021-12-17 12:35
 */
public class ZookeeperWatcher implements Watcher {

    private static final Logger log = LoggerFactory.getLogger(ZookeeperWatcher.class);

    private final CountDownLatch connected = new CountDownLatch(1);

    private final String cnxs;

    private ZooKeeper client;

    private long sessionId;

    private byte[] sessionPasswd;

    public ZookeeperWatcher(String cnxs) {
        this.cnxs = cnxs;
        this.client = initClient();
    }

    public ZooKeeper.States getState() {
        return this.client.getState();
    }

    private ZooKeeper initClient() {
        ZooKeeper zooKeeper = null;
        try {
            zooKeeper = new ZooKeeper(cnxs, 5000, this, sessionId, sessionPasswd);
            // 用于Session 复用，确保连接上一次的 Session
            this.sessionId = zooKeeper.getSessionId();
            this.sessionPasswd = zooKeeper.getSessionPasswd();
        } catch (IOException e) {
            log.error("zookeeper 连接:[{}]失败:[{}]", this.cnxs, e.getMessage(), e);
        }
        try {
            // 等待连接真正创建完成
            this.connected.await();
        } catch (InterruptedException e) {
            log.error("Zookeeper session established.");
        }
        return zooKeeper;
    }

    @Override
    public void process(WatchedEvent event) {
        try {
            Event.KeeperState state = event.getState();
            if (state == Event.KeeperState.Disconnected) {
                log.error("当前 Zookeeper 连接已断开");
                this.client = initClient();
            }
            if (Event.KeeperState.SyncConnected == state) {
                // 等待连接成功
                this.connected.countDown();
            }
            if (event.getType() == Event.EventType.NodeDataChanged) {
                log.info("Zookeeper 节点:[{}]数据发生改变,新数据为:[{}]", event.getPath(), getClient().getData(event.getPath(), true, new Stat()));
            }
            if (event.getType() == Event.EventType.NodeDeleted) {
                log.info("Zookeeper 节点:[{}]被删除", event.getPath());
            }
            if (event.getType() == Event.EventType.NodeChildrenChanged) {
                log.info("Zookeeper 节点:[{}] 子节点目录为:[{}]", event.getPath(), getClient().getChildren(event.getPath(), true));
            }
            if (event.getType() == Event.EventType.NodeCreated) {
                log.info("Zookeeper 节点:[{}]目录被创建", event.getPath());
            }
        } catch (KeeperException | InterruptedException e) {
            log.error("获取数据异常:[{}]", e.getMessage(), e);
        }
    }

    public ZooKeeper getClient() {
        return client;
    }

    public void setClient(ZooKeeper client) {
        this.client = client;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public byte[] getSessionPasswd() {
        return sessionPasswd;
    }

    public void setSessionPasswd(byte[] sessionPasswd) {
        this.sessionPasswd = sessionPasswd;
    }

    /**
     * 异步读取数据回调
     */
    class DataCallBack implements AsyncCallback.DataCallback {

        @Override
        public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {

        }
    }
}
