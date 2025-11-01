package com.sirvja.tuntikirjaus.exporter;

import com.sirvja.tuntikirjaus.exporter.impl.KiekuConfiguration;
import com.sirvja.tuntikirjaus.exporter.impl.KiekuItem;

import java.util.List;

/**
 * Exporter interface to export Tunkirjaus events to another systems
 * @param <C> Configuration type
 * @param <I> Item type
 */
public interface Exporter<C,I> {
    void setConfiguration(C configuration);
    void prepareExporter();
    void exportItems(List<I> items);
    void destroyExporter();
}
