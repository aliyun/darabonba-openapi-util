import Foundation
import CryptoSwift
import Tea

open class Client {
    private static var SEPARATOR: String = "&"
    
    public static func convert(_ from: TeaModel?, _ to: TeaModel?) -> Void {
        if from != nil {
            to?.fromMap(from!.toMap())
        }
    }

    public static func getStringToSign(_ request: TeaRequest?) -> String {
        if request == nil {
            return ""
        }
        let method: String = request!.method
        let pathname: String = request!.pathname
        let headers: [String: String] = request!.headers
        let query: [String: Any] = request!.query

        let accept = headers["accept"] ?? ""
        let contentMD5 = headers["content-md5"] ?? ""
        let contentType = headers["content-type"] ?? ""
        let date = headers["date"] ?? ""

        let headerStr = String(method) + "\n" + String(accept) + "\n" + String(contentMD5) + "\n" + String(contentType) + "\n" + String(date) + "\n"
        let canonicalizedHeaders: String = getCanonicalizedHeaders(headers: headers)
        let canonicalizedResource: String = getCanonicalizedResource(pathname: pathname, query: query)
        return headerStr + canonicalizedHeaders + canonicalizedResource
    }

    private static func getCanonicalizedHeaders(headers: [String: String]) -> String {
        let prefix: String = "x-acs-"
        var canonicalizedKeys: [String] = []
        for (key, _) in headers {
            if key.hasPrefix(prefix) {
                canonicalizedKeys.append(key)
            }
        }
        canonicalizedKeys.sort()
        var result: String = ""
        for key in canonicalizedKeys {
            result.append(key)
            result.append(":")
            result.append(headers[key]?.trimmingCharacters(in: .whitespacesAndNewlines) ?? "")
            result.append("\n")
        }
        return result
    }

    private static func getCanonicalizedResource(pathname: String, query: [String: Any]) -> String {
        if (query.count <= 0) {
            return pathname
        }
        var keys: [String] = []
        for (key, _) in query {
            keys.append(key)
        }
        keys.sort()
        var result: String = pathname
        var sep : String = "?"
        for key in keys {
            result.append(sep)
            result.append(key)
            if query[key] is String && !(query[key] as! String).isEmpty
                || (!(query[key] is String) && query[key] != nil) {
                result.append("=")
                result.append("\(query[key] ?? "")")
            }
            sep = SEPARATOR
        }
        return result
    }

    public static func getROASignature(_ stringToSign: String?, _ secret: String?) -> String {
        if stringToSign == nil || secret == nil {
            return ""
        }
        let r: [UInt8] = try! HMAC(key: secret!, variant: .sha1).authenticate(stringToSign!.bytes)
        return r.toBase64()
    }

    public static func toForm(_ filter: [String:Any]?) -> String {
        return toFormWithSymbol(filter: filter, symbol: SEPARATOR)
    }
    
    private static func toFormWithSymbol(filter: [String:Any]?, symbol: String) -> String {
        let dict: [String: String] = query(filter)
        var result: String = ""
        if dict.count > 0 {
            let keys = Array(dict.keys).sorted()
            var arr: [String] = []
            for key in keys {
                let value = dict[key]
                if value == nil || value!.isEmpty {
                    continue
                }
                arr.append(key.urlEncode() + "=" + value!.urlEncode())
            }
            if arr.count > 0 {
                result = arr.joined(separator: symbol)
            }
        }
        return result
    }

    public static func getTimestamp() -> String {
        let formatter = DateFormatter()
        formatter.timeZone = TimeZone(identifier: "GMT")
        formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'"
        return formatter.string(from: Date())
    }

    public static func query(_ filter: [String: Any]?) -> [String: String] {
        if filter == nil {
            return [:]
        }
        var result: [String: String] = [:]
        parseObject(result: &result, object: filter!, prefix: "")
        return result
    }
    
    private static func parseObject(result: inout [String: String], object: Any, prefix: String) -> Void {
        if object is Array<Any> {
            for (index, item) in (object as! Array<Any>).enumerated() {
                parseObject(result: &result, object: item, prefix: "\(prefix).\(index + 1)")
            }
        } else if object is Dictionary<String, Any> {
            for (key, value) in object as! [String : Any] {
                parseObject(result: &result, object: value, prefix: "\(prefix).\(key)")
            }
        } else if object is TeaModel {
            let dict : [String : Any] = (object as! TeaModel).toMap()
            for (key, value) in dict {
                parseObject(result: &result, object: value, prefix: "\(prefix).\(key)")
            }
        } else {
            var key: String = prefix
            if key.hasPrefix(".") {
                let subIndex = key.index(key.startIndex, offsetBy: 1)
                key = String(key[subIndex...])
            }
            if object is [UInt8] {
                result[key] = String(bytes: object as! [UInt8], encoding: .utf8)
            } else {
                result[key] = "\(object)"
            }
        }
    }

