package com.project.peng.photohelper;

/**
 * Describe：
 * auther：  zhangshaopeng
 * Emile：   1377785991@qq.com
 * Date：    2018/6/18
 */

public class FolderBean {
    String dir;
    String firstImagePath;
    String name;
    int count;

    public String getDir() {
        return dir;
    }

    public String getFirstImagePath() {
        return firstImagePath;
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }

    public void setDir(String dir) {
        this.dir = dir;
        int lastIndexOf = this.dir.lastIndexOf("/");
        this.name = this.dir.substring(lastIndexOf);

    }

    public void setFirstImagePath(String firstImagePath) {
        this.firstImagePath = firstImagePath;
    }



    public void setCount(int count) {
        this.count = count;
    }
}
