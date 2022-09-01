/**
 * This is for OpenApi Util 
 */
// This file is auto-generated, don't edit it. Thanks.

using System;
using System.Collections;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Security.Cryptography;
using System.Text;
using System.Security.Cryptography.X509Certificates;
using Newtonsoft.Json;
using Tea;
using Tea.Utils;

namespace AlibabaCloud.OpenApiUtil
{
    public class Client
    {
        internal static readonly string SEPARATOR = "&";
        internal static readonly string PEM_BEGIN = "-----BEGIN RSA PRIVATE KEY-----\n";
        internal static readonly string PEM_END = "\n-----END RSA PRIVATE KEY-----";

        /**
         * Convert all params of body other than type of readable into content 
         * @param body source Model
         * @param content target Model
         * @return void
         */
        public static void Convert(TeaModel body, TeaModel content)
        {
            Dictionary<string, object> dict = new Dictionary<string, object>();
            Type type = body.GetType();
            PropertyInfo[] properties = type.GetProperties();
            for (int i = 0; i < properties.Length; i++)
            {
                PropertyInfo p = properties[i];
                var propertyType = p.PropertyType;
                if (!typeof(Stream).IsAssignableFrom(propertyType))
                {
                    dict[p.Name] = p.GetValue(body);
                }
            }

            string jsonStr = JsonConvert.SerializeObject(dict);
            TeaModel tempModel = (TeaModel) JsonConvert.DeserializeObject(jsonStr, content.GetType());

            Type outType = content.GetType();
            PropertyInfo[] outPropertyies = outType.GetProperties();
            foreach (PropertyInfo p in outPropertyies)
            {
                var outPropertyType = p.PropertyType;
                p.SetValue(content, p.GetValue(tempModel));
            }
        }

        /**
         * Get the string to be signed according to request
         * @param request  which contains signed messages
         * @return the signed string
         */
        public static string GetStringToSign(TeaRequest request)
        {
            string method = request.Method;
            string pathname = request.Pathname;
            Dictionary<string, string> headers = request.Headers;
            Dictionary<string, string> query = request.Query;
            string accept = headers.ContainsKey("accept") ? headers["accept"] : "";
            string contentMD5 = headers.ContainsKey("content-md5") ? headers["content-md5"] : "";
            string contentType = headers.ContainsKey("content-type") ? headers["content-type"] : "";
            string date = headers.ContainsKey("date") ? headers["date"] : "";
            string header = method + "\n" + accept + "\n" + contentMD5 + "\n" + contentType + "\n" + date + "\n";
            string canonicalizedHeaders = GetCanonicalizedHeaders(headers);
            string canonicalizedResource = GetCanonicalizedResource(pathname, query);
            string stringToSign = header + canonicalizedHeaders + canonicalizedResource;
            return stringToSign;
        }

        /**
         * Get signature according to stringToSign, secret
         * @param stringToSign  the signed string
         * @param secret accesskey secret
         * @return the signature
         */
        public static string GetROASignature(string stringToSign, string secret)
        {
            byte[] signData;
            using (KeyedHashAlgorithm algorithm = CryptoConfig.CreateFromName("HMACSHA1") as KeyedHashAlgorithm)
            {
                algorithm.Key = Encoding.UTF8.GetBytes(secret);
                signData = algorithm.ComputeHash(Encoding.UTF8.GetBytes(stringToSign.ToCharArray()));
            }

            return System.Convert.ToBase64String(signData);
        }

        /**
         * Parse filter into a form string
         * @param filter object
         * @return the string
         */
        public static string ToForm(IDictionary filter)
        {
            if (filter == null)
            {
                return string.Empty;
            }

            Dictionary<string, object> dict = filter.Keys.Cast<string>().ToDictionary(key => key, key => filter[key]);
            Dictionary<string, string> outDict = new Dictionary<string, string>();
            TileDict(outDict, dict);
            List<string> listStr = new List<string>();
            foreach (var keypair in outDict)
            {
                if (string.IsNullOrWhiteSpace(keypair.Value))
                {
                    continue;
                }

                listStr.Add(PercentEncode(keypair.Key) + "=" + PercentEncode(keypair.Value));
            }

            return string.Join("&", listStr);
        }

