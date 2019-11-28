package com.ctftek.player;

import com.stx.xhb.xbanner.entity.SimpleBannerInfo;

public class MyImageInfo extends SimpleBannerInfo {

    private String url;

    public MyImageInfo(String url){
        this.url = url;
    }

    @Override
    public Object getXBannerUrl() {
        return url;
    }
}
