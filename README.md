Quick start Android example for [Banuba SDK Effect API](https://docs.banuba.com/face-ar-sdk/effect_api/getting_started).
Example shows how to work with [Face Beauty API](https://docs.banuba.com/face-ar-sdk/effect_api/face_beauty) and [Makeup API](https://docs.banuba.com/face-ar-sdk/effect_api/makeup).
In both cases the [Makeup effect](https://docs.banuba.com/face-ar-sdk/generated/effects/Makeup.zip) is used.

# Getting Started

1. Get the latest Banuba SDK archive for Android and the client token. Please fill in our form on [form on banuba.com](https://www.banuba.com/face-filters-sdk) website, or contact us via [info@banuba.com](mailto:info@banuba.com).
2. Copy `aar` files from the Banuba SDK archive into `libs` dir:
    `BNBEffectPlayer/bin/banuba_sdk/banuba_sdk-release.aar` => `beauty-android-kotlin/libs/`
    `BNBEffectPlayer/banuba_effect_player-release.aar` => `beauty-android-kotlin/libs/`
3. Copy and Paste your banuba client token into appropriate section of `beauty-android-kotlin/client_token/com/banuba/sdk/example/common/BanubaClientToken.kt` with “” symbols.
4. Open the project in Android Studio and run the necessary target using the usual steps.

# Contributing

Contributions are what make the open source community such an amazing place to be learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request
