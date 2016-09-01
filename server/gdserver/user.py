#coding:utf-8
from __future__ import absolute_import, division, print_function, \
    with_statement

__author__ = 'daimin'

from gdserver import db
from gdserver import  comm
import time


class User(db.Db):

    NORMAL_STATUS  = 1
    DELETE_STATUS  = 0

    READY_ACTION = 1
    PLAYING_ACTION = 2
    CHATTING_ACTION = 3

    tabname = 'c_user'
    conn = None

    id = ''
    sid = None # socket id
    name = ''
    passwd = ''
    sex = ''
    score = ''
    grade = ''
    phone = ''
    devid = ''
    country = ''
    city = ''

    def __init__(self, **kwargs):
        self.id = kwargs.get('id', '')
        self.sid = kwargs.get('sid', None)
        self.name = kwargs.get('name', '')
        self.passwd = kwargs.get('passwd', '')
        self.sex = kwargs.get('sex', 2) # 0 女，1 男，2未知
        self.score = kwargs.get('score', 0)
        self.grade = kwargs.get('grade', 1)
        self.phone = kwargs.get('phone', '')
        self.devid = kwargs.get('devid', '')
        self.country = kwargs.get('country', '')
        self.city = kwargs.get('city', '')
        self.ctime = kwargs.get('ctime', 0)
        self.utime = kwargs.get('utime', 0)
        self.ltime = kwargs.get('ltime', 0)
        self.status = kwargs.get('status', User.NORMAL_STATUS)
        self.action = self.READY_ACTION

        self._dbconn = kwargs.get('conn', None)
        if self._dbconn is None:
            self._dbconn = db.Db.get_conn()

    def __str__(self):
        return super(User, self).__str__()


    def save(self):
        if not self.id:
            u = User.find_user_by_name(self.name)
            if not u:
                self.ctime = int(time.time())
                self.utime = self.ctime
                self.id = self._dbconn.insert("INSERT INTO `%s`(`name`, `passwd`, `sex`, `score`, `grade`, `phone`, `devid`,"
                                    " `country`, `city`, `ctime`, `utime`, `ltime`, `status`) VALUES('%s', '%s', '%s',"
                                    " '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')"
                                    % (self.tabname, self.name, self.passwd, self.sex, self.score, self.grade,
                                       self.phone, self.devid, self.country, self.city, self.ctime, self.utime,
                    self.ltime, self.status))
                return self.id
            else:
                self.id = u.id
                self.utime = int(time.time())
                return self.update()

    def update(self):
        return self._dbconn.update("UPDATE  `%s` SET `name`='%s', `passwd`='%s', `sex`='%s', `score`='%s', `grade`='%s',\
         `phone`='%s', `devid`='%s', `country`='%s', `city`='%s', `ctime`='%s', `utime`='%s', `ltime`='%s', `status`='%s'\
          WHERE `id`='%s'" % (self.tabname, self.name, self.passwd, self.sex,\
           self.score, self.grade, self.phone, self.devid, self.country, self.city, self.ctime, self.utime,\
           self.ltime, self.status, self.id))

    @staticmethod
    def find_user(id):
        conn = db.Db.get_conn()
        result = db.Db.return_result(conn.get("SELECT * FROM `%s` WHERE `id` = '%s'" % (User.tabname, id)))
        if result:
            return User(**result)

    @staticmethod
    def find_user_by_name(name):
        conn = db.Db.get_conn()
        result = db.Db.return_result(conn.get("SELECT * FROM `%s` WHERE `name` = '%s'" % (User.tabname, name)))
        if result:
            return User(**result)



if __name__ == "__main__":
    # u = User.find_user_by_name('daimin')
    # print(u.name)

    u = User(name='戴敏', passwd=comm.md5('1234'), sex=1, score=100, conn=db.Db.get_conn())
    u.save()

        





