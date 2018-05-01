#coding:utf-8
from __future__ import absolute_import, division, print_function, \
    with_statement

__author__ = 'daimin'

import struct
import protocol, conf, message
import random
import hashlib
import logging
import base64


def pack_data(type_, data):
    encode_data = base64.standard_b64encode(data)
    full_data = struct.pack("!hi", type_, len(encode_data)) + encode_data
    print(repr(full_data))
    return full_data


def struct_unpack(data):
    return struct.unpack_from("!hi", data[:6])


def unpack_data(data):
    data = data.encode('utf-8')
    data = base64.standard_b64decode(data)
    return data.rstrip("\0")


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