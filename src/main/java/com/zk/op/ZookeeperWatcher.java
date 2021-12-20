package com.zk.op;

import java.io.IOException;
import lombok.Data;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:zhao___li@163.com">清汤白面<a/>
 * @description
 * @date 2021-12-17 12:35
 */
public class ZookeeperWatcher implements Watcher {

    private static final Logger log = LoggerFactory.getLogger(ZookeeperWatcher.class);

    private final String cnxs;

    private ZooKeeper client;

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
            zooKeeper = new ZooKeeper(cnxs, 10000, this);
        } catch (IOException e) {
            log.error("zookeeper 连接:[{}]失败:[{}]", this.cnxs, e.getMessage(), e);
        }
        return zooKeeper;
    }

    @Override
    public void process(WatchedEvent event) {
        Event.KeeperState state = event.getState();
        if (state == Event.KeeperState.Disconnected) {
            log.error("当前 Zookeeper 连接已断开");
            this.client = initClient();
        }
        if (Event.KeeperState.Closed == state) {
            log.error("当前 Zookeeper 连接已关闭");
        }
    }

    public ZooKeeper getClient() {
        return client;
    }

    public void setClient(ZooKeeper client) {
        this.client = client;
    }
}
