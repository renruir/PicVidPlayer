package com.ctftek.player.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class SecurityWord {
    @Id(autoincrement = true)
    private long id;

    @NotNull
    private String password;

    @Generated(hash = 1336731734)
    public SecurityWord(long id, @NotNull String password) {
        this.id = id;
        this.password = password;
    }

    @Generated(hash = 1399182785)
    public SecurityWord() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
