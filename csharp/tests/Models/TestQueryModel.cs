using System;
using System.Collections.Generic;
using System.Text;
using Tea;

namespace tests.Models
{
    public class TestQueryModel : TeaModel
    {
        public class TestQueryModelItems : TeaModel
        {
            [NameInMap("Description")]
            [Validation(Required = false)]
            public string Description
            {
                get;
                set;
            }

            [NameInMap("ItemId")]
            [Validation(Required = false)]
            public int? ItemId
            {
                get;
                set;
            }

        }

        [NameInMap("ItemGroupId")]
        [Validation(Required = false)]
        public string ServerGroupId
        {
            get;
            set;
        }

        [NameInMap("Items")]
        [Validation(Required = false)]
        public List<TestQueryModelItems> Items
        {
            get;
            set;
        }
    }
}
