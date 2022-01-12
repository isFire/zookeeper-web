package com.zk.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author <a href="mailto:zhao___li@163.com">清汤白面<a/>
 * @description
 * @date 2022-01-12 11:08
 */
@Data
@TableName(value = "zk_data")
public class ZkData {

    private Long id;

    private String zkName;

    private String zkUrl;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getZkName() {
        return zkName;
    }

    public void setZkName(String zkName) {
        this.zkName = zkName;
    }

    public String getZkUrl() {
        return zkUrl;
    }

    public void setZkUrl(String zkUrl) {
        this.zkUrl = zkUrl;
    }
}
