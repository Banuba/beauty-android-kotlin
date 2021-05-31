package com.banuba.sdk.example.beautification.camera

sealed class Feature(
    val name: String,
    val method: String,
    val clearMethod: String,
    val clearValue: String = ""
) {
    abstract fun isDefault(): Boolean
    abstract fun reset()

    class SimpleFeature(
        name: String,
        method: String,
        clearMethod: String,
        clearValue: String = "")  : Feature(name, method, clearMethod, clearValue) {

        private val default = ""
        var parameter = default

        override fun isDefault(): Boolean {
            return default == parameter
        }

        override fun reset() {
            parameter = default
        }
    }

    class LinearFeature(
        name: String,
        method: String,
        clearMethod: String,
        clearValue: String = "",
        val maxValue: Float = 1.0F) : Feature(name, method, clearMethod, clearValue) {

        private val default = .0F
        var value: Float = default

        override fun isDefault(): Boolean {
            return default == value
        }

        override fun reset() {
            value = default
        }
    }

    class ColorFeature(
        name: String,
        method: String,
        clearMethod: String,
        clearValue: String = "") : Feature(name, method, clearMethod, clearValue) {

        private val defaultColor = 0
        private val defaultAlpha = 0xFF
        var color: Int = defaultColor
        var alphaPosition: Int = defaultAlpha

        override fun isDefault(): Boolean {
            return defaultColor == color && defaultAlpha == alphaPosition
        }

        override fun reset() {
            color = defaultColor
            alphaPosition = defaultAlpha
        }
    }
}
