package be.mygod.vpnhotspot.net.wifi.configuration

import android.net.wifi.WifiConfiguration
import androidx.annotation.RequiresApi
import be.mygod.vpnhotspot.net.wifi.WifiApManager
import timber.log.Timber
import java.lang.reflect.InvocationTargetException

val WPA2_PSK = WifiConfiguration.KeyMgmt.strings.indexOf("WPA2_PSK")

/**
 * apBand and apChannel is available since API 23.
 *
 * https://android.googlesource.com/platform/frameworks/base/+/android-6.0.0_r1/wifi/java/android/net/wifi/WifiConfiguration.java#242
 */
private val apBandField by lazy { WifiConfiguration::class.java.getDeclaredField("apBand") }
private val apChannelField by lazy { WifiConfiguration::class.java.getDeclaredField("apChannel") }

/**
 * 2GHz band.
 *
 * https://android.googlesource.com/platform/frameworks/base/+/android-7.0.0_r1/wifi/java/android/net/wifi/WifiConfiguration.java#241
 */
@RequiresApi(23)
const val AP_BAND_2GHZ = 0
/**
 * 5GHz band.
 */
@RequiresApi(23)
const val AP_BAND_5GHZ = 1
/**
 * Device is allowed to choose the optimal band (2Ghz or 5Ghz) based on device capability,
 * operating country code and current radio conditions.
 *
 * Introduced in 9.0, but we will abuse this constant anyway.
 * https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r1/wifi/java/android/net/wifi/WifiConfiguration.java#295
 */
@RequiresApi(23)
const val AP_BAND_ANY = -1

/**
 * The band which AP resides on
 * -1:Any 0:2G 1:5G
 * By default, 2G is chosen
 */
var WifiConfiguration.apBand: Int
    @RequiresApi(23) get() = apBandField.get(this) as Int
    @RequiresApi(23) set(value) = apBandField.set(this, value)
/**
 * The channel which AP resides on
 * 2G  1-11
 * 5G  36,40,44,48,149,153,157,161,165
 * 0 - find a random available channel according to the apBand
 */
var WifiConfiguration.apChannel: Int
    @RequiresApi(23) get() = apChannelField.get(this) as Int
    @RequiresApi(23) set(value) = apChannelField.set(this, value)

/**
 * The frequency which AP resides on (MHz). Resides in range [2412, 5815].
 */
fun channelToFrequency(channel: Int) = when (channel) {
    in 1..14 -> 2407 + 5 * channel
    in 15..165 -> 5000 + 5 * channel
    else -> throw IllegalArgumentException("Invalid channel $channel")
}

/**
 * Based on:
 * https://android.googlesource.com/platform/packages/apps/Settings/+/android-5.0.0_r1/src/com/android/settings/wifi/WifiApDialog.java#88
 * https://android.googlesource.com/platform/packages/apps/Settings/+/b1af85d/src/com/android/settings/wifi/tether/WifiTetherSettings.java#162
 */
fun newWifiApConfiguration(ssid: String, passphrase: String?) = try {
    WifiApManager.configuration
} catch (e: InvocationTargetException) {
    if (e.targetException !is SecurityException) Timber.w(e)
    WifiConfiguration()
}.apply {
    SSID = ssid
    preSharedKey = passphrase
    allowedKeyManagement.clear()
    allowedAuthAlgorithms.clear()
    allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
}