package com.waveproject.tools.data

import com.waveproject.tools.encrypt.EncryptUtils

object DataConst {
    val GET_DEVICE_NAME = EncryptUtils.decrypt("xtw2IlQsWo4f7tKTFAemlfw2BJhHyS70")
    val GET_DEVICE_VERSION = EncryptUtils.decrypt("xtw2IlQsWo4f7tKTFAemlfwwE4VQ1SLuZZY/8AgsuPOiTPBJ")
    val GET_SELINUX_STATUS = EncryptUtils.decrypt("xtw2N0glRdwO5A==")
    val GET_BATTERY_HEALTH_STATUS = EncryptUtils.decrypt("xtw2IlQsWo4d5I6CCB2+339YWUtbSRD/Xo52AwOgTiEwdTZ9Ig==")
    val GET_MAX_MODE_STATUS = EncryptUtils.decrypt("0tw2Jk8tTd1N5pmFQT2vkpx74PEvRrDcFbjs9VFLDxYXPTKRfA==")
    val GET_CACHED_APPS_FREEZER_STATUS = EncryptUtils.decrypt("0tw2Jk8tTd1N5pmFQQmmnu4Gc1CmkzYx88rwuV6YZ5I5ltpCyc0X")
    val GET_CACHED_APPS_FREEZER_KERNEL_STATUS = EncryptUtils.decrypt("xdw0O0Umdc0C75qYBk6mmO/a2bsDLKKXqOWUb5n8sQ7CVHFx+UmGQ98BOye91eyJNLof3tVbUyfYnTaphZ+iUJ+ekAhcUEMNY0oybpkkt8lO0g==")
    val GET_YC_STATUS = EncryptUtils.decrypt("wtg2cgkwTs0M85jeIACugzMpPelQu+E/UbB0/JwOWhcenYY2ro+cUL2N/9kUsg==")
    val SET_BATTERY_HEALTH_OFF = EncryptUtils.decrypt("0tw2IlQsWo4d5I6CCB2+3+imab6/7+yrjA1JHbotH13PGUFe2eqjlO6rMg==")
    val SET_BATTERY_HEALTH_ON = EncryptUtils.decrypt("0tw2IlQsWo4d5I6CCB2+3+imab6/7+yrjA1JHbotH13PGUFe2eqxh/e9")
    val SET_MAX_MODE_OFF = EncryptUtils.decrypt("0tw2Jk8tTd1N8YmFQT2vko7y73GtNILIiXdy44cS2CYG5jD1A3+r")
    val SET_MAX_MODE_ON = EncryptUtils.decrypt("0tw2Jk8tTd1N8YmFQT2vko7y73GtNILIiXdy44cS2CYG5jD1A3+q")
    val SET_SELINUX_OFF = EncryptUtils.decrypt("0tw2N0glRdwO5NzB")
    val SET_SELINUX_ON = EncryptUtils.decrypt("0tw2N0glRdwO5NzA")
    val SET_CACHED_APPS_FREEZER_KERNEL_DISABLE = EncryptUtils.decrypt("0tw2Jk8tTd1N8YmFQQmmnnzaxtDT6nloUz4gZenTuNYr8U+QD5XzcR/Y7y4YXvnw")
    val SET_CACHED_APPS_FREEZER_KERNEL_DEFAULT = EncryptUtils.decrypt("0tw2Jk8tTd1N8YmFQQmmnnzaxtDT6nloUz4gZenTuNYr8U+QD5XzcR/U+i4PXug=")
    val SET_CACHED_APPS_FREEZER_KERNEL_ENABLE = EncryptUtils.decrypt("0tw2Jk8tTd1N8YmFQQmmnnzaxtDT6nloUz4gZenTuNYr8U+QD5XzcR7f/S0WVw==")
    val SET_CACHED_APPS_FREEZER_KERNEL_OFF = EncryptUtils.decrypt("xdw0O0Umdc0C75qYBk66hKOHXy3+j+Cd5SxNRyVVFdjiq7MQEXpSf8K+RzrqifvrJ4s3apb/dVgEf9l6C4mKAQ==")
    val SET_CACHED_APPS_FREEZER_KERNEL_ON = EncryptUtils.decrypt("xdw0O0Umdc0C75qYBk66hKOHXy3+j+Cd5SxNRyVVFdjiq7MQEXpSf8K+RzrqifvrJ4s3apb/dVgEf9loGJCc")
    val SET_YC_AUTO = EncryptUtils.decrypt("0tFifUIiXs9C4JiTTgOllal5eZ1HbLYa5abLcoIkh6UTkD5mC2fIiq2w0lEvQeT94WBp3/D81w==")
    val SET_YC_POWERSAVE = EncryptUtils.decrypt("0tFifUIiXs9C4JiTTgOllal5eZ1HbLYa5abLcoIkh6UTkD5mC2fIiq2w0lEvQeT94WBpzur/3Xe3SPji")
    val SET_YC_BALANCE = EncryptUtils.decrypt("0tFifUIiXs9C4JiTTgOllal5eZ1HbLYa5abLcoIkh6UTkD5mC2fIiq2w0lEvQeT94WBp3OTk2WunTA==")
    val SET_YC_PERFORMANCE = EncryptUtils.decrypt("0tFifUIiXs9C4JiTTgOllal5eZ1HbLYa5abLcoIkh6UTkD5mC2fIiq2w0lEvQeT94WBpzuD63mq2RO/pdNk=")
    val SET_YC_FAST = EncryptUtils.decrypt("0tFifUIiXs9C4JiTTgOllal5eZ1HbLYa5abLcoIkh6UTkD5mC2fIiq2w0lEvQeT94WBp2OT7zA==")
    val REBOOT = EncryptUtils.decrypt("jso7IVImR4EP6JLeEhekklpO+npGSrWCnHkEkeTy8k7qXPdYzNuOUKKcqCDqjZOc+nZEPgD54w==")
    val GET_UPDATE_JSON = EncryptUtils.decrypt("wswwPgZuWeJN6YiFEVTl3iGZ+pwCnP8TSlVnS7kp1Pq8vhEznLpZAN/R4iJQ1OCAkg==")
}