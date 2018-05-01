# coding:utf-8
from __future__ import absolute_import, division, print_function, \
    with_statement
from gevent.server import StreamServer
from handler import *
import comm
import signal
import gevent
import sys

__author__ = 'daimin'


class JBServer(object):

    _handlers = {}
    client_dict = {}
    app_dict = {}

    def __init__(self):
        self._register_handlers()

    def _register_handlers(self):
        print("Register handlers")
        
        self._handlers[protocol.DEFAULT.TID] = DefaultHandler(self)
        self._handlers[protocol.VERSION.TID] = VersionHandler(self)
        self._handlers[protocol.HEARTBEAT.TID] = HeartbeatHandler(self)
        self._handlers[protocol.LOGIN.TID] = LoginHandler(self)

    def on_message(self, sock, tid, message):
        message = message.strip()
        msg_obj = msg.Message(int(tid), data=message)
        handler = self._handlers.get(tid, self._handlers[protocol.DEFAULT.TID])
        handler.request(msg_obj, sock)

    def mainloop(self, socket_, address):
        """mainloop方法对应每个客户端都是一个协程
        """
        print("Client %s connect ... " % str(address))
        jb_sock = JBSocket(socket_, comm.tuple_as_md5(socket_.getpeername()))
        if jb_sock.sid not in JBServer.client_dict:
            self.client_dict[jb_sock.sid] = jb_sock

        while 1:
            try:
                header_data = jb_sock.recv(6)
                if header_data:
                    print(repr(header_data))
                    tid, len_ = comm.struct_unpack(header_data)
                    print("%s %s" % (str(tid), str(len_)))
                    content_data = jb_sock.recv(len_)
                    if content_data:
                        content_data = comm.unpack_data(content_data)
                        if not content_data:
                            continue
                        print(content_data)
                        self.on_message(jb_sock, tid, content_data)
                else:
                    break
            except Exception, e:
                comm.print_exception(e)
                break
        print('Client %s disconnected.' % (str(address), ))
        # 从客户socket列表中关闭并删除掉已经断开的socket
        jb_sock.close()
        self.finalize(JBServer.client_dict[jb_sock.sid])
        del JBServer.client_dict[jb_sock.sid]

    def finalize(self, sock_data):
        for k, h in self._handlers.iteritems():
            h.finalize(sock_data)

    def send_message(self, sock, msg_):
        if msg_.TID < protocol.ERR_NONE:
            self.do_send_message(sock, msg_.TID, msg_.data, msg_.echo)
        else:
            msg_.echo = msg_.data  # 可能默认是填写的data变量
            self.do_send_message(sock, msg_.TID, None, msg_.echo)

    def do_send_message(self, sock, tid, message=None, echo_msg=None):
        # for sid, client_sock in JBServer.client_dict.iteritems():
        #     if client_sock is not sock:
        #         if message is not None:
        #             client_sock.sendall(comm.pack_data(tid, message))
        if echo_msg is not None:
            sock.sendall(comm.pack_data(tid, echo_msg))
            #for pairsock in sock.teams:
            #    pairsock.sendall(comm.pack_data(tid, message))

    def runserver(self, host, port):
        reload(sys)
        sys.setdefaultencoding('utf-8')

        server = StreamServer((host, port), self.mainloop)
        gevent.signal(signal.SIGTERM, server.close)
        gevent.signal(signal.SIGQUIT, server.close)
        gevent.signal(signal.SIGINT, server.close)

        # to start the gdserver asynchronously, use its start() method;
        # we use blocking serve_forever() here because we have no other jobs
        print('Starting gdserver on port %s' % port)
        server.serve_forever()


class JBSocket(object):

    sid = ''

    def __init__(self, sock, sid):
        self._sock = sock
        self.sid = sid
        self._data = {}
        self.teams = []  #小组

    def recv(self, size_):
        return self._sock.recv(size_)

    def sendall(self, data):
        return self._sock.sendall(data)

    def get_sock(self):
        return self._sock

    def close(self):
        return self._sock.close()

    def set_data(self, k, v):
        self._data[k] = v

    def get_data(self, k):
        return self._data.get(k, None)

if __name__ == '__main__':
    JBServer().runserver('0.0.0.0', 14395)
