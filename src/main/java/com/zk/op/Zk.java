package com.zk.op;

import java.util.List;

import org.I0Itec.zkclient.ZkClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zk.entity.ZkData;

public class Zk {
   private static final Logger LOGGER = LoggerFactory.getLogger(Zk.class);
   private ZkClient client;

   public boolean exists(String path) {
      if (StringUtils.isBlank(path)) {
         throw new IllegalArgumentException("path can not be null or empty");
      }
      return getClient().exists(path);
   }

   public ZkData readData(String path) {
      ZkData zkdata = new ZkData();
      Stat stat = new Stat();
      zkdata.setData(getClient().readData(getPath(path), stat));
      zkdata.setStat(stat);
      return zkdata;
   }

   public List<String> getChildren(String path) {
      return getClient().getChildren(getPath(path));
   }

   public void create(String path, byte[] data) {
      path = getPath(path);
      getClient().createPersistent(path, true);
      getClient().writeData(path, data);
      LOGGER.info("create: node:{}", path);
   }

   public void edit(String path, byte[] data) {
      path = getPath(path);
      getClient().writeData(path, data);
      LOGGER.info("edit: node:{}", path);
   }

   public void delete(String path) {
      path = getPath(path);
      boolean del = getClient().delete(path);
      LOGGER.info("delete: node:{}, boolean{}:", path, del);
   }

   public void deleteRecursive(String path) {
      path = getPath(path);
      boolean deleteRecursive = getClient().deleteRecursive(path);
      LOGGER.info("rmr: node:{}, boolean{}:", path, deleteRecursive);
   }

   public Zk(String cxnString) {
      LOGGER.info("cxnString:{}", cxnString);
      this.client = ClientCacheManager.getClient(cxnString);
   }

   public ZkClient getClient() {
      return client;
   }

   public void setClient(ZkClient client) {
      this.client = client;
   }

   private String getPath(String path) {
      path = path == null ? "/" : path.trim();
      if (!StringUtils.startsWith(path, "/")) {
         path = "/" + path;
      }
      return path;
   }

}
