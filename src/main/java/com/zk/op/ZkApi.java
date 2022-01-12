package com.zk.op;

import com.zk.dto.ZkDataDTO;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ZkApi {

   private static final Logger log = LoggerFactory.getLogger(ZkApi.class);

   public ZkDataDTO readData(String path) throws InterruptedException, KeeperException {
      ZkDataDTO zkData = new ZkDataDTO();
      Stat stat = new Stat();
      zkData.setData(getClient().getData(getPath(path), false, stat));
      zkData.setStat(stat);
      return zkData;
   }

   public List<String> getChildren(String path) throws InterruptedException, KeeperException {
      return getClient().getChildren(getPath(path), true);
   }

   public void create(String path, byte[] data) throws InterruptedException, KeeperException {
      path = getPath(path);
      getClient().create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
      log.info("create: node:{}", path);
   }

   public void edit(String path, byte[] data) throws InterruptedException, KeeperException {
      path = getPath(path);
      // 如果只需要根据最新版本更新，则设置为-1，否则需要指定版本，表示基于哪个版本进行更新
      getClient().setData(path, data, -1);
      log.info("edit: node:{}", path);
   }

   public void delete(String path) throws InterruptedException, KeeperException {
      path = getPath(path);
      Stat exists = getClient().exists(path, false);
      getClient().delete(path, exists.getVersion());
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
