// This file is auto-generated, don't edit it
import * as $tea from '@alicloud/tea-typescript';
import { createHash, createHmac } from 'crypto';

const filterKey = ['x-ca-signature', 'x-ca-signature-headers', 'accept', 'content-md5', 'content-type', 'date', 'host', 'token', 'reflect']


function _getSignedHeader(request: $tea.Request): string {
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

function _buildUrl(pathname: string, signedParams: { [key: string]: string }): string {
  let url = pathname;
  let queryKeys = Object.keys(signedParams);
  queryKeys.sort();
  if (queryKeys.length > 0) {
    url += '?';
  }
  queryKeys.forEach((key) => {
    if (!url.endsWith('?')) {
      url += '&';
    }
    if (typeof signedParams[key] === 'undefined') {
      url += encodeURIComponent(key);
    } else {
      url += `${encodeURIComponent(key)}=${encodeURIComponent(signedParams[key])}`;
    }
  });
  return url;
}

function _notNull(obj: { [key: string]: any }): boolean {
  if (typeof obj === 'undefined' || obj == null) {
    return false;
  }
  return Object.keys(obj).length > 0
}


export default class Client {

  static getSignature(request: $tea.Request, secret: string): string {
    const signedHeader = _getSignedHeader(request);
    const url = _buildUrl(request.pathname, request.query);
    const hmac = createHmac('sha256', secret);
    hmac.update(request.method + '\n');
    hmac.update((request.headers['accept'] || '') + '\n');
    hmac.update((request.headers['content-md5'] || '') + '\n');
    hmac.update((request.headers['content-type'] || '') + '\n');
    hmac.update((request.headers['date'] || '') + '\n');
    hmac.update((signedHeader || '') + '\n');
    hmac.update(url || '');
    return hmac.digest('base64');
  }

  static getSignatureV1(request: $tea.Request, signedParams: { [key: string]: string }, secret: string): string {
    const signedHeader = _getSignedHeader(request);
    const url = _buildUrl(request.pathname, signedParams);
    const hmac = createHmac('sha256', secret);
    hmac.update(request.method + '\n');
    hmac.update((request.headers['accept'] || '') + '\n');
    hmac.update((request.headers['content-md5'] || '') + '\n');
    hmac.update((request.headers['content-type'] || '') + '\n');
    hmac.update((request.headers['date'] || '') + '\n');
    hmac.update((signedHeader || '') + '\n');
    hmac.update(url || '');
    return hmac.digest('base64');
  }

  static toQuery(filter: { [key: string]: any }, prefix: string = ''): { [key: string]: string } {
    let ret: { [key: string]: string } = {};
    if (!_notNull(filter)) {
      return ret;
    }
    Object.keys(filter).forEach(key => {
      if (typeof filter[key] === 'undefined' || filter[key] == null) {
        return;
      }
      const newKey = prefix + key;
      if (filter[key] instanceof Object) {
        const tmp = this.toQuery(filter[key], newKey + '.');
        ret = {
          ...ret,
          ...tmp,
        };
        return;
      }
      ret[newKey] = filter[key].toString();
    })
    return ret;
  }

  static isFail(code: number): boolean {
    return code < 200 || code >= 300;
  }

  static getContentMD5(body: string): string {
    const hash = createHash('md5');
    hash.update(body);
    return hash.digest('hex');
  }

  static getTimestamp(): string {
    const timestamp = new Date().getTime();
    return timestamp.toString();
  }

}
