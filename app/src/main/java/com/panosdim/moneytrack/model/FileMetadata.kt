package com.panosdim.moneytrack.model

data class FileMetadata(
    val version: Int,
    val artifactType: ArtifactType,
    val applicationId: String,
    val variantName: String,
    val elements: List<Elements>,
    val elementType: String
)

data class ArtifactType(
    val type: String,
    val kind: String,
)

data class Elements(
    val type: String,
    val filters: List<String>,
    val attributes: List<String>,
    val versionCode: Int,
    val versionName: String,
    val outputFile: String
)
