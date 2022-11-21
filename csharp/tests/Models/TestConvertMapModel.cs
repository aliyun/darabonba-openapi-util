using System.Collections.Generic;

using Tea;

namespace tests.Models
{
    public class TestConvertMapModel : TeaModel
    {
        [NameInMap("RequestId")]
        public string RequestId { get; set; }

        [NameInMap("NoMap2")]
        public int NoMap { get; set; }

        [NameInMap("ExtendId")]
        public int ExtendId { get; set; }

        [NameInMap("Dict")]
        public Dictionary<string, object> Dict { get; set; }

        [NameInMap("SubModel")]
        public TestConvertSubModel SubModel { get; set; }

        [NameInMap("Url")]
        public string Url { get; set; }

        [NameInMap("List")]
        public List<string> list { get; set; }

        [NameInMap("UrlList")]
        public List<UrlList> urlList { get; set; }

        public class TestConvertSubModel : TeaModel
        {
            public string RequestId { get; set; }

            public int Id { get; set; }
        }

        public class UrlList : TeaModel
        {
            [NameInMap("Url")]
            public string Url { get; set; }
        }
    }
}
