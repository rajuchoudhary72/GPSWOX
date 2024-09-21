package com.shazcom.gps.app.data.response

data class DeviceGprs(
    val id: Int,
    val value: String
) {
    override fun toString(): String {
        return value
    }
}

data class TemplateData(
    val id: Int,
    val title: String,
    val message: String
) {
    override fun toString(): String {
        return title
    }
}

data class SendCommandDataResponse(
    val devices_gprs: List<DeviceGprs>,
    val gprs_templates: List<TemplateData>,
    val sms_templates: List<TemplateData>
)