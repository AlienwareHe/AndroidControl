/*
 *
 * MIT License
 *
 * Copyright (c) 2017 朱辉 https://blog.yeetor.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.yeetor.adb;

import com.alibaba.fastjson.JSON;
import com.android.ddmlib.IDevice;
import com.yeetor.androidcontrol.DeviceInfo;
import com.yeetor.util.Util;

import java.util.ArrayList;
import java.util.List;

public class AdbUtils {

    /**
     * 将当前连接的设备列表转换为json
     * @return
     */
    public static String devices2JSON() {
        return devices2JSON(AdbServer.server().getDevices());
    }
    
    public static String devices2JSON(List<AdbDevice> devices) {
        ArrayList<DeviceInfo> list = new ArrayList<DeviceInfo>();
        for (AdbDevice device : devices) {
            list.add(new DeviceInfo(device));
        }
        return JSON.toJSONString(list);
    }


    public static void removeForward(AdbDevice adbDevice,AdbForward forward) {
        if (forward == null || !forward.isForward()) {
            return;
        }
        try {
            adbDevice.getIDevice().removeForward(forward.getPort(), forward.getLocalAbstract(), IDevice.DeviceUnixSocketNamespace.ABSTRACT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static AdbForward createForward(AdbDevice adbDevice){
        AdbForward adbForward = generateForwardInfo(adbDevice);
        try {
            adbDevice.getIDevice().createForward(adbForward.getPort(), adbForward.getLocalAbstract(), IDevice.DeviceUnixSocketNamespace.ABSTRACT);
            return adbForward;
        } catch (Exception e) {
            System.out.println("create forward failed");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 生成forward信息
     */
    private static AdbForward generateForwardInfo(AdbDevice adbDevice) {
        AdbForward[] forwards = AdbServer.server().getForwardList();
        // serial_touch_number
        int maxNumber = 0;
        if (forwards.length > 0) {
            for (AdbForward forward : forwards) {
                if (forward.getSerialNumber().equals(adbDevice.getIDevice().getSerialNumber())) {
                    String l = forward.getLocalAbstract();
                    String[] s = l.split("_");
                    if (s.length == 3) {
                        int n = Integer.parseInt(s[2]);
                        if (n > maxNumber) maxNumber = n;
                    }
                }
            }
        }
        maxNumber += 1;

        String forwardStr = String.format("%s_touch_%d", adbDevice.getIDevice().getSerialNumber(), maxNumber);
        int freePort = Util.getFreePort();
        return new AdbForward(adbDevice.getIDevice().getSerialNumber(), freePort, forwardStr);
    }
    
}
