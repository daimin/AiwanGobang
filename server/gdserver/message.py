#coding:utf-8
from __future__ import absolute_import, division, print_function, \
    with_statement

__author__ = 'daimin'


class Message(object):
    TID =''       #消息的type ID
    len_ = 0      #消息内容的长度
    data = None     #消息的内容
    echo = None     #回传给发送客户的消息

    def __init__(self, tid, **kwargs):
        self.TID = tid 
        self.data  = kwargs.get('data', None)
        self.echo = kwargs.get('echo', None)

    def eq(self, tid):
        return tid == self.TID

    def set_data(self, data=None):
        if data is not None:
            self.data = str(data)

    def set_echo(self, echo=None):
        if echo is not None:
            self.echo = str(echo)

    def __str__(self):
        return "data = %s, echo = %s" % (str(self.data), str(self.echo))








