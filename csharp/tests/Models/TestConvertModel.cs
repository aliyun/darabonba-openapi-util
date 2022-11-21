using System.Collections.Generic;
using System.IO;

using Tea;

namespace tests.Models
{
    public class TestConvertModel : TeaModel
    {
        [NameInMap("RequestId")]
        public string RequestId { get; set; }

        [NameInMap("NoMap1")]
        public int NoMap { get; set; }

        [NameInMap("Dict")]
        public Dictionary<string, object> Dict { get; set; }

        [NameInMap("SubModel")]
        public TestConvertSubModel SubModel { get; set; }

        [NameInMap("Url")]
        public Stream UrlObject { get; set; }

        [NameInMap("List")]
        public List<Stream> ListObject { get; set; }

        [NameInMap("UrlList")]
        public List<UrlList> UrlListObject { get; set; }

        public class TestConvertSubModel : TeaModel
        {
            public string RequestId { get; set; }

            public int Id { get; set; }
        }

        public class UrlList : TeaModel
        {
            [NameInMap("Url")]
            public Stream UrlObject { get; set; }
        }
    }
}
