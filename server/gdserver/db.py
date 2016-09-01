#coding:utf-8

from __future__ import absolute_import, division, print_function, \
    with_statement
__author__ = 'daimin'

from gdserver import conf, torndb


class Db(object):
    _conn = None

    @staticmethod
    def get_conn():
        if Db._conn is None:
            return torndb.Connection(
                             host = "%s:%d" % (conf.MYSQL_CONFIG['host'], conf.MYSQL_CONFIG['port']),
                             database = conf.MYSQL_CONFIG['db'],
                             user = conf.MYSQL_CONFIG['user'],
                             password = conf.MYSQL_CONFIG['passwd'])
        return Db._conn


    @staticmethod
    def return_result(result):
        if result is not None:
            result['conn'] = Db.get_conn()
        return result