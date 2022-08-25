import XCTest
import Tea
@testable import AlibabaCloudOpenApiUtil


class TestModel: TeaModel {
    var num: Int? = 100
    var str: String? = "string"
    
    public override init() {
        super.init()
    }

    public init(_ dict: [String: Any]) {
        super.init()
        self.fromMap(dict)
    }
    
    public override func validate() throws -> Void {
    }
    
    public override func toMap() -> [String : Any] {
        var map = super.toMap()
        if self.num != nil {
            map["num"] = self.num
        }
        if self.str != nil {
            map["str"] = self.str
        }
        return map
    }
    
    public override func fromMap(_ dict: [String: Any]) -> Void {
        if dict.keys.contains("num") {
            self.num = dict["num"] as! Int
        }
        if dict.keys.contains("str") {
            self.str = dict["str"] as! String
        }
    }
}

class TestModel2: TeaModel {
    var num: Int? = 200
    var str1: String? = "model2"
    
    public override init() {
        super.init()
    }

    public init(_ dict: [String: Any]) {
        super.init()
        self.fromMap(dict)
    }
    
    public override func validate() throws -> Void {
    }
    
    public override func toMap() -> [String : Any] {
        var map = super.toMap()
        if self.num != nil {
            map["num"] = self.num
        }
        if self.str1 != nil {
            map["str1"] = self.str1
        }
        return map
    }
    
    public override func fromMap(_ dict: [String: Any]) -> Void {
        if dict.keys.contains("num") {
            self.num = dict["num"] as! Int
        }
        if dict.keys.contains("str1") {
            self.str1 = dict["str1"] as! String
        }
    }
}

final class AlibabaCloudOpenApiUtilTests: XCTestCase {
    
    func testConvert() {
        let model1 = TestModel()
        let model2 = TestModel2()
        Client.convert(model1, model2)
        XCTAssertEqual(100, model2.num!)
        XCTAssertEqual("model2", model2.str1!)
    }

    func testGetStringToSign() {
        let request = TeaRequest()
        var stringToSign: String = Client.getStringToSign(request)
        XCTAssertEqual("GET\n\n\n\n\n", stringToSign)
        request.protocol_ = "http"
        request.pathname = "/test"
        request.query = [
            "x-acs-security-token": "test",
            "x-acs-security-test": "test",
            "accept": "accept",
            "content-md5": "content-md5",
            "content-type": "content-type",
            "date": "date",
            "chineseTest": "汉语",
            "emptyTest": "",
            "spaceTest": " ",
        ]
        request.headers = [
            "x-acs-security-token": "test",
            "x-acs-security-test": "test",
            "accept": "accept",
            "content-md5": "content-md5",
            "content-type": "content-type",
            "date": "date",
            "chineseTest": "汉语",
            "emptyTest": "",
            "spaceTest": " ",
        ]
        stringToSign = Client.getStringToSign(request)
        XCTAssertEqual("GET\n" +
                       "accept\n" +
                       "content-md5\n" +
                       "content-type\n" +
                       "date\n" +
                       "x-acs-security-test:test\n" +
                       "x-acs-security-token:test\n" +
                       "/test?accept=accept&chineseTest=汉语&content-md5=content-md5&content-type=content-type&date=date&" +
                       "emptyTest&spaceTest= &x-acs-security-test=test&x-acs-security-token=test", stringToSign)
    }

    func testGetROASignature() {
        let signature: String = Client.getROASignature("stringtosign", "secret")
        XCTAssertEqual("OmuTAr79tpI6CRoAdmzKRq5lHs0=", signature)
    }

    func testToForm() async throws {
        var dict: [String: Any] = [:]
        var result: String = Client.toForm(nil)
        XCTAssertEqual("", result)
        result = Client.toForm(dict)
        XCTAssertEqual("", result)
        dict["form"] = "test"
        dict["param"] = "test"
        dict["nullTest"] = nil
        result = Client.toForm(dict)
        XCTAssertEqual("form=test&param=test", result)
    }

    func testGetTimeStamp() async throws {
        XCTAssertNotNil(Client.getTimestamp())
        XCTAssertTrue(Client.getTimestamp().contains("T"))
        XCTAssertTrue(Client.getTimestamp().contains("Z"))
    }

