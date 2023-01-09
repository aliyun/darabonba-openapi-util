# -*- coding: utf-8 -*-
# This file is auto-generated, don't edit it. Thanks.
from Tea.model import TeaModel


class SourceModelUrlListObject(TeaModel):
    def __init__(self, url_object=None):
        self.url_object = url_object  # type: READABLE

    def validate(self):
        pass

    def to_map(self):
        _map = super(SourceModelUrlListObject, self).to_map()
        if _map is not None:
            return _map

        result = dict()
        if self.url_object is not None:
            result['url'] = self.url_object
        return result

    def from_map(self, m=None):
        m = m or dict()
        if m.get('url') is not None:
            self.url_object = m.get('url')
        return self


class SourceModel(TeaModel):
    def __init__(self, test=None, empty=None, body_object=None, list_object=None, url_list_object=None):
        self.test = test  # type: str
        self.empty = empty  # type: float
        self.body_object = body_object  # type: READABLE
        self.list_object = list_object  # type: list[READABLE]
        self.url_list_object = url_list_object  # type: list[SourceModelUrlListObject]

    def validate(self):
        if self.url_list_object:
            for k in self.url_list_object:
                if k:
                    k.validate()

    def to_map(self):
        _map = super(SourceModel, self).to_map()
        if _map is not None:
            return _map

        result = dict()
        if self.test is not None:
            result['Test'] = self.test
        if self.empty is not None:
            result['empty'] = self.empty
        if self.body_object is not None:
            result['body'] = self.body_object
        if self.list_object is not None:
            result['list'] = self.list_object
        result['urlList'] = []
        if self.url_list_object is not None:
            for k in self.url_list_object:
                result['urlList'].append(k.to_map() if k else None)
        return result

    def from_map(self, m=None):
        m = m or dict()
        if m.get('Test') is not None:
            self.test = m.get('Test')
        if m.get('empty') is not None:
            self.empty = m.get('empty')
        if m.get('body') is not None:
            self.body_object = m.get('body')
        if m.get('list') is not None:
            self.list_object = m.get('list')
        self.url_list_object = []
        if m.get('urlList') is not None:
            for k in m.get('urlList'):
                temp_model = SourceModelUrlListObject()
                self.url_list_object.append(temp_model.from_map(k))
        return self


class TargetModelUrlList(TeaModel):
    def __init__(self, url=None):
        self.url = url  # type: str

    def validate(self):
        pass

    def to_map(self):
        _map = super(TargetModelUrlList, self).to_map()
        if _map is not None:
            return _map

        result = dict()
        if self.url is not None:
            result['url'] = self.url
        return result

    def from_map(self, m=None):
        m = m or dict()
        if m.get('url') is not None:
            self.url = m.get('url')
        return self


class TargetModel(TeaModel):
    def __init__(self, test=None, empty=None, body=None, list=None, url_list=None):
        self.test = test  # type: str
        self.empty = empty  # type: float
        self.body = body  # type: READABLE
        self.list = list  # type: list[str]
        self.url_list = url_list  # type: list[TargetModelUrlList]

    def validate(self):
        if self.url_list:
            for k in self.url_list:
                if k:
                    k.validate()

    def to_map(self):
        _map = super(TargetModel, self).to_map()
        if _map is not None:
            return _map

        result = dict()
        if self.test is not None:
            result['Test'] = self.test
        if self.empty is not None:
            result['empty'] = self.empty
        if self.body is not None:
            result['body'] = self.body
        if self.list is not None:
            result['list'] = self.list
        result['urlList'] = []
        if self.url_list is not None:
            for k in self.url_list:
                result['urlList'].append(k.to_map() if k else None)
        return result

    def from_map(self, m=None):
        m = m or dict()
        if m.get('Test') is not None:
            self.test = m.get('Test')
        if m.get('empty') is not None:
            self.empty = m.get('empty')
        if m.get('body') is not None:
            self.body = m.get('body')
        if m.get('list') is not None:
            self.list = m.get('list')
        self.url_list = []
        if m.get('urlList') is not None:
            for k in m.get('urlList'):
                temp_model = TargetModelUrlList()
                self.url_list.append(temp_model.from_map(k))
        return self


