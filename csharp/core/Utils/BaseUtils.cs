using System.Collections.Generic;
using System.Linq;

using Tea;

namespace AlibabaCloud.Apigateway.Utils
{
    internal static class BaseUtils
    {
        private static readonly string[] filterKey = { "x-ca-signature", "x-ca-signature-headers", "accept", "content-md5", "content-type", "date", "host", "token" };

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
