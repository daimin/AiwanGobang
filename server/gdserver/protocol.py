# coding:utf-8
from __future__ import absolute_import, division, print_function, \
    with_statement

__author__ = 'daimin'

from gdserver.message import Message

DEFAULT         = Message(0x0000)
VERSION         = Message(0x0001)
HEARTBEAT       = Message(0x0002)
LOGIN           = Message(0x0003)
RANDOM_CHAT     = Message(0x0004)
FIND_CHAT       = Message(0x0005)
SEND_CONT       = Message(0x0006)
RECV_CONT       = Message(0x0007)
OVER_CHAT       = Message(0x0008)
LOGOUT          = Message(0x0009)
OK              = DEFAULT

# 大于等于0x8000用于错误表示
ERR_NONE            = Message(0x8000)
ERR_VERSION         = Message(0x8001, data=u'错误的版本')
ERR_NOT_LOGIN       = Message(0x8002, data=u'你还没有登录')
ERR_LOGIN_FAIL      = Message(0x8003, data=u'登录失败')
ERR_RANDOM_FIND     = Message(0x8004)
ERR_FIND_CHAT       = Message(0x8005)
ERR_NO_SUPPORT      = Message(0x8006, data=u'不支持的协议')
ERR_SEND_CONT       = Message(0x8007, data=u'发送消息失败')



def get_S2C_proto(tid):
    return Message(int(tid) + 0x1000)

