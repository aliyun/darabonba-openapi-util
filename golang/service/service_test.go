package service

import (
	"io"
	"reflect"
	"strings"
	"testing"

	"github.com/alibabacloud-go/tea/tea"
	"github.com/alibabacloud-go/tea/utils"
)

func Test_GetROASignature(t *testing.T) {
	request := tea.NewRequest()
	sign := GetStringToSign(request)
	signature := GetROASignature(sign, tea.String("secret"))
	utils.AssertEqual(t, 28, len(tea.StringValue(signature)))
}

func Test_Sorter(t *testing.T) {
	tmp := map[string]string{
		"key":   "ccp",
		"value": "ok",
	}
	sort := newSorter(tmp)
	sort.Sort()

	len := sort.Len()
	utils.AssertEqual(t, len, 2)

	isLess := sort.Less(0, 1)
	utils.AssertEqual(t, isLess, true)

	sort.Swap(0, 1)
	isLess = sort.Less(0, 1)
	utils.AssertEqual(t, isLess, false)
}

type TestCommon struct {
	Body io.Reader `json:"Body"`
	Test string    `json:"Test"`
}

func Test_Convert(t *testing.T) {
	in := &TestCommon{
		Body: strings.NewReader("common"),
		Test: "ok",
	}
	out := new(TestCommon)
	Convert(in, &out)
	utils.AssertEqual(t, "ok", out.Test)
}

func Test_getStringToSign(t *testing.T) {
	request := tea.NewRequest()
	request.Query = map[string]*string{
		"roa":  tea.String("ok"),
		"null": tea.String(""),
	}
	request.Headers = map[string]*string{
		"x-acs-meta": tea.String("user"),
	}
	str := getStringToSign(request)
	utils.AssertEqual(t, 33, len(str))
}

func Test_ToForm(t *testing.T) {
	filter := map[string]interface{}{
		"client": "test",
		"tag": map[string]*string{
			"key": tea.String("value"),
		},
		"strs": []string{"str1", "str2"},
	}

	result := ToForm(filter)
	utils.AssertEqual(t, "client=test&strs.1=str1&strs.2=str2&tag.key=value", tea.StringValue(result))
}

func Test_flatRepeatedList(t *testing.T) {
	filter := map[string]interface{}{
		"client":  "test",
		"version": "1",
		"null":    nil,
		"slice": []interface{}{
			map[string]interface{}{
				"map": "valid",
			},
			6,
		},
		"map": map[string]interface{}{
			"value": "ok",
		},
	}

	result := make(map[string]*string)
	for key, value := range filter {
		filterValue := reflect.ValueOf(value)
		flatRepeatedList(filterValue, result, key)
	}
	utils.AssertEqual(t, tea.StringValue(result["slice.1.map"]), "valid")
	utils.AssertEqual(t, tea.StringValue(result["slice.2"]), "6")
	utils.AssertEqual(t, tea.StringValue(result["map.value"]), "ok")
	utils.AssertEqual(t, tea.StringValue(result["client"]), "test")
	utils.AssertEqual(t, tea.StringValue(result["slice.1.map"]), "valid")
}

func Test_GetRPCSignature(t *testing.T) {
	signed := map[string]*string{
		"test": tea.String("ok"),
	}

	sign := GetRPCSignature(signed, tea.String(""), tea.String("accessKeySecret"))
	utils.AssertEqual(t, "jHx/oHoHNrbVfhncHEvPdHXZwHU=", tea.StringValue(sign))
}

func Test_GetTimestamp(t *testing.T) {
	stamp := GetTimestamp()
	utils.AssertNotNil(t, stamp)
}

func Test_Query(t *testing.T) {
	filter := map[string]interface{}{
		"client": "test",
		"tag": map[string]string{
			"key": "value",
		},
		"strs": []string{"str1", "str2"},
	}

	result := Query(filter)
	utils.AssertEqual(t, "test", tea.StringValue(result["client"]))
	utils.AssertEqual(t, "value", tea.StringValue(result["tag.key"]))
	utils.AssertEqual(t, "str1", tea.StringValue(result["strs.1"]))
	utils.AssertEqual(t, "str2", tea.StringValue(result["strs.2"]))
}

