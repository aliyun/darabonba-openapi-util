# -*- coding: utf-8 -*-
import datetime
import hashlib
import hmac
import base64
import copy
from urllib.parse import quote_plus, quote

from alibabacloud_tea_util.client import Client as Util
from Tea.stream import STREAM_CLASS
from Tea.model import TeaModel


class Client(object):
    """
    This is for OpenApi Util
    """
    @staticmethod
    def convert(body, content):
        """
        Convert all params of body other than type of readable into content

        :param body: source Model

        :param content: target Model

        :return: void
        """
        pros = {}
        body_map = body.to_map()
        for k, v in body_map.items():
            if not isinstance(v, STREAM_CLASS):
                pros[k] = copy.deepcopy(v)

        content.from_map(pros)

    @staticmethod
    def _get_canonicalized_headers(headers):
        canon_keys = []
        for k in headers:
            if k.startswith('x-acs-'):
                canon_keys.append(k)
        canon_keys = sorted(canon_keys)
        canon_header = ''
        for k in canon_keys:
            canon_header += '%s:%s\n' % (k, headers[k])
        return canon_header

    @staticmethod
    def _get_canonicalized_resource(pathname, query):
        if len(query) <= 0:
            return pathname
        resource = '%s?' % pathname
        query_list = sorted(list(query))
        for key in query_list:
            if query[key] is not None:
                if query[key] == '':
                    s = '%s&' % key
                else:
                    s = '%s=%s&' % (key, query[key])
                resource += s
        return resource[:-1]

    @staticmethod
    def get_string_to_sign(request):
        """
        Get the string to be signed according to request

        :param request:  which contains signed messages

        :return: the signed string
        """
        method, pathname, headers, query = request.method, request.pathname, request.headers, request.query

        accept = '' if headers.get('accept') is None else headers.get('accept')
        content_md5 = '' if headers.get('content-md5') is None else headers.get('content-md5')
        content_type = '' if headers.get('content-type') is None else headers.get('content-type')
        date = '' if headers.get('date') is None else headers.get('date')

        header = '%s\n%s\n%s\n%s\n%s\n' % (method, accept, content_md5, content_type, date)
        canon_headers = Client._get_canonicalized_headers(headers)
        canon_resource = Client._get_canonicalized_resource(pathname, query)
        sign_str = header + canon_headers + canon_resource
        return sign_str

    @staticmethod
    def get_roasignature(string_to_sign, secret):
        """
        Get signature according to stringToSign, secret

        :type string_to_sign: str
        :param string_to_sign:  the signed string

        :type secret: str
        :param secret: accesskey secret

        :return: the signature
        """
        hash_val = hmac.new(secret.encode('utf-8'), string_to_sign.encode('utf-8'), hashlib.sha1).digest()
        signature = base64.b64encode(hash_val).decode('utf-8')
        return signature

    @staticmethod
    def _object_handler(key, value, out):
        if value is None:
            return

        if isinstance(value, dict):
            dic = value
            for k, v in dic.items():
                Client._object_handler('%s.%s' % (key, k), v, out)
        elif isinstance(value, (list, tuple)):
            lis = value
            for index, val in enumerate(lis):
                Client._object_handler('%s.%s' % (key, index + 1), val, out)
        else:
            if key.startswith('.'):
                key = key[1:]
            out[key] = str(value)

    @staticmethod
    def to_form(filter):
        """
        Parse filter into a form string

        :type filter: dict
        :param filter: object

        :return: the string
        """
        result = {}
        if filter:
            Client._object_handler('', filter, result)
        return Util.to_form_string(
            Util.anyify_map_value(result)
        )

    @staticmethod
    def get_timestamp():
        """
        Get timestamp

        :return: the timestamp string
        """
        return datetime.datetime.utcnow().strftime("%Y-%m-%dT%H:%M:%SZ")

    @staticmethod
    def query(filter):
        """
        Parse filter into a object which's type is map[string]string

        :type filter: dict
        :param filter: query param

        :return: the object
        """
        out_dict = {}
        if filter:
            Client._object_handler('', filter, out_dict)
        return out_dict

    @staticmethod
    def get_rpcsignature(signed_params, method, secret):
        """
        Get signature according to signedParams, method and secret

        :type signed_params: dict
        :param signed_params: params which need to be signed

        :type method: str
        :param method: http method e.g. GET

        :type secret: str
        :param secret: AccessKeySecret

        :return: the signature
        """
        queries = signed_params.copy()
        keys = list(queries.keys())
        keys.sort()

        canonicalized_query_string = ""

        for k in keys:
            if queries[k] is not None:
                canonicalized_query_string += "&"
                canonicalized_query_string += quote(k, safe='', encoding="utf-8")
                canonicalized_query_string += "="
                canonicalized_query_string += quote(queries[k], safe='', encoding="utf-8")

        string_to_sign = ""
        string_to_sign += method
        string_to_sign += '&'
        string_to_sign += quote_plus("/", encoding="utf-8")
        string_to_sign += '&'
        string_to_sign += quote_plus(
            canonicalized_query_string[1:] if canonicalized_query_string.__len__() > 0 else canonicalized_query_string,
            encoding="utf-8")
        digest_maker = hmac.new(bytes(secret + '&', encoding="utf-8"),
                                bytes(string_to_sign, encoding="utf-8"),
                                digestmod=hashlib.sha1)
        hash_bytes = digest_maker.digest()
        signed_str = str(base64.b64encode(hash_bytes), encoding="utf-8")

        return signed_str

    @staticmethod
    def array_to_string_with_specified_style(array, prefix, style):
        """
        Parse array into a string with specified style

        :type array: any
        :param array: the array

        :type prefix: str
        :param prefix: the prefix string

        :param style: specified style e.g. repeatList

        :return: the string
        """
        if array is None:
            return ''

        if style == 'repeatList':
            return Client._flat_repeat_list({prefix: array})
        elif style == 'simple':
            return ','.join(map(str, array))
        elif style == 'spaceDelimited':
            return ' '.join(map(str, array))
        elif style == 'pipeDelimited':
            return '|'.join(map(str, array))
        elif style == 'json':
            return Util.to_jsonstring(array)
        else:
            return ''

    @staticmethod
    def _flat_repeat_list(dic):
        query = {}
        if dic:
            Client._object_handler('', dic, query)

        l = []
        q = sorted(query)
        for i in q:
            k = quote_plus(i, encoding='utf-8')
            v = quote_plus(query[i], encoding='utf-8')
            l.append(k + '=' + v)
        return '&&'.join(l)

    @staticmethod
    def parse_to_map(inp):
        """
        Transform input as map.
        """
        try:
            result = Client._parse_to_dict(inp)
            return copy.deepcopy(result)
        except TypeError:
            return

    @staticmethod
    def _parse_to_dict(val):
        if isinstance(val, dict):
            result = {}
            for k, v in val.items():
                if isinstance(v, (list, dict, TeaModel)):
                    result[k] = Client._parse_to_dict(v)
                else:
                    result[k] = v
            return result
        elif isinstance(val, list):
            result = []
            for i in val:
                if isinstance(i, (list, dict, TeaModel)):
                    result.append(Client._parse_to_dict(i))
                else:
                    result.append(i)
            return result
        elif isinstance(val, TeaModel):
            return val.to_map()