    public static func getRPCSignature(_ signedParams: [String: String]?, _ method: String?, _ secret: String?) -> String {
        if signedParams == nil || method == nil || secret == nil || secret!.isEmpty{
            return ""
        }
        var keys: [String] = []
        for (key, _) in signedParams! {
            keys.append(key)
        }
        keys.sort()
        var canonicalizedQueryString: String = ""
        var sep : String = ""
        for key in keys {
            canonicalizedQueryString.append(sep)
            if signedParams![key] != nil && !signedParams![key]!.isEmpty {
                canonicalizedQueryString.append(key.urlEncode())
                canonicalizedQueryString.append("=")
                canonicalizedQueryString.append("\(signedParams![key] ?? "")".urlEncode())
            }
            sep = SEPARATOR
        }
        var stringToSign: String = ""
        stringToSign.append(method!)
        stringToSign.append(SEPARATOR)
        stringToSign.append("/".urlEncode())
        stringToSign.append(SEPARATOR)
        stringToSign.append(canonicalizedQueryString.urlEncode())
        let r: [UInt8] = try! HMAC(key: "\(secret!)\(SEPARATOR)", variant: .sha1).authenticate(stringToSign.bytes)
        return r.toBase64()
    }

    public static func arrayToStringWithSpecifiedStyle(_ array: Any?, _ prefix: String?, _ style: String?) -> String {
        if array == nil || style == nil {
            return ""
        }
        switch style {
        case "repeatList":
            var dict: [String: Any] = [:]
            dict[prefix!] = array
            return toFormWithSymbol(filter: dict, symbol: "&&")
        case "simple", "spaceDelimited", "pipeDelimited":
            return flatArray(array: array as! [Any], style: style!)
        default:
            return ""
        }
    }
    
    private static func flatArray(array: [Any], style: String) -> String {
        if array.isEmpty {
            return ""
        }
        var flag: String = ""
        switch style {
        case "simple":
            flag = " "
            break
        case "spaceDelimited":
            flag = ","
            break
        case "pipeDelimited":
            flag = "|"
            break
        default:
            flag = ""
        }
        var arr: [String] = []
        for item in array {
            arr.append("\(item)")
        }
        return arr.joined(separator: flag)
    }
    
    public static func mapToFlatStyle(_ input: Any?) -> Any {
        if input is Array<Any> {
            var list: [Any] = []
            for item in input as! Array<Any> {
                list.append(mapToFlatStyle(item))
            }
            return list
        } else if input is Dictionary<String, Any> {
            var dict : [String : Any] = [:]
            for (key, value) in input as! [String : Any] {
                dict["#\(key.count)#\(key)"] = mapToFlatStyle(value)
            }
            return dict
        } else if input is TeaModel {
            var dict : [String : Any] = [:]
            for (key, value) in (input as! TeaModel).toMap() {
                dict["#\(key.count)#\(key)"] = mapToFlatStyle(value)
            }
            return dict
        }
        return input!
    }

    public static func parseToMap(_ input: Any?) -> Any {
        if input is Array<Any> {
            var list: [Any] = []
            for item in input as! Array<Any> {
                list.append(parseToMap(item))
            }
            return list
        } else if input is Dictionary<String, Any> {
            var dict : [String : Any] = [:]
            for (key, value) in input as! [String : Any] {
                dict[key] = parseToMap(value)
            }
            return dict
        } else if input is TeaModel {
            return (input as! TeaModel).toMap()
        }
        return input!
    }

    public static func getEndpoint(_ endpoint: String?, _ useAccelerate: Bool?, _ endpointType: String?) -> String {
        var endPoint: String = endpoint ?? ""
        if endpointType == "internal" {
            var str: [String] = endPoint.components(separatedBy: ".")
            str[0] += "-internal"
            endPoint = str.joined(separator: ".")
        }
        if useAccelerate != nil && useAccelerate! && endpointType == "accelerate" {
            return "oss-accelerate.aliyuncs.com"
        }
        return endPoint
    }