        /**
         * Get timestamp
         * @return the timestamp string
         */
        public static string GetTimestamp()
        {
            return DateTime.UtcNow.ToString("yyyy-MM-dd'T'HH:mm:ss'Z'");
        }

        /**
         * Parse filter into a object which's type is map[string]string
         * @param filter query param
         * @return the object
         */
        public static Dictionary<string, string> Query(IDictionary filter)
        {
            Dictionary<string, string> outDict = new Dictionary<string, string>();
            TileDict(outDict, filter);
            return outDict;
        }

        /**
         * Get signature according to signedParams, method and secret
         * @param signedParams params which need to be signed
         * @param method http method e.g. GET
         * @param secret AccessKeySecret
         * @return the signature
         */
        public static string GetRPCSignature(Dictionary<string, string> signedParams, string method, string secret)
        {
            List<string> sortedKeys = signedParams.Keys.ToList();
            sortedKeys.Sort(StringComparer.Ordinal);
            StringBuilder canonicalizedQueryString = new StringBuilder();

            foreach (string key in sortedKeys)
            {
                if (signedParams[key] != null)
                {
                    canonicalizedQueryString.Append("&")
                        .Append(PercentEncode(key)).Append("=")
                        .Append(PercentEncode(signedParams[key]));
                }
            }

            StringBuilder stringToSign = new StringBuilder();
            stringToSign.Append(method);
            stringToSign.Append(SEPARATOR);
            stringToSign.Append(PercentEncode("/"));
            stringToSign.Append(SEPARATOR);
            stringToSign.Append(PercentEncode(
                canonicalizedQueryString.ToString().Substring(1)));
            System.Diagnostics.Debug.WriteLine("GetRPCSignature:stringToSign is " + stringToSign.ToString());
            byte[] signData;
            using (KeyedHashAlgorithm algorithm = CryptoConfig.CreateFromName("HMACSHA1") as KeyedHashAlgorithm)
            {
                algorithm.Key = Encoding.UTF8.GetBytes(secret + SEPARATOR);
                signData = algorithm.ComputeHash(Encoding.UTF8.GetBytes(stringToSign.ToString().ToCharArray()));
            }

            string signedStr = System.Convert.ToBase64String(signData);
            return signedStr;
        }

        /**
         * Parse array into a string with specified style
         * @param array the array
         * @param prefix the prefix string
         * @style specified style e.g. repeatList
         * @return the string
         */
        public static string ArrayToStringWithSpecifiedStyle(object array, string prefix, string style)
        {
            if (array == null)
            {
                return string.Empty;
            }

            switch (style.ToLower())
            {
                case "repeatlist":
                    Dictionary<string, object> map = new Dictionary<string, object>();
                    map.Add(prefix, array);
                    return ToForm(map);
                case "simple":
                case "spacedelimited":
                case "pipedelimited":
                    return FlatArray((IList) array, style);
                case "json":
                    return JsonConvert.SerializeObject(ToJsonObject(array));
                default:
                    return string.Empty;
            }
        }

        /**
         * Transform input as map.
         */
        public static Dictionary<string, object> ParseToMap(object input)
        {
            if (input == null)
            {
                return null;
            }

            Type type = input.GetType();
            var map = (Dictionary<string, object>) TeaModelExtensions.ToMapFactory(type, input);

            return map;
        }

        public static string GetEndpoint(string endpoint, bool? useAccelerate, string endpointType)
        {
            if (endpointType == "internal")
            {
                string[] strs = endpoint.Split('.');
                strs[0] += "-internal";
                endpoint = string.Join(".", strs);
            }

            if (useAccelerate == true && endpointType == "accelerate")
            {
                return "oss-accelerate.aliyuncs.com";
            }

            return endpoint;
        }

