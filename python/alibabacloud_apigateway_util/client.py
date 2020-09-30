import time
import hashlib
import base64
import hmac

MOVE_HEADERS = (
    "x-ca-signature",
    "x-ca-signature-headers",
    "accept",
    "content-md5",
    "content-type",
    "date",
    "host",
    "token",
    "user-agent"
)


class Client:
    """
    This is an util for APIGateway SDK
    """
    @staticmethod
    def get_signature(request, secret):
        """
        Get signature according to request and secret

        :param request: an obejct of $Request
        :param secret: AccessKeySecret
        :return: the signature example h3zZzWDRJ+OiWSlhFl1YKhOvk5hOfxxOVIeH9kV86vw=
        """
        accept = request.headers.get('accept')
        content_md5 = '' if request.headers.get('content-md5') is None else request.headers.get('content-md5')
        content_type = '' if request.headers.get('content-type') is None else request.headers.get('content-type')
        date = request.headers.get('date')

        # get sign header
        dic = {}
        dic.update(request.headers)
        for key in request.headers:
            if key in MOVE_HEADERS:
                dic.pop(key)
        keys = sorted(list(dic))
        sign_headers = ''
        header = ''
        for k in keys:
            val = '' if dic[k] is None else dic[k]
            sign_headers += '%s,' % k
            header += '%s:%s\n' % (k, val)
        header = header[:-1]
        sign_headers = sign_headers[:-1]
        request.headers['x-ca-signature-headers'] = sign_headers

        # get url
        dic = {}
        dic.update(request.query)
        url = request.pathname
        if dic:
            url += '?'
            for key, value in dic.items():
                if value is not None:
                    url += '%s=%s&' % (key, value)
            url = url[:-1]

        # get sign string
        string_to_sign = '%s\n%s\n%s\n%s\n%s\n%s\n%s' % (
            request.method, accept, content_md5, content_type, date, header, url
        )
        hash_val = hmac.new(secret.encode('utf-8'), string_to_sign.encode('utf-8'), hashlib.sha256).digest()
        signature = base64.b64encode(hash_val).decode('utf-8')
        return signature

    @staticmethod
    def to_query(filter):
        """
        Parse filter into a object which's type is map[string]string

        :param filter: filter query param
        :return: the object
        """
        result = {}
        if filter:
            Client._object_handler('', filter, result)
        return result

    @staticmethod
    def _object_handler(key, value, out):
        if value is None:
            return

        if isinstance(value, dict):
            dic = value
            for k, v in dic.items():
                Client._object_handler('%s.%s' % (key, k), v, out)
        elif isinstance(value, (list, tuple)):
            lis = value
            for index, val in enumerate(lis):
                Client._object_handler('%s.%s' % (key, index + 1), val, out)
        else:
            if key.startswith('.'):
                key = key[1:]
            out[key] = str(value)

    @staticmethod
    def is_fail(code):
        """
        If code is between 200 and 300, return false, or return true

        :param code: code the statuscode
        :return: the judged result
        """
        if code:
            if code < 200 or code >= 300:
                return True
        return False

    @staticmethod
    def get_content_md5(body):
        """
        Get md5 according to the body string

        :param body: the string
        :return: the md5
        """
        if not body:
            return ''
        md5 = hashlib.md5(body.encode('utf-8')).digest()
        return base64.b64encode(md5).decode('utf-8')

    @staticmethod
    def get_time_stamp():
        """
        :return: timestamp
        """
        return str(int(round(time.time() * 1000)))
