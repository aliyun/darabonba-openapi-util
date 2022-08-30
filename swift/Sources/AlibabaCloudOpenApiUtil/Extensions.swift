import Foundation

extension String {

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
    
    func toBytes() -> [UInt8] {
        [UInt8](self.utf8)
    }
    
}