        /// <summary>
        /// Encode raw with base16
        /// </summary>
        /// <param name="raw">encoding data</param>
        /// <returns>encoded string</returns>
        public static string HexEncode(byte[] raw)
        {
            if (raw == null)
            {
                return string.Empty;
            }

            StringBuilder result = new StringBuilder(raw.Length * 2);
            for (int i = 0; i < raw.Length; i++)
                result.Append(raw[i].ToString("x2"));
            return result.ToString();
        }

        /// <summary>
        /// Hash the raw data with signatureAlgorithm
        /// </summary>
        /// <param name="raw">hashing data</param>
        /// <param name="signatureAlgorithm">the autograph method</param>
        /// <returns>hashed bytes</returns>
        public static byte[] Hash(byte[] raw, string signatureAlgorithm)
        {
            if (signatureAlgorithm == "ACS3-HMAC-SHA256" || signatureAlgorithm == "ACS3-RSA-SHA256")
            {
                byte[] signData;
                using (SHA256 sha256 = new SHA256Managed())
                {
                    signData = sha256.ComputeHash(raw);
                }

                return signData;
            }

            return null;
        }

        /// <summary>
        /// Get the authorization 
        /// </summary>
        /// <param name="request">request params</param>
        /// <param name="signatureAlgorithm">the autograph method</param>
        /// <param name="payload">the hashed request</param>
        /// <param name="acesskey">the acesskey string</param>
        /// <param name="accessKeySecret">the accessKeySecret string</param>
        /// <returns>authorization string</returns>
        public static string GetAuthorization(TeaRequest request, string signatureAlgorithm, string payload,
            string acesskey, string accessKeySecret)
        {
            string canonicalURI = request.Pathname.ToSafeString("") == ""
                ? "/"
                : request.Pathname.Replace("+", "%20").Replace("*", "%2A").Replace("%7E", "~");
            string method = request.Method;
            string canonicalQueryString = GetAuthorizationQueryString(request.Query);
            Tuple<string, List<string>> tuple = GetAuthorizationHeaders(request.Headers);
            string canonicalheaders = tuple.Item1;
            var signedHeaders = tuple.Item2;

            string canonicalRequest = method + "\n" + canonicalURI + "\n" + canonicalQueryString + "\n" +
                                      canonicalheaders + "\n" +
                                      string.Join(";", signedHeaders) + "\n" + payload;
            byte[] raw = Encoding.UTF8.GetBytes(canonicalRequest);
            string StringToSign = signatureAlgorithm + "\n" + HexEncode(Hash(raw, signatureAlgorithm));
            System.Diagnostics.Debug.WriteLine("GetAuthorization:stringToSign is " + StringToSign);
            var signature = HexEncode(SignatureMethod(accessKeySecret, StringToSign, signatureAlgorithm));
            string auth = signatureAlgorithm + " Credential=" + acesskey + ",SignedHeaders=" +
                          string.Join(";", signedHeaders) + ",Signature=" + signature;

            return auth;
        }

        /// <summary>
        /// Get encoded path
        /// </summary>
        /// <param name="path">path the raw path</param>
        /// <returns>encoded path</returns>
        public static string GetEncodePath(string path)
        {
            List<string> encodeStr = new List<string>();
            string[] strSplit = path.Split('/');
            foreach (string str in strSplit)
            {
                encodeStr.Add(PercentEncode(str));
            }

            return string.Join("/", encodeStr);
        }

        /// <summary>
        /// Get encoded param
        /// </summary>
        /// <param name="param">param the raw path</param>
        /// <returns>encoded param</returns>
        public static string GetEncodeParam(string param)
        {
            return PercentEncode(param);
        }

