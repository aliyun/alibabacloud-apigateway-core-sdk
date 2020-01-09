import * as $tea from '@alicloud/tea-typescript';
import uuid from 'uuid/v4';
import { stringify } from 'querystring';
import { createHmac,createHash } from 'crypto';

const filterKey = ['x-ca-signature', 'x-ca-signature-headers', 'accept', 'content-md5', 'content-type', 'date', 'host', 'token', 'reflect']

export default class BaseClient {

  _regionId: string
  _protocol: string
  _domain: string
  _token: string
  _appKey: string
  _appSecret: string
  _stage: string
  _readTimeout: number
  _connectTimeout: number
  _localAddr: string
  _httpProxy: string
  _httpsProxy: string
  _noProxy: string
  _maxIdleConns: number

  constructor(config: { [key: string]: any }) {
    this._protocol = config['protocol'] || 'http';
    this._regionId = config['regionId'] || '';
    this._domain = config['domain'] || '';
    this._token = config['token'] || '';
    this._appKey = config['appKey'] || '';
    this._appSecret = config['appSecret'] || '';
    this._readTimeout = config['readTimeout'] || 0;
    this._connectTimeout = config['connectTimeout'] || 0;
    this._localAddr = config['localAddr'] || '';
    this._httpProxy = config['httpProxy'] || '';
    this._httpsProxy = config['httpsProxy'] || '';
    this._noProxy = config['noProxy'] || '';
    this._maxIdleConns = config['maxIdleConns'] || 0;
  }

  _getHost(): string {
    return this._domain;
  }

  _isFail(resp: $tea.Response): boolean {
    return resp.statusCode < 200 || resp.statusCode >= 300;
  }

  _defaultNumber(number: number, defaultNum: number): number {
    if (!number) {
      return defaultNum;
    }
    return number;
  }

  _toForm(form: { [key: string]: any }): string {
    return stringify(form);
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

  _getContentMD5(body: { [key: string]: any }): string {
    const bodyJson = JSON.stringify(body);
    const hash = createHash('md5');
    hash.update(bodyJson);
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

  _equal(realStr: string , defaultStr: string): boolean{
    return realStr === defaultStr;
  }

  _notNull(obj: { [key: string]: any }): boolean{
    if (typeof obj === 'undefined' || obj == null) {
      return false;
    }
    return Object.keys(obj).length > 0
  }

  _toQuery(query: { [key: string]: any } , prefix: string = ''): { [key: string]: string } {
    let ret: { [key: string]: string } = {};
    if (!this._notNull(query)) {
      return ret;
    }
    Object.keys(query).forEach(key => {
      if (typeof query[key] === 'undefined' || query[key] == null) {
        return;
      }
      const newKey = prefix + key;
      if (query[key] instanceof Object) {
        const tmp = this._toQuery(query[key], newKey + '.');
        ret = {
          ...ret,
          ...tmp,
        };
        return;
      }
      ret[newKey] = query[key].toString();
    })
    return ret;
  }

}
