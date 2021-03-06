package org.unifiedpush.flutter.connector

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.util.Log
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.view.FlutterMain
import org.unifiedpush.android.connector.MessagingReceiver
import org.unifiedpush.android.connector.MessagingReceiverHandler

/***
 * Handler used when there is a callback
 */

val handler = object : MessagingReceiverHandler {

    override fun onMessage(context: Context?, message: String, instance: String) {
        Log.d("Receiver","OnMessage")
        FlutterMain.startInitialization(context!!)
        FlutterMain.ensureInitializationComplete(context, null)
        if (Plugin.withCallbackChannel != null && !CallbackService.sServiceStarted.get()){
            Log.d("Receiver","foregroundChannel")
            Plugin.withCallbackChannel?.invokeMethod("onMessage", message)
        } else {
            Log.d("Receiver","CallbackChannel")
            val intent = Intent(context, CallbackService::class.java)
            intent.putExtra(EXTRA_CALLBACK_EVENT, CALLBACK_EVENT_MESSAGE)
            intent.putExtra(EXTRA_CALLBACK_DATA, message)
            CallbackService.enqueueWork(context, intent)
        }
    }

    override fun onNewEndpoint(context: Context?, endpoint: String, instance: String) {
        Log.d("Receiver","OnNewEndpoint")
        FlutterMain.startInitialization(context!!)
        FlutterMain.ensureInitializationComplete(context, null)
        if (Plugin.withCallbackChannel != null && !CallbackService.sServiceStarted.get()) {
            Plugin.withCallbackChannel?.invokeMethod("onNewEndpoint", endpoint)
        } else {
            val intent = Intent(context, CallbackService::class.java)
            intent.putExtra(EXTRA_CALLBACK_EVENT, CALLBACK_EVENT_NEW_ENDPOINT)
            intent.putExtra(EXTRA_CALLBACK_DATA, endpoint)
            CallbackService.enqueueWork(context, intent)
        }
    }

    override fun onRegistrationFailed(context: Context?, instance: String) {
        Log.d("Receiver","OnRegistrationFailed")
        Plugin.withCallbackChannel?.invokeMethod("onRegistrationFailed", null)
    }

    override fun onRegistrationRefused(context: Context?, instance: String) {
        Log.d("Receiver","OnRegistrationRefused")
        Plugin.withCallbackChannel?.invokeMethod("onRegistrationRefused", null)
    }

    override fun onUnregistered(context: Context?, instance: String) {
        Log.d("Receiver","OnUnregistered")
        FlutterMain.startInitialization(context!!)
        FlutterMain.ensureInitializationComplete(context, null)
        if (Plugin.withCallbackChannel != null && !CallbackService.sServiceStarted.get()) {
            Plugin.withCallbackChannel?.invokeMethod("onUnregistered", null)
        } else {
            val intent = Intent(context, CallbackService::class.java)
            intent.putExtra(EXTRA_CALLBACK_EVENT, CALLBACK_EVENT_UNREGISTERED)
            CallbackService.enqueueWork(context, intent)
        }
    }
}

class Receiver : MessagingReceiver(handler) {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (Plugin.isWithCallback(context!!)) {
            super.onReceive(context, intent)
        }
    }
}

/***
 * Handler used when the Receiver is defined in the app
 */

abstract class UnifiedPushHandler : MessagingReceiverHandler {
    abstract fun getEngine(context: Context): FlutterEngine

    private val handler = Handler()

    private fun getPlugin(context: Context): Plugin {
        val registry = getEngine(context).getPlugins()
        var plugin = registry.get(Plugin::class.java) as? Plugin
        if (plugin == null) {
            plugin = Plugin()
            registry.add(plugin)
        }
        return plugin;
    }

    override fun onMessage(context: Context?, message: String, instance: String) {
        Log.d("Receiver","OnMessage")
        handler.post {
            getPlugin(context!!).withReceiverChannel?.invokeMethod("onMessage", message)
        }
    }

    override fun onNewEndpoint(context: Context?, endpoint: String, instance: String) {
        Log.d("Receiver","OnNewEndpoint")
        handler.post {
            getPlugin(context!!).withReceiverChannel?.invokeMethod("onNewEndpoint", endpoint)
        }
    }

    override fun onRegistrationFailed(context: Context?, instance: String) {
        handler.post {
            getPlugin(context!!).withReceiverChannel?.invokeMethod("onRegistrationFailed", null)
        }
    }

    override fun onRegistrationRefused(context: Context?, instance: String) {
        Log.d("Receiver","OnRegistrationRefused")
        handler.post {
            getPlugin(context!!).withReceiverChannel?.invokeMethod("onRegistrationRefused", null)
        }
    }

    override fun onUnregistered(context: Context?, instance: String) {
        Log.d("Receiver","OnUnregistered")
        handler.post {
            getPlugin(context!!).withReceiverChannel?.invokeMethod("onUnregistered", null)
        }
    }
}
