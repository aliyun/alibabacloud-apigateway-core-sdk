'use strict';

import BaseClient from "../src/baseclient";
import * as $tea from "@alicloud/tea-typescript";
import { createServer, Server } from 'http';
import { request } from 'httpx';
import 'mocha';
import assert from 'assert';

describe('base client', function () {
  let server: Server;
  const returnData = { ok: true, data: { testId: 1, testName: 'test' } };
  const bodyData = { testId: 1, testName: 'test' };
  before(() => {
    server = createServer((req, res) => {
      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify(returnData));
    }).listen(8848, () => {
      //
    });
  })
  let req = new $tea.Request();



  it('new client should ok', async function () {
    const config = {
      domain: 'em81',
      protocol: 'http',
      appKey: 'appKey',
      appSecret: 'appSecret',
      token: 'token',
      regionId: 'hangzhou',
    };
    const client = new BaseClient(config);
    assert.strictEqual(client._domain, config.domain);
    assert.strictEqual(client._protocol, config.protocol);
    assert.strictEqual(client._token, config.token);
    assert.strictEqual(client._appKey, config.appKey);
    assert.strictEqual(client._appSecret, config.appSecret);
    assert.strictEqual(client._regionId, config.regionId);
    req.protocol = "http";
    req.method = "POST";
    req.pathname = `/api/openapi/v1/data/component/edit`;
    req.headers = {
      host: '127.0.0.1:8848',
      date: client._getDate(),
      'content-type': 'application/json',
      'x-ca-timestamp': client._getTimestamp(),
      'x-ca-nonce': client._getUUID(),
      'x-ca-key': client._appKey,
      accept: "application/json",
      'x-ca-stage': client._default(client._stage, "RELEASE"),
      token: client._token,
    };
    req.body = new $tea.BytesReadable(client._toJSONString(bodyData));
  });

  it('_getHost should ok', async function () {
    const domain = 'em81';
    const client = new BaseClient({ domain });
    assert.strictEqual(await client._getHost(), domain);
  });

  it('_isFail should ok', async function () {
    const client = new BaseClient({});
    const res = await request('http://127.0.0.1:8848', { method: 'GET' });
    const teaRes = new $tea.Response(res);
    assert.strictEqual(await client._isFail(teaRes), false);
  });

  it('_defaultNumber should ok', async function () {
    const client = new BaseClient({});
    assert.strictEqual(client._defaultNumber(undefined, 2019), 2019);
    assert.strictEqual(client._defaultNumber(2020, 2019), 2020);
  });

  it('_toForm should ok', async function () {
    const client = new BaseClient({});
    assert.strictEqual(client._toForm({
      boolean: true,
      string: 'stirng&com',
      number: 1,
      undef: undefined,
      null: null,
    }), 'boolean=true&string=stirng%26com&number=1&undef=&null=');
    assert.strictEqual(client._toForm(undefined), '');
  });

  it('_toJSONString should ok', async function () {
    const client = new BaseClient({});
    assert.strictEqual(client._toJSONString({ test: true }), '{"test":true}');
  });

  it('_getUUID should ok', async function () {
    const client = new BaseClient({});
    assert.strictEqual(client._getUUID().length, 36);
  });

  it('_default should ok', async function () {
    const client = new BaseClient({});
    assert.strictEqual(client._default(undefined, 'default'), 'default');
    assert.strictEqual(client._default('not_default', 'default'), 'not_default');
  });


  it('_getContentMD5 should ok', async function () {
    const client = new BaseClient({
      appKey: 'appKey',
      appSecret: 'appSecret',
    });
    assert.ok(await client._getContentMD5(client._toJSONString(bodyData)));
  });

  it('_buildUrl should ok', async function () {
    const client = new BaseClient({});
    assert.strictEqual(client._buildUrl(req), '/api/openapi/v1/data/component/edit');
    req.query = {
      test: 'test',
      novalue: undefined
    };
    assert.strictEqual(client._buildUrl(req), '/api/openapi/v1/data/component/edit?novalue&test=test');
  });

  it('_getSignedHeader should ok', async function () {
    const client = new BaseClient({});
    const expectStr = 'x-ca-key:appKey\n' +
      'x-ca-nonce:9b3b741d-7525-4de1-b9e4-d7ce1a4fdd43\n' +
      'x-ca-stage:RELEASE\n' +
      'x-ca-timestamp:1575519709587';
    assert.ok(client._getSignedHeader(req));
  });

  it('_isStatusCode should ok', async function () {
    const client = new BaseClient({});
    const res = await request('http://127.0.0.1:8848', { method: 'GET' });
    const teaRes = new $tea.Response(res);
    assert.ok(client._isStatusCode(teaRes, 200));
  });

  it('_readAsJSON should ok', async function () {
    const client = new BaseClient({});
    const res = await request('http://127.0.0.1:8848', { method: 'GET' });
    const teaRes = new $tea.Response(res);
    assert.deepStrictEqual(await client._readAsJSON(teaRes), returnData);
  });

  it('_getDate should ok', async function () {
    const client = new BaseClient({});
    const date = new Date();
    assert.deepStrictEqual(client._getDate(), date.toUTCString());
  });

  it('_getTimestamp should ok', async function () {
    const client = new BaseClient({});
    assert.strictEqual(client._getTimestamp(), Date.now().toString());
  });

  it('_getSignature should ok', async function () {
    const client = new BaseClient({
      appSecret: 'appSecret'
    });
    assert.ok(client._getSignature(req));
  });

  after(() => {
    server.close();
  });
});
