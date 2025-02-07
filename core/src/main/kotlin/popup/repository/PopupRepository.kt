package com.wafflestudio.snutt.popup.repository

import com.wafflestudio.snutt.popup.data.Popup
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface PopupRepository : CoroutineCrudRepository<Popup, String>
