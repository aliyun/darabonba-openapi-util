#ifndef ALIBABACLOUD_OPEN_API_UTIL_SRC_CRYPT_RSA_H
#define ALIBABACLOUD_OPEN_API_UTIL_SRC_CRYPT_RSA_H

#include <iostream>
#include <openssl/aes.h>
#include <openssl/evp.h>
#include <openssl/rsa.h>
#include <openssl/pem.h>
#include <openssl/ssl.h>
#include <openssl/bio.h>
#include <openssl/err.h>
#include <cassert>
#include <string>
#include <cstring>
#include <memory>
#include <vector>
#include <fstream>

namespace RsaSha256{
  vector<uint8_t> RSASignAction(string strEnData, string prikey)
  {
    BIO* in = BIO_new_mem_buf((void*)prikey.c_str(), -1);
    int nlen = strEnData.length();

    RSA *prsa = PEM_read_bio_RSAPrivateKey(in, NULL, NULL, NULL);;
    if (NULL == prsa)
    {
      RSA_free(prsa);
      printf("获取私钥失败\n");
      return vector<uint8_t>();
    }

    char  szTmp[1024] = { 0 };
    SHA256((const unsigned char*)strEnData.c_str(), nlen, (unsigned char*)szTmp);

    int nLength;
    unsigned int nLengthRet;
    char  szTmp1[1024] = { 0 };
    nLength = RSA_sign(NID_sha256, (unsigned char *)szTmp, 32, (unsigned char*)szTmp1, &nLengthRet, prsa);
    if (nLength != 1)
    {
      RSA_free(prsa);
      return vector<uint8_t>();
    }

    RSA_free(prsa);
    return vector<uint8_t>(&szTmp1[0], &szTmp1[nLengthRet]);
  }
}


#endif //ALIBABACLOUD_OPEN_API_UTIL_SRC_CRYPT_RSA_H
