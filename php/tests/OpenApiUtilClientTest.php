<?php

namespace AlibabaCloud\OpenApiUtil\Tests;

use AlibabaCloud\OpenApiUtil\OpenApiUtilClient;
use AlibabaCloud\Tea\Model;
use AlibabaCloud\Tea\Request;
use PHPUnit\Framework\TestCase;

/**
 * @internal
 * @coversNothing
 */
class OpenApiUtilClientTest extends TestCase
{
    public function testConvert()
    {
        $model    = new MockModel();
        $model->a = 'foo';

        $output = new MockModel();
        OpenApiUtilClient::convert($model, $output);
        $this->assertEquals($model->a, $output->a);
    }

    public function testGetStringToSign()
    {
        $request                    = new Request();
        $request->method            = 'GET';
        $request->pathname          = '/';
        $request->headers['accept'] = 'application/json';

        $this->assertEquals("GET\napplication/json\n\n\n\n/", OpenApiUtilClient::getStringToSign($request));

        $request->headers = [
            'accept'       => 'application/json',
            'content-md5'  => 'md5',
            'content-type' => 'application/json',
            'date'         => 'date',
        ];
        $this->assertEquals("GET\napplication/json\nmd5\napplication/json\ndate\n/", OpenApiUtilClient::getStringToSign($request));

        $request->headers = [
            'accept'           => 'application/json',
            'content-md5'      => 'md5',
            'content-type'     => 'application/json',
            'date'             => 'date',
            'x-acs-custom-key' => 'any value',
        ];
        $this->assertEquals("GET\napplication/json\nmd5\napplication/json\ndate\nx-acs-custom-key:any value\n/", OpenApiUtilClient::getStringToSign($request));

        $request->query = [
            'key' => 'val ue with space',
        ];
        $this->assertEquals("GET\napplication/json\nmd5\napplication/json\ndate\nx-acs-custom-key:any value\n/?key=val ue with space", OpenApiUtilClient::getStringToSign($request));
    }

    public function testGetROASignature()
    {
        $this->assertEquals('OmuTAr79tpI6CRoAdmzKRq5lHs0=', OpenApiUtilClient::getROASignature('stringtosign', 'secret'));
    }

    public function testToForm()
    {
        $this->assertEquals('client=test&strs.1=str1&strs.2=str2&tag.key=value', OpenApiUtilClient::toForm([
            'client' => 'test',
            'tag'    => [
                'key' => 'value',
            ],
            'strs'   => ['str1', 'str2'],
        ]));
    }

    public function testGetTimestamp()
    {
        $date = OpenApiUtilClient::getTimestamp();
        $this->assertEquals(20, \strlen($date));
    }

    public function testQuery()
    {
        $array = [
            'a'  => 'a',
            'b1' => [
                'a' => 'a',
            ],
            'b2' => [
                'a' => 'a',
            ],
            'c'  => ['x', 'y', 'z'],
        ];
        $this->assertEquals([
            'a'    => 'a',
            'b1.a' => 'a',
            'b2.a' => 'a',
            'c.1'  => 'x',
            'c.2'  => 'y',
            'c.3'  => 'z',
        ], OpenApiUtilClient::query($array));
    }

    public function testGetRPCSignature()
    {
        $request           = new Request();
        $request->pathname = '';
        $request->query    = [
            'query' => 'test',
            'body'  => 'test',
        ];
        $this->assertEquals('XlUyV4sXjOuX5FnjUz9IF9tm5rU=', OpenApiUtilClient::getRPCSignature($request->query, $request->method, 'secret'));
    }

    public function testArrayToStringWithSpecifiedStyle()
    {
        $data = ['ok', 'test', 2, 3];
        $this->assertEquals(
            'instance.1=ok&instance.2=test&instance.3=2&instance.4=3',
            OpenApiUtilClient::arrayToStringWithSpecifiedStyle(
                $data,
                'instance',
                'repeatList'
            )
        );

        $this->assertEquals(
            '["ok","test",2,3]',
            OpenApiUtilClient::arrayToStringWithSpecifiedStyle(
                $data,
                'instance',
                'json'
            )
        );

        $this->assertEquals(
            'ok,test,2,3',
            OpenApiUtilClient::arrayToStringWithSpecifiedStyle(
                $data,
                'instance',
                'simple'
            )
        );

        $this->assertEquals(
            'ok test 2 3',
            OpenApiUtilClient::arrayToStringWithSpecifiedStyle(
                $data,
                'instance',
                'spaceDelimited'
            )
        );

        $this->assertEquals(
            'ok|test|2|3',
            OpenApiUtilClient::arrayToStringWithSpecifiedStyle(
                $data,
                'instance',
                'pipeDelimited'
            )
        );

        $this->assertEquals(
            '',
            OpenApiUtilClient::arrayToStringWithSpecifiedStyle(
                $data,
                'instance',
                'piDelimited'
            )
        );

        $this->assertEquals(
            '',
            OpenApiUtilClient::arrayToStringWithSpecifiedStyle(
                null,
                'instance',
                'pipeDelimited'
            )
        );
    }

    public function testParseToArray()
    {
        $test     = $this->parseData();
        $data     = $test['data'];
        $expected = $test['expected'];
        foreach ($data as $index => $item) {
            $this->assertEquals($expected[$index], OpenApiUtilClient::parseToArray($item));
        }
    }

    public function testParseToMap()
    {
        $test     = $this->parseData();
        $data     = $test['data'];
        $expected = $test['expected'];
        foreach ($data as $index => $item) {
            $this->assertEquals($expected[$index], OpenApiUtilClient::parseToMap($item));
        }
    }

    private function parseData()
    {
        return [
            'data'     => [
                'NotArray',
                new ParseModel([
                    'str'   => 'A',
                    'model' => new ParseModel(['str' => 'sub model']),
                    'array' => [1, 2, 3],
                ]),
                [ // model item in array
                    new ParseModel([
                        'str' => 'A',
                    ]),
                ],
                [ // model item in map
                    'model' => new ParseModel([
                        'str' => 'A',
                    ]),
                ],
            ],
            'expected' => [
                ['NotArray'],
                [
                    'str'   => 'A',
                    'model' => [
                        'str'   => 'sub model',
                        'model' => null,
                        'array' => null,
                    ],
                    'array' => [1, 2, 3],
                ],
                [
                    [
                        'str'   => 'A',
                        'model' => null,
                        'array' => null,
                    ],
                ],
                [
                    'model' => [
                        'str'   => 'A',
                        'model' => null,
                        'array' => null,
                    ],
                ],
            ],
        ];
    }
}

class MockModel extends Model
{
    public $a = 'A';

    public $b = '';

    public $c = '';

    public function __construct()
    {
        $this->_name['a']     = 'A';
        $this->_required['c'] = true;
        parent::__construct([]);
    }
}

class ParseModel extends Model
{
    public $str;
    public $model;
    public $array;
}
