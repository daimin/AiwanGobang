# coding:utf-8
from __future__ import absolute_import, division, print_function, \
    with_statement
from gevent.server import StreamServer
from gdserver import comm
import signal
import gevent
import sys

__author__ = 'daimin'


class JBServer(object):

    client_dict = {}

    def __init__(self):
        pass

    def on_message(self, sock, type_, message):
        pass

    def mainloop(self, socket_, address):
        """mainloop方法对应每个客户端都是一个协程
        """
        jb_sock = JBSocket(socket_, comm.tuple_as_md5(socket_.getpeername()))
        if jb_sock.sid not in JBServer.client_dict:
            self.client_dict[jb_sock.sid] = jb_sock

        while 1:
            try:
                header_data = jb_sock.recv(4)
                if header_data:
                    tid, len_ = comm.struct_unpack(header_data)
                    content_data = jb_sock.recv(len_)
                    if content_data:
                        content_data = comm.unpack_data(content_data)
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
        pass

    def do_send_message(self, sock, tid, message=None, echo_msg=None):
        # for sid, client_sock in JBServer.client_dict.iteritems():
        #     if client_sock is not sock:
        #         if message is not None:
        #             client_sock.sendall(comm.pack_data(tid, message))

        if echo_msg is not None:
            sock.sendall(comm.pack_data(tid, echo_msg))
            for pairsock in sock.teams:
                pairsock.sendall(comm.pack_data(tid, message))

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
    JBServer().runserver('0.0.0.0', 5005)