        internal static byte[] SignatureMethod(string secret, string source, string signatureAlgorithm)
        {
            if (signatureAlgorithm == "ACS3-HMAC-SHA256")
            {
                byte[] signData;
                using (KeyedHashAlgorithm algorithm = CryptoConfig.CreateFromName("HMACSHA256") as KeyedHashAlgorithm)
                {
                    algorithm.Key = Encoding.UTF8.GetBytes(secret);
                    signData = algorithm.ComputeHash(Encoding.UTF8.GetBytes(source.ToSafeString().ToCharArray()));
                }

                return signData;
            }

            return null;
        }

        internal static Tuple<string, List<string>> GetAuthorizationHeaders(Dictionary<string, string> headers)
        {
            string canonicalheaders = string.Empty;
            var tmp = new Dictionary<string, List<string>>();
            foreach (var keypair in headers)
            {
                var lowerKey = keypair.Key.ToLower();
                if (lowerKey.StartsWith("x-acs-") || lowerKey == "host" || lowerKey == "content-type")
                {
                    if (tmp.ContainsKey(lowerKey))
                    {
                        tmp[lowerKey].Add(keypair.Value.ToSafeString().Trim());
                    }
                    else
                    {
                        tmp[lowerKey] = new List<string> {keypair.Value.ToSafeString().Trim()};
                    }
                }
            }

            var hs = tmp.OrderBy(p => p.Key, StringComparer.Ordinal).ToDictionary(p => p.Key, p => p.Value);

            foreach (var keypair in hs)
            {
                var listSort = new List<string>(keypair.Value);
                listSort.Sort(StringComparer.Ordinal);
                canonicalheaders += string.Format("{0}:{1}\n", keypair.Key, string.Join(", ", listSort));
            }

            return new Tuple<string, List<string>>(canonicalheaders, hs.Keys.ToList());
        }

        internal static string GetAuthorizationQueryString(Dictionary<string, string> query)
        {
            string canonicalQueryString = string.Empty;
            if (query == null || query.Count <= 0)
            {
                return canonicalQueryString;
            }
            var hs = query.OrderBy(p => p.Key, StringComparer.Ordinal).ToDictionary(p => p.Key, p => p.Value);
            foreach (var keypair in hs)
            {
                if (keypair.Value != null)
                {
                    canonicalQueryString += string.Format("&{0}={1}", keypair.Key, PercentEncode(keypair.Value));
                }
            }

            if (!string.IsNullOrEmpty(canonicalQueryString))
            {
                canonicalQueryString = canonicalQueryString.TrimStart('&');
            }

            return canonicalQueryString;
        }

        internal static string GetCanonicalizedHeaders(Dictionary<string, string> headers)
        {
            string prefix = "x-acs-";
            List<string> canonicalizedKeys = new List<string>();
            canonicalizedKeys = headers.Where(p => p.Key.StartsWith(prefix))
                .Select(p => p.Key).ToList();
            canonicalizedKeys.Sort(StringComparer.Ordinal);
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < canonicalizedKeys.Count; i++)
            {
                string key = canonicalizedKeys[i];
                result.Append(key);
                result.Append(":");
                result.Append(headers[key].Trim());
                result.Append("\n");
            }

            return result.ToString();
        }

        internal static string GetCanonicalizedResource(string pathname, Dictionary<string, string> query)
        {
            if (query == null || query.Count <= 0)
            {
                return pathname;
            }

            List<string> keys = query.Keys.ToList();
            keys.Sort(StringComparer.Ordinal);
            string key;
            List<string> result = new List<string>();
            for (int i = 0; i < keys.Count; i++)
            {
                key = keys[i];
                if (query[key] == null)
                {
                    continue;
                }

                if (query[key] == "")
                {
                    result.Add(key);
                }
                else
                {
                    result.Add(key + "=" + query[key]);
                }
            }

            return pathname + "?" + string.Join("&", result);
        }

