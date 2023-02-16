package com.lu.wxmask.config

import androidx.annotation.Keep


@Keep
class AppConfig(
    var mainUi: MainUi?
) {
    constructor() : this(null)
}

@Keep
class MainUi(
    var donateCard: DonateCard?,
    var moduleCard: ModuleCard?
)

@Keep
class DonateCard(
    var des: String?,
    var show: Boolean = false,
    var title: String?
)

@Keep
class ModuleCard(var link: String?)