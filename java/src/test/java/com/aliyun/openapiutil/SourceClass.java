package com.aliyun.openapiutil;

import com.aliyun.tea.NameInMap;
import com.aliyun.tea.TeaModel;

import java.io.InputStream;
import java.util.List;

public class SourceClass extends TeaModel {
    @NameInMap("body")
    public InputStream bodyObject;
    @NameInMap("Test")
    public String test = "test";
    @NameInMap("list")
    public List<InputStream> listObject;
    @NameInMap("urlList")
    public List<UrlList> urlListObject;

    public static class UrlList extends TeaModel {
        @NameInMap("url")
        public InputStream urlObject;
    }
}
