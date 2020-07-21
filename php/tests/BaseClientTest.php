<?php

namespace AlibabaCloud\ApiGateway\Util\Tests;

use AlibabaCloud\ApiGateway\Util\BaseClient;
use AlibabaCloud\Tea\Request;
use PHPUnit\Framework\TestCase;

class BaseClientTest extends TestCase
{
    private $client;

    public function setUp()
    {
        parent::setUp();
        $this->client = new BaseClient();
    }

    public function testGetSignature()
    {
        $request           = new Request();
        $request->method   = "";
        $request->pathname = "";
        $request->query    = [
            "sdk" => "apigateway"
        ];
        $request->headers  = [
            "baseclient" => "go"
        ];
        $this->assertEquals("h3zZzWDRJ+OiWSlhFl1YKhOvk5hOfxxOVIeH9kV86vw=", $this->client->getSignature($request, ""));
    }

    public function testToQuery()
    {
        $query = [
            "test" => "ok",
            "null" => null
        ];
        $res   = $this->client->toQuery($query);
        $this->assertEquals("ok", $res["test"]);
        $this->assertFalse(isset($res["null"]));
    }

    public function testIsFail()
    {
        $this->assertFalse($this->client->isFail(200));
        $this->assertTrue($this->client->isFail(400));
    }

    public function testGetContentMD5()
    {
        $this->assertEquals("b969h28MOfCVGrra1smdCg==", $this->client->getContentMD5('{"test":"ok"}'));
    }
}