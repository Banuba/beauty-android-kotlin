package com.banuba.sdk.example.beautification.camera

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import android.view.SurfaceView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.*
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.banuba.sdk.camera.Facing
import com.banuba.sdk.entity.RecordedVideoInfo
import com.banuba.sdk.example.beautification.camera.Feature.ColorFeature
import com.banuba.sdk.example.beautification.camera.Feature.LinearFeature
import com.banuba.sdk.example.beautification.camera.Feature.SimpleFeature
import com.banuba.sdk.manager.BanubaSdkManager
import com.banuba.sdk.manager.IEventCallback
import com.banuba.sdk.types.Data
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    private val faceBeautyFeatures = arrayListOf(
        LinearFeature("Teeth whitening", "TeethWhitening.strength", "TeethWhitening.strength", "0.0"),
        LinearFeature("Eyes morphing", "FaceMorph.eyes", "FaceMorph.clear"),
        LinearFeature("Face morphing", "FaceMorph.face", "FaceMorph.clear"),
        LinearFeature("Nose morphing", "FaceMorph.nose", "FaceMorph.clear"),
        LinearFeature("Skin softening", "SkinSoftening.strength", "SkinSoftening.strength", "0.0"),
        ColorFeature("Skin coloring", "Skin.color", "Skin.clear"),
        ColorFeature("Hair coloring", "Hair.color", "Hair.clear"),
        ColorFeature("Eyes coloring", "Eyes.color", "Eyes.clear"),
        LinearFeature("Eye flare", "EyesFlare.strength", "EyesFlare.strength", "0.0"),
        LinearFeature("Eyes whitening", "EyesWhitening.strength", "EyesWhitening.strength", "0.0")
    )

    private val makeupFeatures = arrayListOf(
        ColorFeature("Highlighting", "Highlighter.color", "Highlighter.clear"),
        ColorFeature("Contouring", "Contour.color", "Contour.clear"),
        ColorFeature("Foundation", "Foundation.color", "Foundation.clear"),
        LinearFeature("Skin smoothing", "Foundation.strength", "Foundation.clear"),
        ColorFeature("Blush", "Blush.color", "Blush.clear"),
        LinearFeature("Softlight", "Softlight.strength", "Softlight.strength", "0.0"),
        ColorFeature("Eyeliner", "Eyeliner.color", "Eyeliner.clear"),
        ColorFeature("Eyeshadow", "Eyeshadow.color", "Eyeshadow.clear"),
        ColorFeature("Eyelashes", "Eyelashes.color", "Eyelashes.clear"),
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
        callJsMethod(feature)
    }

    fun onColorFeatureChange(feature: ColorFeature, color: Int, alphaPosition: Int) {
        feature.color = color
        feature.alphaPosition = alphaPosition
        callJsMethod(feature)
    }

    fun restoreFeatures() {
        allFeatures.filter { !it.isDefault() }.forEach { callJsMethod(it) }
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
        callJsMethod(textureFeature)
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

    private fun callJsMethod(feature: Feature) {
        when (feature) {
            is SimpleFeature -> callJsMethod(feature.method, feature.parameter)
            is LinearFeature -> callJsMethod(feature.method, feature.value.toString())
            is ColorFeature -> callJsMethod(feature.method, getColor(feature.color))
        }
    }

    private fun callJsMethod(method: String, parameter: String) {
        effect?.callJsMethod(method, parameter)

        Log.i("[===js]", "method: $method\t parameter: $parameter")
    }

    private fun getColor(color: Int): String {
        val a: Int = color shr 24 and 0x000000FF
        val r: Int = color shr 16 and 0x000000FF
        val g: Int = color shr 8 and 0x000000FF
        val b: Int = color and 0x000000FF

        val colorArr = listOf(r, g, b, a).map { it.toFloat() / 0xFF }.toFloatArray()

        return colorArr.joinToString(" ") { v: Float ->
            ((100.0 * v).roundToInt().toFloat() / 100).toString()
        }
    }

    private fun resetFeature(feature: Feature) {
        feature.reset()
        callJsMethod(feature.clearMethod, feature.clearValue)
    }
}
