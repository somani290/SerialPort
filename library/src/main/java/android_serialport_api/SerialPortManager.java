package android_serialport_api;

import android.util.Log;
import android.view.ViewManager;

import com.rairmmd.serialport.OnDataReceiverListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialPortManager {

    private static final String TAG = "SerialPortManager";

    private SerialPortFinder mSerialPortFinder;

    private String mDeviceName;
    private int mBaudRate;
    private SerialPort mSerialPort;

    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadCOMThread mReadCOMThread;

    private OnDataReceiverListener onDataReceiverListener;

    /**
     * 机器控制
     *
     * @param devName  串口设备名
     * @param baudRate 波特率
     *                 <p>
     *                 例如 devName = "/dev/ttyS3"，baudRate =9600。
     */
    public SerialPortManager(String devName, int baudRate) {
        mSerialPortFinder = new SerialPortFinder();
        mDeviceName = devName;
        mBaudRate = baudRate;
        mSerialPort = null;
    }

//    private SerialPortManager() {
//        mSerialPortFinder = new SerialPortFinder();
//        mSerialPort = null;
//    }
//
//    private static class SerialPortManagerHolder {
//        private static SerialPortManager instance = new SerialPortManager();
//    }
//
//    public static SerialPortManager getInstance() {
//        return SerialPortManagerHolder.instance;
//    }
//
//    public void setDeviceName(String deviceName) {
//        this.mDeviceName = deviceName;
//    }
//
//    public void setBaudRate(int baudRate) {
//        this.mBaudRate = baudRate;
//    }

    /**
     * 枚举所有串口的设备名。
     *
     * @return 串口的设备名数组
     */
    public String[] getCOMList() {
        return mSerialPortFinder.getAllDevicesPath();
    }

    /**
     * 打开串口
     *
     * @return 是否成功
     */
    public boolean openCOM() {

        String[] comList = getCOMList();
        for (String comname : comList) {
            Log.i(TAG, "comList : " + comname);
        }
        if (mSerialPort == null) {
            try {
                mSerialPort = new SerialPort(new File(mDeviceName), mBaudRate, 0);
                Log.d("SerialPort","Line 1");
                mOutputStream = mSerialPort.getOutputStream();
                Log.d("SerialPort","Line 2");
                mInputStream = mSerialPort.getInputStream();
                Log.d("SerialPort","Line 3");
                //Start reading serial port data thread
                mReadCOMThread = new ReadCOMThread();
                Log.d("SerialPort","Line 4");
                mReadCOMThread.start();
                Log.d("SerialPort","Line 5");
                return true;
            } catch (Exception e) {
                Log.i(TAG, e.getMessage());
                mSerialPort = null;
            }
        }
        return false;
    }

    /**
     * 关闭串口
     */
    public void closeCOM() {
        if (mSerialPort != null) {
            mReadCOMThread.interrupt();
            mSerialPort.closeIOStream();
            mSerialPort.close();
            mSerialPort = null;
        }
    }

    /**
     * 发送报文
     *
     * @param data 报文
     * @return 是否成功
     */
    public boolean sendCMD(byte[] data) {
        try {
            if (mOutputStream != null) {
                Log.d("SerialPort","send line 1");
                mOutputStream.write(data);
                Log.d("SerialPort","send line 2");

                mOutputStream.flush();
            } else {
                Log.d("SerialPort","Error in sendCMD");
                return false;
            }
        } catch (IOException e) {
            Log.i(TAG, e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 读取串口数据
     */
    private class ReadCOMThread extends Thread {

        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    byte[] buffer = new byte[24];
                    if (mInputStream == null) {
                        break;
                    }
                    int size = mInputStream.read(buffer);
                    if (size > 0) {
                        Thread.sleep(500);
                        onDataReceiverListener.onDataReceiver(buffer, size);
                    }
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                    break;
                } catch (InterruptedException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
    }

    /**
     * 设置回调监听
     *
     * @param onDataReceiverListener onDataReceiverListener
     */
    public void setOnDataReceiverListener(OnDataReceiverListener onDataReceiverListener) {
        this.onDataReceiverListener = onDataReceiverListener;
    }

}
