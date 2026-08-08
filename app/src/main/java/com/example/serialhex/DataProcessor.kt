package com.example.serialhex.logic

import java.util.Locale

object DataProcessor {
    /**
     * Searches for a pattern and applies a transformation.
     * Example: Patching a byte at a specific offset.
     */
    fun patchBytes(data: ByteArray, offset: Int, newValue: Byte): ByteArray {
        val result = data.copyOf()
        if (offset in result.indices) {
            result[offset] = newValue
        }
        return result
    }

    /**
     * Basic Checksum Validation (Sum-8)
     */
    fun calculateChecksum(data: ByteArray): String {
        if (data.isEmpty()) return "00"
        var sum = 0
        for (b in data) {
            sum = (sum + b.toInt()) and 0xFF
        }
        return String.format(Locale.US, "%02X", sum)
    }

    /**
     * Formats ByteArray to Hex String for display
     */
    fun toHexString(byte: Byte): String = String.format(Locale.US, "%02X", byte)

    fun toAscii(byte: Byte): Char {
        return if (byte in 32..126) byte.toInt().toChar() else '.'
    }
}
