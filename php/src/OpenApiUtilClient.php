<?php

namespace AlibabaCloud\OpenApiUtil;

use AlibabaCloud\Tea\Model;
use AlibabaCloud\Tea\Request;
use Psr\Http\Message\StreamInterface;

/**
 * This is for OpenApi Util.
 */
class OpenApiUtilClient
{
    /**
     * Convert all params of body other than type of readable into content.
     *
     * @param Model $body    source Model
     * @param Model $content target Model
     *
     * @throws \ReflectionException
     */
    public static function convert($body, $content)
    {
        $class = new \ReflectionClass($body);
        foreach ($class->getProperties(\ReflectionProperty::IS_PUBLIC) as $property) {
            $name = $property->getName();
            if (!$property->isStatic()) {
                $value = $property->getValue($body);
                if ($value instanceof StreamInterface) {
                    continue;
                }
                $content->{$name} = $value;
            }
        }
    }

    /**
     * Get the string to be signed according to request.
     *
     * @param Request $request which contains signed messages
     *
     * @return string the signed string
     */
    public static function getStringToSign($request)
    {
        $pathname = $request->pathname ? $request->pathname : '';
        $query    = $request->query ? $request->query : [];

        $accept      = isset($request->headers['accept']) ? $request->headers['accept'] : '';
        $contentMD5  = isset($request->headers['content-md5']) ? $request->headers['content-md5'] : '';
        $contentType = isset($request->headers['content-type']) ? $request->headers['content-type'] : '';
        $date        = isset($request->headers['date']) ? $request->headers['date'] : '';

        $result = $request->method . "\n" .
            $accept . "\n" .
            $contentMD5 . "\n" .
            $contentType . "\n" .
            $date . "\n";

        $canonicalizedHeaders  = self::getCanonicalizedHeaders($request->headers);
        $canonicalizedResource = self::getCanonicalizedResource($pathname, $query);

        return $result . $canonicalizedHeaders . $canonicalizedResource;
    }

    /**
     * Get signature according to stringToSign, secret.
     *
     * @param string $stringToSign the signed string
     * @param string $secret       accesskey secret
     *
     * @return string the signature
     */
    public static function getROASignature($stringToSign, $secret)
    {
        return base64_encode(hash_hmac('sha1', $stringToSign, $secret, true));
    }

    /**
     * Parse filter into a form string.
     *
     * @param array $filter object
     *
     * @return string the string
     */
    public static function toForm($filter)
    {
        $query = $filter;
        if (null === $query) {
            return '';
        }
        if ($query instanceof Model) {
            $query = $query->toMap();
        }
        $tmp = [];
        foreach ($query as $k => $v) {
            if (0 !== strpos($k, '_')) {
                $tmp[$k] = $v;
            }
        }
        $res = self::flatten($tmp);
        ksort($res);

        return http_build_query($res);
    }

    /**
     * Get timestamp.
     *
     * @return string the timestamp string
     */
    public static function getTimestamp()
    {
        return gmdate('Y-m-d\\TH:i:s\\Z');
    }

    /**
     * Parse filter into a object which's type is map[string]string.
     *
     * @param array $filter query param
     *
     * @return array the object
     */
    public static function query($filter)
    {
        if (null === $filter) {
            return [];
        }
        $dict = $filter;
        if ($dict instanceof Model) {
            $dict = $dict->toMap();
        }
        $tmp = [];
        foreach ($dict as $k => $v) {
            if (0 !== strpos($k, '_')) {
                $tmp[$k] = $v;
            }
        }

        return self::flatten($tmp);
    }

    /**
     * Get signature according to signedParams, method and secret.
     *
     * @param array  $signedParams params which need to be signed
     * @param string $method       http method e.g. GET
     * @param string $secret       AccessKeySecret
     *
     * @return string the signature
     */
    public static function getRPCSignature($signedParams, $method, $secret)
    {
        $secret .= '&';
        $strToSign = self::getRpcStrToSign($method, $signedParams);

        $signMethod = 'HMAC-SHA1';

        return self::encode($signMethod, $strToSign, $secret);
    }

