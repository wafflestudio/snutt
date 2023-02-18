package com.wafflestudio.snu4t.config

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.wafflestudio.snu4t.common.secret.SecretRepository
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.context.EnvironmentAware
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment

@Configuration
@Profile("!test")
class SecretsManagerConfig : EnvironmentAware, BeanFactoryPostProcessor {
    private lateinit var env: Environment

    override fun setEnvironment(environment: Environment) {
        env = environment
    }

    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        val secretNames = env.getProperty("secret-names", "").split(",")
        val objectMapper = jacksonObjectMapper()
        val secretRepository = beanFactory.getBean(SecretRepository::class.java)

        secretNames.forEach { secretName ->
            val secretString = secretRepository.getSecretString(secretName)
            val map = objectMapper.readValue<Map<String, String>>(secretString)
            map.forEach { (key, value) -> System.setProperty(key, value) }
        }
    }
}
