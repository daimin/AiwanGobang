package com.codecos.gobang;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;


class NetMessage implements Serializable{
    short type;
    String msg;

    NetMessage(short type, String msg) {
        this.type = type;
        this.msg = msg;
    }

}


public class NetClient {


    private Context ctx;
    private String str;

	private boolean connected;
    private Socket mSocket;
    private BufferedInputStream mBufferedInputStream;
    private BufferedOutputStream mBufferedOutputStream;

    /*
      网络配置
     */
    public static final int PORT = 14395;
    public static final String HOST = "127.0.0.1";

    //################################################################


    public static final String PROTO_VERSION = "0.0.1"; // 当前通信协议版本

    /*
     * 协议配置
     *
     */

    public static final short C2L_VERSION = 0x0001;    //通信协议确认
    public static final short L2C_VERSION = 0x1001;    //通信协议确认成功
    public static final short C2L_REG = 0x0002;  // 注册
    public static final short L2C_REG = 0x1002;  // 注册成功
    public static final short C2L_LOGIN = 0x0003;  // 登录
    public static final short L2C_LOGIN = 0x1003;  // 登录成功
    public static final short C2L_KEEPALIVE  = 0x0005;    //定时心跳包
    public static final short L2C_KEEPALIVE = 0x1005;    //心跳返回

    public static final short C2G_LOGIN = 0x2001;    //登录游戏服务器
    public static final short G2C_LOGIN = 0x3001;    //登录游戏服务器返回
    private Handler mhandler;
    private int mwhat;


    //错误信息
    public static final int ERR_PROTO_INVALID = -1;    // 通信协议版本错误

    //###############################################################

    public NetClient(Context ctx, Handler handler) {
        this.ctx = ctx;
        this.mhandler = handler;
        connected();
    }

    /**
     * 检测网络连接是否可用
     *
     * @param ctx
     * @return true 可用; false 不可用
     */
    public static boolean isNetworkAvailable(Context ctx) {

        ConnectivityManager cm = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }
        NetworkInfo[] netinfo = cm.getAllNetworkInfo();
        if (netinfo == null) {
            return false;
        }
        for (int i = 0; i < netinfo.length; i++) {
            if (netinfo[i].isConnected()) {
                return true;
            }
        }
        return false;
    }



    public void send(short type, String msg, int what){
        this.mwhat = what;
        try {
            byte[] strBuf = msg.getBytes();
            ByteBuffer buff = ByteBuffer.allocate(strBuf.length + 6);
            buff.putShort(type);
            buff.putInt(strBuf.length);
            buff.put(strBuf);
            mBufferedOutputStream.write(buff.array());
            mBufferedOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


	public void connected() {
		try {
			mSocket = new Socket(HOST, PORT);
            mBufferedInputStream = new BufferedInputStream(mSocket.getInputStream());
            mBufferedOutputStream = new BufferedOutputStream(mSocket.getOutputStream());
			connected = true;
			new Thread(new Receive()).start();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	public void closeConnected() {
		try {
            if (mBufferedInputStream!= null)
                mBufferedInputStream.close();
            if (mBufferedOutputStream!= null)
                mBufferedOutputStream.close();
			if (mSocket != null)
				mSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    public void setHandler(Handler handler){
        this.mhandler = handler;
    }

    private class Receive implements Runnable {

        private NetMessage resolve(byte[] strBuf){

            ByteBuffer buff = ByteBuffer.wrap(strBuf);
            short dataType = buff.getShort();
            int dataLen = buff.getInt();

            Log.i("NetClient", "bytes = " + Arrays.toString(strBuf) + " dataType = " + dataType + " dataLen = " + dataLen);
            String tmpe = new String(Arrays.copyOfRange(strBuf, 6, dataLen + 6));
            return new NetMessage(dataType, tmpe.trim());
        }

		public void run() {
			String str;
            int len = 0;
            ByteArrayOutputStream bao = null;
			try {
                byte[] buff = new byte[1024];
                bao = new ByteArrayOutputStream();
				while (connected) {
                    while(mBufferedInputStream.read(buff) != -1){
                        bao.write(buff);
                        if(mBufferedInputStream.available() <= 0){
                            break;
                        }
                        Arrays.fill(buff, (byte)0);
                    }

                    bao.flush();

					mhandler.sendMessage(CommUtil.getMessage("result", resolve(bao.toByteArray()), mwhat));

                    bao.reset();
                    Thread.sleep(1);
				}
			} catch (EOFException e) {
				System.out.println("一个客户端退出了！");
			} catch (SocketException e) {
				System.out.println("a client was closed!");
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
                e.printStackTrace();
            } finally {

				try {
                    if(bao != null)
                        bao.close();
					if (mBufferedInputStream!= null)
						mBufferedInputStream.close();
					if (mSocket!= null) {
						mSocket.close();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}

	}
}
