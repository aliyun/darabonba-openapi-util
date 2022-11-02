package com.aliyun.openapiutil;

import com.aliyun.tea.NameInMap;
import com.aliyun.tea.TeaModel;

import java.io.InputStream;
import java.util.List;

public class TargetClass extends TeaModel {
    @NameInMap("Test")
    public String test;
    @NameInMap("empty")
    public String empty;
    @NameInMap("body")
    public InputStream body;
    @NameInMap("list")
    public List<String> list;
    @NameInMap("urlList")
    public List<UrlList> urlList;

    public static class UrlList extends TeaModel {
        @NameInMap("url")
        public String url;
    }
}