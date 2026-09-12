package com.example.readingtutor.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Locale
import kotlin.math.sin

class AudioService(private val context: Context) : TextToSpeech.OnInitListener {

    private val TAG = "AudioService"
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating TextToSpeech: ${e.message}")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w(TAG, "US English TTS voice not fully supported; using default locale")
                tts?.setLanguage(Locale.getDefault())
            }
            tts?.setPitch(1.15f) // Slightly higher, friendly pitch suitable for young children
            tts?.setSpeechRate(0.85f) // Clear, slightly slower cadence for early readers
            isTtsReady = true
        } else {
            Log.e(TAG, "TTS Init failed with status: $status")
        }
    }

    /**
     * Maps phoneme keys to clean pronunciations without distorted 'uh' consonant endings.
     */
    private fun getCleanPhonemeUtterance(soundKey: String): String {
        return when (soundKey.lowercase().trim()) {
            "m" -> "mmm"           // Pure hum, avoids 'muh'
            "s" -> "sss"           // Pure hiss, avoids 'suh'
            "t" -> "t"             // Crisp unvoiced plosive
            "p" -> "p"             // Crisp unvoiced plosive
            "a_short", "a" -> "ah" // Short /æ/ as in 'apple'
            "e_short", "e" -> "eh" // Short /e/ as in 'egg'
            "i_short", "i" -> "ih" // Short /ɪ/ as in 'igloo'
            "o_short", "o" -> "aw" // Short /ɒ/ as in 'octopus'
            "u_short", "u" -> "uh" // Short /ʌ/ as in 'up'
            "b" -> "b"
            "c", "k" -> "k"
            "d" -> "d"
            "f" -> "fff"
            "g" -> "g"
            "h" -> "ha"
            "n" -> "nnn"
            "r" -> "rrr"
            else -> soundKey
        }
    }

    /**
     * Plays a clean letter sound / phoneme.
     */
    fun playSound(soundKey: String, onDone: (() -> Unit)? = null) {
        val assetPath = "audio/phonemes/${soundKey.lowercase()}.mp3"
        if (tryPlayAsset(assetPath, onDone)) {
            return
        }

        playChimeTap()
        val cleanUtterance = getCleanPhonemeUtterance(soundKey)
        speak(cleanUtterance, onDone)
    }

    /**
     * Plays a spoken word.
     */
    fun playWord(word: String, onDone: (() -> Unit)? = null) {
        val clean = word.replace(Regex("[^a-zA-Z]"), "").lowercase()
        val assetPath = "audio/words/$clean.mp3"
        if (tryPlayAsset(assetPath, onDone)) {
            return
        }
        speak(word, onDone)
    }

    /**
     * Plays a spoken instruction for non-readers.
     */
    fun playInstruction(instruction: String, onDone: (() -> Unit)? = null) {
        speak(instruction, onDone)
    }

    /**
     * Plays a complete sentence naturally.
     */
    fun playSentence(sentence: String, onDone: (() -> Unit)? = null) {
        speak(sentence, onDone)
    }

    /**
     * Safe internal speech dispatch.
     */
    private fun speak(text: String, onDone: (() -> Unit)? = null) {
        if (!isTtsReady || tts == null) {
            onDone?.invoke()
            return
        }

        try {
            stopAudio()
            val utteranceId = "utt_${System.currentTimeMillis()}"

            if (onDone != null) {
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(id: String?) {}
                    override fun onDone(id: String?) {
                        if (id == utteranceId) onDone()
                    }
                    @Deprecated("Deprecated in Java")
                    override fun onError(id: String?) {
                        if (id == utteranceId) onDone()
                    }
                })
            }

            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } catch (e: Exception) {
            Log.e(TAG, "TTS speak error: ${e.message}")
            onDone?.invoke()
        }
    }

    private fun tryPlayAsset(assetPath: String, onDone: (() -> Unit)?): Boolean {
        try {
            val afd = context.assets.openFd(assetPath)
            stopAudio()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                prepare()
                setOnCompletionListener {
                    it.release()
                    mediaPlayer = null
                    onDone?.invoke()
                }
                start()
            }
            afd.close()
            return true
        } catch (e: IOException) {
            // Asset not packaged, gracefully fall back to speech/synthesizer
            return false
        }
    }

    fun stopAudio() {
        try {
            tts?.stop()
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping audio: ${e.message}")
        }
    }

    fun release() {
        stopAudio()
        try {
            tts?.shutdown()
            tts = null
        } catch (e: Exception) {
            Log.w(TAG, "Error shutting down TTS: ${e.message}")
        }
    }

    // -------------------------------------------------------------
    // Pure synthesized offline audio chimes (Zero latency & 100% offline)
    // -------------------------------------------------------------

    fun playChimeSuccess() {
        scope.launch {
            // Ascending major chord fanfare: C5 (523Hz) -> E5 (659Hz) -> G5 (784Hz) -> C6 (1046Hz)
            generateToneSequence(
                listOf(
                    Pair(523.25, 90),
                    Pair(659.25, 90),
                    Pair(783.99, 90),
                    Pair(1046.50, 200)
                )
            )
        }
    }

    fun playChimeGentleRetry() {
        scope.launch {
            // Gentle warm dual tone: F4 (349Hz) -> E4 (329Hz), soft & non-punishing
            generateToneSequence(
                listOf(
                    Pair(349.23, 120),
                    Pair(329.63, 160)
                )
            )
        }
    }

    fun playChimeStarPop() {
        scope.launch {
            // Sparkling star pop tone
            generateToneSequence(
                listOf(
                    Pair(880.0, 70),
                    Pair(1318.5, 140)
                )
            )
        }
    }

    fun playChimeTap() {
        scope.launch {
            generateToneSequence(listOf(Pair(600.0, 40)))
        }
    }

    fun playChimeSlideTone(stepIndex: Int) {
        scope.launch {
            // Ascending pitch for sliding fingers: base 440Hz + step * 80Hz
            val freq = 440.0 + (stepIndex * 90.0)
            generateToneSequence(listOf(Pair(freq, 60)))
        }
    }

    private fun generateToneSequence(tones: List<Pair<Double, Int>>) {
        val sampleRate = 22050
        val totalMs = tones.sumOf { it.second }
        val totalSamples = (sampleRate * (totalMs / 1000.0)).toInt()
        val buffer = ShortArray(totalSamples)

        var sampleOffset = 0
        for ((freq, durationMs) in tones) {
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
            for (i in 0 until numSamples) {
                if (sampleOffset + i >= totalSamples) break
                val time = i.toDouble() / sampleRate
                // Sine wave with soft attack/decay envelope to prevent clicking
                val envelope = when {
                    i < 200 -> i / 200.0
                    i > numSamples - 300 -> (numSamples - i) / 300.0
                    else -> 1.0
                }
                val sample = (sin(2.0 * Math.PI * freq * time) * 0.45 * envelope * Short.MAX_VALUE).toInt()
                buffer[sampleOffset + i] = sample.toShort()
            }
            sampleOffset += numSamples
        }

        try {
            val minBufSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
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
                .setBufferSizeInBytes(maxOf(buffer.size * 2, minBufSize))
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            Thread.sleep(totalMs.toLong() + 50)
            audioTrack.release()
        } catch (e: Exception) {
            Log.w(TAG, "AudioTrack chime playback error: ${e.message}")
        }
    }
}
