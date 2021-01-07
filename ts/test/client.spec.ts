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
});
