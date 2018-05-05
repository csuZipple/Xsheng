package zippler.cn.xs.entity;

import java.sql.Timestamp;

/**
 * Created by Zipple on 2018/5/5.
 * The video will return from the cloud
 */
public class Video {
    private int id;
    private User user;
    private String name;
    private String url;
    private String desc;
    private Timestamp deployed;
    private int favorite;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Timestamp getDeployed() {
        return deployed;
    }

    public void setDeployed(Timestamp deployed) {
        this.deployed = deployed;
    }

    public int getFavorite() {
        return favorite;
    }

    public void setFavorite(int favorite) {
        this.favorite = favorite;
    }
}
