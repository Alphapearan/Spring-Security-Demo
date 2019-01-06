package com.neo.app.domain;

import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 权限
 * Created by 杨颖 on 2019/1/5.
 */
@Entity
public class Authority implements GrantedAuthority {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String name; // 权限名称

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取权限名称
     */
    @Override
    public String getAuthority() {
        return name;
    }

    @Override
    public String toString() {
        return "Authority{\'" + name + "\'}";
    }
}
