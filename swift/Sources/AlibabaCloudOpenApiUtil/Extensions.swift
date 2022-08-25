import Foundation

extension String {
    static func randomString(len: Int, randomDict: String = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ") -> String {
        var ranStr = ""
        for _ in 0..<len {
            let index = Int(arc4random_uniform(UInt32(randomDict.count)))
            ranStr.append(randomDict[randomDict.index(randomDict.startIndex, offsetBy: index)])
        }
        return ranStr
    }

    func urlEncode() -> String {
        let unreserved = "*-._"
        let allowedCharacterSet = NSMutableCharacterSet.alphanumeric()
        allowedCharacterSet.addCharacters(in: unreserved)
        allowedCharacterSet.addCharacters(in: " ")
        var encoded = addingPercentEncoding(withAllowedCharacters: allowedCharacterSet as CharacterSet)
        encoded = encoded?.replacingOccurrences(of: " ", with: "%20")
        encoded = encoded?.replacingOccurrences(of: "+", with: "%20")
        encoded = encoded?.replacingOccurrences(of: "*", with: "%2A")
        encoded = encoded?.replacingOccurrences(of: "%7E", with: "~")
        return encoded ?? ""
    }

    func jsonDecode() -> [String: AnyObject] {
        let jsonData: Data = self.data(using: .utf8)!
        guard let data = try? JSONSerialization.jsonObject(with: jsonData, options: .mutableContainers) as? [String: AnyObject] else {
            return [:]
        }
        return data
    }
    
    func toBytes() -> [UInt8] {
        [UInt8](self.utf8)
    }
    
}
