using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Cryptography;
using System.Text;

using AlibabaCloud.Apigateway.Utils;

using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

using Tea;

namespace AlibabaCloud.Apigateway
{
    public class BaseClient
    {
        private string _regionId;
        private string _protocol;
        private string _domain;
        private string _appSecret;

        protected string _token;
        protected string _stage;
        protected string _appKey;

        public BaseClient(Dictionary<string, object> config)
        {
            _regionId = DictUtils.GetDicValue(config, "regionId").ToSafeString();
            _protocol = DictUtils.GetDicValue(config, "protocol").ToSafeString();
            _domain = DictUtils.GetDicValue(config, "domain").ToSafeString();
            _token = DictUtils.GetDicValue(config, "token").ToSafeString();
            _appKey = DictUtils.GetDicValue(config, "appKey").ToSafeString();
            _appSecret = DictUtils.GetDicValue(config, "appSecret").ToSafeString();
        }

        protected Dictionary<string, object> _readAsJSON(TeaResponse response)
        {
            string body = TeaCore.GetResponseBody(response);
            Dictionary<string, object> dic = new Dictionary<string, object>();
            JObject jObj = JObject.Parse(body);
            dic = (Dictionary<string, object>) ReadJsonUtil.DeserializeToDic(jObj);
            return dic;
        }

        protected bool _isStatusCode(TeaResponse response, int code)
        {
            return response.StatusCode == code;
        }

        protected string _toJSONString(Dictionary<string, object> dict)
        {
            return JsonConvert.SerializeObject(dict);
        }

        protected string _getUUID()
        {
            return Guid.NewGuid().ToString();
        }

        protected string _getTimestamp()
        {
            return DateTime.UtcNow.GetTimeMillis().ToString();
        }

        protected string _getDate()
        {
            return TimeUtils.GetGMTDate();
        }

        protected string _getContentMD5(TeaRequest request)
        {
            using(MD5 mi = MD5.Create())
            {
                byte[] buffer = mi.ComputeHash(request.Body);
                request.Body.Seek(0, System.IO.SeekOrigin.Begin);
                return Convert.ToBase64String(buffer);
            }
        }

        protected string _getHost()
        {
            return _domain;
        }

        protected string _getSignature(TeaRequest request)
        {
            string signedHeader = BaseUtils.GetSignedHeader(request);
            string url = BaseUtils.BuildUrl(request);
            string date = DictUtils.GetDicValue(request.Headers, "date").ToSafeString();
            string accept = DictUtils.GetDicValue(request.Headers, "accept").ToSafeString();
            string contentType = DictUtils.GetDicValue(request.Headers, "content-type").ToSafeString();
            string contentMd5 = DictUtils.GetDicValue(request.Headers, "content-md5").ToSafeString();
            string signStr = request.Method + "\n" + accept + "\n" + contentMd5 + "\n" + contentType + "\n" + date + "\n" + signedHeader + "\n" + url;

            byte[] signData;
            using(KeyedHashAlgorithm algorithm = CryptoConfig.CreateFromName("HMACSHA256") as KeyedHashAlgorithm)
            {
                algorithm.Key = Encoding.UTF8.GetBytes(_appSecret);
                signData = algorithm.ComputeHash(Encoding.UTF8.GetBytes(signStr.ToCharArray()));
            }
            string signedStr = Convert.ToBase64String(signData);
            return signedStr;
        }

        public bool _isFail(TeaResponse resp)
        {
            return resp.StatusCode < 200 || resp.StatusCode >= 300;
        }

        public string _default(string strValue, string strDefault)
        {
            if (string.IsNullOrWhiteSpace(strValue))
            {
                return strDefault;
            }
            return strValue;
        }

        public int? _defaultNumber(int? numValue, int? numDefault)
        {
            if (numValue > 0)
            {
                return numValue;
            }
            return numDefault;
        }

    }
}
