package com.cec.videoplayer.module;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

/**
 * 用户信息
 */
public class User extends LitePalSupport implements Serializable {
    private int id;
    @Column(nullable = false)
    private String name;
    private String password;

    public User(){}

    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public User(String name, String password, String token) {
        this.name = name;
        this.password = password;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