        internal static void TileDict(Dictionary<string, string> dicOut, object obj, string parentKey = "")
        {
            if (obj == null)
            {
                return;
            }

            if (typeof(IDictionary).IsAssignableFrom(obj.GetType()))
            {
                Dictionary<string, object> dicIn = ((IDictionary) obj).Keys.Cast<string>()
                    .ToDictionary(key => key, key => ((IDictionary) obj)[key]);
                foreach (var keypair in dicIn)
                {
                    string keyName = parentKey + "." + keypair.Key;
                    if (keypair.Value == null)
                    {
                        continue;
                    }

                    TileDict(dicOut, keypair.Value, keyName);
                }
            }
            else if (typeof(TeaModel).IsAssignableFrom(obj.GetType()))
            {
                Dictionary<string, object> dicIn = ((Dictionary<string, object>)((TeaModel)obj).ToMap());
                foreach (var keypair in dicIn)
                {
                    string keyName = parentKey + "." + keypair.Key;
                    if (keypair.Value == null)
                    {
                        continue;
                    }

                    TileDict(dicOut, keypair.Value, keyName);
                }
            }
            else if (typeof(IList).IsAssignableFrom(obj.GetType()) && !typeof(Array).IsAssignableFrom(obj.GetType()))
            {
                int index = 1;
                foreach (var temp in (IList) obj)
                {
                    TileDict(dicOut, temp, parentKey + "." + index.ToSafeString());
                    index++;
                }
            }
            else
            {
                if (obj.GetType() == typeof(byte[]))
                {
                    dicOut.Add(parentKey.TrimStart('.'), Encoding.UTF8.GetString((byte[]) obj));

                }
                else
                {
                    dicOut.Add(parentKey.TrimStart('.'), obj.ToSafeString(""));
                }
            }
        }

        internal static string PercentEncode(string value)
        {
            if (value == null)
            {
                return null;
            }

            var stringBuilder = new StringBuilder();
            var text = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_.~";
            var bytes = Encoding.UTF8.GetBytes(value);
            foreach (char c in bytes)
            {
                if (text.IndexOf(c) >= 0)
                {
                    stringBuilder.Append(c);
                }
                else
                {
                    stringBuilder.Append("%").Append(string.Format(CultureInfo.InvariantCulture, "{0:X2}", (int) c));
                }
            }

            return stringBuilder.ToString().Replace("+", "%20")
                .Replace("*", "%2A").Replace("%7E", "~");
        }

        internal static string FlatArray(IList array, string sty)
        {
            List<string> strs = new List<string>();
            for (int i = 0; i < array.Count; i++)
            {
                strs.Add(array[i].ToSafeString());
            }

            if (sty.ToSafeString().ToLower() == "simple".ToLower())
            {
                return string.Join(",", strs);
            }
            else if (sty.ToSafeString().ToLower() == "spaceDelimited".ToLower())
            {
                return string.Join(" ", strs);
            }
            else
            {
                return string.Join("|", strs);
            }
        }

        internal static object ToJsonObject(object obj)
        {
            if (obj == null)
            {
                return null;
            }

            if (typeof(IDictionary).IsAssignableFrom(obj.GetType()))
            {
                Dictionary<string, object> dicIn = ((IDictionary) obj).Keys.Cast<string>()
                    .ToDictionary(key => key, key => ((IDictionary) obj)[key]);
                Dictionary<string, object> result = new Dictionary<string, object>();
                foreach (var keypair in dicIn)
                {
                    if (keypair.Value == null)
                    {
                        continue;
                    }
                    result.Add(keypair.Key, ToJsonObject(keypair.Value));
                }
                return result;
            }
            else if (typeof(TeaModel).IsAssignableFrom(obj.GetType()))
            {
                Dictionary<string, object> dicIn = ((Dictionary<string, object>)((TeaModel)obj).ToMap());
                return ToJsonObject(dicIn);
            }
            else if (typeof(IList).IsAssignableFrom(obj.GetType()) && !typeof(Array).IsAssignableFrom(obj.GetType()))
            {
                List<object> array = new List<object>();
                foreach (var temp in (IList) obj)
                {
                    array.Add(ToJsonObject(temp));
                }
                return array;
            }
            return obj;
        }
    }
}
