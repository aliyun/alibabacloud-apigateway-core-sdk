<?php

namespace AlibabaCloud\ApiGateway\Util;

use AlibabaCloud\Tea\Request;

class BaseClient
{
    private static $filterKey = [
        'x-ca-signature',
        'x-ca-signature-headers',
        'accept',
        'content-md5',
        'content-type',
        'date',
        'host',
        'token',
        'user-agent',
    ];

    /**
     * @param string $secret
     *
     * @throws \Exception
     *
     * @return string
     */
    public static function getSignature(Request $request, $secret)
    {
        $signedHeader = self::_getSignedHeader($request);
        $url          = self::_buildUrl($request);
        $date         = isset($request->headers['date']) ? $request->headers['date'] : '';
        $accept       = isset($request->headers['accept']) ? $request->headers['accept'] : '';
        $contentType  = isset($request->headers['content-type']) ? $request->headers['content-type'] : '';
        $contentMd5   = isset($request->headers['content-md5']) ? $request->headers['content-md5'] : '';

        $signStr = implode("\n", [
            strtoupper($request->method),
            $accept,
            $contentMd5,
            $contentType,
            $date,
            $signedHeader,
            $url,
        ]);

        return base64_encode(hash_hmac('sha256', $signStr, $secret, true));
    }

    /**
     * @param array $query
     *
     * @throws \Exception
     *
     * @return array
     */
    public static function toQuery($query)
    {
        $res = [];
        foreach ($query as $key => $val) {
            if (null !== $val) {
                $res[$key] = $val;
            }
        }

        return $res;
    }

    /**
     * @param int $code
     *
     * @throws \Exception
     *
     * @return bool
     */
    public static function isFail($code)
    {
        return $code < 200 || $code >= 300;
    }

    /**
     * @param string $body
     *
     * @throws \Exception
     *
     * @return string
     */
    public static function getContentMD5($body)
    {
        return base64_encode(md5($body, true));
    }

    /**
     * @param Request $request
     *
     * @return string
     */
    private static function _getSignedHeader($request)
    {
        $headers = [];
        foreach ($request->headers as $key => $header) {
            $headers[strtolower($key)] = $header;
        }
        $resHeaders = [];
        ksort($headers);
        $keys = [];
        foreach ($headers as $key => $val) {
            if (!\in_array($key, self::$filterKey)) {
                array_push($keys, $key);
                array_push($resHeaders, $key . ':' . $val);
            }
        }
        $request->headers['x-ca-signature-headers'] = implode(',', $keys);

        return implode("\n", $resHeaders);
    }

    /**
     * @param Request $request
     *
     * @return string
     */
    private static function _buildUrl($request)
    {
        $url   = $request->pathname ? $request->pathname : '';
        $query = $request->query;
        ksort($query);
        if (\count($query) > 0) {
            if (false === strpos($url, '?')) {
                $url .= '?';
            }
            $tmp = [];
            foreach ($query as $key => $val) {
                array_push($tmp, $key . '=' . $val);
            }
            $url .= implode('&', $tmp);
        }

        return $url;
    }
}
