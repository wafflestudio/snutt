package com.wafflestudio.snu4t.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "api")
class ApiConfigs {
    var server: Map<String, ApiConfig> = hashMapOf()
}

class ApiConfig {
    var connectTimeout: Int = 800
    var readTimeout: Long = 1000
    lateinit var baseUrl: String
}
