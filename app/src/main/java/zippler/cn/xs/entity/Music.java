package zippler.cn.xs.entity;

/**
 * Created by Zipple on 2018/5/6.
 * Music entity ,DTO
 */
public class Music {
     private int id;
     private String name;
     private String desc;
     private String url;
     private String localStorageUrl;
     private String poster;//default poster url
     private String length;

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

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLocalStorageUrl() {
        return localStorageUrl;
    }

    public void setLocalStorageUrl(String localStorageUrl) {
        this.localStorageUrl = localStorageUrl;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }
}