func Test_ArrayToStringWithSpecifiedStyle(t *testing.T) {
	strs := []interface{}{tea.String("ok"), "test", 2, tea.Int(3)}

	result := ArrayToStringWithSpecifiedStyle(strs, tea.String("instance"), tea.String("repeatList"))
	utils.AssertEqual(t, "instance.1=ok&&instance.2=test&&instance.3=2&&instance.4=3", tea.StringValue(result))
	result = ArrayToStringWithSpecifiedStyle(strs, tea.String("instance"), tea.String("json"))
	utils.AssertEqual(t, "[\"ok\",\"test\",2,3]", tea.StringValue(result))
	result = ArrayToStringWithSpecifiedStyle(strs, tea.String("instance"), tea.String("simple"))
	utils.AssertEqual(t, "ok,test,2,3", tea.StringValue(result))
	result = ArrayToStringWithSpecifiedStyle(strs, tea.String("instance"), tea.String("spaceDelimited"))
	utils.AssertEqual(t, "ok test 2 3", tea.StringValue(result))
	result = ArrayToStringWithSpecifiedStyle(strs, tea.String("instance"), tea.String("pipeDelimited"))
	utils.AssertEqual(t, "ok|test|2|3", tea.StringValue(result))
	result = ArrayToStringWithSpecifiedStyle(strs, tea.String("instance"), tea.String("piDelimited"))
	utils.AssertEqual(t, "", tea.StringValue(result))
	result = ArrayToStringWithSpecifiedStyle(nil, tea.String("instance"), tea.String("pipeDelimited"))
	utils.AssertEqual(t, "", tea.StringValue(result))
}

type Str struct {
	Key string `json:"key"`
}

func Test_ParseToMap(t *testing.T) {
	in := &Str{
		Key: "value",
	}
	res := ParseToMap(in)
	utils.AssertEqual(t, res["key"], "value")

	in0 := map[string]*Str{"test": in}
	res = ParseToMap(in0)
	utils.AssertEqual(t, res["test"], map[string]interface{}{"key": "value"})

	res = ParseToMap(nil)
	utils.AssertNil(t, res)
}

func Test_GetEndpoint(t *testing.T) {
	endpoint := GetEndpoint(tea.String("common.aliyuncs.com"), tea.Bool(true), tea.String("internal"))
	utils.AssertEqual(t, "common-internal.aliyuncs.com", tea.StringValue(endpoint))

	endpoint = GetEndpoint(tea.String("common.aliyuncs.com"), tea.Bool(true), tea.String("accelerate"))
	utils.AssertEqual(t, "oss-accelerate.aliyuncs.com", tea.StringValue(endpoint))

	endpoint = GetEndpoint(tea.String("common.aliyuncs.com"), tea.Bool(true), tea.String(""))
	utils.AssertEqual(t, "common.aliyuncs.com", tea.StringValue(endpoint))
}

func Test_HexEncode(t *testing.T) {
	res := HexEncode(Hash([]byte("test"), tea.String("ACS3-HMAC-SHA256")))
	utils.AssertEqual(t, "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08", tea.StringValue(res))

	res = HexEncode(Hash([]byte("test"), tea.String("ACS3-RSA-SHA256")))
	utils.AssertEqual(t, "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08", tea.StringValue(res))

	res = HexEncode(Hash([]byte("test"), tea.String("ACS3-HMAC-SM3")))
	utils.AssertEqual(t, "55e12e91650d2fec56ec74e1d3e4ddbfce2ef3a65890c2a19ecf88a307e76a23", tea.StringValue(res))

	res = HexEncode(Hash([]byte("test"), tea.String("ACS3-HM-SM3")))
	utils.AssertEqual(t, "", tea.StringValue(res))
}

func Test_GetEncodePath(t *testing.T) {
	res := GetEncodePath(tea.String("/path/ test"))
	utils.AssertEqual(t, "/path/%20test", tea.StringValue(res))
}

func Test_GetEncodeParam(t *testing.T) {
	res := GetEncodeParam(tea.String("a/b/c/ test"))
	utils.AssertEqual(t, "a%2Fb%2Fc%2F%20test", tea.StringValue(res))
}

