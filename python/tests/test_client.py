import unittest
import os

from alibabacloud_openapi_util.client import Client
from Tea.request import TeaRequest


class TestClient(unittest.TestCase):

    class TestConvertModel:
        def __init__(self):
            self.requestId = "test"
            self.dic = {}
            self.no_map = 1
            self.sub_model = None
            self.file = None

        def to_map(self):
            dic = {
                'requestId': self.requestId,
                'dic': self.dic,
                'no_map': self.no_map,
                'sub_model': self.sub_model,
                'file': self.file
            }
            return dic

    class TestConvertSubModel:
        def __init__(self):
            self.requestId = "subTest"
            self.id = 2

        def to_map(self):
            dic = {
                'requestId': self.requestId,
                'id': self.id
            }
            return dic

    class TestConvertMapModel:
        def __init__(self):
            self.requestId = ""
            self.extendId = 0
            self.dic = {}
            self.sub_model = None

        def to_map(self):
            dic = {
                'requestId': self.requestId,
                'dic': self.dic,
                'extendId': self.extendId,
                'sub_model': self.sub_model,
            }
            return dic

        def from_map(self, dic):
            self.requestId = dic.get("requestId") or ""
            self.extendId = dic.get("extendId") or 0
            self.dic = dic.get("dic")
            self.sub_model = dic.get("sub_model")

    def test_get_rpc_signature(self):
        query = {
            'query': 'test',
            'body': 'test'
        }
        result = Client.get_rpcsignature(query, 'GET', 'secret')
        self.assertEqual("XlUyV4sXjOuX5FnjUz9IF9tm5rU=", result)

    def test_get_timestamp(self):
        self.assertIsNotNone(Client.get_timestamp())

        self.assertIn("T", Client.get_timestamp())

        self.assertIn("Z", Client.get_timestamp())

    def test_query(self):
        result = Client.query(None)
        self.assertEqual(0, len(result))
        dic = {
            'str_test': 'test',
            'none_test': None,
            'int_test': 1
        }
        result = Client.query(dic)
        self.assertEqual('test', result.get('str_test'))
        self.assertIsNone(result.get("none_test"))
        self.assertEqual("1", result.get("int_test"))

        fl = [1, None]
        sub_dict_fl = {
            'none_test': None,
            'int_test': 2,
            'str_test': 'test'
        }
        fl.append(sub_dict_fl)
        sl = [1, None]
        fl.append(sl)
        dic['list'] = fl
        result = Client.query(dic)
        self.assertEqual("1", result.get("list.1"))
        self.assertIsNone(result.get("list.2"))
        self.assertEqual("1", result.get("int_test"))
        self.assertEqual("2", result.get("list.3.int_test"))
        self.assertIsNone(result.get("list.3.none_test"))
        self.assertEqual("test", result.get("list.3.str_test"))
        self.assertEqual("1", result.get("list.4.1"))

        sub_map_fd = {
            'none_test': None,
            'int_test': 2,
            'str_test': 'test'
        }
        fd = {
            'first_map_map': sub_map_fd,
            'first_map_list': sl,
            'none_test': None,
            'int_test': 2,
            'str_test': 'test'
        }
        dic['map'] = fd

        result = Client.query(dic)
        self.assertEqual("1", result.get("map.first_map_list.1"))
        self.assertIsNone(result.get("map.none_test"))
        self.assertEqual("2", result.get("map.int_test"))
        self.assertEqual("test", result.get("map.str_test"))
        self.assertIsNone(result.get("map.first_map_map.none_test"))
        self.assertEqual("2", result.get("map.first_map_map.int_test"))
        self.assertEqual("test", result.get("map.first_map_map.str_test"))

    def test_get_string_to_sign(self):
        request = TeaRequest()
        str_to_sign = Client.get_string_to_sign(request)
        self.assertEqual('GET\n\n\n\n\n', str_to_sign)

        request = TeaRequest()
        request.method = "POST"
        request.query = {
            'test': 'tests'
        }
        str_to_sign = Client.get_string_to_sign(request)
        self.assertEqual('POST\n\n\n\n\n?test=tests', str_to_sign)

        request = TeaRequest()
        request.headers = {
            'content-md5': 'md5',
        }
        str_to_sign = Client.get_string_to_sign(request)
        self.assertEqual('GET\n\nmd5\n\n\n', str_to_sign)

        request = TeaRequest()
        request.pathname = "Pathname"
        request.query = {
            'ccp': 'ok',
            'test': 'tests',
            'test1': ''
        }
        request.headers = {
            'x-acs-meta': 'user',
            "accept": "application/json",
            'content-md5': 'md5',
            'content-type': 'application/json',
            'date': 'date'
        }
        str_to_sign = Client.get_string_to_sign(request)
        s = 'GET\napplication/json\nmd5\napplication/json\ndate\nx-acs-meta:user\nPathname?ccp=ok&test=tests&test1'
        self.assertEqual(s, str_to_sign)

    def test_get_roa_signature(self):
        request = TeaRequest()
        str_to_sign = Client.get_string_to_sign(request)
        signature = Client.get_roasignature(str_to_sign, 'secret')
        self.assertEqual('GET\n\n\n\n\n', str_to_sign)
        self.assertEqual('XGXDWA78AEvx/wmfxKoVCq/afWw=', signature)

    def test_to_form(self):
        filter = {
            'client': 'test',
            'client1': None,
            'strs': ['str1', 'str2'],
            'tag': {
                'key': 'value'
            }
        }
        result = Client.to_form(filter)
        self.assertEqual('client=test&strs.1=str1&strs.2=str2&tag.key=value', result)

    def test_convert(self):
        module_path = os.path.dirname(__file__)
        filename = module_path + "/test.txt"
        with open(filename) as f:
            model = TestClient.TestConvertModel()
            model.dic["key"] = "value"
            model.dic["testKey"] = "testValue"
            sub_model = TestClient.TestConvertSubModel()
            model.sub_model = sub_model
            model.file = f
            map_model = TestClient.TestConvertMapModel()
            Client.convert(model, map_model)
            self.assertIsNotNone(map_model)
            self.assertEqual("test", map_model.requestId)
            self.assertEqual(0, map_model.extendId)

    def test_array_to_string_with_specified_style(self):
        array = ['ok', 'test', 2, 3]
        prefix = 'instance'
        t1 = Client.array_to_string_with_specified_style(array, prefix, 'repeatList')
        t2 = Client.array_to_string_with_specified_style(array, prefix, 'json')
        t3 = Client.array_to_string_with_specified_style(array, prefix, 'simple')
        t4 = Client.array_to_string_with_specified_style(array, prefix, 'spaceDelimited')
        t5 = Client.array_to_string_with_specified_style(array, prefix, 'pipeDelimited')
        t6 = Client.array_to_string_with_specified_style(array, prefix, 'piDelimited')
        t7 = Client.array_to_string_with_specified_style(None, prefix, 'pipeDelimited')
        self.assertEqual('instance.1=ok&&instance.2=test&&instance.3=2&&instance.4=3', t1)
        self.assertEqual('["ok", "test", 2, 3]', t2)
        self.assertEqual('ok,test,2,3', t3)
        self.assertEqual('ok test 2 3', t4)
        self.assertEqual('ok|test|2|3', t5)
        self.assertEqual('', t6)
        self.assertEqual('', t7)
