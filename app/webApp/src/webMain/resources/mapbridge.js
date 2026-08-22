// MapLibre GL JS bridge used by the KMP shared web PlatformMapView (jsMain/wasmJsMain).
// Exposes plain global functions so both Kotlin/JS and Kotlin/Wasm can call into it
// via simple `external fun` declarations (primitive/string/function types only).
(function () {
    var CONTAINER_ID = "maplibre-map-container";
    var SOURCE_ID = "incidents";
    var LAYER_OUTER = "incidents-outer";
    var SELECTED_SOURCE = "selected-location";
    var SELECTED_OUTER = "selected-location-outer";
    var SELECTED_INNER = "selected-location-inner";
    var USER_SOURCE = "user-location";
    var USER_OUTER = "user-location-outer";
    var USER_INNER = "user-location-inner";
    var STYLE_URI = "https://tiles.openfreemap.org/styles/liberty";

    var state = {
        map: null,
        mapLoaded: false,
        incidentsById: {},
        selectedLocation: null,
        userLocation: null,
        onIncidentClick: null,
        onMapClick: null,
        locationSelectionEnabled: false,
    };

    function emptyFeatureCollection() {
        return { type: "FeatureCollection", features: [] };
    }

    function pointFeatureCollection(lon, lat) {
        return {
            type: "FeatureCollection",
            features: [{ type: "Feature", geometry: { type: "Point", coordinates: [lon, lat] }, properties: {} }]
        };
    }

    function container() {
        return document.getElementById(CONTAINER_ID);
    }

    function ensureMap() {
        if (state.map || typeof maplibregl === "undefined") return;
        var el = container();
        if (!el) return;

        state.map = new maplibregl.Map({
            container: el,
            style: STYLE_URI,
            center: [5.0, 52.0],
            zoom: 7
        });
        state.map.addControl(new maplibregl.NavigationControl());

        state.map.on("load", function () {
            var m = state.map;
            m.addSource(SOURCE_ID, { type: "geojson", data: incidentsFeatureCollection() });
            m.addSource(SELECTED_SOURCE, { type: "geojson", data: state.selectedLocation ? pointFeatureCollection(state.selectedLocation.lon, state.selectedLocation.lat) : emptyFeatureCollection() });
            m.addSource(USER_SOURCE, { type: "geojson", data: state.userLocation ? pointFeatureCollection(state.userLocation.lon, state.userLocation.lat) : emptyFeatureCollection() });

            m.addLayer({ id: LAYER_OUTER, type: "circle", source: SOURCE_ID, paint: { "circle-radius": 10, "circle-color": "#F44336", "circle-stroke-width": 2, "circle-stroke-color": "#FFFFFF" } });
            m.addLayer({ id: "incidents-inner", type: "circle", source: SOURCE_ID, paint: { "circle-radius": 4, "circle-color": "#FFFFFF" } });

            m.addLayer({ id: SELECTED_OUTER, type: "circle", source: SELECTED_SOURCE, paint: { "circle-radius": 12, "circle-color": "#2196F3", "circle-stroke-width": 2, "circle-stroke-color": "#FFFFFF" } });
            m.addLayer({ id: SELECTED_INNER, type: "circle", source: SELECTED_SOURCE, paint: { "circle-radius": 6, "circle-color": "#FFFFFF" } });

            m.addLayer({ id: USER_OUTER, type: "circle", source: USER_SOURCE, paint: { "circle-radius": 10, "circle-color": "#4CAF50", "circle-stroke-width": 2, "circle-stroke-color": "#FFFFFF" } });
            m.addLayer({ id: USER_INNER, type: "circle", source: USER_SOURCE, paint: { "circle-radius": 4, "circle-color": "#FFFFFF" } });

            m.on("click", LAYER_OUTER, function (e) {
                var features = e.features;
                var first = features && features[0];
                var id = first && first.properties && first.properties.id;
                if (id != null && state.onIncidentClick) {
                    state.onIncidentClick(String(id));
                }
            });

            m.on("click", function (e) {
                if (state.locationSelectionEnabled && state.onMapClick) {
                    state.onMapClick(e.lngLat.lat, e.lngLat.lng);
                }
            });

            m.on("mouseenter", LAYER_OUTER, function () { m.getCanvas().style.cursor = "pointer"; });
            m.on("mouseleave", LAYER_OUTER, function () { m.getCanvas().style.cursor = ""; });

            state.mapLoaded = true;
            fitToIncidents(Object.keys(state.incidentsById).map(function (k) { return state.incidentsById[k]; }));
        });
    }

    function incidentsFeatureCollection() {
        return {
            type: "FeatureCollection",
            features: Object.keys(state.incidentsById).map(function (k) {
                var i = state.incidentsById[k];
                return { type: "Feature", geometry: { type: "Point", coordinates: [i.lon, i.lat] }, properties: { id: String(i.id) } };
            })
        };
    }

    function fitToIncidents(incidents) {
        var m = state.map;
        if (!m || !incidents.length) return;
        if (incidents.length === 1) {
            flyTo(incidents[0].lat, incidents[0].lon, 14.0);
            return;
        }
        var minLon = incidents[0].lon, maxLon = incidents[0].lon;
        var minLat = incidents[0].lat, maxLat = incidents[0].lat;
        incidents.forEach(function (i) {
            minLon = Math.min(minLon, i.lon); maxLon = Math.max(maxLon, i.lon);
            minLat = Math.min(minLat, i.lat); maxLat = Math.max(maxLat, i.lat);
        });
        var latPad = (maxLat - minLat) === 0 ? 0.02 : (maxLat - minLat) * 0.15;
        var lonPad = (maxLon - minLon) === 0 ? 0.02 : (maxLon - minLon) * 0.15;
        m.fitBounds([[minLon - lonPad, minLat - latPad], [maxLon + lonPad, maxLat + latPad]], { padding: 40, duration: 500 });
    }

    function flyTo(lat, lon, zoom) {
        if (!state.map) return;
        state.map.flyTo({ center: [lon, lat], zoom: zoom, essential: true });
    }

    window.kmpMapShow = function (x, y, w, h) {
        var el = container();
        if (!el) return;
        if (w <= 1 || h <= 1) {
            window.kmpMapHide();
            return;
        }
        el.style.display = "block";
        el.style.position = "absolute";
        el.style.left = x + "px";
        el.style.top = y + "px";
        el.style.width = w + "px";
        el.style.height = h + "px";
        el.style.zIndex = "5";
        el.style.pointerEvents = "auto";
        ensureMap();
        if (state.map) state.map.resize();
    };

    window.kmpMapHide = function () {
        var el = container();
        if (!el) return;
        el.style.display = "none";
        el.style.width = "0px";
        el.style.height = "0px";
        el.style.zIndex = "0";
    };

    window.kmpMapDispose = function () {
        window.kmpMapHide();
        if (state.map) {
            state.map.remove();
            state.map = null;
        }
        state.mapLoaded = false;
        state.incidentsById = {};
        state.selectedLocation = null;
        state.userLocation = null;
        state.onIncidentClick = null;
        state.onMapClick = null;
    };

    window.kmpMapUpdateIncidentsJson = function (json) {
        var incidents = JSON.parse(json);
        state.incidentsById = {};
        incidents.forEach(function (i) { state.incidentsById[String(i.id)] = i; });
        if (!state.map || !state.mapLoaded) return;
        var source = state.map.getSource(SOURCE_ID);
        if (source) {
            source.setData(incidentsFeatureCollection());
        }
        fitToIncidents(incidents);
    };

    window.kmpMapUpdateSelectedLocation = function (lat, lon) {
        state.selectedLocation = { lat: lat, lon: lon };
        if (!state.map || !state.mapLoaded) return;
        var source = state.map.getSource(SELECTED_SOURCE);
        if (source) source.setData(pointFeatureCollection(lon, lat));
    };

    window.kmpMapClearSelectedLocation = function () {
        state.selectedLocation = null;
        if (!state.map || !state.mapLoaded) return;
        var source = state.map.getSource(SELECTED_SOURCE);
        if (source) source.setData(emptyFeatureCollection());
    };

    window.kmpMapUpdateUserLocation = function (lat, lon) {
        state.userLocation = { lat: lat, lon: lon };
        if (!state.map || !state.mapLoaded) return;
        var source = state.map.getSource(USER_SOURCE);
        if (source) source.setData(pointFeatureCollection(lon, lat));
    };

    window.kmpMapClearUserLocation = function () {
        state.userLocation = null;
        if (!state.map || !state.mapLoaded) return;
        var source = state.map.getSource(USER_SOURCE);
        if (source) source.setData(emptyFeatureCollection());
    };

    window.kmpMapFlyTo = function (lat, lon, zoom) {
        flyTo(lat, lon, zoom);
    };

    window.kmpMapSetLocationSelectionEnabled = function (enabled) {
        state.locationSelectionEnabled = enabled;
    };

    window.kmpMapSetOnIncidentClick = function (callback) {
        state.onIncidentClick = callback;
    };

    window.kmpMapSetOnMapClick = function (callback) {
        state.onMapClick = callback;
    };
})();
