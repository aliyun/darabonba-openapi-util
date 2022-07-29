package com.aliyun.openapiutil;

import com.aliyun.tea.NameInMap;
import com.aliyun.tea.TeaModel;

import java.util.List;
import java.util.Map;

public class PaserObjectTest extends TeaModel {
    @NameInMap("SubType")
    public SubType subType;
    public Map<String, String > object;

    public class MapValue extends TeaModel {
        @NameInMap("MapValueString")
        public String test = "string";
    }

    public class ListValue extends TeaModel {
        @NameInMap("Map")
        public Map<String, MapValue> map;
    }

    public class SubType extends TeaModel {
        @NameInMap("List")
        public List<ListValue> list;
    }
}







