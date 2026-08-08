package com.example.serialhex.usb

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.*
import android.util.Log

class UsbCommunicationHelper(private val context: Context) {
    private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private var connection: UsbDeviceConnection? = null
    private var usbInterface: UsbInterface? = null
    private var endpointIn: UsbEndpoint? = null
    private var endpointOut: UsbEndpoint? = null

    companion object {
        private const val ACTION_USB_PERMISSION = "com.example.serialhex.USB_PERMISSION"
        private const val TARGET_VID = 0x1A86
        private const val TARGET_PID = 0x5512
    }

    fun findDevice(): UsbDevice? {
        return usbManager.deviceList.values.find { 
            it.vendorId == TARGET_VID && it.productId == TARGET_PID 
        }
    }

    fun requestPermission(device: UsbDevice) {
        val permissionIntent = PendingIntent.getBroadcast(
            context, 0, Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE
        )
        usbManager.requestPermission(device, permissionIntent)
    }

    fun connect(device: UsbDevice): Boolean {
        connection = usbManager.openDevice(device) ?: return false
        usbInterface = device.getInterface(0)
        
        // Find Bulk Endpoints
        for (i in 0 until usbInterface!!.endpointCount) {
            val ep = usbInterface!!.getEndpoint(i)
            if (ep.type == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                if (ep.direction == UsbConstants.USB_DIR_IN) endpointIn = ep
                else endpointOut = ep
            }
        }

        return connection?.claimInterface(usbInterface, true) ?: false
    }

    fun writeData(data: ByteArray): Int {
        return connection?.bulkTransfer(endpointOut, data, data.size, 1000) ?: -1
    }

    fun readData(bufferSize: Int = 64): ByteArray {
        val buffer = ByteArray(bufferSize)
        val bytesRead = connection?.bulkTransfer(endpointIn, buffer, bufferSize, 1000) ?: -1
        return if (bytesRead > 0) buffer.copyOfRange(0, bytesRead) else byteArrayOf()
    }

    fun disconnect() {
        connection?.apply {
            releaseInterface(usbInterface)
            close()
        }
        connection = null
    }
}
