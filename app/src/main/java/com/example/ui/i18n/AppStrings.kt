package com.example.ui.i18n

import com.example.domain.model.AppLanguage

object AppStrings {
    fun tagline(lang: AppLanguage) = "Create Anything. Imagine Everything."

    fun heroDescription(lang: AppLanguage) = if (lang == AppLanguage.ID) {
        "Studio produksi video, gambar, audio, dan konsistensi karakter all-in-one dengan desain Liquid Glass. Gratis tanpa paywall atau input API Key pengguna."
    } else {
        "All-in-one AI video, image, audio, and character consistency studio in Liquid Glass design. Free with no paywalls or user API keys required."
    }

    fun startCreatingVideo(lang: AppLanguage) =
        if (lang == AppLanguage.ID) "Buat Video Sekarang" else "Create Video Now"

    fun openEditor(lang: AppLanguage) =
        if (lang == AppLanguage.ID) "Buka Editor" else "Open Editor"

    fun generateVideo(lang: AppLanguage) =
        if (lang == AppLanguage.ID) "Render Video MP4" else "Render MP4 Video"

    fun selectModel(lang: AppLanguage) =
        if (lang == AppLanguage.ID) "Pilih Model AI" else "Select AI Model"

    fun promptPlaceholder(lang: AppLanguage) = if (lang == AppLanguage.ID) {
        "Jelaskan adegan sinematik Anda... mis. kamera drone terbang melintasi kota cyberpunk neon hujan malam hari, refleksi kaca realistis"
    } else {
        "Describe your cinematic scene... e.g., drone flying through rain-slicked neon cyberpunk metropolis at twilight, volumetric cyan reflections"
    }

    fun enhancePrompt(lang: AppLanguage) =
        if (lang == AppLanguage.ID) "Sempurnakan Prompt" else "Enhance Prompt"

    fun freeEngineTitle(lang: AppLanguage) =
        if (lang == AppLanguage.ID) "Sistem Gratis & Transparan" else "Free & Transparent Engine"

    fun freeEngineDesc(lang: AppLanguage) = if (lang == AppLanguage.ID) {
        "Tanpa biaya langganan, tanpa kredit palsu, dan tanpa meminta API Key Anda. Mesin Video H.264 Lokal & Audio TTS/SFX aktif gratis. Model cloud premium hanya aktif jika integrasi backend resmi tersedia."
    } else {
        "No subscriptions, no fake credits, and no user API keys required. Local H.264 Video & TTS/SFX Audio engines run free on-device. Proprietary cloud models stay marked unavailable until official backend integration."
    }

    fun unavailableReason(lang: AppLanguage) = if (lang == AppLanguage.ID) {
        "Belum tersedia — Model ini membutuhkan akses provider resmi di backend. Gunakan Videon Local Motion atau Adapter Open-Source Self-Hosted."
    } else {
        "Not available — This model requires official provider backend access. Switch to Videon Local Motion or Self-Hosted Open-Source Adapter."
    }

    fun gpuQueueInfo(lang: AppLanguage, activeCount: Int, queuedCount: Int) = if (lang == AppLanguage.ID) {
        "Antrean Perangkat: $activeCount aktif • $queuedCount menunggu (1 slot encoder perangkat keras)"
    } else {
        "Device Queue: $activeCount active • $queuedCount waiting (1 hardware encoder slot)"
    }
}