    func testQuery() async throws {
        var dict: [String: Any] = [:]
        dict["StringTest"] = "test"
        dict["IntegerTest"] = 1
        dict["nullTest"] = nil
        var firstList: [Any] = []
        firstList.append(1)
        var subMapInFirstList: [String: Any] = [:]
        subMapInFirstList["StringTest"] = "test"
        subMapInFirstList["IntegerTest"] = 2
        subMapInFirstList["nullTest"] = nil
        firstList.append(subMapInFirstList)
        var secondList: [Any] = []
        secondList.append(1)
        firstList.append(secondList)
        dict["list"] = firstList
        
        var firstMap: [String: Any] = [:]
        var subMapInFirstMap: [String: Any] = [:]
        subMapInFirstMap["StringTest"] = "test"
        subMapInFirstMap["IntegerTest"] = 2
        subMapInFirstMap["nullTest"] = nil
        firstMap["firstMapMap"] = subMapInFirstMap
        firstMap["firstMapList"] = secondList
        firstMap["nullTest"] = nil
        firstMap["StringTest"] = "test"
        firstMap["IntegerTest"] = 2
        dict["map"] = firstMap
        
        let model = TestModel()
        dict["model"] = model
        
        var result = Client.query(nil)
        XCTAssertEqual(0, result.count)
        result = Client.query(dict)
        XCTAssertEqual(13, result.count)
        XCTAssertEqual("test", result["StringTest"])
        XCTAssertEqual("1", result["IntegerTest"])
        XCTAssertNil(result["nullTest"])
        XCTAssertEqual("1", result["list.1"])
        XCTAssertEqual("2", result["list.2.IntegerTest"])
        XCTAssertEqual("test", result["list.2.StringTest"])
        XCTAssertNil(result["list.2.nulTest"])
        XCTAssertEqual("1", result["list.3.1"])
        
        XCTAssertEqual("1", result["map.firstMapList.1"])
        XCTAssertNil(result["map.nullTest"])
        XCTAssertEqual("2", result["map.IntegerTest"])
        XCTAssertEqual("test", result["map.StringTest"])
        XCTAssertNil(result["map.firstMapMap.nullTest"])
        XCTAssertEqual("2", result["map.firstMapMap.IntegerTest"])
        XCTAssertEqual("test", result["map.firstMapMap.StringTest"])
        
        XCTAssertEqual("100", result["model.num"])
        XCTAssertEqual("string", result["model.str"])
    }

    func testGetRPCSignature() {
        let query: [String: String] = [
            "query": "test",
            "body": "test",
        ]
        let result: String = Client.getRPCSignature(query, "GET", "secret")
        XCTAssertEqual("XlUyV4sXjOuX5FnjUz9IF9tm5rU=", result)
    }

    func testArrayToStringWithSpecifiedStyle() {
    }

    func testMapToFlatStyle() {
    }

    func testParseToMap() {
    }

    func testGetEndpoint() {
        XCTAssertEqual("cc-internal.abc.com", Client.getEndpoint("cc.abc.com", false, "internal"))
        XCTAssertEqual("oss-accelerate.aliyuncs.com", Client.getEndpoint("", true, "accelerate"))
        XCTAssertEqual("test", Client.getEndpoint("test", true, "test"))
    }

    func testHexEncode() {
        XCTAssertEqual("", Client.hexEncode("".toBytes()))
        let hash: [UInt8] = Client.hash("test".toBytes(), "ACS3-HMAC-SHA256")
        var result = Client.hexEncode(hash)
        XCTAssertEqual("9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08", result)
    }

    func testGetEncodePath() {
        XCTAssertEqual("", Client.getEncodePath(""))
        XCTAssertEqual("/", Client.getEncodePath("/"))
        XCTAssertEqual("test", Client.getEncodePath("test"))
        XCTAssertEqual("/path/%20test", Client.getEncodePath("/path/ test"))
        XCTAssertEqual("/path/%23test", Client.getEncodePath("/path/#test"))
        XCTAssertEqual("/path/%22test", Client.getEncodePath("/path/\"test"))
    }

    func testGetEncodeParam() {
        XCTAssertEqual("a%2Fb%2Fc%2F%20test", Client.getEncodeParam("a/b/c/ test"))
    }

    func testGetAuthorization() {
        let request = TeaRequest()
        request.query = [
            "test": "ok",
            "empty": "",
        ]
        request.headers = [
            "x-acs-test": "http",
            "x-acs-TEST": "https",
        ]
        let auth: String = Client.getAuthorization(request, "ACS3-HMAC-SHA256",
                        "55e12e91650d2fec56ec74e1d3e4ddbfce2ef3a65890c2a19ecf88a307e76a23", "acesskey", "secret")
        XCTAssertEqual("ACS3-HMAC-SHA256 Credential=acesskey,SignedHeaders=x-acs-test,Signature=02e81f9f3cc8839151b0c7278024cbc4bfc9fa786085a0b8305f825f17b5dae7", auth)
    }

    static var allTests = [
        ("testConvert", testConvert),
        ("testGetStringToSign", testGetStringToSign),
        ("testGetROASignature", testGetROASignature),
        ("testToForm", testToForm),
        ("testGetTimeStamp", testGetTimeStamp),
        ("testQuery", testQuery),
        ("testGetRPCSignature", testGetRPCSignature),
        ("testArrayToStringWithSpecifiedStyle", testArrayToStringWithSpecifiedStyle),
        ("testMapToFlatStyle", testMapToFlatStyle),
        ("testParseToMap", testParseToMap),
        ("testGetEndpoint", testGetEndpoint),
        ("testHexEncode", testHexEncode),
        ("testGetEncodePath", testGetEncodePath),
        ("testGetEncodeParam", testGetEncodeParam),
        ("testGetAuthorization", testGetAuthorization),
    ]
}
