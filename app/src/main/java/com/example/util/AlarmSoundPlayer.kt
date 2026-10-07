package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

object AlarmSoundPlayer {

    private const val TAG = "AlarmSoundPlayer"

    private var mediaPlayer: MediaPlayer? = null
    private var audioTrack: AudioTrack? = null
    private var isPlaying = false
    private var toneJob: Job? = null
    private var vibrationJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    @Synchronized
    fun startLoudAlarm(context: Context) {
        if (isPlaying) return
        isPlaying = true

        Log.d(TAG, "Starting loud tuition alarm")

        // 1. Start continuous vibration
        startContinuousVibration(context)

        // 2. Try playing system alarm ringtone via MediaPlayer
        var mediaSuccess = false
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

            if (alarmUri != null) {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(context, alarmUri)
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    isLooping = true
                    prepare()
                    start()
                }
                mediaSuccess = true
            }
        } catch (e: Exception) {
            Log.w(TAG, "MediaPlayer failed, falling back to synthesized alarm tone", e)
            mediaPlayer?.release()
            mediaPlayer = null
            mediaSuccess = false
        }

        // 3. Fallback or parallel synthesized siren beep to ensure loud wake-up sound in all environments
        if (!mediaSuccess || toneJob == null) {
            startSynthesizedAlarmTone()
        }
    }

    @Synchronized
    fun stopAlarm(context: Context? = null) {
        isPlaying = false
        Log.d(TAG, "Stopping alarm sound and vibration")

        try {
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
        } catch (_: Exception) {}
        mediaPlayer = null

        toneJob?.cancel()
        toneJob = null

        try {
            audioTrack?.let {
                if (it.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    it.stop()
                }
                it.release()
            }
        } catch (_: Exception) {}
        audioTrack = null

        vibrationJob?.cancel()
        vibrationJob = null

        if (context != null) {
            stopVibration(context)
        }
    }

    fun isAlarmRinging(): Boolean = isPlaying

    private fun startSynthesizedAlarmTone() {
        toneJob?.cancel()
        toneJob = scope.launch {
            val sampleRate = 44100
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(sampleRate / 4)

            try {
                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioTrack?.play()

                // Generate dual-tone high-intensity wake-up siren (880 Hz & 1200 Hz alternating)
                val tone1 = generateToneBuffer(880.0, 0.25, sampleRate)
                val tone2 = generateToneBuffer(1200.0, 0.25, sampleRate)
                val pause = ShortArray((sampleRate * 0.15).toInt())

                while (isActive && isPlaying) {
                    audioTrack?.write(tone1, 0, tone1.size)
                    audioTrack?.write(pause, 0, pause.size)
                    audioTrack?.write(tone2, 0, tone2.size)
                    audioTrack?.write(pause, 0, pause.size)
                    delay(50)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Synthesized tone generation error", e)
            } finally {
                try {
                    audioTrack?.stop()
                    audioTrack?.release()
                } catch (_: Exception) {}
                audioTrack = null
            }
        }
    }

    private fun generateToneBuffer(freq: Double, durationSec: Double, sampleRate: Int): ShortArray {
        val numSamples = (durationSec * sampleRate).toInt()
        val buffer = ShortArray(numSamples)
        val angularFreq = 2.0 * Math.PI * freq

        for (i in 0 until numSamples) {
            val time = i.toDouble() / sampleRate
            // Loud amplitude near max Short.MAX_VALUE with slight decay envelope for punch
            val envelope = when {
                i < sampleRate * 0.02 -> i / (sampleRate * 0.02)
                i > numSamples - (sampleRate * 0.02) -> (numSamples - i) / (sampleRate * 0.02)
                else -> 1.0
            }
            val sample = sin(angularFreq * time) * 32000.0 * envelope
            buffer[i] = sample.toInt().toShort()
        }
        return buffer
    }

    private fun startContinuousVibration(context: Context) {
        vibrationJob?.cancel()
        vibrationJob = scope.launch {
            val vibrator = getVibrator(context) ?: return@launch
            val pattern = longArrayOf(0, 400, 200, 400, 200, 400, 800)

            while (isActive && isPlaying) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(
                            VibrationEffect.createWaveform(pattern, -1)
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(pattern, -1)
                    }
                } catch (_: Exception) {}
                delay(2400)
            }
        }
    }

    private fun stopVibration(context: Context) {
        try {
            getVibrator(context)?.cancel()
        } catch (_: Exception) {}
    }

    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}
