package com.zk.op;

import com.zk.entity.ZkData;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ZkApi {

   private static final Logger log = LoggerFactory.getLogger(ZkApi.class);

   public boolean exists(String path) throws InterruptedException, KeeperException {
      if (StringUtils.isBlank(path)) {
         throw new IllegalArgumentException("path can not be null or empty");
      }
      Stat exists = getClient().exists(path, false);
      return false;
   }

   public ZkData readData(String path) throws InterruptedException, KeeperException {
      ZkData zkdata = new ZkData();
      Stat stat = new Stat();
      zkdata.setData(getClient().getData(getPath(path), false, stat));
      zkdata.setStat(stat);
      return zkdata;
   }

   public List<String> getChildren(String path) throws InterruptedException, KeeperException {
      return getClient().getChildren(getPath(path), false);
   }

   public void create(String path, byte[] data) throws InterruptedException, KeeperException {
      path = getPath(path);
      getClient().create(path, data, new ArrayList<>(), CreateMode.PERSISTENT);
      // getClient().createPersistent(path, true);
      // getClient().writeData(path, data);
      log.info("create: node:{}", path);
   }

   public void edit(String path, byte[] data) throws InterruptedException, KeeperException {
      path = getPath(path);
      Stat exists = getClient().exists(path, false);
      getClient().setData(path, data, exists.getVersion());
      // getClient().writeData(path, data);
      log.info("edit: node:{}", path);
   }

   public void delete(String path) throws InterruptedException, KeeperException {
      path = getPath(path);
      Stat exists = getClient().exists(path, false);
      getClient().delete(path, exists.getVersion());
      // log.info("delete: node:{}, boolean{}:", path, del);
   }

   public void deleteRecursive(String path) throws InterruptedException, KeeperException {
      delete(path);
   }

   public ZooKeeper getClient() {
      return ClientCacheManager.getClient();
   }


   private String getPath(String path) {
      path = path == null ? "/" : path.trim();
      if (!StringUtils.startsWith(path, "/")) {
         path = "/" + path;
      }
      return path;
   }

}
