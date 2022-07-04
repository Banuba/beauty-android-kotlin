package com.banuba.sdk.example.beautification.camera

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import android.view.SurfaceView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.banuba.sdk.camera.Facing
import com.banuba.sdk.effect_player.FrameDurationListener
import com.banuba.sdk.entity.RecordedVideoInfo
import com.banuba.sdk.example.beautification.camera.Feature.*
import com.banuba.sdk.manager.BanubaSdkManager
import com.banuba.sdk.manager.IEventCallback
import com.banuba.sdk.types.Data
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val UPDATE_DURATION_FREQUENCY = 15
    }

    private val faceBeautyFeatures = arrayListOf(
        LinearFeature("Teeth whitening", "Teeth.whitening", "Teeth.whitening", "0.0"),
        LinearFeature("Eyes morphing", "FaceMorph.eyes", "FaceMorph.clear"),
        LinearFeature("Face morphing", "FaceMorph.face", "FaceMorph.clear"),
        LinearFeature("Nose morphing", "FaceMorph.nose", "FaceMorph.clear"),
        LinearFeature("Skin softening", "Skin.softening", "Skin.softening", "0.0"),
        ColorFeature("Skin coloring", "Skin.color", "Skin.clear"),//
        ColorFeature("Hair coloring", "Hair.color", "Hair.clear"),
        ColorFeature("Eyes coloring", "Eyes.color", "Eyes.clear"),
        LinearFeature("Eye flare", "Eyes.flare", "Eyes.flare", "0.0"),
        LinearFeature("Eyes whitening", "Eyes.whitening", "Eyes.whitening", "0.0")
    )

    private val makeupFeatures = arrayListOf(
        ColorFeature("Highlighting", "Makeup.highlighter", "Makeup.clear"),
        ColorFeature("Contouring", "Makeup.contour", "Makeup.clear"),
        ColorFeature("Foundation", "Skin.color", "Makeup.clear"), //
        LinearFeature("Skin smoothing", "Skin.softening", "Makeup.clear"),
        ColorFeature("Blush", "Makeup.blushes", "Makeup.clear"),
        LinearFeature("Softlight", "Softlight.strength", "Softlight.clear"),
        ColorFeature("Eyeliner", "Makeup.eyeliner", "Makeup.clear"),
        ColorFeature("Eyeshadow", "Makeup.eyeshadow", "Makeup.clear"),
        ColorFeature("Eyelashes", "Makeup.lashes", "Makeup.clear"),
        ColorFeature("Matt lipstick", "Lips.matt", "Lips.clear"),
        ColorFeature("Shiny lipstick", "Lips.shiny", "Lips.clear"),
        ColorFeature("Glitter lipstick", "Lips.glitter", "Lips.clear")
    )

    private val textureFeature = SimpleFeature("Texture", "Makeup.set", "Makeup.clear")

    private val allFeatures = faceBeautyFeatures + makeupFeatures + textureFeature

    private val sdkManager by lazy(LazyThreadSafetyMode.NONE) {
        BanubaSdkManager(getApplication<Application>().applicationContext)
    }

    private var effect: com.banuba.sdk.effect_player.Effect? = null

    enum class ApiType {
        NONE, MAKEUP, FACE_BEAUTY
    }

    private var _activeType = ApiType.NONE

    private val _activeFeatures: MutableLiveData<List<Feature>> by lazy {
        val liveData = MutableLiveData<List<Feature>>()
        liveData.value = emptyList()
        return@lazy liveData
    }
    val activeFeatures: LiveData<List<Feature>>
        get() = _activeFeatures

    private val _isMakeup = MutableLiveData(false)
    val isMakeup: LiveData<Boolean>
        get() = _isMakeup

    private val _isFaceBeauty = MutableLiveData(false)
    val isFaceBeauty: LiveData<Boolean>
        get() = _isFaceBeauty

    private val _currentFeature = MutableLiveData<Feature?>(null)
    val currentFeature: LiveData<Feature?>
        get() = _currentFeature

    private var isFrontCamera = true

    private val _photoReady = MutableLiveData<Bitmap?>(null)
    val photoReady: LiveData<Bitmap?>
        get() = _photoReady

    private val _textureClicked = MutableLiveData(false)
    val textureClicked: LiveData<Boolean>
        get() = _textureClicked

    private val _textureApplied = MutableLiveData(false)
    val textureApplied: LiveData<Boolean>
        get() = _textureApplied

    private var recognizerCounter = 0
    private var cameraCounter = 0
    private var rendererCounter = 0
    private val recognizerStringBuilder = StringBuilder()
    private val cameraStringBuilder = StringBuilder()
    private val rendererStringBuilder = StringBuilder()

    private val _recognizerValue = MutableLiveData<String>()
    val recognizerValue: LiveData<String>
        get() = _recognizerValue

    private val _cameraValue = MutableLiveData<String>()
    val cameraValue: LiveData<String>
        get() = _cameraValue

    private val _rendererValue = MutableLiveData<String>()
    val rendererValue: LiveData<String>
        get() = _rendererValue

    fun attachSurface(surfaceView: SurfaceView) {
        sdkManager.attachSurface(surfaceView)
        effect = sdkManager.loadEffect(BanubaSdkManager.getResourcesBase() + "/effects/Makeup", false)

        sdkManager.setCallback(object : IEventCallback {
            override fun onCameraOpenError(p0: Throwable) {}

            override fun onCameraStatus(p0: Boolean) {}

            override fun onScreenshotReady(image: Bitmap) {
                viewModelScope.launch {
                    _photoReady.value = image
                }
            }

            override fun onHQPhotoReady(p0: Bitmap) {}

            override fun onVideoRecordingFinished(p0: RecordedVideoInfo) {}

            override fun onVideoRecordingStatusChange(p0: Boolean) {}

            override fun onImageProcessed(p0: Bitmap) {}

            override fun onFrameRendered(p0: Data, p1: Int, p2: Int) {}
        })

        val frameDurationChanger: (Int, Float, StringBuilder, String, MutableLiveData<String>) -> Int =
            { counter, averaged, builder, caption, liveData ->
                if (counter < UPDATE_DURATION_FREQUENCY) {
                    counter + 1
                } else {
                    viewModelScope.launch {
                        val fps = 1F / averaged
                        builder.setLength(0)
                        builder.append(caption).append(": ").append(fps)
                        liveData.value = builder.toString()
                    }
                    0
                }
            }

        sdkManager.effectPlayer?.addFrameDurationListener(object : FrameDurationListener {
            override fun onRecognizerFrameDurationChanged(instant: Float, averaged: Float) {
                recognizerCounter = frameDurationChanger(recognizerCounter, averaged, recognizerStringBuilder, "FRX", _recognizerValue)
            }

            override fun onCameraFrameDurationChanged(instant: Float, averaged: Float) {
                cameraCounter = frameDurationChanger(cameraCounter, averaged, cameraStringBuilder, "Camera", _cameraValue)
            }

            override fun onRenderFrameDurationChanged(instant: Float, averaged: Float) {
                rendererCounter = frameDurationChanger(rendererCounter, averaged, rendererStringBuilder, "Draw", _rendererValue)
            }
        })
    }

    fun openCamera() {
        sdkManager.openCamera()
    }

    fun closeCamera() {
        sdkManager.closeCamera()
    }

    fun playbackPlay() {
        sdkManager.effectPlayer.playbackPlay()
    }

    fun playbackPause() {
        sdkManager.effectPlayer.playbackPause()
    }

    fun releaseSurface() {
        sdkManager.releaseSurface()
    }

    fun takePhoto() {
        sdkManager.takePhoto(null)
    }

    fun photoProcessed() {
        _photoReady.value = null
    }

    fun switchCamera() {
        isFrontCamera = !isFrontCamera
        if (isFrontCamera) {
            sdkManager.setCameraFacing(Facing.BACK, false)
        } else {
            sdkManager.setCameraFacing(Facing.FRONT, true)
        }
    }

    fun onFeatureClicked(name: String) {
        val feature = _activeFeatures.value?.find { it.name == name }
        _currentFeature.value = feature
    }

    fun onLinearFeatureChange(feature: LinearFeature, progress: Float) {
        feature.value = progress
        evalJs(feature)
    }

    fun onColorFeatureChange(feature: ColorFeature, color: Int, alphaPosition: Int) {
        feature.color = color
        feature.alphaPosition = alphaPosition
        evalJs(feature)
    }

    fun restoreFeatures() {
        allFeatures.filter { !it.isDefault() }.forEach { evalJs(it) }
    }

    fun resetCurrentFeature() {
        currentFeature.value?.let {
            resetFeature(it)
            _currentFeature.value = it
        }
    }

    fun resetFeatures() {
        allFeatures.forEach { resetFeature(it) }
        _currentFeature.value = currentFeature.value
    }

    fun selectTexture() {
        _textureClicked.value = true
    }

    fun selectTextureDone() {
        _textureClicked.value = false
    }

    fun setTexture(path: String) {
        textureFeature.parameter = path
        _currentFeature.value = textureFeature
        _textureApplied.value = true
        evalJs(textureFeature)
    }

    fun setTextureDone() {
        _textureApplied.value = false
    }

    fun activateFeaturesType(type: ApiType) {
        if (_activeType == type) {
            return
        }

        _activeType = type
        _currentFeature.value = null

        val setActive = { features: List<Feature>, makeup: Boolean, beauty: Boolean ->
            _activeFeatures.value = features
            _isMakeup.value = makeup
            _isFaceBeauty.value = beauty
        }

        when (type) {
            ApiType.MAKEUP -> setActive(makeupFeatures, true, false)
            ApiType.FACE_BEAUTY -> setActive(faceBeautyFeatures, false, true)
            ApiType.NONE -> setActive(emptyList(), false, false)
        }
    }

    private fun evalJs(feature: Feature) {
        when (feature) {
            is SimpleFeature -> evalJs(feature.method, feature.parameter)
            is LinearFeature -> evalJs(feature.method, feature.value.toString())
            is ColorFeature -> evalJs(feature.method, getColor(feature.color))
        }
    }

    private fun makeJsScript(method: String, params: String?): String {
        return method + if (params == null) "()" else "($params)"
    }

    private fun evalJs(method: String, parameter: String) {
        val script = makeJsScript(method, parameter)
        Log.i("[===js]", "method: $method\t parameter : $parameter\t script : $script")
        effect?.evalJs(script, null)
    }

    private fun getColor(color: Int): String {
        val a: Int = color shr 24 and 0x000000FF
        val r: Int = color shr 16 and 0x000000FF
        val g: Int = color shr 8 and 0x000000FF
        val b: Int = color and 0x000000FF

        val colorArr = listOf(r, g, b, a).map { it.toFloat() / 0xFF }.toFloatArray()
        val colorString = colorArr.joinToString(" ") { v: Float ->
            ((100.0 * v).roundToInt().toFloat() / 100).toString()
        }
        return "'$colorString'"
    }

    private fun resetFeature(feature: Feature) {
        feature.reset()
        evalJs(feature.clearMethod, feature.clearValue)
    }
}
