package com.nudriin.mymediapipetextclassification

import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.google.mediapipe.tasks.components.containers.Classifications
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.text.textclassifier.TextClassifier
import java.lang.IllegalStateException
import java.util.concurrent.ScheduledThreadPoolExecutor

class TextClassifierHelper(
    val modelName: String = "bert_classifier.tflite",
    var context: Context,
    val classifierListener: ClassifierListener? = null
) {

    private var textClassifier: TextClassifier? = null
    private var executor: ScheduledThreadPoolExecutor? =null

    init {
        initClassifier()
    }
    interface ClassifierListener {
        fun onError(error: String)
        fun onResults(
            results: List<Classifications>?,
            inferenceTime: Long
            )
    }

    private fun initClassifier() {
        try {
            val optionBuilder = TextClassifier.TextClassifierOptions.builder()

            val baseOptionsBuilder = BaseOptions.builder()
                .setModelAssetPath(modelName)

            optionBuilder.setBaseOptions(baseOptionsBuilder.build())

            textClassifier = TextClassifier.createFromOptions(context, optionBuilder.build())

        } catch (e: IllegalStateException) {
            classifierListener?.onError(context.getString(R.string.text_classifier_failed))
            Log.e(TAG, e.message.toString())
        }
    }

    fun classify(inputText: String) {
        if(textClassifier == null) {
            initClassifier()
        }

        executor = ScheduledThreadPoolExecutor(1)

        executor?.execute {
            var inferenceTime = SystemClock.uptimeMillis()
            val results = textClassifier?.classify(inputText)
            inferenceTime = SystemClock.uptimeMillis() - inferenceTime

            classifierListener?.onResults(results?.classificationResult()?.classifications(), inferenceTime)
        }
    }

    companion object {
        private const val TAG = "TextClassifierHelper"
    }
}