func Test_GetAuthorization(t *testing.T) {
	query := map[string]*string{
		"test":  tea.String("ok"),
		"empty": tea.String(""),
	}

	headers := map[string]*string{
		"x-acs-test": tea.String("http"),
		"x-acs-TEST": tea.String("https"),
	}
	req := &tea.Request{
		Query:   query,
		Headers: headers,
	}
	req.Pathname = tea.String("")
	res := GetAuthorization(req, tea.String("ACS3-HMAC-SHA256"),
		tea.String("55e12e91650d2fec56ec74e1d3e4ddbfce2ef3a65890c2a19ecf88a307e76a23"),
		tea.String("acesskey"), tea.String("secret"))
	utils.AssertEqual(t, "ACS3-HMAC-SHA256 Credential=acesskey,SignedHeaders=x-acs-test,Signature=4ab59fffe3c5738ff8a2729f90cc04fe18b02a4b15b2102cbaf92f9ff3df2ea3", tea.StringValue(res))
}

func Test_SignatureMethod(t *testing.T) {
	priKey := `MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAKzSQmrnH0YnezZ9
	8NK50WjMuci0hgGVcSthIZOTWMIySznY9Jj1hlvek7W0uYagtFHz03BHQnHAb5Xs
	0DZm0Sj9+5r79GggwEzTJDYEsLyFwXM3ZOIxqxL4sRg94MHsa81M9NXGHMyMvvff
	QTn1OBVLTVz5jgJ48foMn7j7r9kRAgMBAAECgYEAnZppw3/ef2XF8Z3Mnv+iP0Zk
	LuqiQpN8TykXK7P1/7NJ8wktlshhrSo/3jdf8axghVQsgHob2Ay8Nidugg4lsxIL
	AUBHvfQsQp1MAWvxslsVj+ddw01MQnt8kHmC/qhok+YuNqqAGBcoD6cthRUjEri6
	hfs599EfPs2DcWW06qECQQDfNqUUhcDQ/SQHRhfY9UIlaSEs2CVagDrSYFG1wyG+
	PXDSMes9ZRHsvVVBmNGmtUTg/jioTU3yuPsis5s9ppbVAkEAxjTAQxv5lBBm/ikM
	TzPShljxDZnXh6lKWG9gR1p5fKoQTzLyyhHzkBSFe848sMm68HWCX2wgIpQLHj0G
	ccYPTQJAduMKBeY/jpBlkiI5LWtj8b0O2G2/Z3aI3ehDXQYzgLoEz0+bNbYRWAB3
	2lpkv+AocZW1455Y+ACichcrhiimiQJAW/6L5hoL4u8h/oFq1zAEXJrXdyqaYLrw
	aM947mVN0dDVNQ0+pw9h7tO3iNkWTi+zdnv0APociDASYPyOCyyUWQJACMNRM1/r
	boXuKfMmVjmmz0XhaDUC/JkqSwIiaZi+47M21e9BTp1218NA6VaPgJJHeJr4sNOn
	Ysx+1cwXO5cuZg==`
	res := SignatureMethod("secret", "source", "ACS3-HMAC-SM3")
	utils.AssertEqual(t, "b9ff646822f41ef647c1416fa2b8408923828abc0464af6706e18db3e8553da8", tea.StringValue(HexEncode(res)))

	res = SignatureMethod("secret", "source", "ACS3-RSA-SHA256")
	utils.AssertEqual(t, "", tea.StringValue(HexEncode(res)))

	res = SignatureMethod(priKey, "source", "ACS3-RSA-SHA256")
	utils.AssertEqual(t, "a00b88ae04f651a8ab645e724949ff435bbb2cf9a37aa54323024477f8031f4e13dc948484c5c5a81ba53a55eb0571dffccc1e953c93269d6da23ed319e0f1ef699bcc9823a646574628ae1b70ed569b5a07d139dda28996b5b9231f5ba96141f0893deec2fbf54a0fa2c203b8ae74dd26f457ac29c873745a5b88273d2b3d12", tea.StringValue(HexEncode(res)))
}
