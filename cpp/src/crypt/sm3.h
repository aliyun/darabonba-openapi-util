#ifndef ALIBABACLOUD_OPEN_API_UTIL_SRC_CRYPT_SM3_H
#define ALIBABACLOUD_OPEN_API_UTIL_SRC_CRYPT_SM3_H

#include<cmath>
#include <string>
#include <cassert>
#include <sstream>
#include <iomanip>
#include <iostream>
#include <vector>
#include <boost/cstdint.hpp>

using namespace std;

boost::uint32_t rotlConstant(boost::uint32_t r, boost::uint32_t x)
{
  x = x % 32;
  return ((r << x) & 0xFFFFFFFF) | ((r & 0xFFFFFFFF) >> (32 - x));
}

int to_int(int c)
{
  if (isdigit(c))
    return c - '0';
  else {
    if (isupper(c))
      c = tolower(c);
    if (c >= 'a' && c <= 'f')
      return c - 'a' + 10;
  }
  return -1;
}


void binascii_unhexlify(char in[], int inlen, unsigned char out[])
{
  int i, j;
  for (i = j = 0; i < inlen; i += 2) {
    int top = to_int(in[i] & 0xFF);
    int bot = to_int(in[i+1] & 0xFF);
    out[j++] = (top << 4) + bot;
  }
}

class sm3 {

public:
  static const unsigned int BLOCK_SIZE     = 64;
  static const unsigned int HASH_SIZE      = 32;
  static const inline vector<boost::uint32_t> IV = vector<boost::uint32_t>({1937774191, 1226093241, 388252375,
                                                                            3666478592, 2842636476, 372324522,
                                                                            3817729613, 2969243214});
  static const inline vector<boost::uint32_t> TJ = vector<boost::uint32_t>({2043430169, 2043430169, 2043430169, 2043430169, 2043430169,
                                                                            2043430169, 2043430169, 2043430169, 2043430169, 2043430169,
                                                                            2043430169, 2043430169, 2043430169, 2043430169, 2043430169,
                                                                            2043430169, 2055708042, 2055708042, 2055708042, 2055708042,
                                                                            2055708042, 2055708042, 2055708042, 2055708042, 2055708042,
                                                                            2055708042, 2055708042, 2055708042, 2055708042, 2055708042,
                                                                            2055708042, 2055708042, 2055708042, 2055708042, 2055708042,
                                                                            2055708042, 2055708042, 2055708042, 2055708042, 2055708042,
                                                                            2055708042, 2055708042, 2055708042, 2055708042, 2055708042,
                                                                            2055708042, 2055708042, 2055708042, 2055708042, 2055708042,
                                                                            2055708042, 2055708042, 2055708042, 2055708042, 2055708042,
                                                                            2055708042, 2055708042, 2055708042, 2055708042, 2055708042,
                                                                            2055708042, 2055708042, 2055708042, 2055708042});;
  vector<boost::uint32_t> intermediate_hash_;

  static boost::uint32_t FFJ(boost::uint32_t x,
                             boost::uint32_t y,
                             boost::uint32_t z,
                             boost::uint32_t j) {
    boost::uint32_t ret;
    if (0 <= j && j < 16) {
      ret = x ^ y ^ z;
    } else if (16 <= j && j < 64) {
      ret = (x & y) | (x & z) | (y & z);
    }
    return ret;
  }

  static boost::uint32_t GGJ(boost::uint32_t x,
                             boost::uint32_t y,
                             boost::uint32_t z,
                             boost::uint32_t j) {
    boost::uint32_t ret;
    if (0 <= j && j < 16) {
      ret = x ^ y ^ z;
    } else if (16 <= j && j < 64) {
      ret = (x & y) | ((~ x) & z);
    }
    return ret;
  }

  static boost::uint32_t P0(boost::uint32_t x) {
    return x ^ rotlConstant(x, 9) ^ rotlConstant(x, 17);
  }

  static boost::uint32_t P1(boost::uint32_t x) {
    return x ^ rotlConstant(x, 15) ^ rotlConstant(x, 23);
  }

