package org.ymegane.android.developsettingstile

import android.content.ActivityNotFoundException
import android.content.Intent
import android.database.ContentObserver
import android.os.Handler
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log

/**
 * Provide for the TileService events
 */
class DeveloperOptionsTileService : TileService() {
    private val developSettingsObserver by lazy { DevelopSettingsObserver(Handler()) }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onClick() {
        Log.d(TAG, "onClick")

        val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            startActivityAndCollapse(intent)
        } catch (e : ActivityNotFoundException) {
            Log.w(TAG, "An activity was not found..", e)
        }
    }

    override fun onTileAdded() {
        super.onTileAdded()
        Log.d(TAG, "onTileAdded")
    }

    override fun onTileRemoved() {
        super.onTileRemoved()
        Log.d(TAG, "onTileRemoved")
    }

    override fun onStartListening() {
        super.onStartListening()
        Log.d(TAG, "onStartListening")
        contentResolver.notifyChange(Settings.Global.getUriFor(Settings.Global.DEVELOPMENT_SETTINGS_ENABLED), developSettingsObserver)
        contentResolver.notifyChange(Settings.Global.getUriFor(Settings.Global.ADB_ENABLED), developSettingsObserver)

        updateAppTile()
    }

    override fun onStopListening() {
        super.onStopListening()
        Log.d(TAG, "onStopListening")

        contentResolver.unregisterContentObserver(developSettingsObserver)
    }

    private fun updateAppTile() {
        try {
            qsTile?.apply {
                state = tileState()
                updateTile()
            }
        } catch (e: Settings.SettingNotFoundException) {
            Log.w(TAG, "Not supported", e)
        }
    }

    private fun tileState(): Int {
        val enabled = Settings.Global.getInt(contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED)
        Log.d(TAG, "DEVELOPMENT_SETTINGS_ENABLED = $enabled")

        return when {
            enabled == 1 -> Tile.STATE_ACTIVE
            else -> Tile.STATE_INACTIVE
        }
    }

    /**
     * Receive change of develop settings
     */
    inner class DevelopSettingsObserver(handler: Handler) : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            updateAppTile()
        }
    }

    companion object {
        private val TAG = "DeveloperOptionsTileService"
    }
}