#coding:utf-8
from __future__ import absolute_import, division, print_function, \
    with_statement

__author__ = 'daimin'

import struct
from gdserver import protocol, conf, message
import msgpack
import random
from Crypto.Cipher import AES
import hashlib
import logging
import base64
import zlib


def pack_data(type_, data):
    print("=========" + data)
    data = str(data)
    data = aes_encode(conf.AES_KEY, data)
    data = struct.pack("!HH", type_, len(data)) + data
    # print(repr(data))
    return data


def struct_unpack(data):
    return struct.unpack_from("!HH", data[:4])


def unpack_data(data):
    return aes_decode(conf.AES_KEY, data)


def msgpack_pack(data):
    return msgpack.packb(data)


def msgpack_unpack(data):
    return msgpack.unpackb(data)

"""
AES加密解密，支持AES/CBC/PKCS5Padding =======================================
"""
BS = 16
pad = lambda s: s + (BS - len(s) % BS) * chr(BS - len(s) % BS)
unpad = lambda s : s[0:-ord(s[-1])]


def aes_encode(key, data):
    cryptor = AES.new(key, AES.MODE_CBC, "\0" * 16)

    data = pad(data)
    senddata = base64.encodestring(cryptor.encrypt(data))

    return zlib.compress(senddata)


def aes_decode(key, data):
    data = base64.decodestring(zlib.decompress(data))
    cryptor = AES.new(key, AES.MODE_CBC, "\0" * 16)
    
    plain_text  = unpad(cryptor.decrypt(data))
    return plain_text.rstrip("\0")
"""
==============================================================================
"""

def create_nonce_str(length=16):
    chars = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'
    str_ = ''
    for i in xrange(length):
        rpos = random.randint(0, len(chars) - 1)
        str_ = '%s%s' % (str_, chars[rpos : rpos + 1])
    return str_


def get_protocols():
    protos = {}
    lis = dir(protocol)
    for li in lis:
        m = getattr(protocol, li)
        if isinstance(m, message.Message):
            m.label = li
            protos[m.TID] = m

    return protos


def to_bytes(s):
    if bytes != str:
        if type(s) == str:
            return s.encode('utf-8')
    return s


def to_str(s):
    if bytes != str:
        if type(s) == bytes:
            return s.decode('utf-8')
    return s


def str2utf8(str_):
    return str_.decode("gbk").encode("utf-8")


def md5(src, t='lower'):
    src = "{}-{}".format(conf.MD5_SALT, src)
    if t is 'lower':
        return hashlib.md5(src).hexdigest().lower()
    else:
        return hashlib.md5(src).hexdigest().upper()


def print_exception(e):
    logging.error(e)
    if conf.DEBUG:
        import traceback
        traceback.print_exc()


def tuple_as_md5(tuple_):
    tuple_ = tuple(tuple_)
    return md5(str(tuple_), 'upper')


if __name__ == '__main__':
    s = "hello"
    # print(len(s))
    # etext = aes_encode(conf.AES_KEY, s)
    # print(etext)
    # print(len(etext))
    # s = pad('11122222222ddddddddddd22ddddddddddddddddd22')
    # print(len(s))
    # print(base64.encodestring(zlib.compress('hello', 9)))

    # print(zlib.decompress(base64.decodestring('eNrLSM3JyQcABiwCFQ==')))
    # d = base64.encodestring(s)
    # print(d)

    # print(etext)
    # print(aes_decode(conf.AES_KEY, etext))
    # print(aes_decode(conf.AES_KEY, aes_decode(conf.AES_KEY, 'jUIfDF/CrdLF0kEsf/D49UNkOSnKaSaJxXJyeaSisvM=')))
    print(tuple_as_md5(('2222',)))