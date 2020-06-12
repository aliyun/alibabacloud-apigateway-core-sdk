'use strict';

import client from "../src/client";
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
  req.protocol = "http";
  req.method = "POST";
  req.pathname = `/api/openapi/v1/data/component/edit`;
  req.headers = {
    host: '127.0.0.1:8848',
    date: 'Mon, 10 Feb 2020 15:19:09 GMT',
    'content-type': 'application/json',
    'x-ca-timestamp': '1581347949491',
    'x-ca-nonce': '8214465f-3d47-4549-849b-b4cc2ffd25e4',
    'x-ca-key': 'appKey',
    accept: "application/json",
    'x-ca-stage': 'RELEASE',
    token: 'token',
  };
  req.body = new $tea.BytesReadable(JSON.stringify(bodyData));




  it('isFail should ok', async function () {
    const res = await request('http://127.0.0.1:8848', { method: 'GET' });
    const teaRes = new $tea.Response(res);
    assert.strictEqual(await client.isFail(teaRes.statusCode), false);
  });

  it('getContentMD5 should ok', async function () {
    assert.ok(client.getContentMD5(JSON.stringify(bodyData)));
  });

  it('getTimestamp should ok', async function () {
    const timestamp = client.getTimestamp();
    assert.strictEqual(timestamp.length, 13);
  });

  it('getSignature should ok', async function () {
    assert.ok(client.getSignature(req, 'appSecret'));
    req.pathname = '/api/openapi/v1/data/component/edit?novalue&test=test';
    req.query = {
      test: 'test',
      novalue: undefined
    };
    assert.ok(client.getSignature(req, 'appSecret'));
    req.headers['accept'] = '*/*';
    req.headers['content-md5'] = 'Q2hlY2sgSW50ZWdyaXR5IQ==';
    req.headers['content-type'] = 'application/json;charset=UTF-8';
    assert.ok(client.getSignature(req, 'appSecret'));
    req.headers['accept'] = undefined;
    req.headers['content-md5'] = undefined;
    req.headers['content-type'] = undefined;
    assert.ok(client.getSignature(req, 'appSecret'));

  });

  it('toQuery should ok', async function () {
    assert.deepStrictEqual(client.toQuery({
      boolean: true,
      string: 'stirng&com',
      number: 1,
      undef: undefined,
      null: null,
      obj: {
        boolean: true,
        string: 'stirng&cominner1',
        number: 1,
        obj: {
          boolean: true,
          string: 'stirng&cominner2',
          number: 1,
        }
      }
    }), {
      boolean: 'true',
      string: 'stirng&com',
      number: '1',
      'obj.boolean': 'true',
      'obj.string': 'stirng&cominner1',
      'obj.number': '1',
      'obj.obj.boolean': 'true',
      'obj.obj.string': 'stirng&cominner2',
      'obj.obj.number': '1',
    });
    assert.deepStrictEqual(client.toQuery(undefined), {});
    assert.deepStrictEqual(client.toQuery(null), {});
  });

  after(() => {
    server.close();
  });
});