    /**
     * Parse array into a string with specified style.
     *
     * @style specified style e.g. repeatList
     *
     * @param mixed  $array  the array
     * @param string $prefix the prefix string
     * @param string $style
     *
     * @return string the string
     */
    public static function arrayToStringWithSpecifiedStyle($array, $prefix, $style)
    {
        if (null === $array) {
            return '';
        }
        if ('repeatList' === $style) {
            return self::toForm([$prefix => $array]);
        }
        if ('simple' == $style || 'spaceDelimited' == $style || 'pipeDelimited' == $style) {
            $strs = self::flatten($array);
            switch ($style) {
                case 'spaceDelimited':
                    return implode(' ', $strs);
                case 'pipeDelimited':
                    return implode('|', $strs);
                default:
                    return implode(',', $strs);
            }
        } elseif ('json' === $style) {
            return json_encode($array);
        }

        return '';
    }

    /**
     * Transform input as array.
     *
     * @param mixed $input
     *
     * @return array
     */
    public static function parseToArray($input)
    {
        self::parse($input, $result);

        return $result;
    }

    /**
     * Transform input as map.
     *
     * @param mixed $input
     *
     * @return array
     */
    public static function parseToMap($input)
    {
        self::parse($input, $result);

        return $result;
    }

    private static function getRpcStrToSign($method, $query)
    {
        ksort($query);

        $params = [];
        foreach ($query as $k => $v) {
            if (!empty($v)) {
                //对参数名称和参数值进行 URL 编码
                $k = rawurlencode($k);
                $v = rawurlencode($v);
                //对编码后的参数名称和值使用英文等号（=）进行连接
                array_push($params, $k . '=' . $v);
            }
        }
        $str = implode('&', $params);

        return $method . '&' . rawurlencode('/') . '&' . rawurlencode($str);
    }

    private static function encode($signMethod, $strToSign, $secret)
    {
        switch ($signMethod) {
            case 'HMAC-SHA256':
                return base64_encode(hash_hmac('sha256', $strToSign, $secret, true));
            default:
                return base64_encode(hash_hmac('sha1', $strToSign, $secret, true));
        }
    }

    /**
     * @param array  $items
     * @param string $delimiter
     * @param string $prepend
     *
     * @return array
     */
    private static function flatten($items = [], $delimiter = '.', $prepend = '')
    {
        $flatten = [];

        foreach ($items as $key => $value) {
            $pos = \is_int($key) ? $key + 1 : $key;
            if (\is_array($value) && !empty($value)) {
                $flatten = array_merge(
                    $flatten,
                    self::flatten($value, $delimiter, $prepend . $pos . $delimiter)
                );
            } else {
                $flatten[$prepend . $pos] = $value;
            }
        }

        return $flatten;
    }

    private static function getCanonicalizedHeaders($headers, $prefix = 'x-acs-')
    {
        ksort($headers);
        $str = '';
        foreach ($headers as $k => $v) {
            if (0 === strpos(strtolower($k), $prefix)) {
                $str .= $k . ':' . trim(self::filter($v)) . "\n";
            }
        }

        return $str;
    }

    private static function getCanonicalizedResource($pathname, $query)
    {
        if (0 === \count($query)) {
            return $pathname;
        }
        ksort($query);
        $tmp = [];
        foreach ($query as $k => $v) {
            array_push($tmp, $k . '=' . $v);
        }

        return $pathname . '?' . implode('&', $tmp);
    }

    private static function parse($input, &$output)
    {
        if (null === $input || '' === $input) {
            $output = [];
        }
        $recursive = function ($input) use (&$recursive) {
            if ($input instanceof Model) {
                $input = $input->toMap();
            } elseif (\is_object($input)) {
                $input = get_object_vars($input);
            }
            if (!\is_array($input)) {
                return $input;
            }
            $data = [];
            foreach ($input as $k => $v) {
                $data[$k] = $recursive($v);
            }

            return $data;
        };
        $output    = $recursive($input);
        if (!\is_array($output)) {
            $output = [$output];
        }
    }

    private static function filter($str)
    {
        return str_replace(["\t", "\n", "\r", "\f"], '', $str);
    }
}
