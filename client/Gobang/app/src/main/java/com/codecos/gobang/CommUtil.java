package com.codecos.gobang;

import android.os.Bundle;
import android.os.Message;

import java.io.Serializable;

/**
 * Created by daimin on 15/3/19.
 */
public class CommUtil {

    public static Message getMessage(String k, String v, int what){

        Message msg = new Message();

        Bundle data = new Bundle();
        data.putString(k, v);
        msg.setData(data);
        msg.what = what;
        return msg;
    }

    public static Message getMessage(String k, Serializable v, int what){

        Message msg = new Message();

        Bundle data = new Bundle();
        data.putSerializable(k, v);
        msg.setData(data);
        msg.what = what;
        return msg;
    }

    public static NetMessage getResult(Bundle data){
        if(data.isEmpty() ) return null;
        NetMessage msg = (NetMessage) data.getSerializable("result");
        return msg;

    }

    public static String checkProtoResult(NetMessage result){
        if(result != null){
            switch(result.type){
                case NetClient.L2C_VERSION:
                    if(Integer.parseInt(result.msg) == NetClient.ERR_PROTO_INVALID){
                        return "通信协议版本错误";
                    }
                    break;
                default:
                    return "不支持的协议类型";
            }
            return "";
        }else{
            return "未知错误";
        }


    }

    public static String intToIp(int i) {

        return (i & 0xFF ) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16 ) & 0xFF) + "." +
                ( i >> 24 & 0xFF) ;
    }
}
