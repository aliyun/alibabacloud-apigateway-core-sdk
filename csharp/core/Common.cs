using System;
using System.Collections.Generic;
using System.Text;
using Tea;
using System.Linq;
using Tea.Utils;
using System.Security.Cryptography;
using System.Collections;

namespace AlibabaCloud.APIGatewayUtil
{
    public class Common
    {
        private static readonly string[] filterKey = { "x-ca-signature", "x-ca-signature-headers", "accept", "content-md5", "content-type", "date", "host", "token" };

        public static string GetSignature(TeaRequest request, string secret)
        {
            string signedHeader = GetSignedHeader(request);
            string url = BuildUrl(request);
            string date = request.Headers.Get("date");
            string accept = request.Headers.Get("accept");
            string contentType = request.Headers.Get("content-type");
            string contentMd5 = request.Headers.Get("content-md5");
            string signStr = request.Method + "\n" + accept + "\n" + contentMd5 + "\n" + contentType + "\n" + date + "\n" + signedHeader + "\n" + url;

            byte[] signData;
            using (KeyedHashAlgorithm algorithm = CryptoConfig.CreateFromName("HMACSHA256") as KeyedHashAlgorithm)
            {
                algorithm.Key = Encoding.UTF8.GetBytes(secret);
                signData = algorithm.ComputeHash(Encoding.UTF8.GetBytes(signStr.ToCharArray()));
            }
            string signedStr = Convert.ToBase64String(signData);
            return signedStr;
        }

        public static Dictionary<string, string> ToQuery(IDictionary filter)
        {
            Dictionary<string, string> result = new Dictionary<string, string>();
            foreach (var keypair in filter.Keys.Cast<string>().ToDictionary(key => key, key => filter[key]))
            {
                if(keypair.Value == null)
                {
                    continue;
                }
                result.Add(keypair.Key, keypair.Value.ToSafeString());
            }

            return result;
        }

        public static bool IsFail(int? code)
        {
            return code < 200 || code >= 300;
        }

        public static string GetContentMD5(string body)
        {
            using (MD5 mi = MD5.Create())
            {
                byte[] buffer = mi.ComputeHash(Encoding.UTF8.GetBytes(body));
                return Convert.ToBase64String(buffer);
            }
        }

        internal static string GetSignedHeader(TeaRequest request)
        {
            string signedHeader = string.Empty;
            string signedHeaderKeys = string.Empty;
            var hs = (from dic in request.Headers orderby dic.Key ascending select dic).ToDictionary(p => p.Key, p => p.Value);
            foreach (var keypair in hs)
            {
                if (!filterKey.Contains(keypair.Key))
                {
                    signedHeaderKeys += keypair.Key + ",";
                    signedHeader += keypair.Key + ":" + keypair.Value + "\n";
                }
            }
            request.Headers["x-ca-signature-headers"] = signedHeaderKeys.TrimEnd(',');
            return signedHeader.Remove(signedHeader.Length - 1);
        }

        internal static string BuildUrl(TeaRequest request)
        {
            string url = request.Pathname.ToSafeString(string.Empty);
            Dictionary<string, string> hs = (from dic in request.Query orderby dic.Key ascending select dic).ToDictionary(p => p.Key, p => p.Value);

            if (hs.Count > 0 && !url.Contains("?"))
            {
                url += "?";
            }

            foreach (var keypair in hs)
            {
                if (!url.EndsWith("?"))
                {
                    url += "&";
                }
                url += keypair.Key + "=" + keypair.Value;
            }
            return url;
        }
    }
}
