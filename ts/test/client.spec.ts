import * as $tea from '@alicloud/tea-typescript';
import assert from 'assert';
import 'mocha';
import Client from '../src/client';

describe('Tea Util', function () {
  it('Module should ok', function () {
    assert.ok(Client);
  });

  it('covert should ok', function () {
    class SubGrant extends $tea.Model {
      grant: string;
      other: string;
      static names(): { [key: string]: string } {
        return {
          grant: 'Grant',
          other: 'Other',
        };
      }

      static types(): { [key: string]: any } {
        return {
          grant: 'string',
          other: 'string',
        };
      }

      constructor(map: { [key: string]: any }) {
        super(map);
      }
    }

    class SubGrantBak extends $tea.Model {
      grant: string;
      diff: string;
      static names(): { [key: string]: string } {
        return {
          grant: 'Grant',
          diff: 'Diff'
        };
      }

      static types(): { [key: string]: any } {
        return {
          grant: 'string',
          diff: 'string',
        };
      }

      constructor(map: { [key: string]: any }) {
        super(map);
      }
    }

    class Grant extends $tea.Model {
        subGrant: SubGrant;
        static names(): { [key: string]: string } {
          return {
            subGrant: 'SubGrant',
          };
        }

        static types(): { [key: string]: any } {
          return {
            subGrant: SubGrant,
          };
        }

        constructor(map: { [key: string]: any }) {
          super(map);
        }
    }

    class GrantBak extends $tea.Model {
      subGrant: SubGrantBak;
      static names(): { [key: string]: string } {
        return {
          subGrant: 'SubGrant',
        };
      }

      static types(): { [key: string]: any } {
        return {
          subGrant: SubGrantBak,
        };
      }

      constructor(map: { [key: string]: any }) {
        super(map);
      }
    }
    let inputModel: $tea.Model = new Grant({
      subGrant: new SubGrant({ grant: 'test', other: 'other'}),
    });
    let outputModel: $tea.Model = new GrantBak({
      subGrant: new SubGrantBak({ grant: 'test', diff: 'diff' }),
    });
    Client.convert(inputModel, outputModel);
    assert.strictEqual(outputModel.subGrant.grant, 'test');
    assert.strictEqual(outputModel.subGrant.other, undefined);
    assert.strictEqual(outputModel.subGrant.diff, 'diff');
    outputModel = new GrantBak({});
    Client.convert(inputModel, outputModel);
    assert.strictEqual(outputModel.subGrant.grant, 'test');
    assert.strictEqual(outputModel.subGrant.other, undefined);
    assert.strictEqual(outputModel.subGrant.diff, undefined);
  });

  it('getSignature', function () {
    assert.deepStrictEqual(Client.getROASignature('stringtosign', 'secret'), 'OmuTAr79tpI6CRoAdmzKRq5lHs0=');
  });

  it('toForm', function (){
    const data: { [key: string]: any } = {
      val1: 'string',
      val2: undefined,
      val3: null,
      val4: 1,
      val5: true,
      val6: {
        subval1: 'string',
        subval2: 1,
        subval3: true,
        subval4: null,
        subval5: [
          '1',
          2,
          true,
          {
            val1: 'string'
          }
        ],
      },
      val7: [
        '1',
        2,
        undefined,
        null,
        true,
        {
          val1: 'string'
        },
        [
          'substring'
        ]
      ]
    };
    assert.deepStrictEqual(Client.toForm(data), 'val1=string&val2=&val3=&val4=1&val5=true&val6.subval1=string&val6.subval2=1&val6.subval3=true&val6.subval4=&val6.subval5.1=1&val6.subval5.2=2&val6.subval5.3=true&val6.subval5.4.val1=string&val7.1=1&val7.2=2&val7.3=&val7.4=&val7.5=true&val7.6.val1=string&val7.7.1=substring');
    assert.deepStrictEqual(Client.toForm(undefined), '');
  })

  it('getStringToSign', function () {
    const request = new $tea.Request();
    request.method = 'GET';
    request.pathname = '/';
    request.headers = {
      'accept': 'application/json'
    };
    assert.deepStrictEqual(Client.getStringToSign(request), 'GET\napplication/json\n\n\n\n/');

    request.headers = {
      'accept': 'application/json',
      'content-md5': 'md5',
      'content-type': 'application/json',
      'date': 'date'
    };

    assert.deepStrictEqual(Client.getStringToSign(request), 'GET\napplication/json\nmd5\napplication/json\ndate\n/');

    request.headers = {
      'accept': 'application/json',
      'content-md5': 'md5',
      'content-type': 'application/json',
      'date': 'date',
      'x-acs-custom-key': 'any value'
    };

    assert.deepStrictEqual(Client.getStringToSign(request), 'GET\napplication/json\nmd5\napplication/json\ndate\nx-acs-custom-key:any value\n/');

    request.query = {
        'key': 'val ue with space'
    };

    assert.deepStrictEqual(Client.getStringToSign(request), 'GET\napplication/json\nmd5\napplication/json\ndate\nx-acs-custom-key:any value\n/?key=val ue with space');
  });

  it('getTimestamp should ok', async function () {
    assert.ok(Client.getTimestamp())
  });

  it('query should ok', async function () {
    const data: { [key: string]: any } = {
      val1: 'string',
      val2: undefined,
      val3: null,
      val4: 1,
      val5: true,
      val6: {
        subval1: 'string',
        subval2: 1,
        subval3: true,
        subval4: null,
        subval5: [
          '1',
          2,
          true,
          {
            val1: 'string'
          }
        ],
      },
      val7: [
        '1',
        2,
        undefined,
        null,
        true,
        {
          val1: 'string'
        },
        [
          'substring'
        ]
      ]
    };
    assert.deepStrictEqual(Client.query(data), {
      val1: 'string',
      val2: '',
      val3: '',
      val4: '1',
      val5: 'true',
      'val6.subval1': 'string',
      'val6.subval2': '1',
      'val6.subval3': 'true',
      'val6.subval4': '',
      'val6.subval5.1': '1',
      'val6.subval5.2': '2',
      'val6.subval5.3': 'true',
      'val6.subval5.4.val1': 'string',
      'val7.1': '1',
      'val7.2': '2',
      'val7.3': '',
      'val7.4': '',
      'val7.5': 'true',
      'val7.6.val1': 'string',
      'val7.7.1': 'substring',
    });
    assert.deepStrictEqual(Client.query(undefined), {});
  });

  it('getRPCSignature should ok', async function () {
    let query = {
      test: 'ok'
    }
    let sign = Client.getRPCSignature(query, '', 'accessKeySecret');
    assert.strictEqual(sign, 'jHx/oHoHNrbVfhncHEvPdHXZwHU=');
  });

  it('arrayToStringWithSpecifiedStyle should ok', async function () {
    let arr = ['ok', 'test', 2, 3];
    let str = Client.arrayToStringWithSpecifiedStyle(arr, 'instance', 'repeatList');
    assert.strictEqual(str, 'instance.1=ok&&instance.2=test&&instance.3=2&&instance.4=3');
    str = Client.arrayToStringWithSpecifiedStyle(arr, 'instance', 'json');
    assert.strictEqual(str, '["ok","test",2,3]');
    str = Client.arrayToStringWithSpecifiedStyle(arr, 'instance', 'simple');
    assert.strictEqual(str, 'ok,test,2,3');
    str = Client.arrayToStringWithSpecifiedStyle(arr, 'instance', 'spaceDelimited');
    assert.strictEqual(str, 'ok test 2 3');
    str = Client.arrayToStringWithSpecifiedStyle(arr, 'instance', 'pipeDelimited');
    assert.strictEqual(str, 'ok|test|2|3');
    str = Client.arrayToStringWithSpecifiedStyle(arr, 'instance', 'piDelimited');
    assert.strictEqual(str, '');
    str = Client.arrayToStringWithSpecifiedStyle(null, 'instance', 'json');
    assert.strictEqual(str, '');
  })

  it('parseToMap should ok', function () {
    assert.strictEqual(Client.parseToMap(null), null);
    assert.strictEqual(Client.parseToMap(1), null);
    assert.strictEqual(Client.parseToMap('1'), null);
    assert.strictEqual(Client.parseToMap(true), null);
    assert.strictEqual(
      Client.parseToMap(() => {}),
      null
    );

    let res = Client.parseToMap({ key: 'value' });
    assert.strictEqual('value', res['key']);

    class SubRequest extends $tea.Model {
      requestId: string;
      id: number;
      static names(): { [key: string]: string } {
        return {
          requestId: 'requestId',
          id: 'id',
        };
      }

      static types(): { [key: string]: any } {
        return {
          requestId: 'string',
          id: 'number',
        };
      }

      constructor(map: { [key: string]: any }) {
        super(map);
      }
    }

    const model = new SubRequest({
      requestId: 'subTest',
      id: 2,
    });
    res = Client.parseToMap(model);
    assert.strictEqual('subTest', res['requestId']);
    assert.strictEqual(2, res['id']);

    res = Client.parseToMap({
      key: 'value',
      model: model,
    });
    assert.strictEqual('value', res['key']);
    assert.strictEqual('subTest', res['model']['requestId']);
    assert.strictEqual(2, res['model']['id']);

    res = Client.parseToMap({
      model_list: [model, model, 'model'],
      model_dict: { model1: model, model2: model },
    });
    assert.strictEqual('subTest', res['model_list'][0]['requestId']);
    assert.strictEqual(2, res['model_list'][1]['id']);
    assert.strictEqual('model', res['model_list'][2]);
    assert.strictEqual('subTest', res['model_dict']['model1']['requestId']);
    assert.strictEqual(2, res['model_dict']['model2']['id']);
  });

  it('getEndpoint should ok', async function () {
    const endpoint1 = Client.getEndpoint("common.aliyuncs.com", true, "internal")
    assert.strictEqual("common-internal.aliyuncs.com", endpoint1)

    const endpoint2 = Client.getEndpoint("common.aliyuncs.com", true, "accelerate")
    assert.strictEqual("oss-accelerate.aliyuncs.com", endpoint2)

    const endpoint3 = Client.getEndpoint("common.aliyuncs.com", true, "")
    assert.strictEqual("common.aliyuncs.com", endpoint3)
  });

  it('hexEncode should ok', function() {
    const test = Buffer.from('test');
    var res = Client.hexEncode(Client.hash(test, "ACS3-HMAC-SHA256"));
    assert.strictEqual("9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08", res);

    res = Client.hexEncode(Client.hash(test, "ACS3-RSA-SHA256"));
    assert.strictEqual("9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08", res);

    res = Client.hexEncode(Client.hash(test, "ACS3-HMAC-SM3"));
    assert.strictEqual("55e12e91650d2fec56ec74e1d3e4ddbfce2ef3a65890c2a19ecf88a307e76a23", res);
  });

  it('getAuthorization should ok', function() {
    const query = {
      "test": "ok",
      "empty": ""
    };

    const headers = {
      "x-acs-test": "http",
      "x-acs-TEST": "https"
    };

    var req = new $tea.Request();
    req.query = query;
    req.headers = headers;
    req.method = "GET";

    var res = Client.getAuthorization(req, "ACS3-HMAC-SHA256", "55e12e91650d2fec56ec74e1d3e4ddbfce2ef3a65890c2a19ecf88a307e76a23", "acesskey", "secret");
    assert.strictEqual("ACS3-HMAC-SHA256 Credential=acesskey,SignedHeaders=x-acs-test,Signature=da772425f29289d3460d5fc961455d40c5e8c6afd0888b78a910c991e6a14846", res);

    req.query = undefined;
    req.headers = undefined;
    req.body = undefined;
    req.method = undefined;
    var res = Client.getAuthorization(req, "ACS3-HMAC-SHA256", "55e12e91650d2fec56ec74e1d3e4ddbfce2ef3a65890c2a19ecf88a307e76a23", "acesskey", "secret");
    assert.strictEqual("ACS3-HMAC-SHA256 Credential=acesskey,SignedHeaders=,Signature=608b887e491f3e88e85276b188a4d24e29230a559464685ccbf2dcc458e1fde8", res);
  });

  it('signatureMethod should ok', function() {
    const priKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAKzSQmrnH0YnezZ9\n" +
    "8NK50WjMuci0hgGVcSthIZOTWMIySznY9Jj1hlvek7W0uYagtFHz03BHQnHAb5Xs\n" +
    "0DZm0Sj9+5r79GggwEzTJDYEsLyFwXM3ZOIxqxL4sRg94MHsa81M9NXGHMyMvvff\n" +
    "QTn1OBVLTVz5jgJ48foMn7j7r9kRAgMBAAECgYEAnZppw3/ef2XF8Z3Mnv+iP0Zk\n" +
    "LuqiQpN8TykXK7P1/7NJ8wktlshhrSo/3jdf8axghVQsgHob2Ay8Nidugg4lsxIL\n" +
    "AUBHvfQsQp1MAWvxslsVj+ddw01MQnt8kHmC/qhok+YuNqqAGBcoD6cthRUjEri6\n" +
    "hfs599EfPs2DcWW06qECQQDfNqUUhcDQ/SQHRhfY9UIlaSEs2CVagDrSYFG1wyG+\n" +
    "PXDSMes9ZRHsvVVBmNGmtUTg/jioTU3yuPsis5s9ppbVAkEAxjTAQxv5lBBm/ikM\n" +
    "TzPShljxDZnXh6lKWG9gR1p5fKoQTzLyyhHzkBSFe848sMm68HWCX2wgIpQLHj0G\n" +
    "ccYPTQJAduMKBeY/jpBlkiI5LWtj8b0O2G2/Z3aI3ehDXQYzgLoEz0+bNbYRWAB3\n" +
    "2lpkv+AocZW1455Y+ACichcrhiimiQJAW/6L5hoL4u8h/oFq1zAEXJrXdyqaYLrw\n" +
    "aM947mVN0dDVNQ0+pw9h7tO3iNkWTi+zdnv0APociDASYPyOCyyUWQJACMNRM1/r\n" +
    "boXuKfMmVjmmz0XhaDUC/JkqSwIiaZi+47M21e9BTp1218NA6VaPgJJHeJr4sNOn\n" +
    "Ysx+1cwXO5cuZg=="

    let res = Client.signatureMethod("secret", "source", "ACS3-HMAC-SM3");
    let resStr = Client.hexEncode(res);
    assert.strictEqual("b9ff646822f41ef647c1416fa2b8408923828abc0464af6706e18db3e8553da8", resStr);

    res = Client.signatureMethod(priKey, "source", "ACS3-RSA-SHA256");
    resStr = Client.hexEncode(res);
    assert.strictEqual("a00b88ae04f651a8ab645e724949ff435bbb2cf9a37aa54323024477f8031f4e13dc948484c5c5a81ba53a55eb0571dffccc1e953c93269d6da23ed319e0f1ef699bcc9823a646574628ae1b70ed569b5a07d139dda28996b5b9231f5ba96141f0893deec2fbf54a0fa2c203b8ae74dd26f457ac29c873745a5b88273d2b3d12", resStr);
  });

  it('getEncodePath should ok', async function () {
    let str = Client.getEncodePath('/path/ test');
    assert.strictEqual(str, '/path/%20test');
    str = Client.getEncodePath('/path/#test');
    assert.strictEqual(str, '/path/%23test');
    str = Client.getEncodePath('/path/"test');
    assert.strictEqual(str, '/path/%22test');
    str = Client.getEncodePath('/path/\'test');
    assert.strictEqual(str, '/path/%27test');
  });
  
});