    public static func hexEncode(_ raw: [UInt8]?) -> String {
        raw?.toHexString() ?? ""
    }

    public static func hash(_ raw: [UInt8]?, _ signatureAlgorithm: String?) -> [UInt8] {
        if raw == nil {
            return []
        }
        if signatureAlgorithm == "ACS3-HMAC-SHA256" || signatureAlgorithm == "ACS3-RSA-SHA256" {
            return Digest.sha2(raw!, variant: .sha256)
        } else if signatureAlgorithm == "ACS3-HMAC-SM3" {
            // 未实现
        }
        return raw!
    }

    public static func getAuthorization(_ request: TeaRequest?, _ signatureAlgorithm: String?, _ payload: String?, _ acesskey: String?, _ accessKeySecret: String?) -> String {
        if request == nil {
            return ""
        }
        var canonicalizedURI: String = request!.pathname
        if canonicalizedURI.isEmpty || canonicalizedURI.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            canonicalizedURI = "/"
        }
        let canonicalizedHeaders: String = buildCanonicalizedHeaders(headers: request!.headers)
        let signedHeaders: [String] = buildSignedHeaders(headers: request!.headers)
        let canonicalizedQueryString: String = buildCanonicalizedResource(query: request!.query)
        var stringToSign: String = ""
        stringToSign.append(request!.method)
        stringToSign.append("\n")
        stringToSign.append(canonicalizedURI)
        stringToSign.append("\n")
        stringToSign.append(canonicalizedQueryString)
        stringToSign.append("\n")
        stringToSign.append(canonicalizedHeaders)
        stringToSign.append("\n")
        stringToSign.append(signedHeaders.joined(separator: ";"))
        stringToSign.append("\n")
        stringToSign.append(payload ?? "")
        let hex: String = hexEncode(hash(stringToSign.toBytes(), signatureAlgorithm))
        stringToSign = "\(signatureAlgorithm!)\n\(hex)"
        let byte: [UInt8] = try! HMAC(key: accessKeySecret ?? "", variant: .sha256).authenticate(stringToSign.bytes)
        let signature: String = hexEncode(byte)
        return "\(signatureAlgorithm!) Credential=\(acesskey!),SignedHeaders=\(signedHeaders.joined(separator: ";")),Signature=\(signature)"
    }
    
    private static func buildCanonicalizedHeaders(headers: [String: String]) -> String {
        var canonicalizedHeaders: String = ""
        var sortedHeaders: [String] = buildSignedHeaders(headers: headers)
        for header in sortedHeaders {
            canonicalizedHeaders.append(header)
            canonicalizedHeaders.append(":")
            canonicalizedHeaders.append(headers[header]?.trimmingCharacters(in: .whitespacesAndNewlines) ?? "")
            canonicalizedHeaders.append("\n")
        }
        return canonicalizedHeaders
    }
    
    private static func buildSignedHeaders(headers: [String: String]) -> [String] {
        var canonicalizedKeys: [String] = []
        for (key, _) in headers {
            if key.lowercased().hasPrefix("x-acs-") || key.lowercased() == "host" || key.lowercased() == "content-type" {
                if !canonicalizedKeys.contains(key.lowercased()) {
                    canonicalizedKeys.append(key.lowercased())
                }
            }
        }
        canonicalizedKeys.sort()
        return canonicalizedKeys
    }

    private static func buildCanonicalizedResource(query: [String: Any]) -> String {
        var keys: [String] = []
        for (key, _) in query {
            keys.append(key)
        }
        keys.sort()
        var result: String = ""
        var sep : String = ""
        for key in keys {
            result.append(sep)
            result.append(key.urlEncode())
            result.append("=")
            if query[key] is String && !(query[key] as! String).isEmpty
                || (!(query[key] is String) && query[key] != nil) {
                result.append("\(query[key] ?? "")")
            }
            sep = SEPARATOR
        }
        return result
    }

    public static func getEncodePath(_ path: String?) -> String {
        if path == nil || path!.isEmpty {
            return ""
        }
        if path == "/" {
            return "/"
        }
        let strs: [String] = path!.components(separatedBy: "/")
        var result: [String] = []
        for part in strs {
            result.append(part.urlEncode())
        }
        return result.joined(separator: "/")
    }

    public static func getEncodeParam(_ param: String?) -> String {
        if param == nil || param!.isEmpty {
            return ""
        }
        return param!.urlEncode()
    }
}
