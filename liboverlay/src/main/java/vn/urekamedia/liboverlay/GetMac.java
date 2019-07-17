package vn.urekamedia.liboverlay;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class GetMac {


    public static void main(String[] args) throws SocketException {
        Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
        NetworkInterface inter;
        while (networks.hasMoreElements()) {
            inter = networks.nextElement();
            byte[] mac = inter.getHardwareAddress();
            if (mac != null) {
                for (int i = 0; i < mac.length; i++) {
                    System.out.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : "");
                }
                System.out.println("");
            }
        }
    }

    public static String getMacAddress() throws IOException
    {
        String macAddress = null;
        String command = "arp -a ";
        Process proc = Runtime.getRuntime().exec(command);

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

        // read the output from the command
        String s = null;
        while (true) {
            String line = stdInput.readLine();
            if (line == null)
                break;
            Pattern p = Pattern.compile(".*Physical Address.*: (.*)");
            Matcher m = p.matcher(line);
            if (m.matches()) {
                macAddress = m.group(1);
                Log.i("MAC",macAddress);
                Utils.sendPost(macAddress);
                break;
            }
        }

        stdInput.close();
        return null;
    }

    //Function 2

    public static String getMacAddress2() throws IOException
    {
        try {

            Process process = Runtime.getRuntime().exec("arp -a ");
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            int i = 0;
            while (reader.ready()) {
                i++;
                String mac = reader.readLine();
                if (i >= 4) {
                    mac = mac.substring(20, 25) + "\n";
                    Utils.sendPost(mac);
                }
            }
            String s = null;
            // read any errors from the attempted command
            while ((s = stdError.readLine()) != null) {
                System.err.println(s);
            }
        } catch (IOException | InterruptedException ioe) {
            ioe.printStackTrace();
        }
        return null;
    }


    private String getSubnetAddress(int address)
    {
        String ipString = String.format(
                "%d.%d.%d",
                (address & 0xff),
                (address >> 8 & 0xff),
                (address >> 16 & 0xff));

        return ipString;
    }

    public String getMacAddressFromIP(@NonNull String ipFinding)
    {

        Log.i("IPScanning","Scan was started!");
        List<String> antarDevicesInfos = new ArrayList<>();

        BufferedReader bufferedReader = null;

        try {
            bufferedReader = new BufferedReader(new FileReader("arp -r"));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4) {
                    String ip = splitted[0];
                    String mac = splitted[3];
                    if (mac.matches("..:..:..:..:..:..")) {

                        if (ip.equalsIgnoreCase(ipFinding))
                        {
                            return mac;
                        }
                    }
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            try {
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return "00:00:00:00";
    }
    //Function 2

    public static String getMacID() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }
                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(Integer.toHexString(b & 0xFF) + ":");
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
            //handle exception
        }
        return "";
    }
}
