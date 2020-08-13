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

using Newtonsoft.Json;

using Tea;
using Tea.Utils;

namespace AlibabaCloud.OpenApiUtil
{
    public class Client
    {
        internal static readonly string SEPARATOR = "&";
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
            using(KeyedHashAlgorithm algorithm = CryptoConfig.CreateFromName("HMACSHA1") as KeyedHashAlgorithm)
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
        public static string ToForm(Dictionary<string, object> filter)
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
        public static Dictionary<string, string> Query(Dictionary<string, object> filter)
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
            sortedKeys.Sort();
            StringBuilder canonicalizedQueryString = new StringBuilder();

            foreach (string key in sortedKeys)
            {
                if (!string.IsNullOrEmpty(signedParams[key]))
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
            using(KeyedHashAlgorithm algorithm = CryptoConfig.CreateFromName("HMACSHA1") as KeyedHashAlgorithm)
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
                    return JsonConvert.SerializeObject(array);
                default:
                    return string.Empty;
            }
        }

        internal static string GetCanonicalizedHeaders(Dictionary<string, string> headers)
        {
            string prefix = "x-acs-";
            List<string> canonicalizedKeys = new List<string>();
            canonicalizedKeys = headers.Where(p => p.Key.StartsWith(prefix))
                .Select(p => p.Key).ToList();
            canonicalizedKeys.Sort();
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
            keys.Sort();
            string key;
            List<string> result = new List<string>();
            for (int i = 0; i < keys.Count; i++)
            {
                key = keys[i];
                if (string.IsNullOrWhiteSpace(query[key]))
                {
                    continue;
                }
                result.Add(key + "=" + query[key]);
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
                Dictionary<string, object> dicIn = ((IDictionary) obj).Keys.Cast<string>().ToDictionary(key => key, key => ((IDictionary) obj) [key]);
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
            else if (typeof(IList).IsAssignableFrom(obj.GetType()))
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
                dicOut.Add(parentKey.TrimStart('.'), obj.ToSafeString(""));
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
                .Replace("*", "%2A").Replace("~", "%7E");
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
    }
}
