package com.zk.test.read;

import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zk.entity.ZkData;
import com.zk.op.Zk;

public class ZkReaderTest {

   private static final Logger LOGGER = LoggerFactory.getLogger(ZkReaderTest.class);
   private static Zk reader;

   @Test
   public void testExists() {
      boolean exist = reader.exists("/tops");
      LOGGER.info("exist:{}", exist);
   }

   @Test
   public void testReadData() {
      ZkData zkData = reader.readData("/tops");
      LOGGER.info("zkData:{}", zkData);
   }

   @Test
   public void testGetChildren() {
      List<String> a = reader.getChildren("/tops");
      for (String s : a) {
         LOGGER.info("child:{}", s);
      }
   }

   @Test
   public void testGetClient() {
      Assert.assertNotNull(reader);
   }

   @BeforeClass
   public static void initReader() {
      reader = new Zk("10.0.4.160:2181,10.0.4.160:3181,10.0.4.160:4181");
   }

}
