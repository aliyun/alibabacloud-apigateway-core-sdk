import unittest
from Tea.request import TeaRequest
from alibabacloud_apigateway_util.client import Client


class TestClient(unittest.TestCase):
    def test_get_signature(self):
        request = TeaRequest()
        request.method = 'test'
        request.headers['accept'] = 'json'
        request.headers['date'] = 'now'
        request.pathname = 'test'
        self.assertEqual(
            'lzttujz/sOhS2QSBBSZBq58ZNSxvSDdkWWlknQxnrt0=',
            Client.get_signature(request, 'sk')
        )
        request.headers['content-md5'] = 'md5'
        request.headers['content-type'] = 'type'
        self.assertEqual(
            'HcjhFWjGB9Xyitos6CHnJvwQoPQzdPUBgv5oUd0tdoA=',
            Client.get_signature(request, 'sk')
        )

        request = TeaRequest()
        request.headers['testKey'] = 'value'
        request.headers['testNull'] = None

        self.assertEqual('6iwWMKFM5HOmBuRm8Xb4i/bv3Zrd8eP0qSoO3iZiSG4=', Client.get_signature(request, 'sk'))

        request = TeaRequest()
        request.pathname = 'test'
        request.query['testKey'] = 'value'
        self.assertEqual('A9g2VbnkB+GS+nyiKTIqSruJvxrgZ9rgot6wVPFbewk=', Client.get_signature(request, 'sk'))

    def test_to_query(self):
        self.assertEqual(0, len(Client.to_query(None)))

        dic = {
            'test': 'test',
            'nullTest': None
        }
        self.assertEqual('test', Client.to_query(dic).get('test'))
        self.assertFalse('nullTest' in Client.to_query(dic))

    def test_is_fail(self):
        self.assertFalse(Client.is_fail(None))
        self.assertTrue(Client.is_fail(100))
        self.assertFalse(Client.is_fail(200))
        self.assertTrue(Client.is_fail(400))

    def test_get_content_md5(self):
        self.assertEqual('', Client.get_content_md5(''))
        self.assertEqual('govO+HY8G8YW4loGvkuQ/w==', Client.get_content_md5('{"test":"test"}'))

    def test_get_time_stamp(self):
        self.assertEqual(13, len(Client.get_time_stamp()))