  static vector<boost::uint32_t> CF(vector<boost::uint32_t> v, vector<boost::uint32_t> bi) {
    vector<boost::uint32_t> w;
    for (int i=0;i<16;i++) {
      boost::uint32_t weight = 0x1000000;
      boost::uint32_t data = 0;
      for (int j=i*4;j<(i+1)*4;j++) {
        data = data + bi[j] * weight;
        weight = boost::uint32_t(weight / 0x100);
      }
      w.push_back(data);
    }

    for (int i=16;i<68;i++) {
        w.push_back(0);
        boost::uint32_t p1 = P1(w[i-16] ^ w[i-9] ^ rotlConstant(w[i-3], 15));
        boost::uint32_t p2 = rotlConstant(w[i-13], 7) ^ w[i-6];

        w[i] = P1(w[i-16] ^ w[i-9] ^ rotlConstant(w[i-3], 15)) ^ rotlConstant(w[i-13], 7) ^ w[i-6];
    }
    vector<boost::uint32_t> w1;
    for (int i=0;i<64;i++) {
      w1.push_back(0);
      w1[i] = w[i] ^ w[i+4];
    }

    boost::uint32_t a = v[0];
    boost::uint32_t b = v[1];
    boost::uint32_t c = v[2];
    boost::uint32_t d = v[3];
    boost::uint32_t e = v[4];
    boost::uint32_t f = v[5];
    boost::uint32_t g = v[6];
    boost::uint32_t h = v[7];
    for (int i=0;i<64;i++) {
      boost::uint32_t ss1 = rotlConstant((rotlConstant(a, 12) + e + rotlConstant(TJ[i], i)), 7);
      boost::uint32_t ss2 = ss1 ^ (rotlConstant(a, 12));
      boost::uint32_t tt1 = (FFJ(a, b, c, i) + d + ss2 + w1[i]) & 0xFFFFFFFF;
      boost::uint32_t tt2 = (GGJ(e, f, g, i) + h + ss1 + w[i]) & 0xFFFFFFFF;
      d = c;
      c = rotlConstant(b, 9);
      b = a;
      a = tt1;
      h = g;
      g = rotlConstant(f, 19);
      f = e;
      e = P0(tt2);

      a = a & 0xFFFFFFFF;
      b = b & 0xFFFFFFFF;
      c = c & 0xFFFFFFFF;
      d = d & 0xFFFFFFFF;
      e = e & 0xFFFFFFFF;
      f = f & 0xFFFFFFFF;
      g = g & 0xFFFFFFFF;
      h = h & 0xFFFFFFFF;
    }

    vector<boost::uint32_t> vi1({
         a ^ v[0],
         b ^ v[1],
         c ^ v[2],
         d ^ v[3],
         e ^ v[4],
         f ^ v[5],
         g ^ v[6],
         h ^ v[7]
    });
    return vi1;
  }

  static vector<boost::uint32_t> hash_msg(vector<boost::uint32_t> msg) {
    boost::uint32_t len1 = msg.size();
    boost::uint32_t reserve1 = len1 % 64;
    msg.push_back(0x80);
    reserve1 += 1;
    boost::uint32_t range_end = 56;
    if (reserve1 > range_end) {
      range_end += 64;
    }

    for (int i=reserve1;i<range_end;i++) {
      msg.push_back(0x00);
    }

    boost::uint32_t bit_length = len1 * 8;
    vector<boost::uint32_t> bit_length_str({bit_length % 0x100});
    for (int i=0;i<7;i++) {
      bit_length = boost::uint32_t(bit_length / 0x100);
      bit_length_str.push_back(bit_length % 0x100);
    }

    for (int i=0;i<8;i++) {
      msg.push_back(bit_length_str[7-i]);
    }

    boost::uint32_t group_count = round(msg.size() / 64);

    vector<vector<boost::uint32_t>> b;
    for (int i=0;i<group_count;i++) {
      b.push_back(vector<boost::uint32_t>(msg.begin()+i*64, msg.begin()+(i+1) * 64));
    }

    vector<vector<boost::uint32_t>> v({IV});
    vector<boost::uint32_t> y;
    for (int i=0;i<group_count;i++) {
      v.push_back(CF(v[i], b[i]));
      if (i == group_count-1) {
        y = v[i+1];
      }
    }
    return y;
  }

  static void hash(const std::string& s, boost::uint8_t digest[HASH_SIZE])
  {
    sm3 ctx;
    ctx.input(reinterpret_cast<const boost::uint8_t*>(s.c_str()), s.size());
    ctx.result(digest);
  }

  void input(const boost::uint8_t *message_array, unsigned int length)
  {
    for (int i=0;i<length;i++) {
      intermediate_hash_.push_back(message_array[i]);
    }
  }

  void result(boost::uint8_t digest[HASH_SIZE])
  {
    vector<uint32_t> res_arr = hash_msg(intermediate_hash_);
    char s[BLOCK_SIZE];
    sprintf(s, "%08x%08x%08x%08x%08x%08x%08x%08x",
            res_arr[0], res_arr[1], res_arr[2],
            res_arr[3], res_arr[4], res_arr[5],
            res_arr[6], res_arr[7]);
    binascii_unhexlify(s, BLOCK_SIZE, digest);
  }

  void reset() {
    intermediate_hash_.clear();
  }
}; // end of class

#endif //ALIBABACLOUD_OPEN_API_UTIL_SRC_CRYPT_SM3_H
