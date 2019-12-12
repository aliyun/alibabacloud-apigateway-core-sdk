import * as $tea from '@alicloud/tea-typescript';
import uuid from 'uuid/v4';
import { createHmac,createHash } from 'crypto';

const filterKey = [ 'x-ca-signature', 'x-ca-signature-headers', 'accept', 'content-md5', 'content-type', 'date', 'host', 'token']

export default class BaseClient {

  _regionId: string
  _protocol: string
  _domain: string
  _token: string
  _appKey: string
  _appSecret: string
  _stage: string

  constructor(config: { [key: string]: any }) {
    this._protocol = config['protocol'] || 'http';
    this._regionId = config['regionId'] || '';
    this._domain = config['domain'] || '';
    this._token = config['token'] || '';
    this._appKey = config['appKey'] || '';
    this._appSecret = config['appSecret'] || '';
  }

  _getHost(): string {
    return this._domain;
  }

  _defaultNumber(number: number, defaultNum: number): number {
    if (!number) {
      return defaultNum;
    }
    return number;
  }

  _toJSONString(body: any): string {
    return JSON.stringify(body);
  }

  _getUUID(): string {
    return uuid();
  }

  _default(value: string, defaultVal: string): string {
    if (!value) {
      return defaultVal;
    }
    return value;
  }

  _getContentMD5(body: string): string {
    const hash = createHash('md5');
    hash.update(body);
    return hash.digest('hex');
  }

  _buildUrl(request: $tea.Request): string{
    let url = request.pathname;
    let queryKeys = Object.keys(request.query);
    queryKeys.sort();
    if (queryKeys.length > 0) {
      url += '?';
    }

    queryKeys.forEach((key) => {
      if (!url.endsWith('?')) {
        url += '&';
      }
      if (typeof request.query[key] === 'undefined') {
        url += encodeURIComponent(key);
      } else {
        url += `${encodeURIComponent(key)}=${encodeURIComponent(request.query[key])}`;
      }
    });
    return url;
  }

  _getSignedHeader(request: $tea.Request): string {
    let signedHeader = '';
    let signedHeaderKeys = '';
    let headerKeys = Object.keys(request.headers);
    headerKeys.sort();

    headerKeys.forEach((key) => {
      if (~filterKey.indexOf(key)) {
        return;
      }
      signedHeaderKeys += `${key},`;
      signedHeader += `${key}:${request.headers[key]}\n`;

    });
    request.headers["x-ca-signature-headers"] = signedHeaderKeys.slice(0, -1);
    return signedHeader.slice(0, -1);
  }

  _getSignature(request: $tea.Request): string {
    const signedHeader = this._getSignedHeader(request);
    const url = this._buildUrl(request);
    // const signStr = `${request.method}\n${accept}\n${contentMd5}\n${contentType}\n${date}\n${signedHeader}\n${url}`;
    // console.log(signStr);
    const hmac = createHmac('sha256', this._appSecret);
    hmac.update(request.method + '\n');
    hmac.update((request.headers['accept'] || '') + '\n');
    hmac.update((request.headers['content-md5'] || '') + '\n');
    hmac.update((request.headers['content-type'] || '') + '\n');
    hmac.update(request.headers['date'] + '\n');
    hmac.update(signedHeader + '\n');
    hmac.update(url);
    return hmac.digest('base64');
  }

  _isStatusCode(response: $tea.Response, code: number): boolean {
    return response.statusCode === code;
  }

  async _readAsJSON(request: $tea.Response): Promise<{ [key: string]: any }> {
    let body = await request.readBytes();
    console.log(body.toString());
    body = JSON.parse(body.toString());
    return body;
  }

  _getDate(): string {
    let now = new Date();
    return now.toUTCString();
  }

  _getTimestamp(): string {
    return Date.now().toString();
  }


}
