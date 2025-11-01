package com.sirvja.tuntikirjaus.exporter.impl;

import java.time.LocalDateTime;

public record KiekuItem(LocalDateTime time, KiekuEvent event) {}
